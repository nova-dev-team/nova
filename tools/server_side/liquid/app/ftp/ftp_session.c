#include <string.h>
#include <stdlib.h>

#include "xmemory.h"
#include "xstr.h"
#include "xnet.h"
#include "xutils.h"

#include "ftp_session.h"
#include "ftp_fs.h"
#include "ftp_acl.h"

/**
  @brief
    Hidden implementation of ftp_session.
*/
struct ftp_session_impl {
  xstr host_addr; ///< @brief The host address on which the FTP server is listening.
  xsocket cmd_sock; ///< @brief The command connection's xsocket object.
  xbool logged_in;  ///< @brief Whether user has logged in?
  xbool username_given; ///< @brief Whether username is given?
  xstr username;  ///< @brief The user's name.
  xstr data_cmd;  ///< @brief The most recent data command, used by data server's acceptor.
  xstr cwd; ///< @brief Current working directory.
  xstr root_jail; ///< @brief The root of FTP. Every filesystem operation MUST be locked inside this root jail.
  xstr user_identifier; ///< @brief User's identifier, in "ip:port(username)" format.
  xbool user_aborted; ///< @brief Records whether user issued ABOR for data transmission.
  char trans_type;  ///< @brief Transmission type, 'A':ASCII, 'I':BINARY/IMAGE.
  char trans_mode;  ///< @brief Transmission mode, 'P':passive (only support this).
  xserver data_server;  ///< @brief Data server.

  int rand_data_port; ///< @brief A helper variable for allocating data server's service port.
  off_t start_offset; ///< @brief Starting offset for RETR and STOR command.
};

ftp_session ftp_session_new(xsocket cmd_sock, xstr host_addr) {
  ftp_session session = xmalloc_ty(1, struct ftp_session_impl);
  session->host_addr = host_addr;
  session->cmd_sock = cmd_sock;
  session->logged_in = XFALSE;
  session->username_given = XFALSE;
  session->username = xstr_new();
  session->data_cmd = xstr_new();
  session->cwd = xstr_new();
  xstr_set_cstr(session->cwd, "/");
  session->root_jail = xstr_new();
  xstr_set_cstr(session->root_jail, "/");
  session->user_identifier = xstr_new();
  xstr_printf(session->user_identifier, "%s:%d", xsocket_get_host_cstr(session->cmd_sock), xsocket_get_port(session->cmd_sock));
  session->user_aborted = XFALSE;
  session->trans_type = 'A';
  session->trans_mode = 'P';
  session->data_server = NULL;
  session->rand_data_port = -1;
  return session;
}

void ftp_session_delete(ftp_session session) {
  // NOTE!!!: DO NOT DELETE cmd_sock! it will be automatically deleted by xserver!
  xstr_delete(session->username);
  xstr_delete(session->data_cmd);
  xstr_delete(session->cwd);
  xstr_delete(session->root_jail);
  xstr_delete(session->user_identifier);
  xfree(session);
}

int ftp_session_cmd_write(ftp_session session, void* data, int len) {
  return xsocket_write(session->cmd_sock, data, len);
}

int ftp_session_cmd_read(ftp_session session, void* buf,  int max_len) {
  // NOTE, we don't read max_len, but 'max_len - 1', because an \0 will be appended to the buf
  return xsocket_read(session->cmd_sock, buf, max_len - 1);
}

const xstr ftp_session_get_root_jail(ftp_session session) {
  return session->root_jail;
}

void ftp_session_set_root_jail(ftp_session session, const char* root_jail_cstr) {
  xstr_set_cstr(session->root_jail, root_jail_cstr);
}

xbool ftp_session_is_logged_in(ftp_session session) {
  return session->logged_in;
}

xbool ftp_session_is_username_given(ftp_session session) {
  return session->username_given;
}


void ftp_session_set_username_cstr(ftp_session session, char* cstr_username) {
  session->username_given = XTRUE;
  xstr_set_cstr(session->username, cstr_username);
  xstr_set_cstr(session->user_identifier, "");
  xstr_printf(session->user_identifier, "%s:%d(%s)", xsocket_get_host_cstr(session->cmd_sock), xsocket_get_port(session->cmd_sock), cstr_username);
}

const char* ftp_session_get_username_cstr(ftp_session session) {
  return xstr_get_cstr(session->username);
}

const char* ftp_session_get_user_identifier_cstr(ftp_session session) {
  return xstr_get_cstr(session->user_identifier);
}

xbool ftp_session_auth_cstr(ftp_session session, char* password) {
  xbool ret = XFALSE;
  if (session->username_given) {
    if (ftp_auth_user(xstr_get_cstr(session->username), password) == XSUCCESS) {
      ret = XTRUE;
      session->logged_in = XTRUE;
    }
  }
  return ret;
}

const char* ftp_session_get_cwd_cstr(ftp_session session) {
  return xstr_get_cstr(session->cwd);
}

char ftp_session_get_trans_mode(ftp_session session) {
  return session->trans_mode;
}

char ftp_session_get_trans_type(ftp_session session) {
  return session->trans_type;
}

void ftp_session_set_trans_type(ftp_session session, char type) {
  session->trans_type = type;
}

xsuccess ftp_session_prepare_data_service(ftp_session session, xserver_acceptor data_acceptor) {
  xsuccess ret = XSUCCESS;
  int data_port_min = 20000;
  int data_port_max = 56000;
  int backlog = 1;
  int serv_count = 1;
  char serv_mode = 'b';
  int max_try = 10;
  int try_count;
  if (session->rand_data_port < 0) {
    session->rand_data_port = data_port_min + rand() % (data_port_max - data_port_min + 1);
  }
  // deal with port confliction here, try to get a new socket for a few times
  for (try_count = 0; try_count < max_try; try_count++) {
    // we must create a new "host" var each round, since it will be automatically
    // destroyed if bind failed
    xstr host = xstr_copy(session->host_addr);  // will be managed by xserver (destroyed automatically after service)
    // TODO when creating a server with serv_count = 1, add a timeout. when time is out, kill the server
    session->data_server = xserver_new(host, session->rand_data_port, backlog, data_acceptor, serv_count, serv_mode, (void *) session);
    session->rand_data_port++;
    if (session->rand_data_port > data_port_max) {
      session->rand_data_port = data_port_min;
    }
    if (session->data_server != NULL) {
      break;
    } else {
      session->rand_data_port = data_port_min + rand() % (data_port_max - data_port_min + 1);
    }
  }
  if (try_count >= max_try) {
    ret = XFAILURE;
  }
  return ret;
}

xbool ftp_session_is_data_service_ready(ftp_session session) {
  if (session->data_server != NULL) {
    return XTRUE;
  } else {
    return XFALSE;
  }
}

void ftp_session_trigger_data_service(ftp_session session) {
  if (session->data_server != NULL) {
    xserver_serve(session->data_server);
    session->data_server = NULL;
  }
}

void ftp_session_discard_data_service(ftp_session session) {
  if (session->data_server != NULL) {
    xserver_delete(session->data_server);
    session->data_server = NULL;
  }
}

xstr ftp_session_get_host_addr(ftp_session session) {
  return session->host_addr;
}

const char* ftp_session_get_host_ip_cstr(ftp_session session) {
  return xserver_get_ip_cstr(session->data_server);
}


int ftp_session_get_data_server_port(ftp_session session) {
  return xserver_get_port(session->data_server);
}

const char* ftp_session_get_data_cmd_cstr(ftp_session session) {
  return xstr_get_cstr(session->data_cmd);
}

void ftp_session_set_data_cmd_cstr(ftp_session session, char* data_cmd) {
  xstr_set_cstr(session->data_cmd, data_cmd);
}

xstr ftp_session_get_cwd(ftp_session session) {
  return session->cwd;
}

xsuccess ftp_session_try_cwd_cstr(ftp_session session, char* new_path, xstr error_msg) {
  xsuccess ret = ftp_fs_try_cwd_cstr(session->root_jail, xstr_get_cstr(session->cwd), new_path, error_msg);
  if (ret == XSUCCESS) {
    xstr fullpath = xstr_new();
    xjoin_path_cstr(fullpath, xstr_get_cstr(session->cwd), new_path);
    xstr_set_cstr(session->cwd, xstr_get_cstr(fullpath));
    xstr_delete(fullpath);
  }
  return ret;
}

off_t ftp_session_get_start_offset(ftp_session session) {
  return session->start_offset;
}

void ftp_session_set_start_offset(ftp_session session, off_t offset) {
  session->start_offset = offset;
}

void ftp_session_cdup(ftp_session session) {
  xstr cwd_copy = xstr_copy(session->cwd);
  xfilesystem_normalize_abs_path(xstr_get_cstr(cwd_copy), session->cwd);
  xfilesystem_path_cdup(session->cwd);
  xstr_delete(cwd_copy);
}


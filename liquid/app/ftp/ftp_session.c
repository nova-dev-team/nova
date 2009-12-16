#include <string.h>
#include <stdlib.h>

#include "xmemory.h"
#include "xstr.h"
#include "xnet.h"

#include "ftp_session.h"

struct ftp_session_impl {
  xstr host_addr;
  xsocket cmd_sock;
  xbool logged_in;
  xbool username_given;
  xstr username;
  xstr data_cmd;
  xstr cwd;
  xstr user_identifier;
  xbool user_aborted; ///< @brief Records whether user issued ABOR for data transmission.
  char trans_type;  // A:ASCII, I:BINARY
  char trans_mode;  // P:passive (only support this)
  xserver data_server;
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
  session->user_identifier = xstr_new();
  xstr_printf(session->user_identifier, "%s:%d", xsocket_get_host_cstr(session->cmd_sock), xsocket_get_port(session->cmd_sock));
  session->user_aborted = XFALSE;
  session->trans_type = 'A';
  session->trans_mode = 'P';
  return session;
}

void ftp_session_delete(ftp_session session) {
  //*** DO NOT DELETE cmd_sock! it will be automatically deleted by xserver!

  xstr_delete(session->username);
  xstr_delete(session->data_cmd);
  xstr_delete(session->cwd);
  xstr_delete(session->user_identifier);

  // TODO kill data server

  xfree(session);
}

int ftp_session_cmd_write(ftp_session session, void* data, int len) {
  return xsocket_write(session->cmd_sock, data, len);
}

int ftp_session_cmd_read(ftp_session session, void* buf,  int max_len) {
  return xsocket_read(session->cmd_sock, buf, max_len);
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
  if (session->username_given) {
    // dummy checking
    if (strcmp(password, "santa") == 0) {
      session->logged_in = XTRUE;
      return XTRUE;
    }
  }
  return XFALSE;
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

void ftp_session_prepare_data_service(ftp_session session, xserver_acceptor data_acceptor) {
  int data_port = 20000 + rand() % 36000;
  int backlog = 1;
  int serv_count = 1;
  char serv_mode = 't';
  xstr host = xstr_copy(session->host_addr);
  session->data_server = xserver_new(host, data_port, backlog, data_acceptor, serv_count, serv_mode, (void *) session);
}

void ftp_session_trigger_data_service(ftp_session session) {
  xserver_serve(session->data_server);
}

xstr ftp_session_get_host_addr(ftp_session session) {
  return session->host_addr;
}

char* ftp_session_get_host_ip_cstr(ftp_session session) {
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
  xstr_set_cstr(session->cwd, new_path);
  return XSUCCESS;
}


#include <string.h>

#include "xmemory.h"
#include "xstr.h"
#include "xnet.h"

#include "ftp_session.h"

struct ftp_session_impl {
  xsocket cmd_sock;
  xbool logged_in;
  xbool username_given;
  xstr username;
  xstr data_cmd;
  xstr cwd;
  xstr user_identifier;
  xbool user_aborted; ///< @brief Records whether user issued ABOR for data transmission.
  
  /*
    TODO:

    pthread signal for data cmd
      when user send PASV cmd, an xserver (serve 1 time, no new thread) is created.
      the data_sock acceptor function should wait on data_cmd signal.
      when user data cmd is recv'ed, the signal will be called.

    data xserver

    client sock
  */
};

ftp_session ftp_session_new(xsocket cmd_sock) {
  ftp_session session = xmalloc_ty(1, struct ftp_session_impl);
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
  return session;
}

void ftp_session_delete(ftp_session session) {
  //*** DO NOT DELETE cmd_sock! it will be automatically deleted by xserver!

  xstr_delete(session->username);
  xstr_delete(session->data_cmd);
  xstr_delete(session->cwd);
  xstr_delete(session->user_identifier);

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



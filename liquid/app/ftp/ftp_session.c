#include "xmemory.h"
#include "xstr.h"
#include "xnet.h"

#include "ftp_session.h"

struct ftp_session_impl {
  xbool logged_in;
  int username_given;
  xstr username;
  xstr data_cmd;
  int user_aborted; ///< @brief Records whether user issued ABOR for data transmission.


  xsocket cmd_sock;
  
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
  return session;
}

void ftp_session_delete(ftp_session session) {
  //*** DO NOT DELETE cmd_sock! it will be automatically deleted by xserver!
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


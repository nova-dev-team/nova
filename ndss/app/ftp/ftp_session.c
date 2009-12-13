#include "ftp_session.h"

#include "xstr.h"

struct ftp_session_impl {
  int logged_in;
  int username_given;
  xstr username;
  xstr data_cmd;
  int user_aborted; ///< @brief Records whether user issued ABOR for data transmission.
  
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



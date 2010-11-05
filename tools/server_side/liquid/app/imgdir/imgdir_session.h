#ifndef IMGDIR_SESSION_H_
#define IMGDIR_SESSION_H_

/**
  @file
    imgdir_session.h

  @author
    Santa Zhang

  @brief
    Abstraction for a client connection.
*/

#include "imgdir_server.h"

// the hidden implementation of imgdir_session
struct imgdir_session_impl;

/**
  @brief
    Interface of imgdir_session.
*/
typedef struct imgdir_session_impl* imgdir_session;

/**
  @brief
    Create a new imgdir_session.

  @param client_sock
    Client socket connection.
  @param svr
    Handle of server model, contains global info.

  @return
    The newly created connection session.
*/
imgdir_session imgdir_session_new(xsocket client_sock, imgdir_server svr);

/**
  @brief
    Start serving a client session.
    The session it self will be destroyed after service is over.

  @param session
    The imgdir connection session.
*/
void imgdir_session_serve(imgdir_session session);

#endif  // IMGDIR_SESSION_H_

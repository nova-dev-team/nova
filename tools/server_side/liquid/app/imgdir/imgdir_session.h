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

#include "xnet.h"

#include "imgdir_server.h"
#include "imgdir_fsdb.h"

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


xsocket get_session_socket(imgdir_session session);

/**
  @brief
    Gets the filesystem root node.

  @param session
    The imgdir connection session.

  @return
    The root node in filesystem.
*/
fs_cache get_fs_root(imgdir_session session);


/**
  @brief
    Get the connection to filesystem database.

  @param session
    The imgdir connection session.

  @return
    The filesystem database handler.
*/
fsdb get_fsdb(imgdir_session session);


#endif  // IMGDIR_SESSION_H_

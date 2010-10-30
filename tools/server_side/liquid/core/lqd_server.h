#ifndef CORE_SERVER_H_
#define CORE_SERVER_H_

#include "xdef.h"
#include "xstr.h"
#include "xvec.h"

/**
  @brief
    Liquid key-value server.

  @author
    Santa Zhang

  @file
    lqd_server.h
*/

/**
  @brief
    The node server instance.
*/
typedef struct {
  xstr bind_addr; ///< @brief The address where liquid node server will be listening.
  int bind_port;  ///< @brief The port number on which liquid node server will be listening.
  int backlog;  ///< @brief Parameter for accept() function.
  xstr basefolder;  ///< @brief The folder in which liquid node server stores data.

  xvec peer_servers;  ///< @brief A list of peer servers, which this node server will connect to
} server_config;

/**
  @brief
    Initialize an raw server_config object.

  @param conf
    The server_config object.
*/
void server_config_initialize(server_config* conf);

/**
  @brief
    Start a node server.

  @param conf
    The configs of the server instance.

  @return
    Returns XFAILURE on error. Otherwise the function blocks, and never returns.
*/
xsuccess start_server(const server_config* conf);

#endif  // #define CORE_SERVER_H_


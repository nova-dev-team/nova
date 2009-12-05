#ifndef XSERVER_H_
#define XSERVER_H_

/**
  @author
    Santa Zhang

  @file
    xserver.h

  @brief
    A simple implementation of server socket.
*/

#include <sys/socket.h>

/**
  @brief
    The acceptor function. It will be invoked for each client connectted.

  Each incoming connection is handled in a new thread.

  @param client_sockfd
    The file descriptor of client socket.
*/
typedef void (*xserver_acceptor)(int client_sockfd);

/**
  @brief
    A simple server.
*/
typedef struct {
  int port; ///< @brief The port on which server is listening.

  /**
    @brief
      Maximum allowed connections from clients.

    Choose the value according to the estimated load of server.
    By default, TCP uses 128.
  */
  int backlog;


  xserver_acceptor acceptor;  ///< @brief The acceptor for each client.
} xserver;


/**
  @brief
    Initialize an xserver.

  @param xs
    The xserver to be initialized.
  @param port
    The port on which xserver will be listening.
  @param backlog
    The maximum number of clients to be accepted at same time.
  @param acceptor
    The acceptor function.
*/
void xserver_init(xserver* xs, int port, int backlog, xserver_acceptor acceptor);

/**
  @brief
    Start serving clients.

  This is a forever while loop that never ends.
  Each incoming connection is handled in a new thread.
  To prevent blocking, use xserve_in_new_thread() instead.

  @return
    -1 if failed to listen
*/
int xserver_serve(xserver* xs);

/**
  @brief
    Start serving clients, in a new thread.

  This function runs xserve in a new thread, so there is no blocking call.

  @return
    The id of thread in which xserve is invoked.
    Or -1 if failed to create thread.

  @warning
    If main process exits, the service will also stop!
*/
int xserve_in_new_thread(xserver* xs);

#endif


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
#include <pthread.h>

/**
  @brief
    The acceptor function. It will be invoked for each client connectted.

  Each incoming connection is handled in a new thread.

  @param client_sockfd
    The file descriptor of client socket.
  @param client_addr
    The client's address info.
  @param sin_size
    The memory size of client_addr.
  @param args
    Additional parameters.

  @warning
    Do not xfree() client_addr, it will be automatically xfree()'d after execution!
*/
typedef void (*xserver_acceptor)(int client_sockfd, struct sockaddr* client_addr, int sin_size, void* args);

/**
  @brief
    A simple server.
*/
typedef struct {
  struct sockaddr_in addr;  ///< @brief The socket address on which server is listening.

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
  @param host
    The xserver hostname.
  @param port
    The port on which xserver will be listening.
  @param backlog
    The maximum number of clients to be accepted at same time.
  @param acceptor
    The acceptor function.

  @return
    -1 if failure, otherwise 0.
*/
int xserver_init(xserver* xs, char* host, int port, int backlog, xserver_acceptor acceptor);

/**
  @brief
    Start serving clients.

  This is a forever while loop that never ends.
  Each incoming connection is handled in a new thread.
  To prevent blocking, use xserve_in_new_thread() instead.

  @param xs
    The serving xserver.
  @param args
    Additional parameters for the xserver

  @return
    -1 if failed to listen.
*/
int xserver_serve(xserver* xs, void* args);

/**
  @brief
    Start serving clients, in a new thread.

  This function runs xserve in a new thread, so there is no blocking call.

  @param xs
    The serving xserver.
  @param args
    Additional parameters for the xserver

  @return
    The id of thread in which xserve is invoked.
    Or -1 if failed to create thread.

  @warning
    If main process exits, the service will also stop!
*/
pthread_t xserve_in_new_thread(xserver* xs, void* args);


/**
  @brief
    Define a type of service handler on an port, which is served only once.

  @param client_sockfd
    The socked file descriptor.
  @param args
    Additional parameters for the service.

  @return
    -1 if failure, 0 if successful. *client_sockfd will be -1 if has accept failure.
*/
typedef int (*xserve_once_handler)(int client_sockfd, void* args);

/**
  @brief
    Run a service once, blocking.

  @param handler
    The function that handles service process.
  @param host
    The host on which service is published.
  @param port
    The port on which service is published.
  @param args
    Additional parameters for the service.

  @return
    -1 if failure, 0 if successful. *client_sockfd will be -1 if has accept failure.
*/
int xserve_once(xserve_once_handler handler, char* host, int port, void* args);

/**
  @brief
    Run a service once in a new thread, thus non-blocking.

  @param handler
    The function that handles service process.
  @param host
    The host on which service is published.
  @param port
    The port on which service is published.
  @param args
    Additional parameters for the service.

  @return
    The thread id that request is being handled.
    Will return -1 if failed to create new thread.

  @warning
    If main process exits, the service will also stop!
*/
pthread_t xserve_once_in_new_thread(xserve_once_handler handler, char* host, int port, void* args);

#endif


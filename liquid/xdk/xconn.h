#ifndef XCONN_H_
#define XCONN_H_

/**
  @author
    Santa Zhang

  @file
    xconn.h

  @brief
    A simple implementation of socket connection.
*/

#include <sys/socket.h>
#include <netinet/in.h>

/**
  @brief
    A simple socket connection implementation.
*/
typedef struct {
  struct sockaddr_in addr;  ///< @brief Address of server.
  int sockfd; ///< @brief Socket file descriptor of this connection.

  /**
    @brief
      Health status of this connection.

    health < 0 means there is error in this connection. health == 0 means the connection is not open.
    health > 0 means the connection is open and healthy.
  */
  int health;
} xconn;

/**
  @brief
    Initialize a connection.

  @param xc
    The xconn to be initialized.
  @param host
    Server host.
  @param port
    The server's port.

  @warning
    Do not initialize a xconn more than once!
*/
void xconn_init(xconn* xc, char* host, int port);

/**
  @brief
    Open a connection for read/write operation.

  @param xc
    The xconn to be opened.

  @return
    0 if connection successfull.
    -1 if connection failure.

  The health status of this connection is checked. If the xconn is already opened (health > 0),
  calling this function will not open it again.
*/
int xconn_open(xconn* xc);

/**
  @brief
    Close an opened connection.

  @param xc
    The xconn to be closed.

  The health status of this connection is checked. If the xconn is already closed/not opened (health = 0),
  calling this function will do nothing.
*/
void xconn_close(xconn* xc);

/**
  @brief
    Read data from an opened connection.

  @param xc
    The xconn where data will be read.
  @param buf
    The buffer that input data will be put.
  @param count
    The maximum number of data that will be read.

  @return
    -1 if error occurred: xconn not healthy/not opened/connection reset/io error, etc.
    Otherwise the number of bytes read will be returned (could be 0).
*/
int xconn_read(xconn* xc, void* buf, int count);

/**
  @brief
    Write some data to the connection.

  @param xc
    The xconn that data will be written to.
  @param buf
    The buffer containing data to be written.
  @param count
    The number of bytes to be written.

  @return
    -1 if error occurred: xconn not healthy/not opened/connection reset/io error, etc.
    Otherwise the number of bytes written will be returned (could be 0).
*/
int xconn_write(xconn* xc, void* buf, int count);

#endif


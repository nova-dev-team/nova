#include <netinet/in.h>

#include "xnet.h"
#include "xmemory.h"

struct xsocket_impl {
  xstr host;
  int port;
  struct sockaddr_in addr;
  int sockfd;
};

struct xserver_impl {
  xsocket xserver_sock;
  xserver_acceptor acceptor;
  int serv_count;
  int new_thread;
  void* args;
};

xsocket xsocket_new(xstr host, int port) {
  xsocket xsock = xmalloc_ty(1, struct xsocket_impl);
  xsock->host = host;
  xsock->port = port;
  return xsock;
}

void xsocket_delete(xsocket xs) {
  // TODO close connection?
  xfree(xs->host);
  xfree(xs);
}


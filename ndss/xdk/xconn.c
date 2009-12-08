#include <unistd.h>
#include <stdio.h>
#include <string.h>

#include "xconn.h"
#include "xutils.h"

void xconn_init(xconn* xc, char* host, int port) {
  xc->sockfd = -1;
  if (xinet_get_sockaddr(host, port, &xc->addr) < 0) {
    xc->health = -1;
    fprintf(stderr, "error in xconn_init: failed to get sockaddr!\n");
  } else {
    xc->health = 0;
  }
}

int xconn_open(xconn* xc) {
  int ret = 0;
  return ret;
}

void xconn_close(xconn* xc) {
  if (xc->health != 0) {
    shutdown(xc->sockfd, SHUT_RDWR);
    xc->health = 0;
  }
}

int xconn_read(xconn* xc, void* buf, int count) {
  if (xc->health > 0) {
    int cnt = read(xc->sockfd, buf, count);
    if (cnt < 0) {
      xc->health = -1;  // mark the connection as unhealthy
      return -1;
    }
    return cnt;
  } else {
    return -1;
  }
}

int xconn_write(xconn* xc, void* buf, int count) {
  if (xc->health > 0) {
    int cnt = write(xc->sockfd, buf, count);
    if (cnt < 0) {
      xc->health = -1;
      return -1;
    }
    return cnt;
  } else {
    return -1;
  }
}



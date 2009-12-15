#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <pthread.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "xnet.h"
#include "xmemory.h"
#include "xutils.h"

struct xsocket_impl {
  xstr host;
  int port;
  struct sockaddr_in addr;
  int sockfd;
};

struct xserver_impl {
  xsocket sock;
  int backlog;
  xserver_acceptor acceptor;
  int serv_count;
  char serv_mode; // 'b' blocking, 'p' new process, 't' new thread
  void* args;
};

// host will be deleted when deleting xsocked
xsocket xsocket_new(xstr host, int port) {
  xsocket xsock = xmalloc_ty(1, struct xsocket_impl);
  xsock->host = host;
  xsock->port = port;
  if (xinet_get_sockaddr(xstr_get_cstr(host), port, &(xsock->addr)) != XSUCCESS) {
    xsocket_delete(xsock);
    return NULL;
  } else {
    xsock->sockfd = socket(AF_INET, SOCK_STREAM, 0);
    return xsock;
  }
}

const char* xsocket_get_host_cstr(xsocket xs) {
  return xstr_get_cstr(xs->host);
}

int xsocket_get_port(xsocket xs) {
  return ntohs(xs->port);
}

int xsocket_write(xsocket xs, const void* data, int len) {
  return write(xs->sockfd, data, len);
}

int xsocket_read(xsocket xs, void* buf, int max_len) {
  return read(xs->sockfd, buf, max_len);
}

void xsocket_delete(xsocket xs) {
  shutdown(xs->sockfd, SHUT_RDWR);
  xstr_delete(xs->host);
  xfree(xs);
}

static void xserver_delete(xserver xs) {
  xsocket_delete(xs->sock);
  xfree(xs);
}

xserver xserver_new(xstr host, int port, int backlog, xserver_acceptor acceptor, int serv_count, char serv_mode, void* args) {
  xserver xs = xmalloc_ty(1, struct xserver_impl);
  xs->sock = xsocket_new(host, port);
  xs->backlog = backlog;
  xs->acceptor = acceptor;
  xs->serv_count = serv_count;
  xs->serv_mode = serv_mode;
  xs->args = args;
  if (bind(xs->sock->sockfd, (struct sockaddr *) &(xs->sock->addr), sizeof(struct sockaddr)) < 0) {
    perror("error in bind()");
    xserver_delete(xs);
    return NULL;
  }
  if (listen(xs->sock->sockfd, xs->backlog) < 0) {
    perror("error in listen()");
    xserver_delete(xs);
    return NULL;
  }
  return xs;
}

static void* acceptor_wrapper(void* pthread_arg) {
  void** arglist = (void **) pthread_arg;
  xserver xserver = arglist[0];
  xsocket client_xs = arglist[1];
  void* args = arglist[2];

  xserver->acceptor(client_xs, args);

  xfree(pthread_arg);
  xsocket_delete(client_xs);
  return NULL;
}


xsuccess xserver_serve(xserver xs) {
  int serv_count = 0;

  // prevent zombie processes
  if (xs->serv_mode == 'p' || xs->serv_mode == 'P') {
    signal(SIGCHLD, SIG_IGN);
  }

  while (xs->serv_count == XUNLIMITED || serv_count < xs->serv_count) {
    struct sockaddr_in client_addr;
    socklen_t sin_size;
    int client_sockfd = accept(xs->sock->sockfd, (struct sockaddr *) &client_addr, &sin_size);
    xsocket client_xs = xmalloc_ty(1, struct xsocket_impl); // xfree'd in acceptor wrapper

    client_xs->sockfd = client_sockfd;
    client_xs->addr = client_addr;
    client_xs->port = ntohs(client_addr.sin_port);
    client_xs->host = xstr_new();
    xstr_set_cstr(client_xs->host, inet_ntoa(client_addr.sin_addr));

    serv_count++;

    if (xs->serv_mode == 't' || xs->serv_mode == 'T') {
      // serve in new thread
      pthread_t tid;
      void** arglist = xmalloc_ty(3, void *);  // will be xfree'd in acceptor wrapper
      arglist[0] = xs;
      arglist[1] = client_xs;
      arglist[2] = xs->args;
      if (pthread_create(&tid, NULL, acceptor_wrapper, (void *) arglist) < 0) {
        perror("error in pthread_create()");
        // TODO handle error creating new thread
        return XFAILURE;
      }
    } else if (xs->serv_mode == 'p' || xs->serv_mode == 'P') {
      pid_t pid = fork();
      if (pid < 0) {
        perror("error in forking new process");
        return XFAILURE;
      } else if (pid == 0) {
        // child process

        // begin monitoring mem usage in service process
        xmem_reset_counter();
        xs->acceptor(client_xs, xs->args);
        
        if (xmem_usage() != 0) {
          printf("[xdk] mem leak: xmem_usage() = %d for xserver's service process\n", xmem_usage());
#ifdef XMEM_DEBUG
          xmem_print_usage();
#endif
        }
        xsocket_delete(client_xs);
        // exit child process
        exit(0);

      } else {
        // parent process
        
        // tricky here, reset rand seed, prevent sub process from rand value collisions
        srand(pid);
      }
    } else {
      // blocking
      xs->acceptor(client_xs, xs->args);
      xsocket_delete(client_xs);
    }
  }
  xserver_delete(xs); // self destroy
  return XSUCCESS;
}

int xserver_get_port(xserver xs) {
  return xs->sock->port;
}


char* xserver_get_ip_cstr(xserver xs) {
  return inet_ntoa(xs->sock->addr.sin_addr);
}


#include <unistd.h>
#include <stdlib.h>
#include <pthread.h>
#include <memory.h>
#include <netinet/in.h>

#include "xserver.h"

static void* acceptor_wrapper(void* arg) {
  void** arg_array = (void **) arg;
  xserver* xs = (xserver *) arg_array[0];
  int client_sockfd = (int) arg_array[1];

  xs->acceptor(client_sockfd);

  close(client_sockfd); // clean up
  return NULL;
}

void xserver_init(xserver* xs, int port, int backlog, xserver_acceptor acceptor) {
  xs->port = port;
  xs->backlog = backlog;
  xs->acceptor = acceptor;
}

int xserver_serve(xserver* xs) {
  int server_sockfd;
  struct sockaddr_in server_addr;
  socklen_t sin_size = sizeof(struct sockaddr_in);

  server_sockfd = socket(AF_INET, SOCK_STREAM, 0);

  server_addr.sin_family = AF_INET;
  server_addr.sin_port = htons(xs->port);
  server_addr.sin_addr.s_addr = INADDR_ANY;
  
  memset(&(server_addr.sin_zero), 0, 8);

  if (bind(server_sockfd, (struct sockaddr *) &server_addr, sizeof(struct sockaddr)) < 0) {
    return -1;
  }
  if (listen(server_sockfd, xs->backlog) < 0) {
    return -1;
  }
  for (;;) {
    struct sockaddr_in client_addr;
    pthread_t tid;
    int client_sockfd = accept(server_sockfd, (struct sockaddr *) &client_addr, &sin_size);
    void* arg[2];
    arg[0] = xs;
    arg[1] = (void *) client_sockfd;
    if (pthread_create(&tid, NULL, acceptor_wrapper, (void *) arg) < 0) {
      // TODO handle error creating new thread
    }
  }

  return 0;
}

static void* xserve_wrapper(void* arg) {
  xserver_serve((xserver*) arg);
  return NULL;
}

int xserve_in_new_thread(xserver* xs) {
  pthread_t tid;
  if (pthread_create(&tid, NULL, xserve_wrapper, (void *) xs) != 0) {
    return -1;
  } else {
    return (int) tid;
  }
}


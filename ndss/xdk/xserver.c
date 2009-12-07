#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <pthread.h>
#include <memory.h>
#include <netinet/in.h>

#include "xserver.h"
#include "xmemory.h"
#include "xutils.h"

static void* acceptor_wrapper(void* arg) {
  void** arg_array = (void **) arg;
  xserver* xs = (xserver *) arg_array[0];
  int client_sockfd = (int) arg_array[1];
  int sin_size = (int) arg_array[3];
  struct sockaddr* client_addr = (struct sockaddr *) xmalloc(sin_size);
  memcpy(client_addr, (struct sockaddr *) arg_array[2], sin_size);
  
  xs->acceptor(client_sockfd, &(xs->addr), client_addr, sin_size);
  
  xfree(client_addr);
  shutdown(client_sockfd, SHUT_RDWR);
  return NULL;
}

int xserver_init(xserver* xs, char* host, int port, int backlog, xserver_acceptor acceptor) {
  if (xinet_get_sockaddr(host, port, &(xs->addr)) < 0) {
    return -1;
  } else {
    xs->backlog = backlog;
    xs->acceptor = acceptor;
    return 0;
  }
}

int xserver_serve(xserver* xs) {
  int server_sockfd;

  server_sockfd = socket(AF_INET, SOCK_STREAM, 0);

  if (bind(server_sockfd, (struct sockaddr *) &(xs->addr), sizeof(struct sockaddr)) < 0) {
    perror("error in bind()");
    return -1;
  }
  if (listen(server_sockfd, xs->backlog) < 0) {
    perror("error in listen()");
    return -1;
  }
  for (;;) {
    struct sockaddr_in client_addr;
    pthread_t tid;
    socklen_t sin_size;
    int client_sockfd = accept(server_sockfd, (struct sockaddr *) &client_addr, &sin_size);
    void* arg[4];
    arg[0] = xs;
    arg[1] = (void *) client_sockfd;
    arg[2] = (void *) &client_addr;
    arg[3] = (void *) sin_size;
    if (pthread_create(&tid, NULL, acceptor_wrapper, (void *) arg) < 0) {
      // TODO handle error creating new thread
    }
  }

  return 0;
}

static void* xserver_serve_wrapper(void* arg) {
  xserver_serve((xserver*) arg);
  return NULL;
}

pthread_t xserve_in_new_thread(xserver* xs) {
  pthread_t tid;
  if (pthread_create(&tid, NULL, xserver_serve_wrapper, (void *) xs) != 0) {
    return -1;
  } else {
    return tid;
  }
}

int xserve_once(xserve_once_handler handler, char* host, int port, void* args, int* client_sockfd) {
  struct sockaddr_in server_addr;
  struct sockaddr_in client_addr;
  int server_sockfd;
  int backlog = 5;
  int ret;
  socklen_t sin_size;
  
  *client_sockfd = -1;

  if (xinet_get_sockaddr(host, port, &server_addr) < 0) { 
    return -1;
  }
  server_sockfd = socket(AF_INET, SOCK_STREAM, 0);
  if (bind(server_sockfd, (struct sockaddr *) &server_addr, sizeof(struct sockaddr)) < 0) {
    perror("error in bind()");
    return -1;
  }
  if (listen(server_sockfd, backlog) < 0) {
    perror("error in listen()");
    return -1;
  }

  *client_sockfd = accept(server_sockfd, (struct sockaddr *) &client_addr, &sin_size);
  ret = handler(host, port, args, *client_sockfd);
  shutdown(*client_sockfd, SHUT_RDWR);
  return ret;
}

static void* xserve_once_wrapper(void* arg) {
  void** arg_list = (void **) arg;
  
  xserve_once_handler handler = (xserve_once_handler) arg_list[0];
  char* host = (char *) arg_list[1];
  int port = (int) arg_list[2];
  void* args = (void *) arg_list[3];
  int* client_sockfd = (int *) arg_list[4];
  int* health = (int *) arg_list[5];

  *health = xserve_once(handler, host, port, args, client_sockfd);
  
  return NULL;
}

pthread_t xserve_once_in_new_thread(
  xserve_once_handler handler,
  char* host,
  int port,
  void* args,
  int* client_sockfd,
  int* health
) {

  pthread_t tid;
  void* arg_list[6];
  arg_list[0] = (void *) handler;
  arg_list[1] = (void *) host;
  arg_list[2] = (void *) port;
  arg_list[3] = (void *) args;
  arg_list[4] = (void *) client_sockfd;
  arg_list[5] = (void *) health;

  if (pthread_create(&tid, NULL, xserve_once_wrapper, (void *) arg_list) != 0) {
    *health = -1; // if failed to create new thread, this action is definitely not healthy
    return -1;
  } else {
    return tid;
  }
}


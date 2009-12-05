#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <memory.h>
#include <pthread.h>

#include "ndss-ftp.h"

void ndss_ftp_help() {
  printf("usage: ndss ftp <-p port> [base_dir]\n");
}

static int ndss_ftp_service() {
  int sockfd, new_fd;
  struct sockaddr_in my_addr, their_addr;
  int sin_size;


  sockfd = socket(AF_INET, SOCK_STREAM, 0);
  my_addr.sin_family = AF_INET;
  my_addr.sin_port = htons(1988);
  my_addr.sin_addr.s_addr = INADDR_ANY;

  memset(&(my_addr.sin_zero), 0, 8);
  
  if (bind(sockfd, (struct sockaddr *)&my_addr, sizeof(struct sockaddr)) == -1) {
    printf("bind() error\n");
    exit(1);
  }

  if (listen(sockfd, 10) == -1) {
    printf("listen() error\n");
    exit(1);
  }

  sin_size = sizeof(struct sockaddr_in);
  for (;;) {
    new_fd = accept(sockfd, (struct sockaddr *)&their_addr, &sin_size);
    printf("Got connection!\n");
    close(new_fd);
  }

}

int ndss_ftp(int argc, char* argv[]) {
  printf("ndss serving as ftp\n");
  ndss_ftp_service();
  return 0;
}


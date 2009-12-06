#include <stdio.h>

#include "xserver.h"

#include "ndss_ftp.h"

void ndss_ftp_help() {
  printf("usage: ndss ftp <-p port> [base_dir]\n");
}

static void ndss_ftp_client_acceptor(int client_sockfd) {
  printf("TODO got client\n");
}

static int ndss_ftp_service() {
  xserver xs;
  xserver_init(&xs, 10000, 80, ndss_ftp_client_acceptor);
  return xserver_serve(&xs);
}

int ndss_ftp(int argc, char* argv[]) {
  printf("ndss serving as ftp\n");
  ndss_ftp_service();
  return 0;
}


#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "xserver.h"

#include "ndss_ftp.h"

void ndss_ftp_help() {
  printf("usage: ndss ftp <-p port> [base_dir]\n");
}

static int ftp_write(int client_sockfd, char* cmd) {
  return write(client_sockfd, cmd, strlen(cmd));
}

static void ndss_ftp_client_acceptor(int client_sockfd) {
  char ibuf[1024];
  char obuf[1024];
  int cnt;
  printf("TODO got client\n");

  ftp_write(client_sockfd, "220 ndss ftp\n");
  cnt = read(client_sockfd, ibuf, 1023);
  // TODO check cnt value, prevent buffer overflow
  ibuf[cnt] = '\0';
  printf("len=%d, %s\n", cnt, ibuf);

  ftp_write(client_sockfd, "331 password required\n");
  cnt = read(client_sockfd, ibuf, 1023);
  // TODO check cnt value, prevent buffer overflow
  ibuf[cnt] = '\0';
  printf("len = %d, %s\n", cnt, ibuf);

  ftp_write(client_sockfd, "230 user logged in\n");
  cnt = read(client_sockfd, ibuf, 1023);
  // TODO check cnt value, prevent buffer overflow
  ibuf[cnt] = '\0';
  printf("len = %d, %s\n", cnt, ibuf);

  // TODO answer SYST
  ftp_write(client_sockfd, "215 UNIX Type: L8\n");
  cnt = read(client_sockfd, ibuf, 1023);
  // TODO check cnt value, prevent buffer overflow
  ibuf[cnt] = '\0';
  printf("len = %d, %s\n", cnt, ibuf);

  // TODO answer FEAT
  ftp_write(client_sockfd, "211-Features:\n MDTM\n REST STREAM\n SIZE\n211 End\n");
  cnt = read(client_sockfd, ibuf, 1023);
  // TODO check cnt value, prevent buffer overflow
  ibuf[cnt] = '\0';
  printf("len = %d, %s\n", cnt, ibuf);

  // TODO answer PWD
  ftp_write(client_sockfd, "257 /");
  cnt = read(client_sockfd, ibuf, 1023);
  // TODO check cnt value, prevent buffer overflow
  ibuf[cnt] = '\0';
  printf("len = %d, %s\n", cnt, ibuf);

  // TODO answer TYPE A
  ftp_write(client_sockfd, "200 Type set to A");
  cnt = read(client_sockfd, ibuf, 1023);
  // TODO check cnt value, prevent buffer overflow
  ibuf[cnt] = '\0';
  printf("len = %d, %s\n", cnt, ibuf);


  // TODO answer PASV
  ftp_write(client_sockfd, "227 Entering Passive Mode");
  cnt = read(client_sockfd, ibuf, 1023);
  // TODO check cnt value, prevent buffer overflow
  ibuf[cnt] = '\0';
  printf("len = %d, %s\n", cnt, ibuf);
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


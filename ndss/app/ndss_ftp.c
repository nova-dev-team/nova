#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "xserver.h"
#include "xmemory.h"
#include "xutils.h"

#include "ndss_ftp.h"

void ndss_ftp_help() {
  printf("usage: ndss ftp <-p port> [base_dir]\n");
}

static int ftp_write(int client_sockfd, char* cmd) {
  return write(client_sockfd, cmd, strlen(cmd));
}

static void get_client_addr_str(struct sockaddr* client_addr, char* addr_str) {
  struct sockaddr_in* client_addr_in = (struct sockaddr_in *) client_addr;
  int pos;
  strcpy(addr_str, inet_ntoa(client_addr_in->sin_addr));
  pos = strlen(addr_str);
  addr_str[pos] = ':';
  xitoa(client_addr_in->sin_port, addr_str + pos + 1, 10);
}

static void strip_trailing_newline(char* str) {
  // abc\r\n -> abc
  int pos = strlen(str) - 1;
  while (pos >= 0 && (str[pos] == '\r' || str[pos] == '\n')) {
    str[pos] = '\0';
    pos--;
  }
}

static void ndss_ftp_client_acceptor(int client_sockfd, struct sockaddr* client_addr, int sin_size) {
  int buf_size = 8192;
  char* ibuf = XMALLOC(buf_size, char);
  char* obuf = XMALLOC(buf_size, char);
  int cnt;
  int logged_in = 0;
  char* username = XMALLOC(80, char); // TODO better username size
  char* password = XMALLOC(80, char); // TODO better password size
  char* addr_str = XMALLOC(100, char);  // ip:port or ip:port(username) TODO better addr_str size
  
  get_client_addr_str(client_addr, addr_str);

  printf("[ftp] got new client connection from %s\n", addr_str);

  // show welcome message
  ftp_write(client_sockfd, "220 ndss ftp\n");

  for (;;) {
    cnt = read(client_sockfd, ibuf, buf_size);
    if (cnt == buf_size) {
      printf("[rep %s] 501 too long request\n", addr_str);
      ftp_write(client_sockfd, "501 too long request\n");
      printf("[ftp] client %s kicked because of too long request\n", addr_str);
      break;
    } else if (cnt == 0) {
      printf("[ftp] client %s prematurely disconnected\n", addr_str);
      break;
    }
    ibuf[cnt] = '\0';
    printf("[req %s] %s", addr_str, ibuf);

    if (xstr_startwith(ibuf, "USER")) {
      strcpy(username, ibuf + 5);
      xstr_strip(username);
      strcat(addr_str, "(");
      strcat(addr_str, username);
      strcat(addr_str, ")");
      printf("[rep %s] 331 password required\n", addr_str);
      ftp_write(client_sockfd, "331 password required\n");

    } else if (xstr_startwith(ibuf, "PASS")) {
      strcpy(password, ibuf + 5);
      strip_trailing_newline(password);

      // TODO user authentication
      printf("[ftp] TODO authentication, user %s with password (quoted) '%s'\n", username, password);

      logged_in = 1;
      printf("[rep %s] 230 user logged in\n", addr_str);
      ftp_write(client_sockfd, "230 user logged in\n");

    } else if (xstr_startwith(ibuf, "SYST")) {
      printf("[rep %s] 215 UNIX Type: L8\n", addr_str);
      ftp_write(client_sockfd, "215 UNIX Type: L8\n");

    } else if (xstr_startwith(ibuf, "FEAT")) {
      printf("[rep %s] 211-Features:\n", addr_str);
      printf("[rep %s]  MDTM\n", addr_str);
      printf("[rep %s]  REST STREAM\n", addr_str);
      printf("[rep %s]  SIZE\n", addr_str);
      printf("[rep %s] 211 End\n", addr_str);
      ftp_write(client_sockfd, "211-Features:\n MDTM\n REST STREAM\n SIZE\n211 End\n");

    } else if (xstr_startwith(ibuf, "QUIT")) {
      printf("[rep %s] 221 see you\n", addr_str);
      ftp_write(client_sockfd, "211 see you\n");
      printf("[ftp] client %s quitted\n", addr_str);
      break;

    } else {
      printf("[rep %s] 500 unknown command\n", addr_str);
      ftp_write(client_sockfd, "500 unknown command\n");
    }
  }
  
  printf("[ftp] releasing resource for client %s\n", addr_str);
  xfree(username);
  xfree(password);
  xfree(addr_str);
  xfree(ibuf);
  xfree(obuf);
}

static int ndss_ftp_service(int port) {
  xserver xs;
  xserver_init(&xs, port, 10, ndss_ftp_client_acceptor);
  return xserver_serve(&xs);
}

int ndss_ftp(int argc, char* argv[]) {
  int port = 10000;
  printf("[ftp] ftp server started on port %d\n", port);
  return ndss_ftp_service(port);
}


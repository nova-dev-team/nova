#include <stdio.h>
#include <stdlib.h>
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

static void strip_trailing_crlf(char* str) {
  // abc\r\n -> abc
  int pos = strlen(str) - 1;
  while (pos >= 0 && (str[pos] == '\r' || str[pos] == '\n')) {
    str[pos] = '\0';
    pos--;
  }
}

// ip & port will be set to -1 if error occured
static void parse_port_cmd(char* cmd, int* ip, int* port) {
  int comma_count = 0;
  char* ptr = cmd + strlen(cmd) - 1;
  int v, w;
  int ip_weight = 1;
  int port_weight = 1;
  *ip = 0;
  *port = 0;
  v = 0;
  w = 1;
  while (ptr >= cmd + 4) {
    if (*ptr == '\r' || *ptr == '\n') {
      ptr--;
      continue;
    }
    if (*ptr == ',')
      comma_count++;
    if (comma_count > 5) {
      *ip = -1;
      *port = -1;
      return;
    }
    if (*ptr == ',' || *ptr == ' ')  {
      // end of a segment
      if (comma_count > 2) {
        *ip += v * ip_weight;
        ip_weight *= 256;
      } else {
        *port += v * port_weight;
        port_weight *= 256;
      }
      v = 0;
      w = 1;
    } else if ('0' <= *ptr && *ptr <= '9') {
      v += w * (*ptr - '0');
      w *= 10;
    } else {
      *ip = -1;
      *port = -1;
      return;
    }
    if (*ptr == '\0')
      break;
    ptr--;
  }
  if (comma_count != 5) {
    *ip = -1;
    *port = -1;
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
  char* cwd = XMALLOC(10240, char); // TODO better current working directory
  char* trans_cmd = XMALLOC(buf_size, char); // the command that starts transfer. set to "\0" if no command is in queue
  char trans_type = 'A';  // transmition type, 'A' is ASCII, 'I' is image
  
  get_client_addr_str(client_addr, addr_str);

  printf("[ftp] got new client connection from %s\n", addr_str);

  // show welcome message
  ftp_write(client_sockfd, "220 ndss ftp\n");

  // start with root at '/'
  strcpy(cwd, "/");

  for (;;) {
    cnt = read(client_sockfd, ibuf, buf_size);
    if (cnt == buf_size) {
      printf("[rep %s] 501 too long request\n", addr_str);
      ftp_write(client_sockfd, "501 too long request\n");
      printf("[ftp] client %s kicked because of too long request\n", addr_str);
      break;  // stop service
    } else if (cnt == 0) {
      printf("[ftp] client %s prematurely disconnected\n", addr_str);
      break;  // stop service
    }
    ibuf[cnt] = '\0';
    printf("[req %s] %s", addr_str, ibuf);

    if (xstr_startwith(ibuf, "USER")) {
      // check if command is correct
      if (strlen(ibuf) < 6) {
        printf("[rep %s] 501 please provode username\n", addr_str);
        ftp_write(client_sockfd, "501 please provide username\n");
      } else {
        strcpy(username, ibuf + 5);
        xstr_strip(username);
        strcat(addr_str, "(");
        strcat(addr_str, username);
        strcat(addr_str, ")");
        printf("[rep %s] 331 password required\n", addr_str);
        ftp_write(client_sockfd, "331 password required\n");
      }

    } else if (xstr_startwith(ibuf, "PASS")) {
      // check if command is correct
      if (strlen(ibuf) < 6) {
        printf("[rep %s] 501 please provide password\n", addr_str);
        ftp_write(client_sockfd, "501 please provide password\n");
      } else {
        strcpy(password, ibuf + 5);
        strip_trailing_crlf(password);

        // TODO user authentication
        printf("[ftp] TODO authentication, user %s with password (quoted) '%s'\n", username, password);

        logged_in = 1;
        printf("[rep %s] 230 user logged in\n", addr_str);
        ftp_write(client_sockfd, "230 user logged in\n");
      }

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

    } else if (xstr_startwith(ibuf, "PWD")) {
      printf("[rep %s] 257 \"%s\" is current directory\n", addr_str, cwd);
      sprintf(obuf, "257 \"%s\" is current directory\n", cwd);
      ftp_write(client_sockfd, obuf);
      strcpy(trans_cmd, ibuf);

    } else if (xstr_startwith(ibuf, "TYPE")) {
      if (strlen(ibuf) < 6) {
        printf("[rep %s] 501 incorrect TYPE command\n", addr_str);
        ftp_write(client_sockfd, "501 incorrect TYPE command\n");
      } else if (ibuf[5] != 'A' && ibuf[5] != 'I') {
        printf("[rep %s] 501 incorrect TYPE option\n", addr_str);
        ftp_write(client_sockfd, "501 incorrect TYPE option\n");
      } else {
        trans_type = ibuf[5];
        printf("[rep %s] 200 Type set to %c\n", addr_str, trans_type);
        sprintf(obuf, "200 Type set to %c\n", trans_type);
        ftp_write(client_sockfd, obuf);
      }

    } else if (xstr_startwith(ibuf, "PORT")) {
      int clnt_ip, clnt_port;
      parse_port_cmd(ibuf, &clnt_ip, &clnt_port);
      if (clnt_ip < 0 || clnt_port < 0) {
        printf("[rep %s] 501 incorrect PORT command\n", addr_str);
        ftp_write(client_sockfd, "501 incorrect PORT command\n");

      } else {
        // TODO exec port cmd
      }

      
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
  xfree(cwd);
  xfree(trans_cmd);
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
  int i;

  for (i = 2; i < argc; i++) {
    if (strcmp(argv[i], "-p") == 0) {
      if (i + 1 < argc) {
        // TODO check if not number
        sscanf(argv[i + 1], "%d", &port);
      } else {
        printf("error in cmdline args: '-p' must be followed by port number!\n");
        exit(1);
      }
    } else if (xstr_startwith(argv[i], "--port=")) {
      // TODO check if not number
      sscanf(argv[i] + 7, "%d", &port);
    }
  }
  printf("[ftp] ftp server started on port %d\n", port);
  return ndss_ftp_service(port);
}


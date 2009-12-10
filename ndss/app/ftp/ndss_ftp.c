#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <time.h>

#include "xserver.h"
#include "xconn.h"
#include "xmemory.h"
#include "xutils.h"

#include "ndss_client.h"

#include "ndss_ftp.h"

void ndss_ftp_help() {
  printf("usage: ndss ftp <-p port|--port=port> <-b bind_addr|--bind=bind_addr>\n");
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

// generate a random port
static int gen_rand_port() {
  return 20000 + (rand() % 36000);
}

// for passive mode address
static void get_comma_separated_addr(char* ip_str, int port, char* dest) {
  char buf[8];
  int i;
  int h = port / 256;
  int l = port % 256;
  strcpy(dest, ip_str);
  for (i = 0; dest[i] != '\0'; i++) {
    if (dest[i] == '.')
      dest[i] = ',';
  }
  strcat(dest, ",");
  xitoa(h, buf, 10);
  strcat(dest, buf);
  strcat(dest, ",");
  xitoa(l, buf, 10);
  strcat(dest, buf);
}

static int ftp_serve_once_ls(int client_sockfd, void* args) {
  FILE* ls_pipe = popen("ls / -al", "r");
  int buf_size = 102400;
  char ch;
  char* buf = xmalloc_ty(buf_size, char);
  int i;

  printf("TODO\n");
  printf("%s\n", (char *) args);

  while (!feof(ls_pipe)) {
    i = 0;
    while (!feof(ls_pipe) && (ch = fgetc(ls_pipe)) != '\n') {
      buf[i] = ch;
      i++;
    }
    if (feof(ls_pipe)) {
      buf[i] = '\0';
    } else {
      buf[i] = '\n';
      buf[i + 1] = '\0';
    }
    printf("%s", buf);
    ftp_write(client_sockfd, buf);
  }

  pclose(ls_pipe);
  xfree(buf);
  return 0;
}

static void ndss_ftp_client_acceptor(int client_sockfd, struct sockaddr* client_addr, int sin_size, void* args) {
  int buf_size = 8192;
  char* ibuf = xmalloc_ty(buf_size, char);
  char* obuf = xmalloc_ty(buf_size, char);
  int cnt;
  int logged_in = 0;
  char* username = xmalloc_ty(80, char); // TODO better username size
  char* password = xmalloc_ty(80, char); // TODO better password size
  char* addr_str = xmalloc_ty(100, char);  // ip:port or ip:port(username) TODO better addr_str size
  char* cwd = xmalloc_ty(10240, char); // TODO better current working directory
  char* trans_cmd = xmalloc_ty(buf_size, char); // the command that starts transfer. set to "\0" if no command is in queue
  char trans_type = 'A';  // transmission type, 'A' is ASCII, 'I' is image
  
  int data_port = -1;
  pthread_t data_conn_tid;
  
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
    } else if (cnt == -1) {
      printf("[ftp] client %s kicked because of socket error\n", addr_str);
      break;
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

    } else if (xstr_startwith(ibuf, "PASV")) {
      char tmp_buf[32];
      struct sockaddr_in* server_addr = (struct sockaddr_in*) args;

      data_port = gen_rand_port();

      get_comma_separated_addr(inet_ntoa(server_addr->sin_addr), data_port, tmp_buf);
      sprintf(obuf, "227 entering passive mode (%s)\n", tmp_buf);
      printf("[rep %s] %s", addr_str, obuf);  // note: no need to add '\n', already added in obuf
      ftp_write(client_sockfd, obuf);

      data_conn_tid = xserve_once_in_new_thread(
        ftp_serve_once_ls,
        inet_ntoa(server_addr->sin_addr),
        data_port,
        ibuf
      );

    } else if (xstr_startwith(ibuf, "LIST")) {
      data_port = gen_rand_port();
      struct sockaddr_in* server_addr = (struct sockaddr_in*) args;
      ftp_write(client_sockfd, "150 opening ASCII mode data connection for file list\n");
      data_conn_tid = xserve_once_in_new_thread(
        ftp_serve_once_ls,
        inet_ntoa(server_addr->sin_addr),
        data_port,
        ibuf
      );
      pthread_join(data_conn_tid, NULL);
      ftp_write(client_sockfd, "226 transfer complete\n");

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

static int ndss_ftp_service(char* host, int port) {
  xserver xs;
  if (xserver_init(&xs, host, port, 10, ndss_ftp_client_acceptor) < 0) {
    fprintf(stderr, "in ndss_ftp_service(): failed to init xserver!\n");
    return -1;
  }
  return xserver_serve(&xs, &(xs.addr));
}

int ndss_ftp(int argc, char* argv[]) {
  int port = 8021;
  char bind[16];
  int i;
  srand(time(NULL));
  strcpy(bind, "0.0.0.0");

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
    } else if (strcmp(argv[i], "-b") == 0) {
      if (i + 1 < argc) {
        // TODO check ip address format
        strcpy(bind, argv[i + 1]);
      } else {
        printf("error in cmdline args: '-b' must be followed by bind address!\n");
        exit(1);
      }
    } else if (xstr_startwith(argv[i], "--bind=")) {
      // TODO check ip address format
      strcpy(bind, argv[i] + 7);
    }
  }
  printf("[ftp] ftp server started on %s:%d\n", bind, port);
  return ndss_ftp_service(bind, port);
}


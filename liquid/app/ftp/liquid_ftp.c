#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include "xdef.h"
#include "xnet.h"
#include "xmemory.h"
#include "xstr.h"
#include "xutils.h"

#include "ftp_session.h"
#include "ftp_fs.h"

void liquid_ftp_help() {
  printf("usage: liquid ftp <-p port|--port=port> <-b bind_addr|--bind=bind_addr>\n");
}

static void strip_trailing_crlf(char* str) {
  // abc\r\n -> abc
  int pos = strlen(str) - 1;
  while (pos >= 0 && (str[pos] == '\r' || str[pos] == '\n')) {
    str[pos] = '\0';
    pos--;
  }
}

static void add_comma_separated_data_server_addr(xstr rep, ftp_session session) {
  int data_server_port = ftp_session_get_data_server_port(session);
  char data_server_ip[16];
  int i;
  strcpy(data_server_ip, ftp_session_get_host_ip_cstr(session));

  xstr_printf(rep, "(");
  for (i = 0; data_server_ip[i] != '\0'; i++) {
    char ch = data_server_ip[i];
    if (ch == '.')
      xstr_append_char(rep, ',');
    else
      xstr_append_char(rep, ch);
  }

  xstr_printf(rep, ",%d,%d)\n", data_server_port / 256, data_server_port % 256);
}

static void reply(ftp_session session, const char* text) {
  printf("[rep %s] %s", ftp_session_get_user_identifier_cstr(session), text);
  ftp_session_cmd_write(session, (void *) text, strlen(text));
}

static xsuccess get_request(ftp_session session, char* inbuf, int buf_size) {
  int cnt = ftp_session_cmd_read(session, inbuf, buf_size);
  if (cnt == buf_size) {
    reply(session, "501 too long request\n");
    printf("[ftp] client %s kicked because of too long request\n", ftp_session_get_user_identifier_cstr(session));
    return XFAILURE;
  } else if (cnt == 0) {
    printf("[ftp] client %s prematurely disconnected\n", ftp_session_get_user_identifier_cstr(session));
    return XFAILURE;
  } else if (cnt == -1) {
    printf("[ftp] client %s kicked because of socket error\n", ftp_session_get_user_identifier_cstr(session));
    return XFAILURE;
  }
  inbuf[cnt] = '\0';
  strip_trailing_crlf(inbuf);
  printf("[req %s] %s\n", ftp_session_get_user_identifier_cstr(session), inbuf);
  return XSUCCESS;
}

static void data_acceptor(xsocket data_xsock, void* args) {
  ftp_session session = (ftp_session) args;
  const char* data_cmd = ftp_session_get_data_cmd_cstr(session);

  if (xcstr_startwith_cstr(data_cmd, "LIST")) {
    xstr ls_data = xstr_new();
    xstr error_msg = xstr_new();
    if (ftp_fs_list_into_xstr(ftp_session_get_cwd(session), ls_data, error_msg) == XSUCCESS) {
      xsocket_write(data_xsock, (const void *) xstr_get_cstr(ls_data), xstr_len(ls_data));
      reply(session, "226 transfer complete\n");
    } else {
      reply(session, xstr_get_cstr(error_msg));
    }
    xstr_delete(error_msg);
    xstr_delete(ls_data);

  } else if (xcstr_startwith_cstr(data_cmd, "RETR")) {
    xstr error_msg = xstr_new();
    if (ftp_fs_retr_file(data_xsock, ftp_session_get_cwd(session), data_cmd + 5, error_msg) == XSUCCESS) {
      reply(session, "226 transfer complete\n");
    } else {
      reply(session, xstr_get_cstr(error_msg));
    }
    xstr_delete(error_msg);
  }
}

static void cmd_acceptor(xsocket client_xs, void* args) {
  int buf_size = 8192;
  xstr host_addr = (xstr) args;
  char* inbuf = xmalloc_ty(buf_size, char);
  char* outbuf = xmalloc_ty(buf_size, char);
  xbool stop_service = XFALSE;

  // client_xs will NOT be deleted by ftp_session, but will be deleted by xserver
  // host_addr will NOT be deleted by ftp_session, but will be deleted by ftp entry
  ftp_session session = ftp_session_new(client_xs, host_addr);
  reply(session, "220 liquid ftp\n");

  // pre-login
  while (stop_service == XFALSE && ftp_session_is_logged_in(session) == XFALSE) {
    if (get_request(session, inbuf, buf_size) != XSUCCESS) {
      stop_service = XTRUE;
      break;
    }

    if (xcstr_startwith_cstr(inbuf, "USER")) {
      // check if command is correct
      if (strlen(inbuf) < 6) {
        reply(session, "501 please provide username\n");
      } else {
        ftp_session_set_username_cstr(session, inbuf + 5);
        reply(session, "331 password required\n");
      }

    } else if (xcstr_startwith_cstr(inbuf, "PASS")) {
      // check if command is correct
      if (strlen(inbuf) < 6) {
        reply(session, "501 please provide password\n");
      } else {
        if (ftp_session_auth_cstr(session, inbuf + 5)) {
          reply(session, "230 user logged in\n");
        } else {
          reply(session, "530 login incorrect\n");
        }
      }

    } else if (xcstr_startwith_cstr(inbuf, "QUIT")) {
      reply(session, "211 see you\n");
      stop_service = XTRUE;

    } else {
      xstr rep = xstr_new();
      xstr_printf(rep, "500 unknown command '%s'\n", inbuf);
      reply(session, xstr_get_cstr(rep));
      xstr_delete(rep);
    }
  }

  // post-login
  while (stop_service == XFALSE) {
    if (get_request(session, inbuf, buf_size) != XSUCCESS) {
      stop_service = XTRUE;
      break;
    }

    if (xcstr_startwith_cstr(inbuf, "USER")) {
      reply(session, "500 cannot change username\n");
    } else if (xcstr_startwith_cstr(inbuf, "QUIT")) {
      reply(session, "211 see you\n");
      stop_service = XTRUE;
    } else if (xcstr_startwith_cstr(inbuf, "SYST")) {
      reply(session, "215 UNIX Type: L8\n");
    } else if (xcstr_startwith_cstr(inbuf, "FEAT")) {
      reply(session, "211-Features:\n");
      reply(session, " MDTM\n");
      reply(session, " REST STREAM\n");
      reply(session, " SIZE\n");
      reply(session, "211 End\n");
    } else if (xcstr_startwith_cstr(inbuf, "PWD")) {
      xstr rep = xstr_new();
      xstr_printf(rep, "257 \"%s\" is current directory\n", ftp_session_get_cwd_cstr(session));
      reply(session, xstr_get_cstr(rep));
      xstr_delete(rep);
    } else if (xcstr_startwith_cstr(inbuf, "TYPE")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 please provide type\n");
      } else {
        char type = inbuf[5];
        if (type == 'a' || type == 'A') {
          ftp_session_set_trans_type(session, 'A');
          reply(session, "200 type set to A\n");
        } else if (type == 'i' || type == 'I') {
          ftp_session_set_trans_type(session, 'I');
          reply(session, "200 type set to I\n");
        } else {
          reply(session, "500 invalid TYPE command\n");
        }
      }

    } else if (xcstr_startwith_cstr(inbuf, "PASV")) {
      xstr rep = xstr_new();
      xstr_set_cstr(rep, "227 entering passive mode ");
      ftp_session_prepare_data_service(session, data_acceptor);
      add_comma_separated_data_server_addr(rep, session);
      reply(session, xstr_get_cstr(rep));
      xstr_delete(rep);
    } else if (xcstr_startwith_cstr(inbuf, "LIST")) {
      reply(session, "150 here comes the listing\n");
      ftp_session_set_data_cmd_cstr(session, inbuf);
      ftp_session_trigger_data_service(session);

    } else if (xcstr_startwith_cstr(inbuf, "RETR")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 please provide filename\n");
      } else {
        reply(session, "150 sending file\n");
        ftp_session_set_data_cmd_cstr(session, inbuf);
        ftp_session_trigger_data_service(session);
      }

    } else if (xcstr_startwith_cstr(inbuf, "CWD")) {
      if (strlen(inbuf) < 4) {
        reply(session, "501 invalid CWD command\n");
      } else {
        xstr error_msg = xstr_new();
        if (ftp_session_try_cwd_cstr(session, inbuf + 4, error_msg) == XSUCCESS) {
          reply(session, "250 CWD command successful\n");
        } else {
          reply(session, xstr_get_cstr(error_msg));
        }
        xstr_delete(error_msg);
      }
    } else {
      xstr rep = xstr_new();
      xstr_printf(rep, "500 unknown command '%s'\n", inbuf);
      reply(session, xstr_get_cstr(rep));
      xstr_delete(rep);
    }
  }

  ftp_session_delete(session);
  xfree(inbuf);
  xfree(outbuf);
}


static xsuccess liquid_ftp_service(xstr host, int port) {
  int ret;
  int backlog = 10;
  void* args = (void *) host;
  char serv_mode = 'p'; // serv in new process
  xserver xs = xserver_new(host, port, backlog, cmd_acceptor, XUNLIMITED, serv_mode, args);
  if (xs == NULL) {
    fprintf(stderr, "in liquid_ftp_service(): failed to init xserver!\n");
    return XFAILURE;
  }
  printf("[ftp] ftp server started on %s:%d\n", xstr_get_cstr(host), port);
  ret = xserver_serve(xs);  // xserver is self destrying, after service, it will destroy it self
  return ret;
}

xsuccess liquid_ftp(int argc, char* argv[]) {
  int port = 8021;
  xstr bind_addr = xstr_new();  // will be sent into liquid_ftp_service(), and work as a component of xserver. will be destroyed when xserver is deleted
  int i;
  srand(time(NULL));
  xstr_set_cstr(bind_addr, "0.0.0.0");

  for (i = 2; i < argc; i++) {
    if (strcmp(argv[i], "-p") == 0) {
      if (i + 1 < argc) {
        // TODO check if not number
        sscanf(argv[i + 1], "%d", &port);
      } else {
        printf("error in cmdline args: '-p' must be followed by port number!\n");
        exit(1);
      }
    } else if (xcstr_startwith_cstr(argv[i], "--port=")) {
      // TODO check if not number
      sscanf(argv[i] + 7, "%d", &port);
    } else if (strcmp(argv[i], "-b") == 0) {
      if (i + 1 < argc) {
        // TODO check ip address format
        xstr_set_cstr(bind_addr, argv[i + 1]);
      } else {
        printf("error in cmdline args: '-b' must be followed by bind address!\n");
        exit(1);
      }
    } else if (xcstr_startwith_cstr(argv[i], "--bind=")) {
      // TODO check ip address format
      xstr_set_cstr(bind_addr, argv[i + 1]);
    }
  }
  return liquid_ftp_service(bind_addr, port);
}


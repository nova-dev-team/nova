#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/select.h>
#include <sys/types.h>
#include <unistd.h>

#include "xstr.h"
#include "xnet.h"
#include "xmemory.h"
#include "xutils.h"


// global information of port mapping destination
xstr forward_to_host;
int forward_to_port;

static void port_mapper_acceptor(xsocket client_xs, void* args) {
  xsocket dest_xs = xsocket_new(xstr_copy(forward_to_host), forward_to_port);
  xsocket_connect(dest_xs);
  xsocket_shortcut(dest_xs, client_xs);
  xsocket_delete(dest_xs);
}

static xsuccess start_port_mapper_server(xstr bind_addr, int port) {
  int backlog = 10;
  xsuccess ret;
  xserver xs = xserver_new(bind_addr, port, backlog, port_mapper_acceptor, XUNLIMITED, 'p', NULL);
  if (xs == NULL) {
    fprintf(stderr, "in start_port_mapper_server(): failed to init xserver!\n");
    ret = XFAILURE;
  } else {
    printf("[info] port mapper running on %s:%d, dest is %s:%d\n", xstr_get_cstr(bind_addr), port, xstr_get_cstr(forward_to_host), forward_to_port);
    ret = xserver_serve(xs);
  }
  xstr_delete(bind_addr);
  return ret;
}

int main(int argc, char* argv[]) {
  xstr bind_addr = xstr_new();
  int port = -1;
  xstr forward_dest = xstr_new();
  int ret = 0;
  int i;
  xbool asked_for_help = XFALSE;

  for (i = 1; i < argc; i++) {
    if (xcstr_startwith_cstr(argv[i], "--help") == XTRUE || strcmp(argv[i], "-h") == 0) {
      asked_for_help = XTRUE;
    }
  }

  if (argc == 1 || asked_for_help == XTRUE) {

    printf("usage: port_mapper [-b bind_addr] [-p bind_port] <-d dest_addr[:dest_port]>\n");
    printf("       bind_addr default to 0.0.0.0\n");
    printf("       if dest_port not given, it is default to bind_port\n");
    printf("       if bind_port not given, it is default to dest_port\n");
    exit(0);
  }

  srand(time(NULL));
  xstr_set_cstr(bind_addr, "0.0.0.0");

  forward_to_host = xstr_new();
  forward_to_port = -1;

  for (i = 1; i < argc; i++) {
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
      xstr_set_cstr(bind_addr, argv[i] + 7);
    } else if (strcmp(argv[i], "-d") == 0) {
      if (i + 1 < argc) {
        // TODO check format
        xstr_set_cstr(forward_dest, argv[i + 1]);
      } else {
        printf("error in cmdline args: '-d' must be followed by destination address!\n");
        exit(1);
      }
    } else if (xcstr_startwith_cstr(argv[i], "--dest=")) {
      // TODO check format
      xstr_set_cstr(forward_dest, argv[i] + 7);
    }
  }

  if (xstr_len(forward_dest) == 0) {
    printf("[error] destination not given!\n");
    exit(1);
  } else {
    const char* dest_cstr = xstr_get_cstr(forward_dest);
    xbool has_dest_port = XFALSE;
    int split_index = 0;
    for (i = 0; dest_cstr[i] != '\0'; i++) {
      if (dest_cstr[i] == ':') {
        has_dest_port = XTRUE;
        split_index = i;
        break;
      }
    }
    if (has_dest_port == XTRUE) {
      forward_to_port = atoi(dest_cstr + split_index + 1);
      if (port == -1) {
        port = forward_to_port;
      }
      for (i = 0; i < split_index; i++) {
        xstr_append_char(forward_to_host, dest_cstr[i]);
      }
    } else {
      forward_to_port = port;
      xstr_set_cstr(forward_to_host, dest_cstr);
    }
  }

  ret = start_port_mapper_server(bind_addr, port);

  xstr_delete(forward_dest);
  xstr_delete(forward_to_host);
  return ret;
}


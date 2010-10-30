#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "xkeepalive.h"
#include "xoption.h"
#include "xlog.h"
#include "xstr.h"

#include "lqd_server.h"

#include "liquid_node.h"

static void parse_peer_servers(xvec vec, const char* peer_cstr) {
  if (peer_cstr != NULL) {
    const char* delim = ",";
    char* ptr = strdup(peer_cstr); // make gcc happy, discard "const"
    char* saveptr = NULL;
    char* tok_ptr = NULL;
    xstr xs = xstr_new();
    xlog_info("parsing peer servers: %s", peer_cstr);
    tok_ptr = strtok_r(ptr, delim, &saveptr);
    if (tok_ptr != NULL) {
      xstr_set_cstr(xs, tok_ptr);
      xvec_push_back(vec, xs);
    }
    while (tok_ptr != NULL) {
      xs = xstr_new();
      tok_ptr = strtok_r(NULL, delim, &saveptr);
      if (tok_ptr != NULL) {
        xstr_set_cstr(xs, tok_ptr);
        xvec_push_back(vec, xs);
      }
    }
    free(ptr);  // cleanup the strdup'ed peer_cstr
  }
}

static xsuccess liquid_node_real(int argc, char* argv[]) {
  // everything used by 'conf' will be xfree'd after the server instance is destroyed
  server_config conf;
  int port = 3122;
  xstr bind_addr = xstr_new();
  xstr basefolder = xstr_new();
  xoption xopt = xoption_new();

  server_config_initialize(&conf);
  xstr_set_cstr(bind_addr, "0.0.0.0");
  xstr_set_cstr(basefolder, ".");

  xoption_parse_with_xconf(xopt, argc, argv);

  if (xoption_has(xopt, "p")) {
    if (xoption_get_size(xopt, "p") == 0) {
      xlog_error("error in command line args: '-p' must be followed by port number!\n");
    } else {
      port = atoi(xoption_get(xopt, "p"));
    }
  }
  if (xoption_has(xopt, "port")) {
    port = atoi(xoption_get(xopt, "port"));
  }
  if (xoption_has(xopt, "b")) {
    if (xoption_get_size(xopt, "b") == 0) {
      xlog_error("error in command line args: '-b' must be followed by bind address!\n");
    } else {
      xstr_set_cstr(bind_addr, xoption_get(xopt, "b"));
    }
  }
  if (xoption_has(xopt, "bind")) {
    xstr_set_cstr(bind_addr, xoption_get(xopt, "bind"));
  }
  if (xoption_has(xopt, "r")) {
    if (xoption_get_size(xopt, "r") == 0) {
      xlog_error("error in command line args: '-r' must be followed by root address!\n")
    } else {
      xstr_set_cstr(basefolder, xoption_get(xopt, "r"));
    }
  }
  if (xoption_has(xopt, "root")) {
    xstr_set_cstr(basefolder, xoption_get(xopt, "root"));
  }
  if (xoption_has(xopt, "peer")) {
    // --peer=a.b.c.d:e,a1.b1.c1.d1:e1,...
    parse_peer_servers(conf.peer_servers, xoption_get(xopt, "peer"));
  }

  xoption_delete(xopt);

  conf.bind_addr = bind_addr;
  conf.bind_port = port;
  conf.basefolder = basefolder;
  conf.backlog = 10;  // TOOD move these things into config file
  return start_server(&conf);
}

xsuccess liquid_node(int argc, char* argv[]) {
  xsuccess ret = XSUCCESS;
  xoption xopt = xoption_new();
  xoption_parse(xopt, argc, argv);
  if (xoption_has(xopt, "h") || xoption_has(xopt, "help")) {
    liquid_node_help();
  } else if (xoption_has(xopt, "keepalive")) {
    xkeep_alive(liquid_node_real, argc, argv);
  } else {
    liquid_node_real(argc, argv);
  }
  xoption_delete(xopt);
  return ret;
}

void liquid_node_help() {
  printf("usage: liquid node [-b <bind_addr>|--bind=<bind_addr>] [-p <bind_port>|--port=<bind_port>]\n");
  printf("                   [-r <root_folder>|--root=<root_folder>] [--keepalive]\n");
}


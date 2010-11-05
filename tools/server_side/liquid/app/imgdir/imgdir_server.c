#include <stdio.h>
#include <stdlib.h>

#include "xkeepalive.h"
#include "xoption.h"
#include "xlog.h"
#include "xstr.h"
#include "xnet.h"
#include "xmemory.h"

#include "imgdir_server.h"
#include "imgdir_session.h"

void imgdir_print_help() {
  printf("This shall be done!\n");
}

static void imgdir_server_delete(imgdir_server svr) {
  if (svr->root != NULL) {
    fs_cache_delete(svr->root);
  }
  if (svr->fs != NULL) {
    fsdb_close(svr->fs);
  }
  xfree(svr);
}

static imgdir_server imgdir_server_new(const char* fsdb_fn) {
  imgdir_server svr = xmalloc_ty(1, struct imgdir_server_impl);
  svr->root = fsdb_load_root(fsdb_fn, &(svr->fs));
  if (svr->root == NULL || svr->fs == NULL) {
    // failed to create server model, delete it now!
    imgdir_server_delete(svr);
    svr = NULL;
  }
  return svr;
}

static void cmd_acceptor(xsocket client_xs, void* args) {
  imgdir_server svr = (imgdir_server) args;
  imgdir_session session = imgdir_session_new(client_xs, svr);
  xlog_info("[imgdir] new client connected\n");
  imgdir_session_serve(session);
}

static xsuccess imgdir_service(xstr host, int port, int backlog) {
  xsuccess ret;
  char serv_mode = 't'; // serv in new thread
  char* fsdb_fn = "fsdb.sqlite3"; // TODO parse from command line
  imgdir_server svr = imgdir_server_new(fsdb_fn);
  if (svr == NULL) {
    ret = XFAILURE;
    xlog_fatal("[imgdir] failed to create server model!\n");
    // release "host", since it is not managed by xserver yet
    xstr_delete(host);
  } else {
    xserver xs = xserver_new(host, port, backlog, cmd_acceptor, XUNLIMITED, serv_mode, svr);
    if (xs == NULL) {
      xlog_fatal("[imgdir] in imgstore_service(): failed to init xserver!\n");
      ret = XFAILURE;
    } else {
      xlog_info("[imgdir] imgstore server started on %s:%d\n", xstr_get_cstr(host), port);
      ret = xserver_serve(xs);  // xserver is self destrying, after service, it will destroy it self
    }
    imgdir_server_delete(svr);
    // don't need to release "host", since it is managed by xservers
  }
  return ret;
}

static xsuccess imgdir_server_real(int argc, char* argv[]) {
  xsuccess ret = XFAILURE;
  int port = 2010;
  xstr bind_addr = xstr_new();  // will be send into imgstore_service(), and work as a component of xserver. will be destroyed when xserver is deleted
  int back_log = 10;
  xoption xopt = xoption_new();
  xoption_parse_with_xconf(xopt, argc, argv);
  xlog_init(argc, argv);

  xstr_set_cstr(bind_addr, "0.0.0.0");

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
  if (xoption_has(xopt, "backlog")) {
    back_log = atoi(xoption_get(xopt, "backlog"));
  }
  xoption_delete(xopt);
  ret = imgdir_service(bind_addr, port, back_log);
  return ret;
}

xsuccess imgdir_server_main(int argc, char* argv[]) {
  xsuccess ret = XSUCCESS;
  xoption xopt = xoption_new();
  xoption_parse(xopt, argc, argv);
  // check if need help
  if (xoption_has(xopt, "help") || xoption_has(xopt, "h")) {
    imgdir_print_help();
  } else {
    // check if keepalive is required
    if (xoption_has(xopt, "keepalive")) {
      ret = xkeep_alive(imgdir_server_real, argc, argv);
    } else {
      ret = imgdir_server_real(argc, argv);
    }
  }

  xoption_delete(xopt);
  return ret;
}


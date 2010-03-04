#include <stdlib.h>
#include <stdio.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <errno.h>
#include <string.h>

#include "xstr.h"
#include "xnet.h"
#include "xlog.h"
#include "xsys.h"
#include "xutils.h"

#include "core_defs.h"
#include "server.h"
#include "token.h"

/**
  @brief
    The server instance, descripting the server's parameters.
*/
typedef struct {
  const server_config* conf;  ///< @brief The basic server config.
  token_set tkn_set;  ///< @brief The set of tokens.
} server_instance;

static void server_config_peers_servers_free(void* ptr) {
  xstr xs = (xstr) ptr;
  xstr_delete(xs);
}

void server_config_initialize(server_config* conf) {
  conf->bind_addr = NULL;
  conf->bind_port = -1;
  conf->backlog = 10;
  conf->basefolder = NULL;
  conf->peer_servers = xvec_new(server_config_peers_servers_free);
}

// get a line of request, upto \r\n
// return XFAILURE on end of connection stream, or premature disconnection
static xsuccess get_request(xsocket client_xs, xstr req) {
  xsuccess ret = xsocket_read_line(client_xs, req);
  if (ret == XSUCCESS) {
    xlog_info("[req] %s", xstr_get_cstr(req));
  }
  return ret;
}

static xsuccess send_reply(xsocket client_xs, const char* rep) {
  xsuccess ret;
  xlog_info("[rep] %s", rep);
  ret = xsocket_write_line(client_xs, rep);
  if (ret == XFAILURE) {
    xlog_error("client %s disconnected prematurely", xsocket_get_host_cstr(client_xs));
  }
  return ret;
}


static void node_server_acceptor(xsocket client_xs, void* args) {
  const server_config* conf = (const server_config*) args;
  xstr req_xstr = xstr_new();
  xsuccess succ = XSUCCESS;
  xlog_info("got client: %s", xsocket_get_host_cstr(client_xs));
  xlog_info("server basefolder = %s", xstr_get_cstr(conf->basefolder));

  // do handshake
  succ = send_reply(client_xs, "liquid node\r\n");
  if (succ == XSUCCESS) {
    succ = get_request(client_xs, req_xstr);
  }
  if (succ == XSUCCESS) {
    succ = send_reply(client_xs, LIQUID_VER_CSTR);
  }
  if (succ == XSUCCESS) {
    succ = get_request(client_xs, req_xstr);
  }
  if (succ == XSUCCESS) {
    succ = send_reply(client_xs, "success: client accepted\r\n");
  }
  // finished handshake

  while (succ == XSUCCESS && get_request(client_xs, req_xstr) == XSUCCESS) {
    if (xstr_startwith_cstr(req_xstr, "put") == XTRUE) {
      succ = send_reply(client_xs, "success: TODO\r\n");
    } else if (xstr_startwith_cstr(req_xstr, "get") == XTRUE) {
      succ = send_reply(client_xs, "success: TODO\r\n");
    } else if (xstr_startwith_cstr(req_xstr, "delete") == XTRUE) {
      succ = send_reply(client_xs, "success: TODO\r\n");
    } else if (xstr_startwith_cstr(req_xstr, "rename") == XTRUE) {
      succ = send_reply(client_xs, "success: TODO\r\n");
    } else if (xstr_startwith_cstr(req_xstr, "bye") == XTRUE) {
      succ = send_reply(client_xs, "success: see you\r\n");
      break;
    } else {
      succ = send_reply(client_xs, "failure: command not recognized\r\n");
      xlog_error("failure: command not recognized: '%s'", xstr_get_cstr(req_xstr));
    }
  }
 
  xlog_info("connection to client %s closed", xsocket_get_host_cstr(client_xs));

  xstr_delete(req_xstr);
}

static xsuccess mkdir_helper(const xstr base, const char* sub) {
  xsuccess ret = XSUCCESS;
  xstr folder_path = xstr_new();
  xstr_printf(folder_path, "%s%c%s", xstr_get_cstr(base), xsys_fs_sep_char, sub);
  if (mkdir(xstr_get_cstr(folder_path), 0755) != 0 && errno != EEXIST) {
    xlog_error("failed to create folder: %s", xstr_get_cstr(folder_path));
    ret = XFAILURE;
  }
  xstr_delete(folder_path);
  return ret;
}

xsuccess start_server(const server_config* conf) {
  xsuccess ret = XSUCCESS;
  char serv_mode = 'p';
  xbool has_error = XFALSE;
  xbool connected_to_peer = XFALSE;
  server_instance serv_inst;
  int i;

  serv_inst.conf = conf;
  serv_inst.tkn_set = NULL;

  // prepare folders
  if (mkdir_helper(conf->basefolder, "data") != XSUCCESS) {
    has_error = XTRUE;
  }
  if (mkdir_helper(conf->basefolder, "info") != XSUCCESS) {
    has_error = XTRUE;
  }
  if (mkdir_helper(conf->basefolder, "tmp") != XSUCCESS) {
    has_error = XTRUE;
  }
  if (mkdir_helper(conf->basefolder, "log") != XSUCCESS) {
    has_error = XTRUE;
  }

  if (has_error == XFALSE) {
    // try to connect peer servers
    for (i = 0; i < xvec_size(conf->peer_servers); i++) {
      xstr peer_xs = xvec_get(conf->peer_servers, i);
      xstr peer_host = xstr_new();
      int peer_port = -1;
      if (xinet_split_host_port(xstr_get_cstr(peer_xs), peer_host, &peer_port) == XSUCCESS) {
        // peer_host is managed by xsock, so we don't need to explicitly delete it
        xsocket xsock = xsocket_new(peer_host, peer_port);
        xlog_info("trying to connect peer server %s:%d", xstr_get_cstr(peer_host), peer_port);
        if (xsock != NULL && xsocket_connect(xsock) == XSUCCESS) {
          xlog_info("connectted to peer server %s:%d", xstr_get_cstr(peer_host), peer_port);
          connected_to_peer = XTRUE;

          // exchange token info
          serv_inst.tkn_set = create_token_set_from_peer(xsock);
          xsocket_delete(xsock);
          if (serv_inst.tkn_set != NULL) {
            break;
          } else {
            // don't use peer_host here, because it is already destroyed before
            xlog_info("failed to get proper token set from peer server");
            // if failed to get token set from peer, try next peer
          }
        } else {
          // don't use peer_host here, because it is already destroyed on failure
          xlog_error("error connecting to peer server");
          if (xsock != NULL) {
            xsocket_delete(xsock);
          }
        }
      } else {
        xstr_delete(peer_host);
      }
    }

    if (connected_to_peer == XFALSE || serv_inst.tkn_set == NULL) {
      xlog_info("not connectted to peer servers, generating tokens myself");
      serv_inst.tkn_set = create_token_set();
    }

    xserver xs = xserver_new(conf->bind_addr, conf->bind_port, conf->backlog, node_server_acceptor, XUNLIMITED, serv_mode, (void *) conf);
    if (xs == NULL) {
      xlog_fatal("[node] in start_server(): failed to init xserver!");
      ret = XFAILURE;
    } else {
      xlog_info("[node] server started on %s:%d\n", xstr_get_cstr(conf->bind_addr), conf->bind_port);
      ret = xserver_serve(xs);
    }
  } else {
    ret = XFAILURE;
  }

  xstr_delete(conf->bind_addr);
  xstr_delete(conf->basefolder);
  return ret;
}

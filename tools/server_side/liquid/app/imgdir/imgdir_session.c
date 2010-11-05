#include "xnet.h"
#include "xmemory.h"
#include "xstr.h"

#include "imgdir_session.h"
#include "imgdir_handlers.h"

// this macro is for imgdir_session_serve() function
#define REPLY_OR_DIE(text) \
  if (xsocket_write_line(session->clnt_sock, (text)) == XFALSE) {  \
    stop_service = XTRUE; \
  }

/**
  @brief
    Implementation of imgdir session.
*/
struct imgdir_session_impl {
  xsocket clnt_sock;  ///< @brief Connection to client.
  imgdir_server svr;  ///< @brief Pointer to server model, contains global info.
};

imgdir_session imgdir_session_new(xsocket client_sock, imgdir_server svr) {
  imgdir_session session = xmalloc_ty(1, struct imgdir_session_impl);
  // NOTE: clnt_sock is not managed by imgdir session, by managed by xserver
  session->clnt_sock = client_sock;
  // NOTE: svr is not managed by imgdir session
  session->svr = svr;
  return session;
}

static void imgdir_sessoin_delete(imgdir_session session) {
  // don't delete the clnt_sock, since it will be deleted by xserver, when service thread ended
  // neither should we delete svr
  xfree(session);
}

void imgdir_session_serve(imgdir_session session) {
  xstr req_head = xstr_new();
  xbool stop_service = XFALSE;

  // start handshake
  if (xsocket_read_line(session->clnt_sock, req_head) == XFAILURE || xstr_eql_cstr(req_head, "accept_me") == XFALSE) {
    printf("line: %s\n", xstr_get_cstr(req_head));
    stop_service = XTRUE;
  } else {
    // accept client
    REPLY_OR_DIE("accepted\r\n");
  }
  // handshake done

  while (stop_service == XFALSE) {
    if (xsocket_read_line(session->clnt_sock, req_head) == XFAILURE) {
      stop_service = XTRUE;
    }
    if (stop_service == XTRUE) {
      break;
    }
    if (xstr_startwith_cstr(req_head, "list ")) {
      imgdir_handle_ls(session, req_head);
    } else if (xstr_startwith_cstr(req_head, "mkdir ")) {
      // TODO handle mkdir
      REPLY_OR_DIE("ok\r\n");
    } else {
      REPLY_OR_DIE("confused\r\n");
    }
  }

  xstr_delete(req_head);
  imgdir_sessoin_delete(session);
}

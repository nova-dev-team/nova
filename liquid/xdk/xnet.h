#ifndef XNET_H_
#define XNET_H_

#include "xdef.h"
#include "xstr.h"

struct xsocket_impl;

typedef struct xsocket_impl* xsocket;

xsocket xsocket_new(xstr host, int port);

int xsocket_write(xsocket xs, void* data, int len);

int xsocket_read(xsocket xs, void* buf, int max_len);

void xsocket_delete(xsocket xs);

struct xserver_impl;

typedef struct xserver_impl* xserver;

typedef void (*xserver_acceptor)(xsocket client_xs, void* args);

xserver xserver_new(xstr host, int port, int backlog, xserver_acceptor acceptor, int serv_count, xbool new_thread, void* args);

xsuccess xserver_serve(xserver xs);

void xserver_delete(xserver xs);

#endif


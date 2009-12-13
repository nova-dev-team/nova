#ifndef XNET_H_
#define XNET_H_

#include "xstr.h"

struct xsocket_impl;

typedef struct xsocket_impl* xsocket;

xsocket xsocket_new(xstr host, int port);

void xsocket_delete(xsocket xs);

struct xserver_impl;

typedef struct xserver_impl* xserver;

typedef void (*xserver_acceptor)(xsocket client_xs, void* args);

xserver xserver_new(xstr host, int port, xserver_acceptor acceptor, int serv_count, int new_thread, void* args);

void xserver_delete(xserver xs);

#endif


#ifndef XNET_H_
#define XNET_H_

#include "xdef.h"
#include "xstr.h"

struct xsocket_impl;

typedef struct xsocket_impl* xsocket;

xsocket xsocket_new(xstr host, int port);

const char* xsocket_get_host_cstr(xsocket xs);

int xsocket_get_port(xsocket xs);

int xsocket_write(xsocket xs, const void* data, int len);

int xsocket_read(xsocket xs, void* buf, int max_len);

void xsocket_delete(xsocket xs);

struct xserver_impl;

typedef struct xserver_impl* xserver;

typedef void (*xserver_acceptor)(xsocket client_xs, void* args);

xserver xserver_new(xstr host, int port, int backlog, xserver_acceptor acceptor, int serv_count, char serv_mode, void* args);

xsuccess xserver_serve(xserver xs);

int xserver_get_port(xserver xs);

char* xserver_get_ip_cstr(xserver xs);

#endif  // XNET_H_


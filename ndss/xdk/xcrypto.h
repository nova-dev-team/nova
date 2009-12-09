#ifndef XCRYPTO_H_
#define XCRYPTO_H_

/**
  @author
    Santa Zhang

  @file
    xcrypto.h

  @brief
    A wrapper for 3rd party crypto functions. Currently included SHA-1, MD5
*/

struct xmd5_impl;

typedef struct xmd5_impl* xmd5;

xmd5 xmd5_new();

void xmd5_feed(xmd5 xm, const void* data, int size);

void xmd5_result(xmd5 xm, unsigned char* result);

void xmd5_delete(xmd5 xm);

struct xsha1_impl;

typedef struct xsha1_impl* xsha1;

xsha1 xsha1_new();

void xsha1_feed(xsha1 xsh, void* data, int size);

int xsha1_result(xsha1 xsh, unsigned int* result);

void xsha1_delete(xsha1 xsh);

#endif


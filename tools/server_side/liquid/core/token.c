#include <stddef.h>

#include "token.h"

inline int static get_md5_token(unsigned char* md5) {
  int val = 0;

  // use the last 3 bytes for token.
  // we don't use the first bytes, because they are used to organize filesystem layout.
  // if they are used, then files tend to cumulate in a certain subfolder, while others are not used.

  val |= md5[13];
  val <<= 8;
  val |= md5[14];
  val <<= 8;
  val |= md5[15];
  return val;
}

xbool token_include(token tkn, unsigned char* md5) {
  xbool ret = XFALSE;
  int val = get_md5_token(md5);
  if (tkn.start <= val && val <= tkn.end) {
    ret = XTRUE;
  }
  return ret;
}

token_set create_token_set() {
  // TODO
  return NULL;
}

token_set create_token_set_from_peer(xsocket xsock) {
  // TODO
  return NULL;
}

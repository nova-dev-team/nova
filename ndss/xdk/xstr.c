#include <string.h>
#include <stdio.h>

#include "xstr.h"
#include "xmemory.h"

struct _xstr {
  char* str;
};

xstr xstr_new() {
  xstr xs = xmalloc_ty(1, struct _xstr);
  xs->str = xmalloc_ty(10, char);
  strcpy(xs->str, "woka\n");
  return xs;
}

void xstr_delete(xstr xs) {
  xfree(xs->str);
  xfree(xs);
}

const char* xstr_as_cstr(xstr xs) {
  return xs->str;
}

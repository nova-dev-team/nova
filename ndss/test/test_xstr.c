#include <stdio.h>

#include "xstr.h"

int main() {
  xstr xs = xstr_new();
  char* cs = xstr_as_cstr(xs);
  cs[2] = 'f';
  printf("good!\n%s\n", xstr_as_cstr(xs));
  xstr_delete(xs);
  return 0;
}

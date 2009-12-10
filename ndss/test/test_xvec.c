#include <stdio.h>

#include "xvec.h"
#include "xmemory.h"

void int_xv_free(void* ptr) {
  xfree(ptr);
}

int main() {
  xvec xv = xvec_new(int_xv_free);
  int i;
  int *q;
  for (i = 0; i < 10; i++) {
    int* p = xmalloc_ty(1, int);
    *p = i;
    xvec_push_back(xv, p);
    printf("size=%d\n", xvec_size(xv));
  }
  for (i = 0; i < xvec_size(xv); i++) {
    int* p = xvec_get(xv, i);
    printf("%d: %d\n", i, *p);
  }
  for (i = 0; i < xvec_size(xv); i+= 2) {
    xvec_remove(xv, i);
  }
  for (i = 0; i < xvec_size(xv); i++) {
    int* p = xvec_get(xv, i);
    printf("%d: %d\n", i, *p);
  }
  for (i = 0; i < 10; i++) {
    q = xmalloc_ty(1, int);
    *q = 10000 + i;
    xvec_insert(xv, 0, q);
  }
  for (i = 0; i < xvec_size(xv); i++) {
    int* p = xvec_get(xv, i);
    printf("%d: %d\n", i, *p);
  }
  xvec_delete(xv);
  printf("mem use=%d\n", xmem_usage());
  return xmem_usage();
}

#include <malloc.h>

#include "xmemory.h"

static int _xmalloc_counter = 0;

void* xmalloc(int size) {
  if (size <= 0)
    return NULL;
  else {
// TODO pthread lock 'counter++' operation
    _xmalloc_counter++;
    return malloc(size);
  }
}

void xfree(void* ptr) {
// TODO pthread lock 'counter--' operation
  _xmalloc_counter--;
  free(ptr);
}

int xmem_use() {
  return _xmalloc_counter;
}


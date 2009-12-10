#include <stddef.h>

#include "xvec.h"
#include "xmemory.h"

struct xvec_impl {
  xvec_free xvfree;
  int size;
  int mem_size;
  void** data;
};

xvec xvec_new(xvec_free xvfree) {
  xvec xv = xmalloc_ty(1, struct xvec_impl);
  xv->xvfree = xvfree;
  xv->size = 0;
  xv->mem_size = 16;
  xv->data = xmalloc_ty(xv->mem_size, void *);
  return xv;
}

void xvec_delete(xvec xv) {
  int i;
  for (i = 0; i < xv->size; i++) {
    xv->xvfree(xv->data[i]);
  }
  xfree(xv->data);
  xfree(xv);
}

int xvec_size(xvec xv) {
  return xv->size;
}

void* xvec_get(xvec xv, int index) {
  if (index < 0 || index >= xv->size)
    return NULL;

  return xv->data[index];
}

static void ensure_mem_size(xvec xv, int mem_size) {
  if (xv->mem_size < mem_size) {
    xv->mem_size = mem_size * 2;
    xv->data = xrealloc(xv->data, xv->mem_size * sizeof(void *));
  }
}

int xvec_put(xvec xv, int index, void* data) {
  if (index < 0 || index > xv->size)
    return -1;
  
  ensure_mem_size(xv, index + 1);
  if (index == xv->size) {
    xv->size++; // append new data
  }
  xv->data[index] = data;
  return index;
}

int xvec_insert(xvec xv, int index, void* data) {
  if (index < 0 || index > xv->size)
    return -1;

  ensure_mem_size(xv, xv->size + 1);
  xv->size++;
  if (index == xv->size) {
    // append to end
    xv->data[index] = data;
  } else {
    int i;
    for (i = xv->size - 1; i >= index; i--) {
      xv->data[i + 1] = xv->data[i];
    }
    xv->data[index] = data;
  }

  return index;
}

int xvec_push_back(xvec xv, void* data) {
  return xvec_put(xv, xv->size, data);
}

int xvec_remove(xvec xv, int index) {
  int i;
  if (index < 0 || index >= xv->size)
    return -1;
  
  xv->xvfree(xv->data[index]);
  for (i = index; i < xv->size - 1; i++) {
    xv->data[i] = xv->data[i + 1];
  }
  xv->size--;
  return index;
}


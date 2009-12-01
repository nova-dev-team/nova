#include <stdio.h>

#include "xmemory.h"

static void test_single_alloc() {
  void* ptr = xmalloc(3);
  xfree(ptr);
}

static void test_array_alloc() {
  int n = 4;
  int i;
  int** ptr = (int **) xmalloc(sizeof(int *) * n);
  for (i = 0; i < n; i++) {
    ptr[i] = (int *) xmalloc(sizeof(int) * n);
  }
  printf("memeory_usage=%d\n", xmem_use());
  for (i = 0; i < n; i++) {
    xfree(ptr[i]);
  }
  xfree(ptr);
}

static void test_multi_thread_alloc() {
  // TODO test multi_thread_alloc
}

int main() {
  test_single_alloc();
  test_array_alloc();
  test_multi_thread_alloc();
  return xmem_use();
}

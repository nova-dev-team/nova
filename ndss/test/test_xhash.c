#include <stdio.h>

#include "xhash.h"
#include "xmemory.h"

// hash function for int-int hash table
int ii_hash_func(void* key) {
  return (int) key;
}

// eql function for int-int hash table
int ii_eql_func(void* key1, void* key2) {
  return (int) key1 == (int) key2;
}

// free function for int-int hash table
void ii_free_func(void* key, void* value) {
  // do nothing
}

void test_int_int_hash() {
  xhash xh;
  int i;
  xh = xhash_new(ii_hash_func, ii_eql_func, ii_free_func);
  for (i = 0; i < 190000; i++) {
    xhash_put(xh, (void *) i, (void *) i);
    if (i % 5000 == 0) {
      printf("size(xh)=%d\n", xhash_size(xh));
      printf("xmem_usage=%d\n", xmem_usage());
    }
  }
  for (i = 0; i < 189900; i++) {
    xhash_remove(xh, (void *) i);
  }
  for (i = 189900; i < 190000; i++) {
    void* ptr = xhash_get(xh, (void *) i);
    int val = (int) ptr;
    printf("val=%d\n", val);
  }
  printf("size(xh)=%d\n", xhash_size(xh));
  printf("xmem_usage=%d\n", xmem_usage());
  xhash_delete(xh);
  printf("xmem_usage=%d\n", xmem_usage());
}

int main() {
  test_int_int_hash();
  return xmem_usage();
}


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
  xhash_init(&xh, ii_hash_func, ii_eql_func, ii_free_func);
  xhash_release(&xh);
}

int main() {
  test_int_int_hash();
  printf("xmem_usage=%d\n", xmem_use());
  return xmem_use();
}

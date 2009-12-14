#include <stdio.h>

#include "xhash.h"
#include "xmemory.h"

// hash function for int-int hash table
int ii_hash_func(void* key) {
  return *(int *)key;
}

// eql function for int-int hash table
xbool ii_eql_func(void* key1, void* key2) {
  if ((*(int *) key1) == (*(int *) key2))
    return XTRUE;
  else
    return XFALSE;
}

// free function for int-int hash table
void ii_free_func(void* key, void* value) {
  xfree(key);
  xfree(value);
}

void test_int_int_hash() {
  xhash xh;
  int i;
  int* helper_key = xmalloc_ty(1, int);
  xh = xhash_new(ii_hash_func, ii_eql_func, ii_free_func);
  for (i = 0; i < 190000; i++) {
    int* key = xmalloc_ty(1, int);
    int* value = xmalloc_ty(1, int);
    *key = i;
    *value = i;
    xhash_put(xh, key, value);
    if (i % 5000 == 0) {
      printf("size(xh)=%d\n", xhash_size(xh));
      printf("xmem_usage=%d\n", xmem_usage());
    }
  }
  for (i = 0; i < 189900; i++) {
    *helper_key = i;
    xhash_remove(xh, helper_key);
  }
  for (i = 189900; i < 190000; i++) {
    *helper_key = i;
    void* ptr = xhash_get(xh, helper_key);
    int val = *(int *) ptr;
    printf("val=%d\n", val);
  }
  printf("size(xh)=%d\n", xhash_size(xh));
  printf("xmem_usage=%d\n", xmem_usage());
  xhash_delete(xh);
  xfree(helper_key);
  printf("xmem_usage=%d\n", xmem_usage());
}

int main() {
  test_int_int_hash();
  return xmem_usage();
}


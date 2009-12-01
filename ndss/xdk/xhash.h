#ifndef XHASH_H_
#define XHASH_H_

typedef int (*xhash_hash)(void* key);
typedef int (*xhash_eql)(void* key1, void* key2);
typedef void (*xhash_free)(void* key, void* value);

typedef struct hash_entry {
  void* key;
  void* value;
  struct xhash_entry* next;
} xhash_entry;

typedef struct xhash {
  xhash_entry** slot;

  int entry_count; // for calculating load average
  int extend_ptr;  // index of next element to be expanded
  int extend_level; // how many times the table get expanded
  int base_size; // the size of the hash table in the begining
    // current hash table's "appearing" size is base_size * 2 ^ level
    // actuall size could be calculated by: extend_ptr + base_size * 2 ^ level

  xhash_hash hash_func;
  xhash_eql eql_func;
  xhash_free free_func;
} xhash;


void xhash_init(xhash* xh, xhash_hash arg_hash, xhash_eql arg_eql, xhash_free arg_free);

void xhash_release(xhash* xh);

void xhash_put(xhash* xh, void* key, void* value);

void* xhash_get(xhash* xh, void* key);

int xhash_remove(xhash* xh, void* key);

#endif


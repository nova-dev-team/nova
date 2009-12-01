#include <stdlib.h>

#include "xhash.h"
#include "xmemory.h"

#define XHASH_INIT_SLOT_COUNT 16
#define XHASH_THRESHOLD 3

#define ALLOC(ty, n) ((ty *) xmalloc(sizeof(ty) * (n)))
#define REALLOC(ty, ptr, n) (ty *) xrealloc(ptr, sizeof(ty) * (n))

void xhash_init(xhash* xh, xhash_hash arg_hash, xhash_eql arg_eql, xhash_free arg_free) {
  int i;

  xh->extend_ptr = 0;
  xh->extend_level = 0;
  xh->base_size = XHASH_INIT_SLOT_COUNT;
  xh->entry_count = 0;

  xh->hash_func = arg_hash;
  xh->eql_func = arg_eql;
  xh->free_func = arg_free;

  xh->slot = ALLOC(xhash_entry*, xh->base_size);

  for (i = 0; i < xh->base_size; i++) {
    xh->slot[i] = NULL;
  }

}

void xhash_release(xhash* xh) {
  size_t i;
  size_t actual_size = (xh->base_size << xh->extend_level) + xh->extend_ptr;
  xhash_entry* p;
  xhash_entry* q;
  
  for (i = 0; i < actual_size; i++) {
    p = xh->slot[i];
    while (p != NULL) {
      q = p->next;
      xh->free_func(p->key, p->value);
      xfree(p);
      p = q;
    }
  }

  xfree(xh->slot);

}

// calculates the actuall slot id of a key
static int _xhash_slot_id(xhash* xh, void* key) {
  size_t hcode = xh->hash_func(key);
  if (hcode % (xh->base_size << xh->extend_level) < xh->extend_ptr) {
    // already extended part
    return hcode % (xh->base_size << (1 + xh->extend_level));
  } else {
    // no the yet-not-extended part
    return hcode % (xh->base_size << xh->extend_level);
  }
}


// extend the hash table if necessary
static void _xhash_try_extend(xhash *xh) {
  if (xh->entry_count > XHASH_THRESHOLD * (xh->base_size << xh->extend_level)) {
    xhash_entry* p;
    xhash_entry* q;

    size_t actual_size = (xh->base_size << xh->extend_level) + xh->extend_ptr;

    xh->slot = REALLOC(xhash_entry *, xh->slot, actual_size + 1);
    p = xh->slot[xh->extend_ptr];
    xh->slot[xh->extend_ptr] = NULL;
    xh->slot[actual_size] = NULL;
    while (p != NULL) {
      size_t hcode = xh->hash_func(p->key);
      size_t slot_id = hcode % (xh->base_size << (1 + xh->extend_level));
      q = p->next;
      p->next = xh->slot[slot_id];
      xh->slot[slot_id] = p;
      p = q;
    }

    xh->extend_ptr++;
    if (xh->extend_ptr == (xh->base_size << xh->extend_level)) {
      xh->extend_ptr = 0;
      xh->extend_level++;
    }
  }

}

void xhash_put(xhash* xh, void* key, void* value) {
  size_t slot_id;
  xhash_entry *entry = ALLOC(xhash_entry, 1);

  // first of all, test if need to expand
  _xhash_try_extend(xh);

  slot_id = _xhash_slot_id(xh, key);

  entry->next = xh->slot[slot_id];
  entry->key = key;
  entry->value = value;
  xh->slot[slot_id] = entry;

  xh->entry_count++;
}

void *xhash_get(xhash* xh, void* key) {
  size_t slot_id = _xhash_slot_id(xh, key);

  xhash_entry* p;
  xhash_entry* q;

  p = xh->slot[slot_id];
  while (p != NULL) {
    q = p->next;
    if (xh->eql_func(key, p->key))
      return p->value;
    p = q;
  }
  return NULL;
}

// shrink the hash table if necessary
// table size is shrinked to 1/2
static void _xhash_try_shrink(xhash *xh) {
  if (xh->extend_level == 0)
    return;

  if (xh->entry_count * XHASH_THRESHOLD < (xh->base_size << xh->extend_level)) {
    int i;
    int actual_size = (xh->base_size << xh->extend_level) + xh->extend_ptr;
    int new_size = xh->base_size << (xh->extend_level - 1);

    for (i = new_size; i < actual_size; i++) {
      int slot_id = i % new_size;
      //merge(&lh->slot[i % new_size], lh->slot[i]);
      if (xh->slot[slot_id] == NULL) {
        xh->slot[slot_id] = xh->slot[i];
      } else {
        xhash_entry *p = xh->slot[slot_id];
        while (p->next != NULL)
          p = p->next;
        p->next = xh->slot[i];
      }
    }

    xh->extend_level--;
    xh->extend_ptr = 0;

    xh->slot = REALLOC(xhash_entry *, xh->slot, xh->base_size << xh->extend_level);
  }
}

int xhash_remove(xhash* xh, void* key) {
  int slot_id;
  xhash_entry* p;
  xhash_entry* q;

  _xhash_try_shrink(xh);
  slot_id = _xhash_slot_id(xh, key);
  p = xh->slot[slot_id];
  if (p == NULL) {
    // head is null, so no element found
    return -1;
  } else if (xh->eql_func(key, p->key)) {
    // head is target
    xh->slot[slot_id] = xh->slot[slot_id]->next;
    xh->free_func(p->key, p->value);
    xfree(p);
    xh->entry_count--;
    return 0;
  } else {
    // head is not target
    q = p->next;
    for (;;) {
      // q is the item to be checked
      q = p->next;
      if (q == NULL)
        return -1;
      
      if (xh->eql_func(key, q->key)) {
        // q is target
        p->next = q->next;
        xh->free_func(q->key, q->value);
        xfree(q);
        xh->entry_count--;
        return 0;
      }
      p = q;
    }
  }
  return -1;
}


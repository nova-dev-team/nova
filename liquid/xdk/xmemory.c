#include <malloc.h>

#include "xhash.h"

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

#ifdef XMEM_DEBUG

static xhash _xmalloc_registry = NULL;

int registry_hashcode(void* key) {
  return (int) key;
}

xbool registry_hash_key_eql(void* key1, void* key2) {
  return key1 == key2;
}

void registry_hash_free(void* key, void* value) {
  xfree(value);
}

typedef struct loc_info {
  const char* file;
  int line;
} loc_info;

void* xmalloc_debug(int size, const char* file, int line) {
  void* ptr = xmalloc(size);
  loc_info* loc = (loc_info *) xmalloc(sizeof(loc_info));
  if (_xmalloc_registry == NULL) {
    _xmalloc_registry = xhash_new(registry_hashcode, registry_hash_key_eql, registry_hash_free);
  }
  loc->file = file;
  loc->line = line;
  xhash_put(_xmalloc_registry, ptr, (void *) loc);
  return ptr;
}
#endif

void xfree(void* ptr) {
// TODO pthread lock 'counter--' operation

#ifdef XMEM_DEBUG
  xhash_remove(_xmalloc_registry, ptr);
#endif

  _xmalloc_counter--;
  free(ptr);
}

void* xrealloc(void* ptr, int new_size) {
  void* new_ptr = realloc(ptr, new_size);
#ifdef XMEM_DEBUG
  loc_info* loc = (loc_info *) xhash_get(_xmalloc_registry, ptr);
  if (loc != NULL) {
    loc_info* new_loc = (loc_info *) xmalloc(sizeof(loc_info));
    new_loc->file = loc->file;
    new_loc->line = loc->line;
    xhash_put(_xmalloc_registry, new_ptr, (void *) new_loc);
  }
#endif
  return new_ptr;
}

int xmem_usage() {
  return _xmalloc_counter;
}

#ifdef XMEM_DEBUG

static xbool xmem_reg_print_visitor(void* key, void* value) {
  loc_info* loc = (loc_info *) value;
  printf("mem_usage: %s\t %d\n", loc->file, loc->line);
  return XTRUE;
}

void xmem_print_usage() {
  xhash_visit(_xmalloc_registry, xmem_reg_print_visitor);
}
#endif

void xmem_reset_counter() {
#ifdef XMEM_DEBUG
  if (_xmalloc_registry != NULL) {
    xhash_delete(_xmalloc_registry);
    _xmalloc_registry = NULL;
  }
#endif
  _xmalloc_counter = 0;
}


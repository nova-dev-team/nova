#include <time.h>
#include <assert.h>

#include "xmemory.h"
#include "xutils.h"

#include "imgmount_fs_cache.h"

static fs_cache fs_cache_new_raw() {
  fs_cache entry = xmalloc_ty(1, struct fs_cache_impl);
  entry->child = NULL;
  entry->mtime = time(NULL);
  entry->sync_time.tv_sec = -1;
  entry->sync_time.tv_usec = -1;
  entry->sync_expire.tv_sec = 1;
  entry->sync_expire.tv_usec = 0;
  pthread_mutex_init(&(entry->mutex), NULL);
  return entry;
}

fs_cache fs_cache_new_root() {
  return fs_cache_new_dir("/", NULL);
}

static void fs_cache_delete_child(void* key, void* value) {
  xstr_delete((xstr) key);
  fs_cache_delete((fs_cache) value);
}

fs_cache fs_cache_new_dir(const char* name, fs_cache parent) {
  fs_cache entry = fs_cache_new_raw();
  entry->name = xstr_new_from_cstr(name);
  entry->type = FS_CACHE_DIR;
  entry->child = xhash_new(xhash_hash_xstr, xhash_eql_xstr, fs_cache_delete_child);
  if (parent == NULL) {
    // root folder!
    entry->parent = entry;
  } else {
    entry->parent = parent;
    // also add to parent's child list
    xstr child_key = xstr_copy(entry->name);
    xhash_put(entry->parent->child, child_key, entry);
  }
  entry->perm = 0755;
  entry->size = 0;
  return entry;
}

fs_cache fs_cache_new_file(const char* name, fs_cache parent) {
  fs_cache entry = fs_cache_new_raw();
  xstr child_key;
  entry->name = xstr_new_from_cstr(name);
  entry->type = FS_CACHE_FILE;
  entry->child = NULL;
  assert(parent != NULL);
  entry->parent = parent;
  // add this entry to parent's child list
  child_key = xstr_copy(entry->name);
  xhash_put(entry->parent->child, child_key, entry);
  entry->perm = 0644;
  entry->size = 0;
  return entry;
}

void fs_cache_clear_child(fs_cache entry) {
  assert(entry->type == FS_CACHE_DIR);
  xhash_delete(entry->child);
  entry->child = xhash_new(xhash_hash_xstr, xhash_eql_xstr, fs_cache_delete_child);
}

void fs_cache_delete(fs_cache entry) {
  xstr_delete(entry->name);
  if (entry->type == FS_CACHE_FILE) {
    assert(entry->child == NULL);
  } else {
    assert(entry->type == FS_CACHE_DIR);
    assert(entry->child != NULL);
    xhash_delete(entry->child);
  }
  pthread_mutex_destroy(&(entry->mutex));
  xfree(entry);
}

xbool fs_cache_is_synced(fs_cache entry) {
  xbool ret;
  if (entry->sync_time.tv_sec < 0) {
    ret = XFALSE;
  } else {
    ret = XTRUE;
  }
  return ret;
}

xsuccess fs_cache_get_fullpath(fs_cache entry, xstr fullpath) {
  xsuccess ret = XSUCCESS;
  fs_cache e = entry;
  xstr_set_cstr(fullpath, "/");
  for (;;) {
    if (e == e->parent) {
      break;
    } else {
      xstr_add_prefix_cstr(fullpath, xstr_get_cstr(e->name));
      xstr_add_prefix_cstr(fullpath, "/");
      e = e->parent;
    }
  }
  return ret;
}


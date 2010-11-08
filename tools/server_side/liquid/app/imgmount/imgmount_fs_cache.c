#include <time.h>
#include <assert.h>

#include "xmemory.h"
#include "xutils.h"
#include "xhash.h"

#include "imgmount_fs_cache.h"

fs_cache fs_cache_new_raw() {
  fs_cache entry = xmalloc_ty(1, struct fs_cache_impl);
  entry->name = xstr_new();
  entry->child = NULL;
  entry->parent = NULL;
  entry->mtime = (int) time(NULL);

  // a special mark for "not sync'ed yet"
  entry->sync_time.tv_sec = -1;
  entry->sync_time.tv_usec = -1;

  // default expire time of 10 sec
  entry->sync_expire.tv_sec = 10;
  entry->sync_expire.tv_usec = 0;
  pthread_mutex_init(&(entry->mutex), NULL);

  // The following fields are used by imgdir. By default, set them to 'invalid' state.
  entry->uid = FS_CACHE_INVALID_UID;
  entry->parent_uid = FS_CACHE_INVALID_UID;
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
  xstr_set_cstr(entry->name, name);
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
  xstr_set_cstr(entry->name, name);
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
    if (entry->child != NULL) {
      xhash_delete(entry->child);
    }
  }
  pthread_mutex_destroy(&(entry->mutex));
  xfree(entry);
}

xbool fs_cache_is_synced(fs_cache entry) {
  xbool ret;
  if (entry->sync_time.tv_sec < 0) {
    // special case, never sync'ed yet
    ret = XFALSE;
  } else {
    struct timeval now;
    struct timeval expire_time;
    expire_time.tv_sec = entry->sync_time.tv_sec + entry->sync_expire.tv_sec;
    expire_time.tv_usec = entry->sync_time.tv_usec + entry->sync_expire.tv_usec;
    // normalize the expire_time value
    while (expire_time.tv_usec > 1000 * 1000) {
      expire_time.tv_usec -= 1000 * 1000;
      expire_time.tv_sec += 1;
    }
    gettimeofday(&now, NULL);
    if (now.tv_sec > expire_time.tv_sec || (now.tv_sec == expire_time.tv_sec && now.tv_usec > expire_time.tv_usec)) {
      ret = XFALSE;
    } else {
      ret = XTRUE;
    }
  }
  return ret;
}

xsuccess fs_cache_get_fullpath(fs_cache entry, xstr fullpath) {
  xsuccess ret = XSUCCESS;
  fs_cache e = entry;
  if (entry->type == FS_CACHE_DIR) {
    xstr_set_cstr(fullpath, "/");
  } else {
    xstr_set_cstr(fullpath, "");
  }
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

static xbool fs_cach_tree_count_visitor(void* key, void* value, void* arg) {
  int* p_count = (int *) arg;
  *p_count += fs_cache_tree_count((fs_cache) value);
  return XTRUE;
}


static void fs_cache_tree_count_real(fs_cache root, int* p_count) {
  (*p_count)++;
  if (root->type == FS_CACHE_DIR) {
    assert(root->child != NULL);
    xhash_visit(root->child, fs_cach_tree_count_visitor, p_count);
  } else {
    assert(root->child == NULL && root->type == FS_CACHE_FILE);
  }
}

int fs_cache_tree_count(XIN fs_cache root) {
  int count = 0;
  fs_cache_tree_count_real(root, &count);
  return count;
}



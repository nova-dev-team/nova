#include <string.h>
#include <assert.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>

#include "xdef.h"
#include "xutils.h"

#include "imgmount_fs_cache.h"
#include "imgmount_fuse.h"
#include "imgmount_instance.h"

/**
  @brief
    Retrieve the imgmount instance from FUSE's private data.

  @return
    The imgmount instance that we started FUSE.
*/
static imgmount_instance get_imgmount_instance() {
  return (imgmount_instance) (fuse_get_context()->private_data);
}


/**
  @brief
    Do real update work on fs_cache.

  @param entry
    The fs_entry to be updated.

  @return
    Whether the resync process is successful.
*/
static xsuccess resync_fs_entry(fs_cache entry) {
  xsuccess ret = XSUCCESS;
  struct timeval now;
  xstr fullpath = xstr_new();
  struct stat st;
  fs_cache_get_fullpath(entry, fullpath);

  // TODO real resync! currently we just mirror local root fs!
  if (stat(xstr_get_cstr(fullpath), &st) == 0) {
    ret = XSUCCESS;
  } else {
    ret = XFAILURE;
  }
  if (ret == XSUCCESS) {
    // fill in file info
    entry->mtime = st.st_mtime;
    entry->size = st.st_size;
    entry->perm = st.st_mode & 0777;

    if (S_ISDIR(st.st_mode)) {
      DIR* p_dir;
      assert(entry->type == FS_CACHE_DIR);

      // clear all existing child entries
      fs_cache_clear_child(entry);
      p_dir = opendir(xstr_get_cstr(fullpath));
      if (p_dir == NULL) {
        // failed to open dir!
        ret = XFAILURE;
      } else {
        struct dirent* p_dirent;
        xstr dirent_path = xstr_new();;
        while ((p_dirent = readdir(p_dir)) != NULL) {
          xstr_set_cstr(dirent_path, xstr_get_cstr(fullpath));
          xstr_append_char(dirent_path, '/');
          xstr_append_cstr(dirent_path, p_dirent->d_name);
          if (lstat(xstr_get_cstr(dirent_path), &st) == 0) {
            if (S_ISDIR(st.st_mode)) {
              fs_cache_new_dir(p_dirent->d_name, entry);
            } else if (S_ISREG(st.st_mode)) {
              fs_cache_new_file(p_dirent->d_name, entry);
            }
          }
        }
        xstr_delete(dirent_path);
        closedir(p_dir);
      }

    } else if (S_ISREG(st.st_mode)) {
      assert(entry->type == FS_CACHE_FILE);
    } else {
      // just ignore other files
      ret = XFAILURE;
    }
  }

  // updata the sync_time, AFTER retrieved all data
  // NOTE: we update the sync_time even in case of failure, this prevents continuous resync ops
  gettimeofday(&now, NULL);
  entry->sync_time = now;
  xstr_delete(fullpath);
  return ret;
}

/**
  @brief
    Update a fs_cache's info if necessary.
    All updating work, including setting up the children, updating sync_time value, is done in this function.
    If connection failed, the entry will be left unchanged, but a XFAILURE will be returned.

  @param entry
    The fs_cache entry to be updated.

  @return
    Whether the resync process is successful.
*/
static xsuccess resync_fs_entry_if_necessary(fs_cache entry) {
  xsuccess ret = XSUCCESS;
  xbool need_sync = XFALSE;
  struct timeval now;
  if (fs_cache_is_synced(entry) == XFALSE) {
    need_sync = XTRUE;
  } else {
    struct timeval expire_time;
    expire_time.tv_sec = entry->sync_time.tv_sec + entry->sync_expire.tv_sec;
    expire_time.tv_usec = entry->sync_time.tv_usec + entry->sync_expire.tv_usec;
    // normalize the expire_time value
    while (expire_time.tv_usec > 1000 * 1000) {
      expire_time.tv_usec -= 1000 * 1000;
      expire_time.tv_sec += 1;
    }
    gettimeofday(&now, NULL);
    if (now.tv_sec > expire_time.tv_sec || (now.tv_sec == expire_time.tv_sec && now.tv_usec >= expire_time.tv_usec)) {
      need_sync = XTRUE;
    }
  }
  if (need_sync == XTRUE) {
    ret = resync_fs_entry(entry);
  }
  return ret;
}

/**
  @brief
    A helper function to find a file entry.

  @param path
    The path to the file entry.

  @return
    The corresponding fs_cache entry. If not found, NULL will be returned.
*/
static fs_cache find_fs_entry(const char* path) {
  fs_cache entry = NULL;
  fs_cache parent = get_root(get_imgmount_instance());
  xstr norm_path = xstr_new();
  xfilesystem_normalize_abs_path(path, norm_path);
  if (xstr_eql_cstr(norm_path, "/") == XTRUE) {
    entry = parent;
  } else {
    const char* npath = xstr_get_cstr(norm_path); // make life easier, pick elements of norm_path directly
    int start = 0, stop = 0;  // npath[start:stop] is an entry
    for (;;) {
      xstr seg_name = NULL;
      if (resync_fs_entry_if_necessary(parent) == XFAILURE) {
        // TODO log error!
      }

      while (npath[start] == '/')
        start++;
      if (npath[start] == '\0')
        break;
      // now npath[start] is the beginning of an entry's name
      stop = start;
      while (npath[stop + 1] != '/' && npath[stop + 1] != '\0')
        stop++;
      assert(stop >= start);
      // now npath[start:stop] is an entry

      seg_name = xstr_substr2(norm_path, start, stop - start + 1);
      // find in child
      entry = xhash_get(parent->child, seg_name);
      xstr_delete(seg_name);
      if (entry == NULL) {
        break;
      }

      // set new position for finding next entry name
      start = stop + 1;
      // go along the dir tree
      parent = entry;
    }
  }
  xstr_delete(norm_path);
  return entry;
}


// a helper fuction to fill in child nodes for imgmount_readdir()
static xbool readdir_fill_child_visitor(void* key, void* value, void* args) {
  void** arg_list = (void **) args;
  void* buf = arg_list[0];
  fuse_fill_dir_t filler = arg_list[1];
  filler(buf, xstr_get_cstr((xstr) key), NULL, 0);
  return XTRUE;
}

int imgmount_readdir(const char *path, void *buf, fuse_fill_dir_t filler, off_t offset, struct fuse_file_info *fi) {
  int ret = 0;
  fs_cache entry = find_fs_entry(path);
  if (entry == NULL) {
    ret = -ENOENT;
  } else {
    void* args[] = {buf, filler};
    // first check if is DIR, then fill data
    if (entry->type != FS_CACHE_DIR) {
      ret = -ENOTDIR;
    } else {
      // fill in dir data
      xhash_visit(entry->child, readdir_fill_child_visitor, args);
    }
  }
  return ret;
}

int imgmount_getattr(const char *path, struct stat *st) {
  int ret = 0;
  fs_cache entry = NULL;
  memset(st, 0, sizeof(struct stat));
  entry = find_fs_entry(path);
  if (entry == NULL) {
    ret = -ENOENT;
  } else {
    if (entry->type == FS_CACHE_DIR) {
      st->st_mode = S_IFDIR | entry->perm;
      st->st_nlink = 1; // TODO how to set this value?
    } else {
      assert(entry->type == FS_CACHE_FILE);
      st->st_mode = S_IFREG | entry->perm;
      st->st_nlink = 1; // TODO how to set this value?
    }
    st->st_size = entry->size;
  }
  return ret;
}



static const char  *file_path      = "/hello.txt";
static const char   file_content[] = "Hello World!\n";
static const size_t file_size      = sizeof(file_content)/sizeof(char) - 1;

int imgmount_open(const char *path, struct fuse_file_info *fi) {
  // TODO
  if (strcmp(path, file_path) != 0) /* We only recognize one file. */
    return -ENOENT;

  if ((fi->flags & O_ACCMODE) != O_RDONLY) /* Only reading allowed. */
    return -EACCES;

  return 0;
}


int imgmount_read(const char *path, char *buf, size_t size, off_t offset, struct fuse_file_info *fi) {
  // TODO
  if (strcmp(path, file_path) != 0)
    return -ENOENT;

  if (offset >= file_size) /* Trying to read past the end of file. */
    return 0;

  if (offset + size > file_size) /* Trim the read to the file size. */
    size = file_size - offset;

  memcpy(buf, file_content + offset, size); /* Provide the content. */

  return size;
}

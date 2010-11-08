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
#include "imgmount_protocol.h"

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
  @param p_errcode
    Pointer to error code holder.

  @return
    Whether the resync process is successful.
*/
static xsuccess resync_fs_entry(fs_cache entry, int* p_errcode) {
  xsuccess ret = XSUCCESS;
  struct timeval now;
  xstr fullpath = xstr_new();
  fs_cache_get_fullpath(entry, fullpath);

  // sync by "list" request if is dir, or use getattr to sync a single file
  if (entry->type == FS_CACHE_DIR) {
    *p_errcode = protocol_request_list(get_imgmount_instance(), fullpath, entry);
  } else {
    // TODO sync through getattr request
    printf("TODO sync through getattr request\n");
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
  @param p_errcode
    Pointer to error code holder.

  @return
    Whether the resync process is successful.
*/
static xsuccess resync_fs_entry_if_necessary(fs_cache entry, int* p_errcode) {
  xsuccess ret = XSUCCESS;
  if (fs_cache_is_synced(entry) == XFALSE) {
    ret = resync_fs_entry(entry, p_errcode);
  }
  return ret;
}

/**
  @brief
    A helper function to find a file entry.

  @param path
    The path to the file entry.
  @param p_errcode
    Pointer to error code holder.

  @return
    The corresponding fs_cache entry. If not found, NULL will be returned.
*/
static fs_cache find_fs_entry(const char* path, int* p_errcode) {
  fs_cache entry = NULL;
  if (path[0] != '/') {
    // must start with '/'!
    *p_errcode = -ENOENT;
    entry = NULL;
  } else {
    fs_cache parent = get_imgmount_instance()->fs_root;
    xstr norm_path = xstr_new();
    xfilesystem_normalize_abs_path(path, norm_path);
    *p_errcode = 0;
    if (xstr_eql_cstr(norm_path, "/") == XTRUE) {
      entry = parent;
    } else {
      const char* npath = xstr_get_cstr(norm_path); // make life easier, pick elements of norm_path directly
      int start = 0, stop = 0;  // npath[start:stop] is an entry
      for (;;) {
        xstr seg_name = NULL;
        if (resync_fs_entry_if_necessary(parent, p_errcode) == XFAILURE) {
          // TODO log error!
        }
        if (*p_errcode != 0) {
          entry = NULL;
          break;
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
          // not found!
          *p_errcode = -ENOENT;
          break;
        }

        // set new position for finding next entry name
        start = stop + 1;
        // go along the dir tree
        parent = entry;
      }
    }
    xstr_delete(norm_path);
  }
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
  fs_cache entry = find_fs_entry(path, &ret);
  if (entry != NULL) {
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
  entry = find_fs_entry(path, &ret);
  if (entry != NULL) {
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


int imgmount_statfs(const char *path, struct statvfs *vfs) {
  int ret = 0;
  // The 'f_frsize', 'f_favail', 'f_fsid' and 'f_flag' fields are ignored

  // TODO set those parameters
  // pretending a 1GB drive
  long long pretend_disk_size = 1024LL * 1024 * 1024;
  vfs->f_bsize = 8192;      // filesystem block size
  vfs->f_blocks = pretend_disk_size / vfs->f_bsize;  // size of fs in f_frsize units
  vfs->f_bfree = vfs->f_blocks;   // free blocks
  vfs->f_bavail = vfs->f_blocks;  // free blocks for non-root
  vfs->f_files = 1024 * 1024;  // inodes
  vfs->f_ffree = vfs->f_files;
  vfs->f_namemax = 255;     // maximum filename length

  return ret;
}


int imgmount_access(const char* path, int mode) {
  int ret = 0;
  // TODO
  return ret;
}


int imgmount_mkdir(const char* path, mode_t mode) {
  int ret = 0;
  xstr xpath = xstr_new_from_cstr(path);
  xstr parent_folder = xstr_new();
  xstr subfolder_name = xstr_new();
  fs_cache parent_node = NULL;

  xstr_strip(xpath, "\r\n");
  xfilesystem_split_path(xpath, parent_folder, subfolder_name);
  parent_node = find_fs_entry(xstr_get_cstr(parent_folder), &ret);

  if (parent_node == NULL) {
    ret = -ENOENT;
  } else if (parent_node->type != FS_CACHE_DIR) {
    ret = -ENOTDIR;
  } else {
    // if necessary, protocol_request_mkdir will setup the parent/child relationship
    ret = protocol_request_mkdir(get_imgmount_instance(), parent_node, path, xstr_get_cstr(subfolder_name));
  }

  xstr_delete(xpath);
  xstr_delete(parent_folder);
  xstr_delete(subfolder_name);
  return ret;
}



int imgmount_open(const char *path, struct fuse_file_info *fi) {
  int ret = 0;
  fs_cache entry = NULL;
  entry = find_fs_entry(path, &ret);
  // TODO implement real logic
  if (entry == NULL) {
    ret = -ENOENT;
  } else if ((fi->flags & O_ACCMODE) != O_RDONLY) {
    // currently only support reading
    ret = -EACCES;
  }
  return ret;
}


int imgmount_read(const char *path, char *buf, size_t size, off_t offset, struct fuse_file_info *fi) {
  const char* file_content = "TODO!\n";
  const size_t file_size = strlen(file_content);;
  int ret = 0;
  fs_cache entry = NULL;
  entry = find_fs_entry(path, &ret);
  // TODO implement real logic
  if (entry == NULL) {
    ret = -ENOENT;
  } else {
    // reading file content
    if (offset >= file_size) {
      size = 0;  // 0 bytes read
    } else if (offset + size > file_size) {
      size = file_size - offset;
    }
    memcpy(buf, file_content + offset, size);
    ret = size;
  }
  return ret;
}


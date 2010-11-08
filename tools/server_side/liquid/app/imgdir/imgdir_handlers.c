#include <stdio.h>
#include <errno.h>
#include <assert.h>

#include "xutils.h"
#include "xlog.h"
#include "xstr.h"

#include "imgdir_handlers.h"

/**
  @brief
    A helper function that find a node with a given path.

  @param root
    The root from where the search starts.
  @param path
    The path of the node to be found.
  @param p_errcode
    Pointer to error code holder. The error code will be returned in this value. If nothing goes wrong, 0 will be returned.

  @return
    The node with given path, or NULL if nothing was found (and p_errcode will be set to corresponding value).
*/
static fs_cache find_fs_node(XIN fs_cache root, XIN const char* path, XOUT int *p_errcode) {
  // adapted from find_fs_entry() in imgmount_fuse.c
  fs_cache entry = NULL;
  if (path[0] != '/') {
    // must start with '/'!
    *p_errcode = ENOENT;
    entry = NULL;
  } else {
    fs_cache parent = root;
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
        if (parent->type != FS_CACHE_DIR) {
          // oops, trying to find a sub node in a file node, this is not correct
          *p_errcode = ENOTDIR;
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
          *p_errcode = ENOENT;
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

// a helper function that replies errcode
static void reply_errcode(xsocket sock, int errcode) {
  switch (errcode) {
  case 0:
    xsocket_write_line(sock, "ok No error.\r\n");
    break;
  case ENOENT:
    xsocket_write_line(sock, "enoent Entry not found!\r\n");
    break;
  case ENOTDIR:
    xsocket_write_line(sock, "enotdir Some element in path is not directory!\r\n");
    break;
  case EEXIST:
    xsocket_write_line(sock, "eexist The entry already exists!\r\n");
    break;
  default:
    {
      xstr rep_text = xstr_new();
      xstr_printf(rep_text, "error Error code is %d!\n", errcode);
      xsocket_write_line(sock, xstr_get_cstr(rep_text));
      xstr_delete(rep_text);
      break;
    }
  }
}

// reply info for each child node
static xbool reply_ls_helper(void* key, void* value, void* arg) {
  xsocket sock = (xsocket) arg;
  fs_cache entry = (fs_cache) value;
  xstr rep_text = xstr_new();
  xstr_printf(
    rep_text, "name=%s\r\ntype=%d,size=%ld,mtime=%d,perm=%d\r\n",
    xstr_get_cstr(entry->name), (int) entry->type, (long long) entry->size, entry->mtime, entry->perm
  );
  xsocket_write_line(sock, xstr_get_cstr(rep_text));
  xstr_delete(rep_text);
  return XTRUE;
}

xsuccess imgdir_handle_ls(XIN imgdir_session session, xstr req_head) {
  xsuccess ret = XFAILURE;
  xsocket sock = get_session_socket(session);
  xstr path = xstr_substr(req_head, 5);
  fs_cache node;
  int errcode;
  xstr_strip(path, "\r\n");
  xlog_debug("[debug] list: %s", xstr_get_cstr(path));
  node = find_fs_node(get_fs_root(session), xstr_get_cstr(path), &errcode);
  reply_errcode(sock, errcode);

  if (errcode == 0) {
    xstr rep_text = xstr_new();
    assert(node != NULL && node->type == FS_CACHE_DIR);
    // reply directory listing

    // 1st line: count=%d\r\n
    xstr_printf(rep_text, "count=%d", xhash_size(node->child));
    xsocket_write_line(sock, xstr_get_cstr(rep_text));

    // write info for each node
    // (2k)-th line: name=%s\r\n
    // (2k+1)-th line: type=%d,size=%d,mtime=%d,perm=%d
    xhash_visit(node->child, reply_ls_helper, sock);

    xstr_delete(rep_text);
    ret = XSUCCESS;
  }

  xstr_delete(path);
  return ret;
}


xsuccess imgdir_handle_mkdir(XIN imgdir_session session, xstr req_head) {
  xsuccess ret = XFAILURE;
  xsocket sock = get_session_socket(session);
  xstr path = xstr_substr(req_head, 6);
  xstr parent_folder = xstr_new();
  xstr subfolder_name = xstr_new();
  fs_cache parent_node = NULL;
  fs_cache subdir_node = NULL;
  int errcode;

  xstr_strip(path, "\r\n");
  xfilesystem_split_path(path, parent_folder, subfolder_name);
  xlog_debug("[debug] mkdir: '%s' under '%s'", xstr_get_cstr(subfolder_name), xstr_get_cstr(parent_folder));
  parent_node = find_fs_node(get_fs_root(session), xstr_get_cstr(parent_folder), &errcode);

  if (errcode == 0) {
    // try to mkdir
    assert(parent_node != NULL && parent_node->type == FS_CACHE_DIR);
    subdir_node = xhash_get(parent_node->child, subfolder_name);
    if (subdir_node != NULL) {
      errcode = EEXIST;
    } else {
      // create new dir entry
      errcode = fsdb_mknode(get_fsdb(session), parent_node, xstr_get_cstr(subfolder_name), FS_CACHE_DIR, &subdir_node);
    }
    reply_errcode(sock, errcode);
    if (subdir_node != NULL && errcode == 0) {
      // reply new dir attr
      xstr rep_text = xstr_new();
      xstr_printf(
        rep_text, "size=%ld,mtime=%d,perm=%d\r\n",
        (long long) subdir_node->size, subdir_node->mtime, subdir_node->perm
      );
      xsocket_write_line(sock, xstr_get_cstr(rep_text));
      xstr_delete(rep_text);
    }
    ret = XSUCCESS;
  } else {
    reply_errcode(sock, errcode);
    ret = XFAILURE;
  }
  xstr_delete(path);
  xstr_delete(parent_folder);
  xstr_delete(subfolder_name);
  return ret;
}


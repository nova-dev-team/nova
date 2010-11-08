#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <errno.h>

#include "xnet.h"
#include "xhash.h"
#include "xlog.h"
#include "xutils.h"
#include "xvec.h"
#include "imgmount_protocol.h"

static void remote_listing_delete_handle(void* key, void* value) {
  xstr_delete((xstr) key);
  fs_cache_delete((fs_cache) value);
}

static xbool mark_entry_child_if_not_in_remote_listing(void* key, void* value, void* arg) {
  void** arg_list = (void **) arg;
  xstr key_xs = (xstr) key;
  xhash remote_listing = (xhash) arg_list[1];
  xvec del_list = (xvec) arg_list[2];
  if (xhash_get(remote_listing, key_xs) == NULL) {
    xstr elem = xstr_copy(key_xs);
    xvec_push_back(del_list, elem);
  }
  return XTRUE;
}

static xbool update_entry_from_remote_listing(void *key, void* value, void* arg) {
  fs_cache remote_node = (fs_cache) value;
  xstr key_xs = (xstr) key;
  void** arg_list = (void **) arg;
  fs_cache local_entry = (fs_cache) arg_list[0];
  fs_cache local_child = xhash_get(local_entry->child, key_xs);
  if (local_child == NULL) {
    // new child
    if (remote_node->type == FS_CACHE_DIR) {
      local_child = fs_cache_new_dir(xstr_get_cstr(key_xs), local_entry);
    } else {
      assert(remote_node->type == FS_CACHE_FILE);
      local_child = fs_cache_new_file(xstr_get_cstr(key_xs), local_entry);
    }
  }
  // update info like size, perm, mtime
  local_child->size = remote_node->size;
  local_child->perm = remote_node->perm;
  local_child->mtime = remote_node->mtime;
  return XTRUE;
}

static void xvec_delete_xstr_element(void* elem) {
  xstr_delete((xstr) elem);
}

// make sure entry's child is the same as remote listing
static void remote_listing_merge(fs_cache entry, xhash remote_listing) {
  // store the items to be deleted in a list, since it is not safe to remove hash table elements when visiting it
  xvec del_list = xvec_new(xvec_delete_xstr_element);
  void* args[] = {entry, remote_listing, del_list};
  int i;

  xhash_visit(entry->child, mark_entry_child_if_not_in_remote_listing, args);
  xhash_visit(remote_listing, update_entry_from_remote_listing, args);

  // remove elements
  for (i = 0; i < xvec_size(del_list); i++) {
    xhash_remove(entry->child, xvec_get(del_list, i));
  }

  xvec_delete(del_list);
}

// update fs_cache entry's value from parsed result of value_text
static void parse_fs_cache_value(fs_cache entry, const char* value_text) {
  xstr item = xstr_new();
  xstr value = xstr_new();
  int i = 0;
  while (value_text[i] != '\0') {
    xstr_set_cstr(item, "");
    xstr_set_cstr(value, "");
    while (value_text[i] != '\0') {
      if (value_text[i] == '=') {
        break;
      }
      xstr_append_char(item, value_text[i]);
      i++;
    }
    if (value_text[i] == '=') {
      i++;
    }
    while (value_text[i] != '\0') {
      if (value_text[i] == ',') {
        break;
      }
      xstr_append_char(value, value_text[i]);
      i++;
    }
    if (value_text[i] == ',') {
      i++;
    }
    if (xstr_eql_cstr(item, "type")) {
      int type_val = atoi(xstr_get_cstr(value));
      if (type_val == 0) {
        entry->type = FS_CACHE_DIR;
      } else {
        assert(type_val == 1);
        entry->type = FS_CACHE_FILE;
      }
    } else if (xstr_eql_cstr(item, "size")) {
      entry->size = atoi(xstr_get_cstr(value));
    } else if (xstr_eql_cstr(item, "mtime")) {
      entry->mtime = atoi(xstr_get_cstr(value));
    } else if (xstr_eql_cstr(item, "perm")) {
      entry->perm = atoi(xstr_get_cstr(value));
    } else {
      printf("DONT UNDERSTAND: '%s'='%s' from '%s', i=%d\n", xstr_get_cstr(item), xstr_get_cstr(value), value_text, i);
      // impossible to reach here
      assert(0);
    }
  }
  xstr_delete(item);
  xstr_delete(value);
}


static int try_parse_errno_from_reply(xstr rep_text) {
  int ret;
  if (xstr_startwith_cstr(rep_text, "enoent")) {
    ret = -ENOENT;
  } else if (xstr_startwith_cstr(rep_text, "enotdir")) {
    ret = -ENOTDIR;
  } else if (xstr_startwith_cstr(rep_text, "eexist")) {
    ret = -EEXIST;
  } else if (xstr_startwith_cstr(rep_text, "confused")) {
    // TODO find error code for 'general error'
    xlog_error("Server confused!");
    ret = -EIO;
  } else {
    // impossible to reach here, if all possible error code are listed correctly above
    xlog_fatal("unexpected message: %s", xstr_get_cstr(rep_text));
    assert(0);
  }
  return ret;
}

int protocol_request_list(imgmount_instance inst, xstr dir, fs_cache entry) {
  int ret = 0;
  xstr req_head = xstr_new();
  xstr rep_text = xstr_new();
  xsocket sock = inst->imgdir_sock;

  pthread_mutex_lock(&(inst->imgdir_sock_lock));
  xstr_printf(req_head, "list %s\r\n", xstr_get_cstr(dir));
  if (xsocket_write_line(sock, xstr_get_cstr(req_head)) == XFAILURE) {
    ret = -EIO;
  } else {
    // get reply, head line
    if (xsocket_read_line(sock, rep_text) == XFAILURE) {
      ret = -EIO;
    } else if (xstr_startwith_cstr(rep_text, "ok")) {
      // a hash table of remote file listing, it will be merged with the fs_cache entry
      xhash remote_listing = xhash_new(xhash_hash_xstr, xhash_eql_xstr, remote_listing_delete_handle);
      int count = 0, i;

      // get 1st line, "count=%d"
      xsocket_read_line(sock, rep_text);
      assert(xstr_startwith_cstr(rep_text, "count="));
      count = atoi(xstr_get_cstr(rep_text) + 6);
      for (i = 0; i < count; i++) {
        fs_cache remote_node = fs_cache_new_raw();
        xstr remote_node_key = NULL;

        // get (2k)-th line, "name=%s"
        xsocket_read_line(sock, rep_text);
        assert(xstr_startwith_cstr(rep_text, "name="));
        remote_node_key = xstr_substr(rep_text, 5);

        // get (2k+1)-th line, "type=%d,....."
        xsocket_read_line(sock, rep_text);
        parse_fs_cache_value(remote_node, xstr_get_cstr(rep_text));

        xhash_put(remote_listing, remote_node_key, remote_node);
      }

      // merge remote listing with local listing
      remote_listing_merge(entry, remote_listing);
      xhash_delete(remote_listing);
    } else {
      // error occurred
      ret = try_parse_errno_from_reply(rep_text);
    }
  }
  pthread_mutex_unlock(&(inst->imgdir_sock_lock));
  xstr_delete(rep_text);
  xstr_delete(req_head);
  return ret;
}

int protocol_request_mkdir(imgmount_instance inst, fs_cache parent_dir, const char* fullpath, const char* child_name) {
  int ret = 0;
  xstr req_head = xstr_new();
  xstr rep_text = xstr_new();
  xsocket sock = inst->imgdir_sock;

  pthread_mutex_lock(&(inst->imgdir_sock_lock));
  // this function assumes the request is probably correct (ie, the dir does not exist, and the path is correct)
  // so it does not check them
  xstr_printf(req_head, "mkdir %s\r\n", fullpath);
  if (xsocket_write_line(sock, xstr_get_cstr(req_head)) == XFAILURE) {
    ret = -EIO;
  } else {
    if (xsocket_read_line(sock, rep_text) == XFAILURE) {
      ret = -EIO;
    } else if (xstr_startwith_cstr(rep_text, "ok")) {
      // done! create corresponding fs_cache entry
      fs_cache child = fs_cache_new_dir(child_name, parent_dir);

      // receive another line of attr
      if (xsocket_read_line(sock, rep_text) == XFAILURE) {
        ret = -EIO;
      } else {
        // parse the attr, and set it to the new node
        parse_fs_cache_value(child, xstr_get_cstr(rep_text));
      }
    } else {
      // error occurred
      ret = try_parse_errno_from_reply(rep_text);
    }
  }
  pthread_mutex_unlock(&(inst->imgdir_sock_lock));
  xstr_delete(rep_text);
  xstr_delete(req_head);
  return ret;
}


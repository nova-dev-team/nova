#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <assert.h>
#include <pthread.h>

// we are using sqlite to store filesystem layout
#include "3rdparty/sqlite3/sqlite3.h"
#include "xmemory.h"
#include "xlog.h"
#include "xstr.h"
#include "xhash.h"
#include "xutils.h"
#include "imgdir_fsdb.h"


/**
  @brief
    Implementation of the filesystem database model.
*/
struct fsdb_impl {
  sqlite3* conn;  ///< @brief Connection to sqlite3 database.
  pthread_mutex_t conn_mutex;  ///< @brief Mutex to protect the sqlite3 connection.
};

static void fsdb_die(fsdb db, char* error_msg) {
  xlog_fatal("[sqlite3] %s\n", error_msg);
  sqlite3_free(error_msg);
  fsdb_close(db);
  // just die
  abort();
}

static void exec_sql_or_die(fsdb db, const char* query) {
  char* error_msg = NULL;
  int ret;
  xlog_info("[sqlite] %s\n", query);
  ret = sqlite3_exec(db->conn, query, NULL, NULL, &error_msg);
  if (ret != SQLITE_OK) {
    fsdb_die(db, error_msg);
  }
}

static int check_if_has_fs_nodes_table(void* arg, int n_cols, char* val[], char* col[]) {
  int i;
  xbool *p_has_fs_nodes_table = (xbool *) arg;
  for (i = 0; i < n_cols; i++) {
    if (strcmp(col[i], "name") == 0 && strcmp(val[i], "fs_nodes") == 0) {
      *p_has_fs_nodes_table = XTRUE;
      break;
    }
  }
  return 0;
}

static void insert_dummy_data_real(fsdb db, int parent_id, int depth, int* id_counter) {
  xstr query = xstr_new();
  int i;

  // insert 2 folders
  for (i = 0; i < 2; i++) {
    int folder_id = *id_counter;
    (*id_counter)++;
    xstr_set_cstr(query, "");
    xstr_printf(query,
      "insert into fs_nodes(id, parent_id, name, perm, type, size, mtime) "
      "values (%d, %d, 'folder_%d', %d, %d, %d, %d)",
      folder_id, parent_id, folder_id, 0755, 0, 0, (int) time(NULL)
    );
    exec_sql_or_die(db, xstr_get_cstr(query));
    if (depth < 2) {
      // at most 2 levels of subdir
      insert_dummy_data_real(db, folder_id, depth + 1, id_counter);
    }
  }

  // insert 5 files
  for (i = 0; i < 5; i++) {
    int file_id = *id_counter;
    (*id_counter)++;
    xstr_set_cstr(query, "");
    xstr_printf(query,
      "insert into fs_nodes(id, parent_id, name, perm, type, size, mtime) "
      "values (%d, %d, 'file_%d', %d, %d, %d, %d)",
      file_id, parent_id, file_id, 0644, 1, 0, (int) time(NULL)
    );
    exec_sql_or_die(db, xstr_get_cstr(query));
  }
  xstr_delete(query);
}

static void insert_dummy_data(fsdb db) {
  int id_counter = 2;
  insert_dummy_data_real(db, FS_CACHE_ROOT_UID, 0, &id_counter);
}

static void insert_initial_data_into_fs_nodes_table(fsdb db) {
  xstr query = xstr_new();
  // insert the root node
  // for root node, its parent is it self
  xstr_printf(query,
    "insert into fs_nodes(id, parent_id, name, perm, type, size, mtime) "
    "values (%d, %d, '%s', %d, %d, %d, %d)",
    FS_CACHE_ROOT_UID, FS_CACHE_ROOT_UID, "/", 0755, 0, 0, (int) time(NULL)
  );
  exec_sql_or_die(db, xstr_get_cstr(query));

  // insert dummy debug data into database
  insert_dummy_data(db);
  xstr_delete(query);
}


// helper function that cleans up all_nodes hash table in fs_cache_load_tree()
static void fs_cache_all_nodes_hash_delete_handler(void* key, void* value) {
  xfree((int *) key);
}

// directly copied from imgmount_fs_cache.c
static void fs_cache_delete_child(void* key, void* value) {
  xstr_delete((xstr) key);
  fs_cache_delete((fs_cache) value);
}

static void parse_fs_node_from_sql_result(fs_cache node, int n_cols, char* val[], char* col[]) {
  int i;
  for (i = 0; i < n_cols; i++) {
    if (strcmp(col[i], "id") == 0) {
      node->uid = atoi(val[i]);
      if (node->uid == FS_CACHE_ROOT_UID) {
        node->parent = node;
      }
    } else if (strcmp(col[i], "parent_id") == 0) {
      node->parent_uid = atoi(val[i]);
    } else if (strcmp(col[i], "name") == 0) {
      xstr_set_cstr(node->name, val[i]);
    } else if (strcmp(col[i], "perm") == 0) {
      node->perm = atoi(val[i]);
    } else if (strcmp(col[i], "type") == 0) {
      switch (atoi(val[i])) {
      case 0:
        node->type = FS_CACHE_DIR;
        node->child = xhash_new(xhash_hash_xstr, xhash_eql_xstr, fs_cache_delete_child);
        break;
      case 1:
        node->type = FS_CACHE_FILE;
        break;
      default:
        // should never reach here!
        assert(0);
      }
    } else if (strcmp(col[i], "size") == 0) {
      node->size = atol(val[i]);
    } else if (strcmp(col[i], "mtime") == 0) {
      node->mtime = atoi(val[i]);
    }
  }
}

// helper function to fill in node data
static int fill_in_all_nodes_hash(void* arg, int n_cols, char* val[], char* col[]) {
  int* node_id = xmalloc_ty(1, int);
  xhash all_nodes = (xhash) arg;
  fs_cache node = fs_cache_new_raw();
  parse_fs_node_from_sql_result(node, n_cols, val, col);
  *node_id = node->uid;

  // print debug info
  xlog_debug("[debug] id=%d, parent_id=%d, name='%s', perm=%d, type=%d, size=%lld, mtime=%d\n",
    node->uid, node->parent_uid, xstr_get_cstr(node->name), node->perm, node->type, (long long) node->size, node->mtime
  );
  xhash_put(all_nodes, node_id, node);
  return 0;
}

// a helper function to register a raw_child to parent
static void register_parent_child(fs_cache parent, fs_cache raw_child) {
  xstr child_key;
  assert(parent != NULL && raw_child != NULL);
  raw_child->parent_uid = parent->uid;
  raw_child->parent = parent;
  child_key = xstr_copy(raw_child->name);
  xhash_put(raw_child->parent->child, child_key, raw_child);
}

// a helper function that setup node relationships
static xbool setup_all_nodes_parent_child(void* key, void* value, void* args) {
  xhash all_nodes = (xhash) args;
  fs_cache child_node = (fs_cache) value;
  if (child_node->parent == NULL && child_node->uid != FS_CACHE_ROOT_UID) {
    fs_cache parent = xhash_get(all_nodes, &(child_node->parent_uid));
    register_parent_child(parent, child_node);
  } else {
    assert(child_node->uid == FS_CACHE_ROOT_UID);
  }
  return XTRUE;
}

/**
  @brief
    A helper function that loads the fs_cache node tree into memory.

  @param db
    The handle of filesystem database.

  @return
    NULL if failed to load root node. Otherwise the root node will be retuned.
*/
static fs_cache fs_cache_load_tree(XIN fsdb db) {
  fs_cache root_node = NULL;
  xhash all_nodes = xhash_new(xhash_hash_int, xhash_eql_int, fs_cache_all_nodes_hash_delete_handler);
  const char* query = "select * from fs_nodes";
  int sql_ret, t;
  char* error_msg;

  xlog_info("[sqlite] %s\n", query);
  sql_ret = sqlite3_exec(db->conn, query, fill_in_all_nodes_hash, all_nodes, &error_msg);
  if (sql_ret != SQLITE_OK) {
    fsdb_die(db, error_msg);
  }
  t = 1;
  root_node = xhash_get(all_nodes, &t);
  if (root_node != NULL) {
    // has found root node!
    // now, setup parent/child relation ship
    xhash_visit(all_nodes, setup_all_nodes_parent_child, all_nodes);
    assert(xhash_size(all_nodes) == fs_cache_tree_count(root_node));
  } else {
    xlog_fatal("[fsdb] failed to find root node!");
  }

  xhash_delete(all_nodes);
  return root_node;
}

fs_cache fsdb_load_root(XIN const char* db_fn, XOUT fsdb* p_db) {
  fs_cache root = NULL;
  *p_db = xmalloc_ty(1, struct fsdb_impl);
  pthread_mutex_init(&((*p_db)->conn_mutex), NULL);
  if (sqlite3_open(db_fn, &((*p_db)->conn)) == SQLITE_OK) {
    int ret;
    char* error_msg;
    xbool has_fs_nodes_table = XFALSE;

    ret = sqlite3_exec((*p_db)->conn, "select * from sqlite_master where type='table'",
      check_if_has_fs_nodes_table, &has_fs_nodes_table, &error_msg);

    if (ret != SQLITE_OK) {
      fsdb_die(*p_db, error_msg);
    }

    exec_sql_or_die(*p_db,
    "create table if not exists fs_nodes("
      "id integer primary key autoincrement not null,"
      "parent_id integer not null,"
      "name varchar(255) not null,"
      "perm integer not null,"
      "type integer not null,"
      "size integer not null,"
      "mtime integer not null"
    ")");

    if (has_fs_nodes_table == XFALSE) {
      // insert initial data
      insert_initial_data_into_fs_nodes_table(*p_db);
    }

    // load real root node
    root = fs_cache_load_tree(*p_db);
  } else {
    // failed to open sqlite3 db
    fsdb_close(*p_db);
    p_db = NULL;
  }
  return root;
}


static int load_fs_cache_data_from_db_helper(void* arg, int n_cols, char* val[], char* col[]) {
  fs_cache child = (fs_cache) arg;
  parse_fs_node_from_sql_result(child, n_cols, val, col);
  return 0;
}

// a helper function that determines uid of a certain sub fs_cache node
// when calling this function, assume db->conn_mutex is hold
static xsuccess load_fs_cache_data_from_db(XIN fsdb db, XIN fs_cache parent, XIN const char* sub_name, XIN fs_cache child) {
  xsuccess ret = XSUCCESS;
  char* error_msg = NULL;
  xstr query = xstr_new();
  // TODO escape sub_name!
  xstr_printf(query, "select * from fs_nodes where parent_id = %d and name = '%s'", parent->uid, sub_name);
  xlog_info("[sqlite] %s\n", xstr_get_cstr(query));
  ret = sqlite3_exec(db->conn, xstr_get_cstr(query), load_fs_cache_data_from_db_helper, child, &error_msg);
  if (ret != SQLITE_OK) {
    fsdb_die(db, error_msg);
  }
  xstr_delete(query);
  return ret;
}

int fsdb_mknode(XIN fsdb db, XIN fs_cache parent, XIN const char* name, XIN fs_cache_type type, XOUT fs_cache *p_node) {
  int errcode = 0;
  *p_node = fs_cache_new_raw();
  xstr query = xstr_new();
  pthread_mutex_lock(&(db->conn_mutex));

  // print debug info
  xlog_debug("[debug] parent: id=%d, parent_id=%d, name='%s', perm=%d, type=%d, size=%lld, mtime=%d\n",
    parent->uid, parent->parent_uid, xstr_get_cstr(parent->name), parent->perm, parent->type, (long long) parent->size, parent->mtime
  );

  // first, insert new node into database
  // TODO use pre-parsed sqlite query structure to speed up insert process
  xstr_printf(query,
    "insert into fs_nodes(parent_id, name, perm, type, size, mtime) "
    "values (%d, '%s', %d, %d, %d, %d)",
    parent->uid, name, 0755, 0, 0, (int) time(NULL)
  );
  exec_sql_or_die(db, xstr_get_cstr(query));

  // then, determine sqlite provided auto increase uid
  load_fs_cache_data_from_db(db, parent, name, *p_node);

  // finally, setup relationship
  register_parent_child(parent, *p_node);

  pthread_mutex_unlock(&(db->conn_mutex));
  xstr_delete(query);
  return errcode;
}


int fsdb_rename(XIN fsdb db, XIN fs_cache node, XIN fs_cache new_parent, XIN const char* new_name) {
  // TODO
  return 0;
}

int fsdb_unlink(XIN fsdb db, XIN fs_cache node) {
  // TODO
  return 0;
}


xsuccess fsdb_close(XIN fsdb db) {
  xsuccess ret = XSUCCESS;
  if (db != NULL) {
    pthread_mutex_destroy(&(db->conn_mutex));
    if (sqlite3_close(db->conn) != SQLITE_OK) {
      ret = XFAILURE;
    }
    xfree(db);
  }
  return ret;
}

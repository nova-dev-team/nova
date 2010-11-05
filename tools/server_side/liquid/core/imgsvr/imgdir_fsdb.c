// we are using sqlite to store filesystem layout
#include "3rdparty/sqlite3/sqlite3.h"

#include "imgdir_fsdb.h"

/**
  @brief
    Implementation of the filesystem database model.
*/
struct fsdb_impl {
  sqlite3* conn;  ///< @brief Connection to sqlite3 database
};

fs_cache fsdb_load_root(XIN const char* db_fn, XOUT fsdb* p_db) {
  *p_db = NULL;
  // TODO
  return NULL;
}


int fsdb_mknode(XIN fsdb db, XIN fs_cache parent, XIN const char* name, XIN fs_cache_type type, XOUT fs_cache *p_node) {
  // TODO
  *p_node = NULL;
  return 0;
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
    if (sqlite3_close(db->conn) != SQLITE_OK) {
      ret = XFAILURE;
    }
  }
  return ret;
}

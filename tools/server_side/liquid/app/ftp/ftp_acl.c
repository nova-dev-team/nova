#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <pthread.h>
#include <sys/time.h>
#include <time.h>

#include "3rdparty/sqlite3.h"

#include "xmemory.h"
#include "xlog.h"
#include "xutils.h"
#include "xvec.h"
#include "xhash.h"
#include "ftp_acl.h"

typedef struct {
  xstr ftp_path;
  xbool read;
  xbool write;
  xbool del;
} ftp_rule;

typedef struct {
  xstr name;
  xvec rules; // a vector of pointers to ftp_rules, those pointers are not managed
} ftp_group;

typedef struct {
  xstr name;
  xstr passwd;
  xstr root_jail;
  ftp_group* group; // not managed
  xvec rules; // a vector of pointers to ftp_rules, those pointers are not managed
} ftp_user;

static int single_user_mode = -1;
static char* single_user_mode_user = NULL;
static char* single_user_mode_pwd = NULL;
static char* single_user_mode_root_jail = NULL;
static int single_user_mode_readonly = -1;

static int multi_user_mode = -1;
static pthread_mutex_t cache_mutex = PTHREAD_MUTEX_INITIALIZER;
static sqlite3* conn;
static xhash users_map = NULL;  // user data is managed by this xhash
static xhash users_map_by_id = NULL;  // just pointers to users_map according to id
static xhash groups_map = NULL; // id -> group map
static xvec ftp_rules_vec = NULL;

xsuccess ftp_acl_single_user_mode(const char* user, const char* pwd, const char* root_jail, const xbool readonly) {
  assert(multi_user_mode == -1);
  single_user_mode = 1;
  single_user_mode_user = strdup(user);
  single_user_mode_pwd = strdup(pwd);
  single_user_mode_root_jail = strdup(root_jail);
  single_user_mode_readonly = readonly;
  return XSUCCESS;
}

// caller should have locked cache_mutex
static void destroy_cached_data() {
  if (ftp_rules_vec != NULL) {
    xvec_delete(ftp_rules_vec);
    ftp_rules_vec = NULL;
  }
  if (users_map != NULL) {
    xhash_delete(users_map);
    users_map = NULL;
  }
  if (users_map_by_id != NULL) {
    xhash_delete(users_map_by_id);
    users_map_by_id = NULL;
  }
  if (groups_map != NULL) {
    xhash_delete(groups_map);
    groups_map = NULL;
  }
}

static void xvec_free_do_nothing(void* ptr) {
  // don't do any thing to the xvec
  // used by ftp_user and ftp_group's rules
}

static ftp_user* ftp_user_new() {
  ftp_user* u = xmalloc_ty(1, ftp_user);
  u->name = xstr_new();
  u->passwd = xstr_new();
  u->root_jail = xstr_new();
  u->group = NULL;    // not managed
  u->rules = xvec_new(xvec_free_do_nothing);
  return u;
}

static void ftp_user_delete(ftp_user* u) {
  xstr_delete(u->name);
  xstr_delete(u->passwd);
  xstr_delete(u->root_jail);
  xvec_delete(u->rules);
  xfree(u);
}

static ftp_group* ftp_group_new() {
  ftp_group* g = xmalloc_ty(1, ftp_group);
  g->name = xstr_new();
  g->rules = xvec_new(xvec_free_do_nothing);
  return g;
}

static void ftp_group_delete(ftp_group* g) {
  xstr_delete(g->name);
  xvec_delete(g->rules);
  xfree(g);
}

static ftp_rule* ftp_rule_new() {
  ftp_rule* rule = xmalloc_ty(1, ftp_rule);
  rule->ftp_path = xstr_new();
  rule->read = XFALSE;
  rule->write = XFALSE;
  rule->del = XFALSE;
  return rule;
}

static void ftp_rule_delete(ftp_rule* rule) {
  xstr_delete(rule->ftp_path);
  xfree(rule);
}

static xbool is_valid_rule(ftp_rule* rule) {
  xbool ret = XTRUE;
  if (xstr_startwith_cstr(rule->ftp_path, "/") == XFALSE) {
    ret = XFALSE;
  } else {
    xstr path_cpy = xstr_copy(rule->ftp_path);
    xfilesystem_normalize_abs_path(xstr_get_cstr(path_cpy), rule->ftp_path);
    xstr_delete(path_cpy);
  }
  if (ret == XFALSE) {
    xlog_warning("[ftp acl] path \"%s\" is not valid, ignore it\n", xstr_get_cstr(rule->ftp_path));
  }
  return ret;
}

static void ftp_rules_vec_element_free_handler(void* raw_ptr) {
  ftp_rule* rule = (ftp_rule *) raw_ptr;
  ftp_rule_delete(rule);
}

static void users_map_free(void* key, void* value) {
  xstr xs = (xstr) key;
  ftp_user* user = (ftp_user *) value;
  ftp_user_delete(user);
  xstr_delete(xs);
}

static int load_groups_into_cache(void* arg, int n_cols, char* val[], char* col[]) {
  // cols: id, name
  ftp_group* g = ftp_group_new();
  int* group_id = xmalloc_ty(1, int);
  int i;

  for (i = 0; i < n_cols; i++) {
    if (strcmp(col[i], "id") == 0) {
      *group_id = atoi(val[i]);
    } else if (strcmp(col[i], "name") == 0) {
      xstr_set_cstr(g->name, val[i]);
    }
  }

  xhash_put(groups_map, group_id, g);
  return 0;
}

static int load_users_into_cache(void* arg, int n_cols, char* val[], char* col[]) {
  // cols: id, name, passwd, root_jail, group_id
  ftp_user* u = ftp_user_new();
  xstr key_xs = xstr_new();
  ftp_group* grp = NULL;
  int i;
  int* user_id = xmalloc_ty(1, int);
  int* group_id = xmalloc_ty(1, int);

  for (i = 0; i < n_cols; i++) {
    if (strcmp(col[i], "id") == 0) {
      *user_id = atoi(val[i]);
    } else if (strcmp(col[i], "name") == 0) {
      xstr_set_cstr(u->name, val[i]);
      xstr_set_cstr(key_xs, val[i]);
    } else if (strcmp(col[i], "passwd") == 0) {
      xstr_set_cstr(u->passwd, val[i]);
    } else if (strcmp(col[i], "root_jail") == 0) {
      xstr_set_cstr(u->root_jail, val[i]);
    } else if (strcmp(col[i], "group_id") == 0) {
      *group_id = atoi(val[i]);
    }
  }

  // set group
  grp = xhash_get(groups_map, group_id);
  if (grp != NULL) {
    u->group = grp;
  }
  xfree(group_id);  // free this after the group is found

  xhash_put(users_map, key_xs, u);
  // also add a pointer into users_map_by_id
  xhash_put(users_map_by_id, user_id, u);

  return 0;
}

static int load_user_rules_into_cache(void* arg, int n_cols, char* val[], char* col[]) {
  // cols: user_id, path, readable, writable, deletable
  ftp_rule* rule = ftp_rule_new();
  int* user_id = xmalloc_ty(1, int);
  int i;
  ftp_user* usr;
  for (i = 0; i < n_cols; i++) {
    if (strcmp(col[i], "user_id") == 0) {
      *user_id = atoi(val[i]);
    } else if (strcmp(col[i], "path") == 0) {
      xstr_set_cstr(rule->ftp_path, val[i]);
    } else if (strcmp(col[i], "readable") == 0) {
      if (val[i][0] == 't' || val[i][0] == 'T' || val[i][0] == '1') {
        rule->read = XTRUE;
      } else {
        rule->read = XFALSE;
      }
    } else if (strcmp(col[i], "writable") == 0) {
      if (val[i][0] == 't' || val[i][0] == 'T' || val[i][0] == '1') {
        rule->write = XTRUE;
      } else {
        rule->write = XFALSE;
      }
    } else if (strcmp(col[i], "deletable") == 0) {
      if (val[i][0] == 't' || val[i][0] == 'T' || val[i][0] == '1') {
        rule->del = XTRUE;
      } else {
        rule->del = XFALSE;
      }
    }
  }
  if (is_valid_rule(rule) == XTRUE) {
    xvec_push_back(ftp_rules_vec, rule);

    // update possible user info
    usr = xhash_get(users_map_by_id, user_id);
    if (usr != NULL) {
      xvec_push_back(usr->rules, rule);
    }
  } else {
    ftp_rule_delete(rule);
  }
  xfree(user_id);
  return 0;
}

static int load_group_rules_into_cache(void* arg, int n_cols, char* val[], char* col[]) {
  // cols: group_id, path, readable, writable, deletable
  ftp_rule* rule = ftp_rule_new();
  int* group_id = xmalloc_ty(1, int);
  int i;
  ftp_group* grp;
  for (i = 0; i < n_cols; i++) {
    if (strcmp(col[i], "group_id") == 0) {
      *group_id = atoi(val[i]);
    } else if (strcmp(col[i], "path") == 0) {
      xstr_set_cstr(rule->ftp_path, val[i]);
    } else if (strcmp(col[i], "readable") == 0) {
      if (val[i][0] == 't' || val[i][0] == 'T' || val[i][0] == '1') {
        rule->read = XTRUE;
      } else {
        rule->read = XFALSE;
      }
    } else if (strcmp(col[i], "writable") == 0) {
      if (val[i][0] == 't' || val[i][0] == 'T' || val[i][0] == '1') {
        rule->write = XTRUE;
      } else {
        rule->write = XFALSE;
      }
    } else if (strcmp(col[i], "deletable") == 0) {
      if (val[i][0] == 't' || val[i][0] == 'T' || val[i][0] == '1') {
        rule->del = XTRUE;
      } else {
        rule->del = XFALSE;
      }
    }
  }
  if (is_valid_rule(rule)) {
    xvec_push_back(ftp_rules_vec, rule);

    // update possible group info
    grp = xhash_get(groups_map, group_id);
    if (grp != NULL) {
      xvec_push_back(grp->rules, rule);
    }
  } else {
    ftp_rule_delete(rule);
  }
  xfree(group_id);
  return 0;
}

static void users_map_by_id_free(void* key, void* value) {
  int* int_key = (int *) key;
  xfree(int_key);
  // do nothing to the value, it is managed by users_map
}

static void groups_map_free(void* key, void* value) {
  int* int_key = (int *) key;
  ftp_group* g = (ftp_group *) value;
  xfree(int_key);
  ftp_group_delete(g);
}

static void sqlite_die(char* error_msg) {
  xlog_fatal("[sqlite3] %s\n", error_msg);
  sqlite3_free(error_msg);
  sqlite3_close(conn);
  exit(1);
}

static int ftp_rules_sort_handler(void* obj1, void* obj2) {
  ftp_rule* r1 = (ftp_rule *) obj1;
  ftp_rule* r2 = (ftp_rule *) obj2;
  // sort in reverse order
  return xstr_compare(r2->ftp_path, r1->ftp_path);
}

static xbool visit_and_sort_user_rules(void* key, void* value, void* args) {
  ftp_user* u = (ftp_user *) value;
  xvec_sort(u->rules, ftp_rules_sort_handler);
  return XTRUE;
}

static xbool visit_and_sort_group_rules(void* key, void* value, void* args) {
  ftp_group* g = (ftp_group *) value;
  xvec_sort(g->rules, ftp_rules_sort_handler);
  return XTRUE;
}

// caller should have locked cache_mutex
static void reload_db_data() {
  int ret;
  char* error_msg = NULL;
  if (ftp_rules_vec == NULL) {
    ftp_rules_vec = xvec_new(ftp_rules_vec_element_free_handler);
  }
  if (users_map == NULL) {
    users_map = xhash_new(
      xhash_hash_xstr,
      xhash_eql_xstr,
      users_map_free
    );
  }
  if (users_map_by_id == NULL) {
    users_map_by_id = xhash_new(
      xhash_hash_int,
      xhash_eql_int,
      users_map_by_id_free
    );
  }
  if (groups_map == NULL) {
    groups_map = xhash_new(
      xhash_hash_int,
      xhash_eql_int,
      groups_map_free
    );
  }

  // load order: group, user, rules
  // load into group
  ret = sqlite3_exec(conn, "select id, name from groups", load_groups_into_cache,
      NULL, &error_msg);
  if (ret != SQLITE_OK) {
    sqlite_die(error_msg);
  }
  xlog_info("[ftp db] %d groups in database\n", xhash_size(groups_map));

  // load into user
  ret = sqlite3_exec(conn, "select id, name, passwd, root_jail, group_id from users",
    load_users_into_cache, NULL, &error_msg);
  if (ret != SQLITE_OK) {
    sqlite_die(error_msg);
  }
  xlog_info("[ftp db] %d users in database\n", xhash_size(users_map));

  // load into rules, and for each user/group, sort them in order
  ret = sqlite3_exec(conn, "select group_id, path, readable, writable, deletable from group_rules",
    load_group_rules_into_cache, NULL, &error_msg);
  if (ret != SQLITE_OK) {
    sqlite_die(error_msg);
  }

  ret = sqlite3_exec(conn, "select user_id, path, readable, writable, deletable from user_rules",
    load_user_rules_into_cache, NULL, &error_msg);
  if (ret != SQLITE_OK) {
    sqlite_die(error_msg);
  }

  xlog_info("[ftp db] %d rules in database\n", xvec_size(ftp_rules_vec));

  // sort the user/group rules
  xhash_visit(users_map, visit_and_sort_user_rules, NULL);
  xhash_visit(groups_map, visit_and_sort_group_rules, NULL);
}

// try to update data from database
// this function won't query database unless an interval has passed
static void try_update_data() {
  static int is_first_call = 1;
  static struct timeval last_call_time;
  struct timeval time_now;

  // 5 seconds interval between calls
  const int call_interval = 5;

  if (is_first_call != 1) {
    gettimeofday(&time_now, NULL);
    if (time_now.tv_sec < last_call_time.tv_sec + call_interval) {
      return;
    }
  }

  // we lock very early, in order to protect last_call_time
  pthread_mutex_lock(&cache_mutex);

  gettimeofday(&last_call_time, NULL);
  is_first_call = 0;

  // start do real job
  destroy_cached_data();
  reload_db_data();

  pthread_mutex_unlock(&cache_mutex);
}

static void exec_sql_or_die(char* query) {
  char* error_msg = NULL;
  int ret;
  xlog_info("[sqlite] %s\n", query);
  ret = sqlite3_exec(conn, query, NULL, NULL, &error_msg);
  if (ret != SQLITE_OK) {
    sqlite_die(error_msg);
  }
}

xsuccess ftp_acl_multi_user_mode(const char* db_fname) {
  xsuccess ret = XFAILURE;
  multi_user_mode = 1;
  if (sqlite3_open(db_fname, &conn) == SQLITE_OK) {
    ret = XSUCCESS;
  }
  exec_sql_or_die(
  "create table if not exists users("
    "id integer primary key autoincrement not null,"
    "name varchar(255) not null,"
    "passwd varchar(255) not null,"
    "root_jail varchar(255) not null,"
    "group_id integer default -1"
  ")");
  exec_sql_or_die(
  "create table if not exists user_rules("
    "user_id integer not null,"
    "path varchar(255) not null,"
    "readable boolean,"
    "writable boolean not null,"
    "deletable boolean not null"
  ")");
  exec_sql_or_die(
  "create table if not exists groups("
    "id integer primary key autoincrement not null,"
    "name varchar(255) not null"
  ")");
  exec_sql_or_die(
  "create table if not exists group_rules("
    "group_id integer not null,"
    "path varchar(255) not null,"
    "readable boolean,"
    "writable boolean not null,"
    "deletable boolean not null"
  ")");
  // load data for the first time
  try_update_data();
  return ret;
}

xsuccess ftp_auth_user(const char* user, const char* pwd) {
  xsuccess ret = XFAILURE;
  if (single_user_mode == 1) {
    if (strcmp(single_user_mode_user, "") == 0) {
      // user name can be any thing
      if (strcmp(single_user_mode_pwd, "") == 0 || strcmp(single_user_mode_pwd, pwd) == 0) {
        // also considered the case where pwd can be any thing
        ret = XSUCCESS;
      }
    } else {
      // user is determined, only one user
      if (strcmp(user, single_user_mode_user) == 0 &&
          (strcmp(pwd, single_user_mode_pwd) == 0 || strcmp(single_user_mode_pwd, "") == 0)) {
        // also considered the case where user password can be anything
        ret = XSUCCESS;
      }
    }
  } else {
    assert(multi_user_mode == 1);
    try_update_data();

    pthread_mutex_lock(&cache_mutex);
    xstr key = xstr_new();
    xstr_set_cstr(key, user);
    ftp_user* u = xhash_get(users_map, key);
    if (u == NULL) {
      xlog_info("[ftp.auth] user %s not found!\n", user);
    } else {
      // if passwd not required, or passwd matches, return XSUCCESS
      if (strcmp(xstr_get_cstr(u->passwd), "") == 0
          || strcmp(xstr_get_cstr(u->passwd), pwd) == 0) {
        ret = XSUCCESS;
      }
    }
    xstr_delete(key);
    if (ret == XSUCCESS) {
      int i;
      // for debug
      if (u->group != NULL) {
        xlog_debug("[ftp.auth] user %s is in group %s\n", user, xstr_get_cstr(u->group->name));
      }
      xlog_debug("[ftp.auth] user %s has %d rules\n", user, xvec_size(u->rules));
      for (i = 0; i < xvec_size(u->rules); i++) {
        ftp_rule* rule = (ftp_rule *) xvec_get(u->rules, i);
        xlog_debug("[ftp.auth] user %s: %s, R=%d, W=%d, D=%d\n", user, xstr_get_cstr(rule->ftp_path),
          rule->read == XTRUE, rule->write == XTRUE, rule->del == XTRUE
        );
      }
      if (u->group != NULL) {
        ftp_group* g = u->group;
        xlog_debug("[ftp.auth] group %s has %d rules\n", xstr_get_cstr(g->name), xvec_size(g->rules));
        for (i = 0; i < xvec_size(g->rules); i++) {
          ftp_rule* rule = (ftp_rule *) xvec_get(g->rules, i);
          xlog_debug("[ftp.auth] group %s: %s, R=%d, W=%d, D=%d\n", xstr_get_cstr(g->name), xstr_get_cstr(rule->ftp_path),
            rule->read == XTRUE, rule->write == XTRUE, rule->del == XTRUE
          );
        }
      }
    }
    pthread_mutex_unlock(&cache_mutex);
  }
  return ret;
}

xsuccess ftp_get_root_jail(const char* user, xstr root_jail) {
  xsuccess ret = XFAILURE;
  if (single_user_mode == 1) {
    // when single_user_mode_user is "", it indicates every user
    if (strcmp(single_user_mode_user, "") == 0 || strcmp(user, single_user_mode_user) == 0) {
      ret = XSUCCESS;
      xstr_set_cstr(root_jail, single_user_mode_root_jail);
    }
  } else {
    assert(multi_user_mode == 1);
    try_update_data();

    pthread_mutex_lock(&cache_mutex);
    xstr key = xstr_new();
    xstr_set_cstr(key, user);
    ftp_user* u = xhash_get(users_map, key);
    if (u == NULL) {
      xlog_info("[ftp.auth] user %s not found!\n", user);
    } else {
      ret = XSUCCESS;
      xstr_set_cstr(root_jail, xstr_get_cstr(u->root_jail));
    }
    xstr_delete(key);
    pthread_mutex_unlock(&cache_mutex);
  }
  return ret;
}

xsuccess static do_match_acl(ftp_rule* rule, const char* path_cstr, int* priv_flag) {
  xsuccess ret = XFAILURE;
  if (xcstr_startwith_cstr(path_cstr, xstr_get_cstr(rule->ftp_path)) == XTRUE) {
    xlog_debug("[ftp acl] matched acl: %s -> %s\n", path_cstr, xstr_get_cstr(rule->ftp_path));
    *priv_flag = 0;
    if (rule->read == XTRUE) {
      *priv_flag |= FTP_ACL_READ_FLAG;
    }
    if (rule->write == XTRUE) {
      *priv_flag |= FTP_ACL_WRITE_FLAG;
    }
    if (rule->del == XTRUE) {
      *priv_flag |= FTP_ACL_DEL_FLAG;
    }
    ret = XSUCCESS;
  }
  return ret;
}

xsuccess ftp_path_privilege(const char* user, const char* path, int* priv_flag) {
  xsuccess ret = XFAILURE;
  if (single_user_mode == 1) {
    ret = XSUCCESS;
    if (single_user_mode_readonly == XTRUE) {
      *priv_flag = FTP_ACL_READ_FLAG;
    } else {
      *priv_flag = FTP_ACL_READ_FLAG | FTP_ACL_WRITE_FLAG | FTP_ACL_DEL_FLAG;
    }
  } else {
    xbool matched = XFALSE;
    ftp_user* u;
    xstr user_xs = xstr_new();
    xstr_set_cstr(user_xs, user);
    assert(multi_user_mode == 1);
    try_update_data();

    pthread_mutex_lock(&cache_mutex);
    // match ACL in databse
    u = xhash_get(users_map, user_xs);
    if (u != NULL) {
      // first match the user's rules
      int i;
      for (i = 0; i < xvec_size(u->rules); i++) {
        ftp_rule* rule = xvec_get(u->rules, i);
        if (do_match_acl(rule, path, priv_flag) == XSUCCESS) {
          matched = XTRUE;
          break;
        }
      }
      // if user's rules not matched, go on with group's rules
      if (matched == XFALSE && u->group != NULL) {
        ftp_group* g = u->group;
        for (i = 0; i < xvec_size(g->rules); i++) {
          ftp_rule* rule = xvec_get(g->rules, i);
          if (do_match_acl(rule, path, priv_flag) == XSUCCESS) {
            matched = XTRUE;
            break;
          }
        }
      }
    }
    pthread_mutex_unlock(&cache_mutex);
    xstr_delete(user_xs);

    if (matched == XFALSE) {
      *priv_flag = FTP_ACL_READ_FLAG | FTP_ACL_WRITE_FLAG | FTP_ACL_DEL_FLAG;
    }
    ret = XSUCCESS;
  }
  xlog_info("[ftp acl] (user=%s) privilege of %s, R=%d, W=%d, D=%d\n",
    user, path, FTP_ACL_CAN_READ(*priv_flag) != 0,
    FTP_ACL_CAN_WRITE(*priv_flag) != 0, FTP_ACL_CAN_DEL(*priv_flag) != 0
  );
  return ret;
}

xsuccess ftp_acl_finalize() {
  xsuccess ret = XSUCCESS;
  if (single_user_mode == 1) {
    free(single_user_mode_user);
    free(single_user_mode_pwd);
    free(single_user_mode_root_jail);
  } else {
    pthread_mutex_lock(&cache_mutex);
    assert(multi_user_mode == 1);
    if (sqlite3_close(conn) != SQLITE_OK) {
      ret = XFAILURE;
    }
    destroy_cached_data();
    pthread_mutex_unlock(&cache_mutex);
  }
  return ret;
}


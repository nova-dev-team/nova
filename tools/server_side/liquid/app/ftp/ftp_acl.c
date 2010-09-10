#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "3rdparty/sqlite3.h"

#include "xlog.h"
#include "xvec.h"
#include "ftp_acl.h"

typedef struct {
  xstr ftp_path;
  xbool read;
  xbool write;
  xbool del;
} ftp_rules;

typedef struct {
  xstr name;
  xvec rules;
} ftp_group;

typedef struct {
  xstr name;
  xstr root_jail;
  ftp_group* group;
  xvec rules;
} ftp_user;

static int single_user_mode = -1;
static char* single_user_mode_user = NULL;
static char* single_user_mode_pwd = NULL;
static char* single_user_mode_root_jail = NULL;
static int single_user_mode_readonly = -1;

static int multi_user_mode = -1;

xsuccess ftp_acl_single_user_mode(const char* user, const char* pwd, const char* root_jail, const xbool readonly) {
  assert(multi_user_mode == -1);
  single_user_mode = 1;
  single_user_mode_user = strdup(user);
  single_user_mode_pwd = strdup(pwd);
  single_user_mode_root_jail = strdup(root_jail);
  single_user_mode_readonly = readonly;
  return XSUCCESS;
}

xsuccess ftp_acl_multi_user_mode(const char* db_fname) {
  // TODO implement multi user mode, connect to sqlite3 database
  xsuccess ret = XFAILURE;
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
    // TODO fetch info from sqlite3 database
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
    // TODO fetch info from sqlite3 database
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
    assert(multi_user_mode == 1);
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
    assert(multi_user_mode == 1);
    // TODO close sqlite3 connection
  }
  return ret;
}


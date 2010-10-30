#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>

#include "xsys.h"
#include "lqd_defs.h"
#include "lqd_archive.h"

xbool archive_has(const char* basefolder, xbyte* key) {
  xbool ret = XTRUE;
  xstr path = xstr_new();
  if (archive_path(basefolder, key, path) == XFAILURE) {
    ret = XFALSE;
  }
  xstr_delete(path);
  return ret;
}

xsuccess archive_path(const char* basefolder, xbyte* key, xstr path) {
  xsuccess ret = XSUCCESS;
  int i;
  char buf[4];
  struct stat st;
  xstr_set_cstr(path, basefolder);
  // 3 levels of directory
  for (i = 0; i < FOLDER_LEVELS; i++) {
    sprintf(buf, "%02x", key[i]);
    xstr_printf(path, "%c%s", xsys_fs_sep_char, buf);
  }
  xstr_printf(path, "%c", xsys_fs_sep_char);
  for (i = 0; i < FILE_KEY_BYTES; i++) {
    sprintf(buf, "%02x", key[i]);
    xstr_printf(path, "%s", buf);
  }
  if (lstat(xstr_get_cstr(path), &st) != 0) {
    ret = XFAILURE;
  }
  return ret;
}

FILE* archive_open_fp(const char* basefolder, xbyte* key, const char* modes) {
  xstr path = xstr_new();
  xstr folder_path = xstr_new();
  FILE* fp;
  int i;
  char buf[4];

  // make folders
  xstr_set_cstr(folder_path, basefolder);
  mkdir(xstr_get_cstr(folder_path), 0755);
  for (i = 0; i < FOLDER_LEVELS; i++) {
    sprintf(buf, "%02x", key[i]);
    xstr_printf(folder_path, "%c%s", xsys_fs_sep_char, buf);
    mkdir(xstr_get_cstr(folder_path), 0755);
  }
  archive_path(basefolder, key, path);
  fp = fopen(xstr_get_cstr(path), modes);
  xstr_delete(folder_path);
  xstr_delete(path);
  return fp;
}

int archive_open_fd(const char* basefolder, xbyte* key, int options) {
  xstr path = xstr_new();
  xstr folder_path = xstr_new();
  int i, fd;
  char buf[4];

  // make folders
  xstr_set_cstr(folder_path, basefolder);
  mkdir(xstr_get_cstr(folder_path), 0755);
  for (i = 0; i < FOLDER_LEVELS; i++) {
    sprintf(buf, "%02x", key[i]);
    xstr_printf(folder_path, "%c%s", xsys_fs_sep_char, buf);
    mkdir(xstr_get_cstr(folder_path), 0755);
  }
  archive_path(basefolder, key, path);
  fd = open(xstr_get_cstr(path), options);
  xstr_delete(folder_path);
  xstr_delete(path);
  return fd;
}


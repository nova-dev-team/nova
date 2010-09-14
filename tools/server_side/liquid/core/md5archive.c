#include <sys/stat.h>
#include <sys/types.h>

#include "xsys.h"

#include "md5archive.h"

// ENHANCE it is said that some other hash method is faster than md5
// however, hash calculation is mostly left for clients, so it may not be a big performance problem, on the server side

xbool md5archive_has(const char* basefolder, unsigned char* md5) {
  xbool ret = XTRUE;
  xstr path = xstr_new();
  if (md5archive_path(basefolder, md5, path) == XFAILURE) {
    ret = XFALSE;
  }
  xstr_delete(path);
  return ret;
}

xsuccess md5archive_path(const char* basefolder, unsigned char* md5, xstr path) {
  xsuccess ret = XSUCCESS;
  int i;
  char buf[4];
  struct stat st;
  xstr_set_cstr(path, basefolder);
  for (i = 0; i < 2; i++) {
    sprintf(buf, "%02x", md5[i]);
    xstr_printf(path, "%c%s", xsys_fs_sep_char, buf);
  }
  xstr_printf(path, "%c", xsys_fs_sep_char);
  for (i = 0; i < 16; i++) {
    sprintf(buf, "%02x", md5[i]);
    xstr_printf(path, "%s", buf);
  }
  if (lstat(xstr_get_cstr(path), &st) != 0) {
    ret = XFAILURE;
  }
  return ret;
}

FILE* md5archive_open(const char* basefolder, unsigned char* md5, const char* modes) {
  xstr path = xstr_new();
  xstr folder_path = xstr_new();
  FILE* fp;
  int i;
  char buf[4];

  // make folders
  xstr_set_cstr(folder_path, basefolder);
  mkdir(xstr_get_cstr(folder_path), 0755);
  for (i = 0; i < 2; i++) {
    sprintf(buf, "%02x", md5[i]);
    xstr_printf(folder_path, "%c%s", xsys_fs_sep_char, buf);
    mkdir(xstr_get_cstr(folder_path), 0755);
  }
  md5archive_path(basefolder, md5, path);
  fp = fopen(xstr_get_cstr(path), modes);
  xstr_delete(folder_path);
  xstr_delete(path);
  return fp;
}


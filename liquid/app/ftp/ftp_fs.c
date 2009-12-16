#include <dirent.h>
#include <stdio.h>
#include <sys/stat.h>
#include <string.h>
#include <time.h>

#include "xmemory.h"
#include "xutils.h"
#include "liquid_client.h"

#include "ftp_fs.h"

static void fill_stat_info_into_xstr(struct stat* st, xstr holder) {
  char time_str_buf[32];
  struct tm* tm_struct;

  if (S_ISDIR(st->st_mode)) {
    xstr_append_char(holder, 'd');
  } else if (S_ISLNK(st->st_mode)) {
    xstr_append_char(holder, 'l');
  } else {
    xstr_append_char(holder, '-');
  }

  if (S_IRUSR & st->st_mode) {
    xstr_append_char(holder, 'r');
  } else {
    xstr_append_char(holder, '-');
  }
  if (S_IWUSR & st->st_mode) {
    xstr_append_char(holder, 'w');
  } else {
    xstr_append_char(holder, '-');
  }
  if (S_IXUSR & st->st_mode) {
    xstr_append_char(holder, 'x');
  } else {
    xstr_append_char(holder, '-');
  }

  if (S_IRGRP & st->st_mode) {
    xstr_append_char(holder, 'r');
  } else {
    xstr_append_char(holder, '-');
  }
  if (S_IWGRP & st->st_mode) {
    xstr_append_char(holder, 'w');
  } else {
    xstr_append_char(holder, '-');
  }
  if (S_IXGRP & st->st_mode) {
    xstr_append_char(holder, 'x');
  } else {
    xstr_append_char(holder, '-');
  }

  if (S_IROTH & st->st_mode) {
    xstr_append_char(holder, 'r');
  } else {
    xstr_append_char(holder, '-');
  }
  if (S_IWOTH & st->st_mode) {
    xstr_append_char(holder, 'w');
  } else {
    xstr_append_char(holder, '-');
  }
  if (S_IXOTH & st->st_mode) {
    xstr_append_char(holder, 'x');
  } else {
    xstr_append_char(holder, '-');
  }

  tm_struct = localtime(&(st->st_mtime));
  strftime(time_str_buf, sizeof(time_str_buf), "%b %d %H:%M", tm_struct);
  xstr_printf(holder, " %d %s %s %d %s", st->st_nlink, "user", "group", st->st_size, time_str_buf);
}

xsuccess ftp_fs_list_into_xstr(xstr path, xstr holder, xstr error_msg) {
// example list content:
// drwxr-xr-x 2 user group 4096 Dec 07 07:09 folder\r\n

  DIR* p_dir;
  struct dirent* p_dirent;
  struct stat st;
  char* pathbuf;
  int path_len = xstr_len(path);

  p_dir = opendir(xstr_get_cstr(path));
  if (p_dir == NULL) {
    xstr_set_cstr(error_msg, "500 failed to open dir\n");
    return XFAILURE;
  }

  pathbuf = xmalloc_ty(8192, char);
  while ((p_dirent = readdir(p_dir)) != NULL) {
    strcpy(pathbuf, xstr_get_cstr(path));
    if (pathbuf[path_len - 1] != '/') {
      strcpy(pathbuf + path_len, "/");
      strcpy(pathbuf + path_len + 1, p_dirent->d_name);
    } else {
      strcpy(pathbuf + path_len, p_dirent->d_name);
    }
    lstat(pathbuf, &st);

    fill_stat_info_into_xstr(&st, holder);
    xstr_printf(holder, " %s\r\n", p_dirent->d_name);
  }
  closedir(p_dir);
  xfree(pathbuf);
  return XSUCCESS;
}

xsuccess ftp_fs_try_cwd_cstr(const char* current_dir, const char* new_path, xstr error_msg) {
  if (xcstr_startwith_cstr(new_path, "/")) {
    DIR* p_dir = opendir(new_path);
    if (p_dir == NULL) {
      xstr_set_cstr(error_msg, "500 failed to change working directory\n");
      return XFAILURE;
    }
    closedir(p_dir);
  } else {
    DIR* p_dir;
    int current_dir_len = strlen(current_dir);
    int new_path_len = strlen(new_path);
    char* new_full_path = xmalloc_ty(current_dir_len + new_path_len + 2, char);
    strcpy(new_full_path, current_dir);
    if (current_dir[current_dir_len - 1] != '/') {
      strcpy(new_full_path + current_dir_len, "/");
      strcpy(new_full_path + current_dir_len + 1, new_path);
    } else {
      strcpy(new_full_path + current_dir_len, new_path);
    }
    p_dir = opendir(new_full_path);
    xfree(new_full_path);
    if (p_dir == NULL) {
      xstr_set_cstr(error_msg, "500 failed to change working directory\n");
      return XFAILURE;
    }
    closedir(p_dir);
  }
  return XSUCCESS;
}

xsuccess ftp_fs_retr_file(xsocket xsock, xstr current_dir, const char* filename, xstr error_msg) {
  FILE* fp;
  xstr fullpath = xstr_copy(current_dir);
  xstr_append_char(fullpath, '/');
  xstr_append_cstr(fullpath, filename);
  
  fp = fopen(xstr_get_cstr(fullpath), "r");
  if (fp == NULL) {
    xstr_delete(fullpath);
    xstr_set_cstr(error_msg, "500 failed to open file\n");
    return XFAILURE;
  } else {
    int buf_size = 8192;
    int cnt;
    void* buf = xmalloc_ty(buf_size, char);
    while (!feof(fp)) {
      cnt = fread(buf, 1, buf_size, fp);
      if (cnt <= 0) {
        break;
      }
      xsocket_write(xsock, buf, cnt);
    }
    fclose(fp);
    xfree(buf);
    xstr_delete(fullpath);
    return XSUCCESS;
  }
}



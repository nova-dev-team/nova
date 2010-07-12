#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <utime.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <errno.h>

#include "xmemory.h"
#include "xutils.h"
#include "xlog.h"

#include "ftp_fs.h"

// if this is set to 1, then every filesystem operation will be profiled
#ifndef PROFILE_FTP_FS
#define PROFILE_FTP_FS 1
#endif  // PROFILE_FTP_FS

// disable sendfile function for MacOS
#ifdef __APPLE__
#define USE_LINUX_SENDFILE 0
#endif  // __APPLE__

// if this is set to 1, then sendfile() of Linux will be used
#ifndef USE_LINUX_SENDFILE
#define USE_LINUX_SENDFILE 1
#endif  // USE_LINUX_SENDFILE

#if USE_LINUX_SENDFILE == 1
#include <sys/sendfile.h>
#include <fcntl.h>
#endif  // USE_LINUX_SENDFILE == 1

// a helper function that behaves like xjoin_path_cstr.
// xjoin_path_cstr will discard 'jail' if 'unjailed_path' starts with '/'
// but this function will not

// input:
//    jail: the root jail path
//    unjailed_path: the path that is not jailed (seen by user)
//
// ouput:
//    jailed_path: the path that is jailed (seen by server, and is safe)
static void jail_path(xstr jailed_path, const xstr jail, const xstr unjailed_path) {
  char sep = '/'; // filesystem path separator
  xstr jailed_path_copy = xstr_new();
  xstr_set_cstr(jailed_path_copy, "");
  xstr_set_cstr(jailed_path, "");

  xstr_printf(jailed_path_copy, "%s%c%s", xstr_get_cstr(jail), sep, xstr_get_cstr(unjailed_path));
  xfilesystem_normalize_abs_path(xstr_get_cstr(jailed_path_copy), jailed_path);

  // TODO check really inside jail. what about links?

  xstr_delete(jailed_path_copy);
}

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
  xstr_printf(holder, " %d %s %s %ld %s", st->st_nlink, "user", "group", st->st_size, time_str_buf);
}

xsuccess ftp_fs_list_into_xstr(const xstr root_jail, const xstr unjailed_path, xstr holder, xstr error_msg) {
// example list content:
// drwxr-xr-x 2 user group 4096 Dec 07 07:09 folder\r\n
  DIR* p_dir;
  xsuccess ret = XSUCCESS;
  xstr jailed_path = xstr_new();

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  jail_path(jailed_path, root_jail, unjailed_path);

  p_dir = opendir(xstr_get_cstr(jailed_path));
  if (p_dir == NULL) {
    xstr_set_cstr(error_msg, "500 failed to open dir\r\n");
    ret = XFAILURE;

  } else {
    struct dirent* p_dirent;
    struct stat st;
    xstr fullpath = xstr_new();

    while ((p_dirent = readdir(p_dir)) != NULL) {
      xjoin_path_cstr(fullpath, xstr_get_cstr(jailed_path), p_dirent->d_name);
      lstat(xstr_get_cstr(fullpath), &st);
      fill_stat_info_into_xstr(&st, holder);
      xstr_printf(holder, " %s\r\n", p_dirent->d_name);
    }
    closedir(p_dir);
    xstr_delete(fullpath);
  }

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_list_into_xstr took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1

  xstr_delete(jailed_path);
  return ret;
}

xsuccess ftp_fs_try_cwd_cstr(const xstr root_jail, const char* current_dir, const char* new_path, xstr error_msg) {
  xsuccess ret = XSUCCESS;
  xstr unjailed_fullpath = xstr_new();
  xstr jailed_fullpath = xstr_new();
  DIR* p_dir;

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_fullpath, current_dir, new_path);
  jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

  p_dir = opendir(xstr_get_cstr(jailed_fullpath));
  if (p_dir == NULL) {
    xstr_set_cstr(error_msg, "500 failed to change working directory\r\n");
    ret = XFAILURE;
  } else {
    closedir(p_dir);
  }
  xstr_delete(unjailed_fullpath);
  xstr_delete(jailed_fullpath);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_try_cwd_cstr took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1

  return ret;
}

xsuccess ftp_fs_retr_file(xsocket xsock, const xstr root_jail, const xstr current_dir, const char* filename, off_t start_pos, xstr error_msg) {
  struct stat st;
  xstr unjailed_fullpath = xstr_new();
  xstr jailed_fullpath = xstr_new();
  xsuccess ret = XSUCCESS;

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_fullpath, xstr_get_cstr(current_dir), filename);
  jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

  lstat(xstr_get_cstr(jailed_fullpath), &st);
  if (S_ISDIR(st.st_mode)) {
    // send dir
    xstr ls_data = xstr_new();

    // the param here should be unjailed, since it will be jailed again inside ftp_fs_list_into_xstr
    if (ftp_fs_list_into_xstr(root_jail, unjailed_fullpath, ls_data, error_msg) != XSUCCESS) {
      ret = XFAILURE;
    } else {
      xsocket_write(xsock, (const void *) xstr_get_cstr(ls_data), xstr_len(ls_data));
    }

    xstr_delete(ls_data);
  } else {
#if USE_LINUX_SENDFILE == 1
    // send file
    int in_fd = open(xstr_get_cstr(jailed_fullpath), O_RDONLY);
    if (in_fd == -1) {
      // error opening file
      xstr_set_cstr(error_msg, "500 failed to open file\r\n");
      ret = XFAILURE;
    } else {
      int out_fd = xsocket_get_socket_fd(xsock);
      off_t off_value = start_pos;
      struct stat st;
      off_t send_count = st.st_size - off_value;

      // note that sendfile() has a limit of 2GB, so if the file is larger than 1G (a smaller value than 2G, just to avoid margin values), we send 1G at a time
      const int send_limit = 1 * 1024 * 1024 * 1024;  // 1 GB
      int limited_send_count;

      stat(xstr_get_cstr(jailed_fullpath), &st);
      while (send_count > 0) {
        int cnt;

        // limit send count to 1G
        if (send_count > send_limit) {
          limited_send_count = send_limit;
        } else {
          limited_send_count = send_count;
        }
        if ((cnt = sendfile(out_fd, in_fd, &off_value, limited_send_count)) < 0) {
          xstr_set_cstr(error_msg, "500 server side error\r\n");
          ret = XFAILURE;
          break;
        }
        if (cnt == 0) {
          break;
        }
        send_count -= cnt;
      }
      close(in_fd);
    }
#else
    FILE* fp = fopen(xstr_get_cstr(jailed_fullpath), "r");
    if (fp == NULL) {
      xstr_set_cstr(error_msg, "500 failed to open file\r\n");
      ret = XFAILURE;
    } else {
      int buf_size = 8192;
      int cnt;
      void* buf = xmalloc_ty(buf_size, char);

      if (start_pos != 0) {
        fseeko(fp, start_pos, SEEK_SET);
      }
      while (!feof(fp)) {
        cnt = fread(buf, 1, buf_size, fp);
        if (cnt <= 0) {
          break;
        }
        if (xsocket_write(xsock, buf, cnt) < 0) {
          xstr_set_cstr(error_msg, "500 failed to send file contents\r\n");
          ret = XFAILURE;
          break;
        }
      }
      fclose(fp);
      xfree(buf);
    }
#endif  // USE_LINUX_SENDFILE == 1
  }
  xstr_delete(unjailed_fullpath);
  xstr_delete(jailed_fullpath);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_retr_file took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1

  return ret;
}

xsuccess ftp_fs_mdtm(const xstr root_jail, const char* current_dir, const char* filename, xstr mdtm_str, xstr error_msg) {
  struct stat st;
  xsuccess ret = XSUCCESS;
  xstr unjailed_fullpath = xstr_new();
  xstr jailed_fullpath = xstr_new();

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_fullpath, current_dir, filename);
  jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

  if (lstat(xstr_get_cstr(jailed_fullpath), &st) < 0) {
    xstr_set_cstr(error_msg, "");
    xstr_printf(error_msg, "550 MDTM failure on file '%s'\r\n", filename);
    ret = XFAILURE;
  } else if (S_ISREG(st.st_mode)) {
    char time_str_buf[32];
    struct tm* tm_struct;

    tm_struct = localtime(&(st.st_mtime));
    strftime(time_str_buf, sizeof(time_str_buf), "%Y%m%d%H%M%S", tm_struct);

    xstr_set_cstr(mdtm_str, "");
    xstr_printf(mdtm_str, "213 %s\r\n", time_str_buf);
  } else {
    xstr_set_cstr(error_msg, "");
    xstr_printf(error_msg, "550 not a regular file: '%s'\r\n", filename);
    ret = XFAILURE;
  }
  xstr_delete(jailed_fullpath);
  xstr_delete(unjailed_fullpath);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_mdtm took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1

  return ret;
}

xsuccess ftp_fs_size(const xstr root_jail, const char* current_dir, const char* filename, xstr size_str, xstr error_msg) {
  struct stat st;
  xsuccess ret = XSUCCESS;
  xstr unjailed_fullpath = xstr_new();
  xstr jailed_fullpath = xstr_new();

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_fullpath, current_dir, filename);
  jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

  if (lstat(xstr_get_cstr(jailed_fullpath), &st) < 0) {
    xstr_set_cstr(error_msg, "");
    xstr_printf(error_msg, "550 SIZE failure on file '%s'\r\n", filename);
    ret = XFAILURE;
  } else if (S_ISREG(st.st_mode)) {
    xstr_set_cstr(size_str, "");
    xstr_printf(size_str, "213 %ld\r\n", st.st_size);
  } else {
    xstr_set_cstr(error_msg, "");
    xstr_printf(error_msg, "550 not a regular file: '%s'\r\n", filename);
    ret = XFAILURE;
  }
  xstr_delete(unjailed_fullpath);
  xstr_delete(jailed_fullpath);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_size took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1
  return ret;
}

xsuccess ftp_fs_mkdir(const xstr root_jail, const char* current_dir, const char* dirname, xstr error_msg) {
  xsuccess ret = XSUCCESS;
  xstr unjailed_fullpath = xstr_new();
  xstr jailed_fullpath = xstr_new();

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_fullpath, current_dir, dirname);
  jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

  if (mkdir(xstr_get_cstr(jailed_fullpath), 0755) != 0) {
    xstr_set_cstr(error_msg, "500 failed to mkdir\r\n");
    ret = XFAILURE;
  }
  xstr_delete(unjailed_fullpath);
  xstr_delete(jailed_fullpath);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_mkdir took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1
  return ret;
}

xsuccess ftp_fs_rename(const xstr root_jail, const char* current_dir, const char* from_name, const char* to_name, xstr error_msg) {
  xsuccess ret = XSUCCESS;
  xstr unjailed_from_path = xstr_new();
  xstr unjailed_to_path = xstr_new();

  xstr jailed_from_path = xstr_new();
  xstr jailed_to_path = xstr_new();

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_from_path, current_dir, from_name);
  xjoin_path_cstr(unjailed_to_path, current_dir, to_name);
  jail_path(jailed_from_path, root_jail, unjailed_from_path);
  jail_path(jailed_to_path, root_jail, unjailed_to_path);

  if (rename(xstr_get_cstr(jailed_from_path), xstr_get_cstr(jailed_to_path)) != 0) {
    xstr_set_cstr(error_msg, "550 rename failed\r\n");
    ret = XFAILURE;
  }

  xstr_delete(unjailed_from_path);
  xstr_delete(unjailed_to_path);
  xstr_delete(jailed_from_path);
  xstr_delete(jailed_to_path);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_rename took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1
  return ret;
}

xsuccess ftp_fs_dele(const xstr root_jail, const char* current_dir, const char* dirname, xstr error_msg) {
  xsuccess ret = XSUCCESS;
  xstr unjailed_fullpath = xstr_new();
  xstr jailed_fullpath = xstr_new();

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_fullpath, current_dir, dirname);
  jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

  if (xfilesystem_rmrf(xstr_get_cstr(jailed_fullpath)) != XSUCCESS) {
    xstr_set_cstr(error_msg, "500 error occured during DELE execution\r\n");
    ret = XFAILURE;
  }
  xstr_delete(jailed_fullpath);
  xstr_delete(unjailed_fullpath);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_dele took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1
  return ret;
}

xbool ftp_fs_could_stor_file(const xstr root_jail, const xstr current_dir, const char* filename, off_t start_pos) {
  xstr unjailed_fullpath = xstr_new();
  xstr jailed_fullpath = xstr_new();
  xbool ret = XTRUE;

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_fullpath, xstr_get_cstr(current_dir), filename);
  jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

  // test if could write file
  FILE* fp;
  if (start_pos == 0) {
    // start pos == 0, so should truncate
    fp = fopen(xstr_get_cstr(jailed_fullpath), "w");
  } else {
    fp = fopen(xstr_get_cstr(jailed_fullpath), "a");
  }
  if (fp == NULL) {
    ret = XFALSE;
  } else {
    if (start_pos != 0) {
      if (fseeko(fp, start_pos, SEEK_SET) != 0) {
        ret = XFALSE;
      }
    }
    fclose(fp);
    // the file must exist at here
    // now we need to truncate the file to 'start_pos', if start_pos != 0
    if (start_pos != 0) {
      xlog_debug("[ftp] truncate the file '%s' to %lld\n", xstr_get_cstr(jailed_fullpath), (long long) start_pos);
      if (truncate(xstr_get_cstr(jailed_fullpath), start_pos) != 0) {
        ret = XFALSE;
      }
    }
  }
  xstr_delete(unjailed_fullpath);
  xstr_delete(jailed_fullpath);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_could_stor_file took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1

  return ret;
}

xsuccess ftp_fs_stor_file(xsocket xsock, const xstr root_jail, const xstr current_dir, const char* filename, off_t start_pos, xstr error_msg) {
  xstr unjailed_fullpath = xstr_new();
  xstr jailed_fullpath = xstr_new();
  xsuccess ret = XSUCCESS;
  off_t total_bytes_read = 0;

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  xjoin_path_cstr(unjailed_fullpath, xstr_get_cstr(current_dir), filename);
  jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

  FILE* fp;
  if (start_pos == 0) {
    // start pos == 0, so should truncate
    fp = fopen(xstr_get_cstr(jailed_fullpath), "w");
  } else {
    // start pos != 0, so should NOT truncate
    fp = fopen(xstr_get_cstr(jailed_fullpath), "r+");
  }
  if (fp == NULL) {
    // test again, if could create file
    xstr_set_cstr(error_msg, "500 failed to create file");
    xstr_append_cstr(error_msg, xstr_get_cstr(jailed_fullpath));
    xstr_append_cstr(error_msg, "\r\n");
    ret = XFAILURE;
  } else {
    int buf_size = 8192;
    int cnt;
    void* buf = xmalloc_ty(buf_size, char);
    if (start_pos != 0) {
      xlog_debug("[ftp-fs] seeking %lld\n", (long long) start_pos);
      fseeko(fp, start_pos, SEEK_SET);
    }
    for (;;) {
      cnt = xsocket_read(xsock, buf, buf_size);
      if (cnt < 0) {
        xstr_set_cstr(error_msg, "426 failed to read client input\r\n");
        ret = XFAILURE;
        break;
      } else if (cnt == 0) {
        // got all file content
        xlog_debug("[ftp] client input stream stopped\n");
        xlog_debug("[ftp] total bytes read=%lld\n", (long long) total_bytes_read);
        break;
      } else {
        total_bytes_read += cnt;
        if (fwrite(buf, 1, cnt, fp) < 0) {
          xstr_set_cstr(error_msg, "451 failed to write file contents\r\n");
          ret = XFAILURE;
          break;
        }
      }
    }
    fclose(fp);
    xfree(buf);
  }
  xstr_delete(unjailed_fullpath);
  xstr_delete(jailed_fullpath);

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_stor_file took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1

  return ret;
}

xsuccess ftp_fs_site_cmd(const xstr root_jail, const char* current_dir, const char* cmd, xstr error_msg) {
  xsuccess ret = XSUCCESS;

#if PROFILE_FTP_FS == 1
  struct timeval begin_time, end_time;
  gettimeofday(&begin_time, NULL);
#endif  // PROFILE_FTP_FS == 1

  if (xcstr_startwith_cstr(cmd, "UTIME")) {
    int i;
    struct tm t;
    time_t time_val;
    char strbuf[16];
    xbool has_error = XFALSE;

    xstr unjailed_fullpath = xstr_new();
    xstr jailed_fullpath = xstr_new();
    xstr fn = xstr_new();


    for (i = 0; i < 14; i++) {
      if (cmd[i + 6] < '0' || cmd[i + 6] > '9') {
        has_error = XTRUE;
        break;
      } else {
        strbuf[i] = cmd[i + 6];
      }
    }

    if (has_error == XTRUE) {
      ret = XFAILURE;
      xstr_set_cstr(error_msg, "501 bad SITE command\r\n");
    } else {
      char timestr_buf[32];
      t.tm_sec = atoi(strbuf + 12);
      strbuf[12] = '\0';
      t.tm_min = atoi(strbuf + 10);
      strbuf[10] = '\0';
      t.tm_hour = atoi(strbuf + 8);
      strbuf[8] = '\0';
      t.tm_mday = atoi(strbuf + 6);
      strbuf[6] = '\0';
      t.tm_mon = atoi(strbuf + 4) - 1;
      strbuf[4] = '\0';
      t.tm_year = atoi(strbuf) - 1900;
      time_val = mktime(&t);
      strftime(timestr_buf, 31, "%Y.%m.%d %H:%M:%S", &t);
      xlog_debug("[site utime] new time is: %s\n", timestr_buf);
    }

    if (ret != XFAILURE) {
      xstr_set_cstr(fn, cmd + 6 + 14);
      xstr_strip(fn, " \t");
      if (xstr_len(fn) == 0) {
        ret = XFAILURE;
        xstr_set_cstr(error_msg, "501 SITE command must provide enough arguments\r\n");
      } else {
        struct utimbuf ut;
        ut.actime = time_val;
        ut.modtime = time_val;
        xjoin_path_cstr(unjailed_fullpath, current_dir, xstr_get_cstr(fn));
        jail_path(jailed_fullpath, root_jail, unjailed_fullpath);
        xlog_debug("[site utime] going to utime on file: '%s'\n", xstr_get_cstr(jailed_fullpath));
        if (utime(xstr_get_cstr(jailed_fullpath), &ut) != 0) {
          ret = XFAILURE;
          xstr_set_cstr(error_msg, "500 Failed to execute SITE UTIME command\r\n");
        }
      }
    }

    xstr_delete(fn);
    xstr_delete(unjailed_fullpath);
    xstr_delete(jailed_fullpath);

  } else if (xcstr_startwith_cstr(cmd, "CHMOD")) {
    int mode_val = 0;
    int i;
    xstr unjailed_fullpath = xstr_new();
    xstr jailed_fullpath = xstr_new();
    xstr fn = xstr_new();

    for (i = 6; cmd[i] != ' ' && cmd[i] != '\0'; i++) {
      mode_val *= 8;
      mode_val += cmd[i] - '0';
    }

    xlog_debug("[site chmod] new mode value is %o\n", mode_val);

    xstr_set_cstr(fn, cmd + i);
    xstr_strip(fn, " \t");

    xjoin_path_cstr(unjailed_fullpath, current_dir, xstr_get_cstr(fn));
    jail_path(jailed_fullpath, root_jail, unjailed_fullpath);

    if (chmod(xstr_get_cstr(jailed_fullpath), mode_val) != 0) {
      ret = XFAILURE;
      xstr_set_cstr(error_msg, "500 Failed to execute SITE CHMOD command\r\n");
    }

    xstr_delete(fn);
    xstr_delete(unjailed_fullpath);
    xstr_delete(jailed_fullpath);

  } else {
    ret = XFAILURE;
    xstr_set_cstr(error_msg, "500 unrecognized SITE command\r\n");
  }

#if PROFILE_FTP_FS == 1
  gettimeofday(&end_time, NULL);
  xlog_debug("[prof] ftp_fs_site_cmd took %d, %d (sec, usec)\n", (int) (end_time.tv_sec - begin_time.tv_sec), (int) (end_time.tv_usec - begin_time.tv_usec));
#endif  // PROFILE_FTP_FS == 1
  return ret;
}


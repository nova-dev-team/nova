#include <stddef.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <assert.h>

#include "xmemory.h"
#include "xutils.h"
#include "xcrypto.h"
#include "xsys.h"

// adapted from www.jb.man.ac.uk/~slowe/cpp/itoa.html
char* xitoa(int value, char* buf, int base) {
  char* ptr = buf;
  char* ptr1 = buf;
  char* digits = "zyxwvutsrqponmlkjihgfedcba9876543210123456789abcdefghijklmnopqrstuvwxyz";
  char ch;
  int v;

  if (base < 2 || base > 36) {
    *buf = '\0';
    return buf;
  }

  do {
    v = value;
    value /= base;
    *ptr++ = digits[35 + (v - value * base)];
  } while (value);

  if (v < 0)
    *ptr++ = '-';

  *ptr-- = '\0';
  while (ptr1 < ptr) {
    ch = *ptr;
    *ptr-- = *ptr1;
    *ptr1++ = ch;
  }

  return buf;
}

// adapted from www.jb.man.ac.uk/~slowe/cpp/itoa.html
char* xltoa(long long value, char* buf, int base) {
  char* ptr = buf;
  char* ptr1 = buf;
  char* digits = "zyxwvutsrqponmlkjihgfedcba9876543210123456789abcdefghijklmnopqrstuvwxyz";
  char ch;
  long v;

  if (base < 2 || base > 36) {
    *buf = '\0';
    return buf;
  }

  do {
    v = value;
    value /= base;
    *ptr++ = digits[35 + (v - value * base)];
  } while (value);

  if (v < 0)
    *ptr++ = '-';

  *ptr-- = '\0';
  while (ptr1 < ptr) {
    ch = *ptr;
    *ptr-- = *ptr1;
    *ptr1++ = ch;
  }

  return buf;
}


xbool xcstr_startwith_cstr(const char* str, const char* head) {
  int i;
  for (i = 0; str[i] != '\0' && head[i] != '\0'; i++) {
    if (str[i] != head[i])
      return XFALSE;
  }
  return head[i] == '\0';
}

char* xcstr_strip(char* str) {
  int begin = 0;
  int end = strlen(str) - 1;
  int i;

  while (str[begin] == ' ' || str[begin] == '\t' || str[begin] == '\r' || str[begin] == '\n') {
    begin++;
  }

  while (end >= 0 && (str[end] == ' ' || str[end] == '\t' || str[end] == '\r' || str[end] == '\n')) {
    end--;
  }

  if (end <= begin) {
    // whole string stripped
    str[0] = '\0';
    return str;
  }

  for (i = 0; str[begin + i] != '\0' && begin + i <= end; i++) {
    str[i] = str[begin + i];
  }
  str[i] = '\0';

  return str;
}

char* xcstr_strip_trailing_crlf(char* str) {
  // abc\r\n -> abc
  int pos = strlen(str) - 1;
  while (pos >= 0 && (str[pos] == '\r' || str[pos] == '\n')) {
    str[pos] = '\0';
    pos--;
  }
  return str;
}

xsuccess xinet_ip2str(int ip, char* str) {
  unsigned char* p = (unsigned char *) &ip;
  int i;
  char seg_str[4];
  str[0] = '\0';

  // TODO big endian? little endian?
  for (i = 3; i >= 0; i--) {
    int seg = p[i];
    xitoa(seg, seg_str, 10);
    strcat(str, seg_str);
    if (i != 0) {
      strcat(str, ".");
    }
  }
  return XSUCCESS;
}

xsuccess xinet_get_sockaddr(const char* host, int port, struct sockaddr_in* addr) {
  in_addr_t a;
  if (host == NULL) {
    return XFAILURE;
  }
  bzero(addr, sizeof(*addr));
  addr->sin_family = AF_INET;
  a = inet_addr(host);
  if (a != INADDR_NONE) {
    addr->sin_addr.s_addr = a;
  } else {
    struct hostent* hp = gethostbyname(host);
    if (hp == 0 || hp->h_length != 4) {
      return XFAILURE;
    }
  }
  addr->sin_port = htons(port);
  return XSUCCESS;
}

// helper function, split ipv4 and port
static xsuccess xinet_split_ipv4_port(XIN const char* host_port, XOUT xstr host, XOUT int* port) {
  xsuccess ret = XSUCCESS;
  int i, j, seg_count = 0;
  char seg_text[4];
  xstr_set_cstr(host, "");
  j = 0;
  for (i = 0; host_port[i] != '\0' && host_port[i] != ':'; i++) {
    if (host_port[i] == '.') {
      if (i >= 1 && host_port[i - 1] == '.') {
        // should not have 2 '.' consecutively
        ret = XFAILURE;
        break;
      }
      seg_count++;
      if (atoi(seg_text) > 255) {
        // for ipv4, no seg could be > 255
        ret = XFAILURE;
        break;
      }
      j = 0;  // start a new seg
      xstr_append_char(host, host_port[i]);
    } else if (host_port[i] == ':') {
      seg_count++;
      break;
    } else if ('0' <= host_port[i] && host_port[i] <= '9') {
      seg_text[j] = host_port[i];
      j++;
      seg_text[j] = '\0';
      if (j > 3) {
        // for ipv4, no seg could have length > 3
        ret = XFAILURE;
        break;
      }
      xstr_append_char(host, host_port[i]);
    } else {
      ret = XFAILURE;
      break;
    }
  }
  seg_count++;
  if (atoi(seg_text) > 255 || seg_count != 4) {
    // check last segmnt, for ipv4, no seg could be > 255
    // check if host ip has 4 parts
    ret = XFAILURE;
  }
  if (ret == XSUCCESS && host_port[i] == ':') {
    // check if there is only '0-9' after the ':'
    for (j = i + 1; '0' <= host_port[j] && host_port[j] <= '9'; j++) {
    }
    if (host_port[j] != '\0' || j == i + 1) {
      ret = XFAILURE;
    } else {
      int prt = atoi(host_port + i + 1);
      if (prt < 65536) {
        *port = atoi(host_port + i + 1);
        ret = XSUCCESS;
      } else {
        ret = XFAILURE;
      }
    }
  }
  if (ret == XFAILURE) {
    // revert changes to 'host' variable
    xstr_set_cstr(host, "");
  }
  return ret;
}

static xsuccess xinet_split_host_port_helper(XIN const char* host_port, XOUT xstr host, XOUT int* port) {
  xbool has_dot = XFALSE;
  int i;
  for (i = 0; host_port[i] != '\0'; i++) {
    if (host_port[i] == '.') {
      has_dot = XTRUE;
    }
  }
  if (has_dot == XTRUE) {
    // split as ipv4 + port
    // currently only check for ipv4
    return xinet_split_ipv4_port(host_port, host, port);
  } else {
    xstr_set_cstr(host, "");
    for (i = 0; host_port[i] != '\0' && host_port[i] != ':'; i++) {
      xstr_append_char(host, host_port[i]);
    }
    if (host_port[i] == ':') {
      // check if length > 0 and only has 0~9 on port value
      int prt = 0, prt_len = 0;
      for (;;) {
        char ch = host_port[i + 1 + prt_len];
        if (ch == '\0') {
          break;
        } else if ('0' <= ch && ch <= '9') {
          prt_len++;
        } else {
          // illegal chars on port
          xstr_set_cstr(host, "");
          return XFAILURE;
        }
      }
      if (prt_len == 0) {
        xstr_set_cstr(host, "");
        return XFAILURE;
      }
      prt = atoi(host_port + i + 1);
      if (prt < 65536) {
        *port = prt;
        return XSUCCESS;
      } else {
        xstr_set_cstr(host, "");
        return XFAILURE;
      }
    } else {
      // no port, don't touch the 'port' variable
      return XSUCCESS;
    }
  }
}

xsuccess xinet_split_host_port(const char* host_port, xstr host, int* port) {
  return xinet_split_host_port_helper(host_port, host, port);
}

void xjoin_path_cstr(XOUT xstr fullpath, XIN const char* current_dir, XIN const char* append_dir) {
  const char sep = xsys_fs_sep_char; // filesystem path seperator
  const char* sep_str = xsys_fs_sep_cstr;
  if (append_dir[0] == sep) {
    xstr_set_cstr(fullpath, append_dir);
  } else {
    xstr_set_cstr(fullpath, "");
    xstr_printf(fullpath, "%s%c%s", current_dir, sep, append_dir);
  }
  if (xstr_startwith_cstr(fullpath, sep_str)) {
    xstr fullpath_copy = xstr_copy(fullpath);
    xfilesystem_normalize_abs_path(xstr_get_cstr(fullpath_copy), fullpath);
    xstr_delete(fullpath_copy);
  }
}

void xsleep_msec(int msec) {
  struct timespec req = {0};
  time_t sec = msec / 1000;
  long milli = msec - (sec * 1000);
  req.tv_sec = sec;
  req.tv_nsec = milli * 1000000L;
  nanosleep(&req, NULL);
}

xbool xfilesystem_exists(const char* path) {
  struct stat st;
  if (stat(path, &st) != 0) {
    return XFALSE;
  } else {
    return XTRUE;
  }
}

xbool xfilesystem_is_file(const char* path, xsuccess* optional_succ) {
  struct stat st;
  if (stat(path, &st) == 0) {
    if (optional_succ != NULL) {
      *optional_succ = XSUCCESS;
    }
    if (S_ISREG(st.st_mode)) {
      return XTRUE;
    } else {
      return XFALSE;
    }
  } else if (optional_succ != NULL){
    *optional_succ = XFAILURE;
  }
  return XFALSE;
}

xbool xfilesystem_is_dir(const char* path, xsuccess* optional_succ) {
  struct stat st;
  if (stat(path, &st) == 0) {
    if (optional_succ != NULL) {
      *optional_succ = XSUCCESS;
    }
    if (S_ISDIR(st.st_mode)) {
      return XTRUE;
    } else {
      return XFALSE;
    }
  } else if (optional_succ != NULL){
    *optional_succ = XFAILURE;
  }
  return XFALSE;
}

xsuccess xfilesystem_rmrf(const char* path) {
  xsuccess ret = XSUCCESS;
  if (xfilesystem_is_dir(path, NULL) == XTRUE) {
    // TODO detect loops incurred by soft links (or just ignore folder link?)
    xstr subpath = xstr_new();
    DIR* p_dir = opendir(path);

    if (p_dir == NULL) {
      ret = XFAILURE;
    } else {
      struct dirent* p_dirent;
      while ((p_dirent = readdir(p_dir)) != NULL) {
        if (strcmp(p_dirent->d_name, ".") == 0 || strcmp(p_dirent->d_name, "..") == 0) {
          // skip these 2 cases, or the whole filesystem might get removed
          continue;
        }
        xjoin_path_cstr(subpath, path, p_dirent->d_name);
        if (xfilesystem_rmrf(xstr_get_cstr(subpath)) != XSUCCESS) {
          ret = XFAILURE;
          break;
        }
      }
      closedir(p_dir);
      if (ret != XFAILURE) {
        if (rmdir(path) != 0) {
          ret = XFAILURE;
        }
      }
    }
    xstr_delete(subpath);
  } else if (xfilesystem_is_file(path, NULL)) {
    if (unlink(path) != 0) {
      ret = XFAILURE;
    }
  } else {
    // possibly file not fould
    ret = XFAILURE;
  }
  return ret;
}


// requirement: norm_path is normalized
// result: basename will be the last entry's name in norm_path.
//         if norm_path == /, then basename will be /
//
// eg: /abs -> abs
//     / -> /
//     /abs/nice/ -> nice
//     /abs/last -> last
void xfilesystem_basename(XIN xstr norm_path, XOUT xstr basename) {
  xstr_set_cstr(basename, "");
  assert(xstr_len(norm_path) > 0);
  if (strcmp(xstr_get_cstr(norm_path), xsys_fs_sep_cstr) == 0) {
    // special case, / -> /
    xstr_set_cstr(basename, xsys_fs_sep_cstr);
  } else {
    int end = xstr_len(norm_path) - 1;
    int begin = -1;
    int i;
    const char* norm_path_cstr = xstr_get_cstr(norm_path);
    while (end >= 0 && norm_path_cstr[end] == xsys_fs_sep_char) {
      // skip the trailing '/'
      end--;
    }
    begin = end;
    while (begin >= 0 && norm_path_cstr[begin] != xsys_fs_sep_char) {
      begin--;
    }
    for (i = begin + 1; i <= end; i++) {
      xstr_append_char(basename, norm_path_cstr[i]);
    }
  }
}


// requirement: norm_path is normalized
// result: norm_path is changed, if necessary
// when norm_path is '/', we cannot cdup
// the result will have an '/' at the end
xsuccess xfilesystem_path_cdup(xstr norm_path) {
  // check if not '/'
  if (strcmp(xstr_get_cstr(norm_path), xsys_fs_sep_cstr) != 0) {
    // ok, not '/', could cdup
    int new_len = xstr_len(norm_path);
    int index = new_len - 1;
    char sep = xsys_fs_sep_char; // filesystem separator
    char* new_cstr_path = xmalloc_ty(new_len + 1, char);
    strcpy(new_cstr_path, xstr_get_cstr(norm_path));

    // skip the trailing '/'
    if (new_cstr_path[index] == sep) {
      index--;
    }

    while (index > 0 && new_cstr_path[index] != sep) {
      index--;
    }
    new_cstr_path[index] = '\0';
    xstr_set_cstr(norm_path, new_cstr_path);
    xfree(new_cstr_path);
  }
  return XSUCCESS;
}

static void xfilesystem_normalize_abs_path_helper(xstr seg, xstr norm_path) {
  const char sep = xsys_fs_sep_char; // filesystem path seperator
  if (strcmp(xstr_get_cstr(seg), ".") == 0) {
    // do nothing
  } else if (strcmp(xstr_get_cstr(seg), "..") == 0) {
    // cd up
    xfilesystem_path_cdup(norm_path);
  } else {
    // append '/' & 'seg'
    if (xstr_last_char(norm_path) != sep) {
      xstr_append_char(norm_path, sep);
    }
    xstr_append_cstr(norm_path, xstr_get_cstr(seg));
  }
  xstr_set_cstr(seg, "");
}

xsuccess xfilesystem_normalize_abs_path(const char* abs_path, xstr norm_path) {
  xsuccess ret = XSUCCESS;
  const char sep = xsys_fs_sep_char; // filesystem path seperator
  const char* sep_str = xsys_fs_sep_cstr;  // filesystem path seperator, in c-string
  xstr seg = xstr_new();  // a segment in the path
  int i;

  xstr_set_cstr(norm_path, sep_str);

  // check if input is absolute path
  if (abs_path[0] != sep) {
    ret = XFAILURE;
  } else {
    for (i = 0; abs_path[i] != '\0'; i++) {
      if (abs_path[i] == sep) {
        if (xstr_len(seg) != 0) {
          // got a new segment
          xfilesystem_normalize_abs_path_helper(seg, norm_path);
        }
      } else {
        xstr_append_char(seg, abs_path[i]);
      }
    }
    if (xstr_len(seg) != 0) {
      // got a new segment
      xfilesystem_normalize_abs_path_helper(seg, norm_path);
    }
  }
  xstr_delete(seg);
  return ret;
}

long xfilesystem_parse_filesize(const char* size_cstr) {
  long size = 0;
  char* p_end;
  double d_val = strtod(size_cstr, &p_end);
  int i;
  long unit = 1;
  if (p_end == size_cstr) {
    return -1;
  }
  for (i = 0; p_end[i] != '\0'; i++) {
    if (p_end[i] == ' ') {
      continue;
    } else if (p_end[i] == 'G' || p_end[i] == 'g') {
      unit = 1024L * 1024 * 1024;
    } else if (p_end[i] == 'M' || p_end[i] == 'm') {
      unit = 1024L * 1024;
    } else if (p_end[i] == 'K' || p_end[i] == 'k') {
      unit = 1024L;
    } else if (p_end[i] == 'B' || p_end[i] == 'b') {
      unit = 1;
    }
    break;
  }
  size = (long) (d_val * unit);
  return size;
}

// helper function to do real work for mkdir_p operation
static xsuccess xfilesystem_mkdir_p_helper(xstr path, int mode) {
  xsuccess ret = XFAILURE;
  struct stat st;
  if (stat(xstr_get_cstr(path), &st) == 0) {
    // file exists, only need to check if it is a folder
    if (S_ISDIR(st.st_mode)) {
      ret = XSUCCESS;
    } else {
      ret = XFAILURE;
    }
  } else {
    // file does not exist, need to mkdir on parent
    xstr parent_path = xstr_copy(path);
    xfilesystem_path_cdup(parent_path);
    if (xstr_eql(parent_path, path)) {
      // cannot cdup!
      ret = XFAILURE;
    } else {
      // recursive call, make parent folders
      ret = xfilesystem_mkdir_p_helper(parent_path, mode);
      if (ret == XSUCCESS) {
        if (mkdir(xstr_get_cstr(path), mode) == 0) {
          ret = XSUCCESS;
        } else {
          ret = XFAILURE;
        }
      }
    }
    xstr_delete(parent_path);
  }
  return ret;
}

xsuccess xfilesystem_mkdir_p(const char* path, int mode) {
  xsuccess ret = XSUCCESS;
  xstr path_xstr = xstr_new_from_cstr(path);
  const int cwd_buf_len_max = 16 * 1024;
  int cwd_buf_len = 256;
  char* cwd_cstr = xmalloc_ty(cwd_buf_len, char);
  while (getcwd(cwd_cstr, cwd_buf_len) == NULL) {
    // expand the buffer if necessary
    cwd_buf_len *= 2;
    // stop allocation if too much memory is required
    if (cwd_buf_len > cwd_buf_len_max) {
      ret = XFAILURE;
      break;
    }
    cwd_cstr = xrealloc(cwd_cstr, cwd_buf_len);
  }
  if (ret != XFAILURE) {
    xjoin_path_cstr(path_xstr, cwd_cstr, path);
    ret = xfilesystem_mkdir_p_helper(path_xstr, mode);
  }
  xfree(cwd_cstr);
  xstr_delete(path_xstr);
  return ret;
}


xsuccess xfilesystem_split_path(XIN xstr path, XOUT xstr parent, XOUT xstr child) {
  xsuccess ret = XSUCCESS;
  const char* cpath = xstr_get_cstr(path);
  int i;
  xstr_set_cstr(parent, "");
  xstr_set_cstr(child, "");
  for (i = 0; cpath[i] != '\0'; i++) {
    char ch = cpath[i];
    if (ch == xsys_fs_sep_char) {
      xstr_append_cstr(parent, xstr_get_cstr(child));
      xstr_append_char(parent, ch);
      xstr_set_cstr(child, "");
    } else {
      xstr_append_char(child, ch);
    }
  }
  return ret;
}

int xhash_hash_cstr(const void* key) {
  int hv = 0;
  unsigned char digest[16];
  int i;
  const char* cstr = (const char *) key;
  xmd5 xm = xmd5_new();
  xmd5_feed(xm, cstr, strlen(cstr));
  xmd5_result(xm, digest);
  for (i = 0; i < 16; i++) {
    hv ^= (((int) digest[i]) << ((4 * i) % 16));
  }
  xmd5_delete(xm);
  if (hv < 0) {
    hv = -hv;
  }
  return hv;
}

xbool xhash_eql_cstr(const void* key1, const void* key2) {
  if (strcmp((const char *) key1, (const char *) key2) == 0) {
    return XTRUE;
  } else {
    return XFALSE;
  }
}

int xhash_hash_xstr(const void* key) {
  const xstr xs = (const xstr) key;
  return xhash_hash_cstr((const void *) xstr_get_cstr(xs));
}

xbool xhash_eql_xstr(const void* key1, const void* key2) {
  return xstr_eql((xstr) key1, (xstr) key2);
}

int xhash_hash_int(const void* key) {
  const int* int_key = (const int *) key;
  return *int_key;
}

xbool xhash_eql_int(const void* key1, const void* key2) {
  const int* int_key1 = (const int *) key1;
  const int* int_key2 = (const int *) key2;
  if (*int_key1 == *int_key2) {
    return XTRUE;
  } else {
    return XFALSE;
  }
}

xsuccess xgetline_fp(FILE* fp, xstr line) {
  xsuccess ret = XSUCCESS;
  xstr_set_cstr(line, "");
  if (feof(fp)) {
    ret = XFAILURE;
  } else {
    int code;
    int counter = 0;
    for (;;) {
      counter++;
      code = fgetc(fp);
      if (code == '\r') {
        continue;
      } else if (code == '\n') {
        break;
      } else if (feof(fp)) {
        if (counter == 1) {
          ret = XFAILURE;
        }
        break;
      } else {
        char ch = (char) code;
        xstr_append_char(line, ch);
      }
    }
  }
  return ret;
}


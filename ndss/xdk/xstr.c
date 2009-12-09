#include <string.h>
#include <stdio.h>
#include <stdarg.h>

#include "xstr.h"
#include "xmemory.h"
#include "xutils.h"

/**
  @brief
    Protected implementation of xstr.
*/
struct xstr_impl {
  char* str;  ///< @brief Pointer to c-string.
  int len; ///< @brief Length of the xstr.
  int mem_size; ///< @brief Memory usage.
};

xstr xstr_new() {
  xstr xs = xmalloc_ty(1, struct xstr_impl);
  xs->mem_size = 16;
  xs->str = xmalloc_ty(xs->mem_size, char);
  xs->str[0] = '\0';
  xs->len = 0;
  return xs;
}

void xstr_delete(xstr xs) {
  xfree(xs->str);
  xfree(xs);
}

const char* xstr_get_cstr(xstr xs) {
  return xs->str;
}

/**
  @brief
    Ensure that the xstr has got enough memroy.

  @param xstr
    The xstr that requires enough memory.
  @param mem_size
    The minimum required memroy size.
*/
static void ensure_mem_size(xstr xs, int mem_size) {
  if (mem_size > xs->mem_size) {

    // Note that we allocated mem_size * 2, which will reduce
    // calls to this function.
    // Think about the case where many chars are appended to an xstr.
    xs->mem_size = mem_size * 2;
    xs->str = xrealloc(xs->str, xs->mem_size);
  }
}

void xstr_set_cstr(xstr xs, const char* cs) {
  int cs_len = strlen(cs);
  xs->len = cs_len;

  // note the "+1", because of trailing '\0'
  ensure_mem_size(xs, xs->len + 1);
  strcpy(xs->str, cs);
}

int xstr_len(xstr xs) {
  return xs->len;
}

void xstr_append_char(xstr xs, char ch) {
  if (ch != '\0') {
    ensure_mem_size(xs, xs->len + 1);
    xs->str[xs->len] = ch;
    xs->len++;
    xs->str[xs->len] = '\0';
  }
}

// for the case where cs_len is pre-calculated
// prevent calling strlen(cs) more than once
static void xstr_append_cstr_len_precalculated(xstr xs, char* cs, int cs_len) {
  if (cs_len > 0) {
    ensure_mem_size(xs, xs->len + cs_len + 1);

    // Note: we did not use strcat(xs->str, ...), but used strcpy(xs->str + xs->len),
    // because we know exactly where new string should be appended, rather than scanning xs->str
    // to find the end point. This might enhance performance.
    strcpy(xs->str + xs->len, cs);
    xs->len += cs_len;
  }
}

void xstr_append_cstr(xstr xs, char* cs) {
  int cs_len = strlen(cs);
  xstr_append_cstr_len_precalculated(xs, cs, cs_len);
}


int xstr_printf(xstr xs, const char* fmt, ...) {
  va_list argp;
  const char* p;
  int cnt = 0;
  int ival;
  char* sval;
  char cval;
  char buf[16]; // for xitoa
  int str_len;

  va_start(argp, fmt);

  for (p = fmt; *p != '\0' && cnt >= 0; p++) {
    if (*p != '%') {
      cnt++;
      xstr_append_char(xs, *p);
      continue;
    }

    // otherwise, found an '%', go to next char
    p++;
    switch(*p) {
    case 'c':
      // Note, we use 'int' here instead of 'char', because 'char' type uses same number of bytes as 'int' on stack
      cval = va_arg(argp, int);
      xstr_append_char(xs, cval);
      cnt++;
      break;

    case 'd':
      ival = va_arg(argp, int);
      xitoa(ival, buf, 10);
      printf("num str=%s len=%d\n", buf, strlen(buf));
      str_len = strlen(buf);
      cnt += str_len;
      xstr_append_cstr_len_precalculated(xs, buf, str_len);
      break;

    case 's':
      sval = va_arg(argp, char *);
      str_len = strlen(sval);
      printf("str_len = %d, s=%s\n", str_len, sval);
      cnt += str_len;
      xstr_append_cstr_len_precalculated(xs, sval, str_len);
      break;

    case '%':
      cnt++;
      break;

    default:
      xstr_append_cstr(xs, "** FORMAT ERROR ***");
      cnt = -1;
      break;
    }
  }
  va_end(argp);
  return cnt;
}


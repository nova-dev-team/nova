#include <string.h>
#include <stdarg.h>
#include <assert.h>
#include <stdlib.h>

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

xstr xstr_new_from_cstr(const char* cs) {
  xstr xs = xmalloc_ty(1, struct xstr_impl);
  xs->len = strlen(cs);
  xs->mem_size = xs->len + 4;
  xs->str = xmalloc_ty(xs->mem_size, char);
  strcpy(xs->str, cs);
  return xs;
}

xstr xstr_substr(xstr xs, int start) {
  if (start >= xstr_len(xs)) {
    return xstr_new();
  } else {
    return xstr_new_from_cstr(xs->str + start);
  }
}

xstr xstr_substr2(xstr xs, int start, int len) {
  int orig_len = xstr_len(xs);
  xstr subxs;
  if (start >= orig_len) {
    subxs = xstr_new();
  } else if (start + len >= orig_len) {
    subxs = xstr_new_from_cstr(xs->str + start);
  } else {
    int i;
    subxs = xstr_new();
    const char* orig_cstr = xstr_get_cstr(xs);
    for (i = 0; i < len; i++) {
      xstr_append_char(subxs, orig_cstr[start + i]);
    }
  }
  return subxs;
}

void xstr_delete(void* xs) {
  xfree(((xstr) xs)->str);
  xfree(xs);
}

const char* xstr_get_cstr(const xstr xs) {
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

    // Note that we allocated mem_size * 2 + 2, which will reduce
    // calls to this function.
    // Think about the case where many chars are appended to an xstr.
    xs->mem_size = mem_size * 2 + 2;
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

void xstr_add_prefix_cstr(xstr xs, const char* prefix) {
  int prefix_len = strlen(prefix);
  // note the "+1", because of the trailing '\0'
  ensure_mem_size(xs, xs->len + prefix_len + 1);
  // note the "+1", because we also copy the '\0'
  memmove(xs->str + prefix_len, xs->str, xs->len + 1);
  memcpy(xs->str, prefix, prefix_len);
  xs->len += prefix_len;
}

void xstr_append_char(xstr xs, char ch) {
  if (ch != '\0') {
    // +2 because of the trailing '\0' and the new char
    ensure_mem_size(xs, xs->len + 2);
    xs->str[xs->len] = ch;
    xs->len++;
    xs->str[xs->len] = '\0';
  }
}

// for the case where cs_len is pre-calculated
// prevent calling strlen(cs) more than once
static void xstr_append_cstr_len_precalculated(xstr xs, const char* cs, int cs_len) {
  if (cs_len > 0) {
    ensure_mem_size(xs, xs->len + cs_len + 1);  // +1 because of the trailing '\0'

    // Note: we did not use strcat(xs->str, ...), but used strcpy(xs->str + xs->len),
    // because we know exactly where new string should be appended, rather than scanning xs->str
    // to find the end point. This might enhance performance.
    strcpy(xs->str + xs->len, cs);
    xs->len += cs_len;
  }
}

void xstr_append_cstr(xstr xs, const char* cs) {
  int cs_len = strlen(cs);
  xstr_append_cstr_len_precalculated(xs, cs, cs_len);
}


int xstr_printf(xstr xs, const char* fmt, ...) {
  va_list argp;
  int cnt;
  va_start(argp, fmt);
  cnt = xstr_vprintf(xs, fmt, argp);
  va_end(argp);
  return cnt;
}

int xstr_vprintf(xstr append_to, const char* fmt, va_list va) {
  char* ret;
  int cnt = vasprintf(&ret, fmt, va);
  if (cnt >= 0) {
    xstr_append_cstr(append_to, ret);
  }
  free(ret);
  return cnt;
}

xbool xstr_startwith_cstr(xstr xs, const char* head) {
  int i;
  for (i = 0; xs->str[i] != '\0' && head[i] != '\0'; i++) {
    if (xs->str[i] != head[i]) {
      return XFALSE;
    }
  }
  return head[i] == '\0';
}

xbool xstr_endwith_cstr(xstr xs, const char* tail) {
  int i;
  int tail_len = strlen(tail);
  int xs_len = xstr_len(xs);
  if (tail_len > xs_len) {
    return XFALSE;
  }
  for (i = 0; i < tail_len; i++) {
    if (xs->str[xs_len - i - 1] != tail[tail_len - i - 1]) {
      return XFALSE;
    }
  }
  return XTRUE;
}

xstr xstr_copy(xstr orig) {
  xstr new_str = xstr_new();
  xstr_set_cstr(new_str, xstr_get_cstr(orig));
  return new_str;
}

char xstr_last_char(xstr xs) {
  if (xs->len == 0) {
    return '\0';
  } else {
    return xs->str[xs->len - 1];
  }
}

xbool xstr_eql_cstr(xstr xs, const char* cstr) {
  if (strcmp(xstr_get_cstr(xs), cstr) == 0) {
    return XTRUE;
  } else {
    return XFALSE;
  }
}

xbool xstr_eql(xstr xstr1, xstr xstr2) {
  if (strcmp(xstr_get_cstr(xstr1), xstr_get_cstr(xstr2)) == 0) {
    return XTRUE;
  } else {
    return XFALSE;
  }
}

void xstr_strip(xstr xs, char* strip_set) {
  int new_begin = 0;
  int new_end = xs->len;  // exclusive end point
  int i;
  xbool should_strip;
  char* stripped_cstr;

  while (new_begin < new_end) {
    should_strip = XFALSE;
    for (i = 0; strip_set[i] != '\0'; i++) {
      if (xs->str[new_begin] == strip_set[i]) {
        should_strip = XTRUE;
        break;
      }
    }
    if (should_strip == XTRUE) {
      new_begin++;
    } else {
      break;
    }
  }

  while (new_begin < new_end) {
    should_strip = XFALSE;
    for (i = 0; strip_set[i] != '\0'; i++) {
      if (xs->str[new_end - 1] == strip_set[i]) {
        should_strip = XTRUE;
        break;
      }
    }
    if (should_strip == XTRUE) {
      new_end--;
    } else {
      break;
    }
  }

  stripped_cstr = xmalloc_ty(new_end - new_begin + 1, char);
  memcpy(stripped_cstr, xs->str + new_begin, new_end - new_begin);
  stripped_cstr[new_end - new_begin] = '\0';
  xstr_set_cstr(xs, stripped_cstr);
  xfree(stripped_cstr);
}

int xstr_compare(xstr xs1, xstr xs2) {
  return strcmp(xstr_get_cstr(xs1), xstr_get_cstr(xs2));
}


xvec xstr_split_xvec(xstr xs, const char* sep) {
  xvec xv = xvec_new(xstr_delete);
  xstr seg = xstr_new();
  int i, j;
  if (sep[0] == '\0') {
    for (i = 0; i < xs->len; i++) {
      xstr_append_char(seg, xs->str[i]);
      xvec_push_back(xv, xstr_copy(seg));
      xstr_set_cstr(seg, "");
    }
  } else {
    for (i = 0; i < xs->len; i++) {
      // ENHANCE: use KMP here (conditionally, if sep is long enough)
      xbool is_sep = XTRUE;
      for (j = 0; sep[j] != '\0' && xs->str[i + j] != '\0'; j++) {
        if (sep[j] != xs->str[i + j]) {
          is_sep = XFALSE;
          break;
        }
      }
      if (is_sep == XTRUE) {
        if (seg->len > 0) {
          xvec_push_back(xv, xstr_copy(seg));
          xstr_set_cstr(seg, "");
        }
        assert(j > 0);
        i += j - 1; // i will still be added 1 by the for loop, so actually we have increased i by j, in this loop, discarding the separator
      } else {
        xstr_append_char(seg, xs->str[i]);
      }
    }
    if (seg->len > 0) {
      xvec_push_back(xv, xstr_copy(seg));
      xstr_set_cstr(seg, "");
    }
  }
  xstr_delete(seg);
  return xv;
}

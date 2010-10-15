#include <assert.h>

#include "xmemory.h"
#include "xjson.h"
#include "xstr.h"
#include "xvec.h"
#include "xhash.h"

typedef enum {
  XJSON_OBJECT,
  XJSON_ARRAY,
  XJSON_STRING,
  XJSON_INT,
  XJSON_DOUBLE,
  XJSON_TRUE,
  XJSON_FALSE,
  XJSON_NULL
} xjson_type_enum;

struct xjson_impl {
  xjson_type_enum type;
  union {
    xhash obj_hash;
    xvec array_vec;
    xstr xstr_value;
    int int_value;
    double double_value;
  };
};

xjson xjson_new() {
  xjson xj = xmalloc_ty(1, struct xjson_impl);
  // by default, newly created json object is null
  xj->type = XJSON_NULL;
  return xj;
}

// helper function, delete the content of json object
// it is used to delete old content, when the object is set to new value
static void xjson_delete_content(xjson xj) {
  switch (xj->type) {
  case XJSON_OBJECT:
    xhash_delete(xj->obj_hash);
    break;
  case XJSON_ARRAY:
    xvec_delete(xj->array_vec);
    break;
  case XJSON_STRING:
    xstr_delete(xj->xstr_value);
    break;
  case XJSON_INT:
  case XJSON_DOUBLE:
  case XJSON_TRUE:
  case XJSON_FALSE:
  case XJSON_NULL:
    // non-pointer value, do nothing
    break;
  default:
    // impossible to reach here
    assert(0);
    break;
  }
  // for safety, set the json object to null
  xj->type = XJSON_NULL;
}

void xjson_delete(xjson xj) {
  xjson_delete_content(xj);
  xfree(xj);
}

void xjson_set_null(xjson xj) {
  xjson_delete_content(xj);
  xj->type = XJSON_NULL;
}

void xjson_set_true(xjson xj) {
  xjson_delete_content(xj);
  xj->type = XJSON_TRUE;
}

void xjson_set_false(xjson xj) {
  xjson_delete_content(xj);
  xj->type = XJSON_FALSE;
}

void xjson_set_int(xjson xj, int val) {
  xjson_delete_content(xj);
  xj->type = XJSON_INT;
  xj->int_value = val;
}

void xjson_set_double(xjson xj, double val) {
  xjson_delete_content(xj);
  xj->type = XJSON_DOUBLE;
  xj->double_value = val;
}

void xjson_set_xstr(xjson xj, xstr xs) {
  xjson_set_cstr(xj, xstr_get_cstr(xs));
}

void xjson_set_cstr(xjson xj, const char* cstr) {
  xjson_delete_content(xj);
  xj->type = XJSON_STRING;
  xj->xstr_value = xstr_new_from_cstr(cstr);
}

void xjson_array_vec_free_func(void* ptr) {
  xjson_delete((xjson) ptr);
}

void xjson_set_array(xjson xj) {
  xjson_delete_content(xj);
  xj->type = XJSON_ARRAY;
  xj->array_vec = xvec_new(xjson_array_vec_free_func);
}

static void escape_json_string(xstr json_xs, xstr holder) {
  const char* p = xstr_get_cstr(json_xs);
  xstr_set_cstr(holder, "");
  xstr_append_char(holder, '"');
  while (*p != '\0') {
    switch (*p) {
    case '\b':
      xstr_append_cstr(holder, "\\b");
      break;
    case '\n':
      xstr_append_cstr(holder, "\\n");
      break;
    case '\r':
      xstr_append_cstr(holder, "\\r");
      break;
    case '\'':
      xstr_append_cstr(holder, "\\\'");
      break;
    case '\"':
      xstr_append_cstr(holder, "\\\"");
      break;
    case '/':
      xstr_append_cstr(holder, "\\/");
      break;
    case '\\':
      xstr_append_cstr(holder, "\\\\");
      break;
    default:
      xstr_append_char(holder, *p);
    }
    p++;
  }
  xstr_append_char(holder, '"');
}

void xjson_to_xstr(xjson xj, xstr xs) {
  xstr_set_cstr(xs, "");
  switch (xj->type) {
  case XJSON_OBJECT:
    // TODO
    break;
  case XJSON_ARRAY:
    // TODO
    break;
  case XJSON_STRING:
    escape_json_string(xj->xstr_value, xs);
    break;
  case XJSON_INT:
    xstr_printf(xs, "%d", xj->int_value);
    break;
  case XJSON_DOUBLE:
    xstr_printf(xs, "%lg", xj->double_value);
    break;
  case XJSON_TRUE:
    xstr_set_cstr(xs, "true");
    break;
  case XJSON_FALSE:
    xstr_set_cstr(xs, "false");
    break;
  case XJSON_NULL:
    xstr_set_cstr(xs, "null");
    break;
  default:
    // impossible to reach here
    assert(0);
    break;
  }
}


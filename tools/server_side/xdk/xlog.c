#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <time.h>
#include <stdarg.h>
#include <assert.h>
#include <sys/stat.h>
#include <unistd.h>

#include "xstr.h"
#include "xmemory.h"
#include "xlog.h"
#include "xoption.h"
#include "xutils.h"
#include "xsys.h"
#include "xhash.h"

#define DEFAULT_XLOGGER_PATTERN "[%Y.%m.%d %H:%M:%S] [%e] %l"

static const char* log_level_names[] = {"fatal", "error", "warning", "info", "debug", "level5", "level6", "level7"};

// this is the mutex which protects the logging system
static pthread_mutex_t logging_lock = PTHREAD_MUTEX_INITIALIZER;

// logger model
typedef struct {
  char* name;  // name of the logger (need to be free'd)

  char* pattern; // the logger pattern (need to be free'd)
  // available patterns:
  //
  // %%: % itself
  // %f: the source code file
  // %i: the source code line
  // %e: the log level, "fatal", "error", .. "level5", .. etc
  // %v: the log level, as a number, 0, ... 7
  // %Y.%m.%d %H:%M:%S: like in strftime
  // %l: the logging text (will be separated to several lines, each line as a single log)
  //
  // pattern by default: [%Y.%m.%d %H:%M:%S] [%e] %l
  //
  // other char will be outputed as given
  // an end-of-line will be attached to each line

  char* fn; // file name for the logger file
  // a series of files fn.1, fn.2, .. etc will be created, when the base log file is too big

  FILE* fp; // pointer to the log file pointer

  xbool enabled;  // whether the logger is enabled
  int levels; // a flag indicating which levels should the logger run, by default, fatal~info
  // use "enabled == XTRUE && ((log_levels >> level) & 0x1) == 1" to check if need to print log

  xbool special; // if it is a special logger (stdout, stderr, etc)

  long max_size;  // size threshold of a log file (not used for special loggers)
  int history;  // max history count of a log (not used for special loggers)
} xlogger_ty;

// a hash table containing all the loggers
static xhash g_xloggers = NULL;



static char* get_log_basename(char* argv0) {
  int i = 0;
  char sep = xsys_fs_sep_char;
  char* log_basename = NULL;
  for (i = strlen(argv0) - 1; i >= 0 && argv0[i] != sep; i--) {
  }
  if (i == 0) {
    log_basename = argv0;
  } else {
    // got sep char
    log_basename = argv0 + i + 1;
  }
  return log_basename;
}


static void destroy_xlogger(xlogger_ty* lg) {
  if (lg->special == XFALSE) {
    fclose(lg->fp);
    lg->fp = NULL;
  }
  if (lg->fn != NULL) {
    free(lg->fn);
  }
  free(lg->name);  // also used as key, created by strdup, so we use free instead of xfree here
  free(lg->pattern);
  xfree(lg);  // xfree the logger, because it is initialized using xmalloc()
}

static void xloggers_free(void* key, void* value) {
  // the key is also lg->name, so we do nothing on key
  destroy_xlogger((xlogger_ty *) value);
}

// make sure the logging system is initialized, ready to use
static inline xsuccess xlog_ensure_initialized() {
  if (g_xloggers == NULL) {
    g_xloggers = xhash_new(xhash_hash_cstr, xhash_eql_cstr, xloggers_free);
  }
  return XSUCCESS;
}


void xlog_finalize() {
  xlog_ensure_initialized();
  pthread_mutex_lock(&logging_lock);
  xhash_delete(g_xloggers);
  g_xloggers = NULL;
  pthread_mutex_unlock(&logging_lock);
}

xsuccess xlog_init(int argc, char* argv[]) {
  xsuccess ret = XSUCCESS;
  xoption xopt = xoption_new();
  xstr log_fn = xstr_new();

  xoption_parse(xopt, argc, argv);
  xlog_add("stderr", NULL);
  if (xoption_has(xopt, "log-dir")) {
    xstr log_folder = xstr_new();
    xstr log_fn_abs = xstr_new();
    char* log_basename = get_log_basename(argv[0]);
    const int cwd_buf_size = 256;
    char cwd_buffer[cwd_buf_size];
    char* log_dir = (char *) xoption_get(xopt, "log-dir");

    getcwd(cwd_buffer, cwd_buf_size);
    xjoin_path_cstr(log_folder, cwd_buffer, log_dir);
    xstr_printf(log_fn_abs, "%s%c%s.log", xstr_get_cstr(log_folder), xsys_fs_sep_char, log_basename);
    xfilesystem_normalize_abs_path(xstr_get_cstr(log_fn_abs), log_fn);

    xstr_delete(log_fn_abs);
    xstr_delete(log_folder);
  } else {
    // no log given, log will be in same folder as binary
    xstr_printf(log_fn, "%s.log", argv[0]);
  }

  if (xoption_has(xopt, "log-screenonly") == XFALSE) {
    xlog_add("default-log-file", xstr_get_cstr(log_fn));
  }

  if (xoption_has(xopt, "log-level")) {
    int i;
    int log_level_flag = 0;
    int log_level = atoi(xoption_get(xopt, "log-level"));
    for (i = 0; i <= log_level; i++) {
      log_level_flag |= (1 << i);
    }
    xlog_ctl("stderr", XLOG_CTL_SET_LEVELS, log_level_flag);
    xlog_ctl("default-log-file", XLOG_CTL_SET_LEVELS, log_level_flag);
  }

  if (xoption_has(xopt, "log-maxsize")) {
    int log_max_size = xfilesystem_parse_filesize(xoption_get(xopt, "log-maxsize"));
    xlog_ctl("default-log-file", XLOG_CTL_SET_SIZE, log_max_size);
  }

  if (xoption_has(xopt, "log-history")) {
    int log_history = atoi(xoption_get(xopt, "log-history"));
    xlog_ctl("default-log-file", XLOG_CTL_SET_HISTORY, log_history);
  }

  xstr_delete(log_fn);
  xoption_delete(xopt);
  return ret;
}


static xsuccess xlog_ctl_add(const char* logger_name, const char* log_fn) {
  xsuccess ret = xlog_ensure_initialized();
  xlogger_ty* lg;
  if (ret == XSUCCESS) {
    // check if name already used
    lg = xhash_get(g_xloggers, logger_name);
    if (lg != NULL) {
      // name already used
      ret = XFAILURE;
    }
  }
  if (ret == XSUCCESS) {
    lg = xmalloc_ty(1, xlogger_ty);
    lg->name = strdup(logger_name);
    lg->pattern = strdup(DEFAULT_XLOGGER_PATTERN);
    lg->fn = NULL;  // no file
    lg->fp = NULL;
    if (strcmp(logger_name, "stderr") == 0) {
      lg->fp = stderr;
      lg->special = XTRUE;
    } else if (strcmp(logger_name, "stdout") == 0) {
      lg->fp = stdout;
      lg->special = XTRUE;
    } else {
      lg->special = XFALSE;
      lg->fn = strdup(log_fn);
      lg->fp = fopen(lg->fn, "a");
      if (lg->fp == NULL) {
        // failed to open file
        ret = XFAILURE;
      }
    }
    lg->enabled = XTRUE;
    lg->levels = 0xF; // fatal,error,warning,info
    lg->max_size = 10 * 1024 * 1024;  // default value
    lg->history = 5;  // default value
    if (ret == XSUCCESS) {
      xhash_put(g_xloggers, lg->name, lg);
    } else {
      destroy_xlogger(lg);
    }
  }
  return ret;
}

static xsuccess xlog_ctl_remove(const char* logger_name) {
  xsuccess ret = xlog_ensure_initialized();
  if (ret == XSUCCESS) {
    xhash_remove(g_xloggers, logger_name);
  }
  return ret;
}

static xsuccess xlog_ctl_enable(const char* logger_name) {
  xsuccess ret = xlog_ensure_initialized();
  xlogger_ty* lg;
  if (ret == XSUCCESS) {
    // check if name already used
    lg = xhash_get(g_xloggers, logger_name);
    if (lg != NULL) {
      lg->enabled = XTRUE;
    } else {
      ret = XFAILURE;
    }
  }
  return ret;
}

static xsuccess xlog_ctl_disable(const char* logger_name) {
  xsuccess ret = xlog_ensure_initialized();
  xlogger_ty* lg;
  if (ret == XSUCCESS) {
    // check if name already used
    lg = xhash_get(g_xloggers, logger_name);
    if (lg != NULL) {
      lg->enabled = XFALSE;
    } else {
      ret = XFAILURE;
    }
  }
  return ret;
}

static xsuccess xlog_ctl_set_file(const char* logger_name, const char* fn) {
  xsuccess ret = xlog_ensure_initialized();
  FILE* fp = fopen(fn, "a");
  xlogger_ty* lg;
  if (fp == NULL) {
    // failed to open file
    ret = XFAILURE;
  }
  if (ret == XSUCCESS) {
    // check if name already used
    lg = xhash_get(g_xloggers, logger_name);
    if (lg != NULL) {
      if (lg->fn != NULL) {
        free(lg->fn);
      }
      lg->fn = strdup(fn);
      lg->fp = fp;
      lg->special = XFALSE;
    } else {
      ret = XFAILURE;
    }
  }
  return ret;
}

static xsuccess xlog_ctl_set_levels(const char* logger_name, int levels) {
  xsuccess ret = xlog_ensure_initialized();
  xlogger_ty* lg;
  if (ret == XSUCCESS) {
    // check if name already used
    lg = xhash_get(g_xloggers, logger_name);
    if (lg != NULL) {
      lg->levels = levels;
    } else {
      ret = XFAILURE;
    }
  }
  return ret;
}

static xsuccess xlog_ctl_set_history(const char* logger_name, int history) {
  xsuccess ret = xlog_ensure_initialized();
  xlogger_ty* lg;
  if (ret == XSUCCESS) {
    // check if name already used
    lg = xhash_get(g_xloggers, logger_name);
    if (lg != NULL) {
      lg->history = history;
    } else {
      ret = XFAILURE;
    }
  }
  return ret;
}

static xsuccess xlog_ctl_set_size(const char* logger_name, int max_size) {
  xsuccess ret = xlog_ensure_initialized();
  xlogger_ty* lg;
  if (ret == XSUCCESS) {
    // check if name already used
    lg = xhash_get(g_xloggers, logger_name);
    if (lg != NULL) {
      lg->max_size = max_size;
    } else {
      ret = XFAILURE;
    }
  }
  return ret;
}

xsuccess xlog_ctl(const char* logger_name, xlog_ctl_action action, ...) {
  va_list argp;
  xsuccess ret = XSUCCESS;
  char* sval = NULL;
  int ival = 0;

  va_start(argp, action);
  pthread_mutex_lock(&logging_lock);

  switch(action) {
  case XLOG_CTL_ADD:
    sval = va_arg(argp, char *);
    ret = xlog_ctl_add(logger_name, sval);
    break;
  case XLOG_CTL_REMOVE:
    ret = xlog_ctl_remove(logger_name);
    break;
  case XLOG_CTL_ENABLE:
    ret = xlog_ctl_enable(logger_name);
    break;
  case XLOG_CTL_DISABLE:
    ret = xlog_ctl_disable(logger_name);
    break;
  case XLOG_CTL_SET_FILE:
    sval = va_arg(argp, char *);
    ret = xlog_ctl_set_file(logger_name, sval);
    break;
  case XLOG_CTL_SET_LEVELS:
    ival = va_arg(argp, int);
    ret = xlog_ctl_set_levels(logger_name, ival);
    break;
  case XLOG_CTL_SET_SIZE:
    ival = va_arg(argp, int);
    ret = xlog_ctl_set_size(logger_name, ival);
    break;
  case XLOG_CTL_SET_HISTORY:
    ival = va_arg(argp, int);
    ret = xlog_ctl_set_history(logger_name, ival);
    break;
  default:
    // action not understood
    fprintf(stderr, "xlog_clt action not understood! action id = %d\n", action);
    ret = XFAILURE;
    break;
  }

  pthread_mutex_unlock(&logging_lock);
  va_end(argp);
  return ret;
}

// this is a container for passing multiple params into do_log_visitor() function
// it's ugly, but it works
typedef struct {
  int level;
  const char* code_fn;
  int code_ln;
  xstr log_line; // a single line in log (no \n)
} do_log_visitor_param;


static xsuccess rotate_log(xlogger_ty* lg) {
  xsuccess ret = XSUCCESS;
  xstr current_log_fn = xstr_new_from_cstr(lg->fn);
  int i;
  fclose(lg->fp);
  lg->fp = NULL;

  for (i = 1; i <= lg->history; i++) {
    xstr detect_log_fn = xstr_new();
    xstr_printf(detect_log_fn, "%s.%d", xstr_get_cstr(current_log_fn), i);
    if (xfilesystem_exists(xstr_get_cstr(detect_log_fn)) == XFALSE) {
      break;
    }
    xstr_delete(detect_log_fn);
  }

  if (i <= lg->history) {
    xstr history_log_fn = xstr_new();
    xstr_printf(history_log_fn, "%s.%d", xstr_get_cstr(current_log_fn), i);
    rename(xstr_get_cstr(current_log_fn), xstr_get_cstr(history_log_fn));
    xstr_delete(history_log_fn);
  } else {
    xstr history_log_fn = xstr_new();
    xstr_printf(history_log_fn, "%s.%d", xstr_get_cstr(current_log_fn), 1);
    remove(xstr_get_cstr(history_log_fn));
    for (i = 2; i <= lg->history; i++) {
      xstr from_fn = xstr_new();
      xstr to_fn = xstr_new();
      xstr_printf(from_fn, "%s.%d", xstr_get_cstr(current_log_fn), i);
      xstr_printf(to_fn, "%s.%d", xstr_get_cstr(current_log_fn), i - 1);
      rename(xstr_get_cstr(from_fn), xstr_get_cstr(to_fn));
      xstr_delete(from_fn);
      xstr_delete(to_fn);
    }
    xstr_printf(history_log_fn, "%s.%d", xstr_get_cstr(current_log_fn), lg->history);
    rename(xstr_get_cstr(current_log_fn), xstr_get_cstr(history_log_fn));
    xstr_delete(history_log_fn);
  }
  xstr_delete(current_log_fn);

  lg->fp = fopen(lg->fn, "a");
  return ret;
}

static xbool do_log_visitor(const void* key, void* value, void* args) {
  do_log_visitor_param* p_param = (do_log_visitor_param *) args;
  int level = p_param->level;
  xlogger_ty* lg = (xlogger_ty *) value;
  if (lg->special == XFALSE) {
    assert(lg->fp != NULL);
  }
  if (lg->enabled == XTRUE && ((lg->levels >> level) & 0x1) == 1) {
    char* p = lg->pattern;
    char timestr_buf[32];
    struct tm* tm_struct;
    time_t tm_val = time(NULL);
    tm_struct = localtime(&tm_val);

    assert(p != NULL);
    while (*p != '\0') {
      if (*p == '%') {
        p++;
        switch (*p) {
        case 'Y':
          strftime(timestr_buf, sizeof(timestr_buf), "%Y", tm_struct);
          fprintf(lg->fp, "%s", timestr_buf);
          break;
        case 'm':
          strftime(timestr_buf, sizeof(timestr_buf), "%m", tm_struct);
          fprintf(lg->fp, "%s", timestr_buf);
          break;
        case 'd':
          strftime(timestr_buf, sizeof(timestr_buf), "%d", tm_struct);
          fprintf(lg->fp, "%s", timestr_buf);
          break;
        case 'H':
          strftime(timestr_buf, sizeof(timestr_buf), "%H", tm_struct);
          fprintf(lg->fp, "%s", timestr_buf);
          break;
        case 'M':
          strftime(timestr_buf, sizeof(timestr_buf), "%M", tm_struct);
          fprintf(lg->fp, "%s", timestr_buf);
          break;
        case 'S':
          strftime(timestr_buf, sizeof(timestr_buf), "%S", tm_struct);
          fprintf(lg->fp, "%s", timestr_buf);
          break;
        case '%':
          fprintf(lg->fp, "%%");
          break;
        case 'f':
          fprintf(lg->fp, "%s", p_param->code_fn);
          break;
        case 'i':
          fprintf(lg->fp, "%d", p_param->code_ln);
          break;
        case 'e':
          fprintf(lg->fp, "%s", log_level_names[p_param->level]);
          break;
        case 'v':
          fprintf(lg->fp, "%d", p_param->level);
          break;
        case 'l':
          fprintf(lg->fp, "%s", xstr_get_cstr(p_param->log_line));
          break;
        default:
          fprintf(lg->fp, "(*** bad format '%%%c' ***)", *p);
          break;
        }
      } else {
        fprintf(lg->fp, "%c", *p);
      }
      p++;
    }
    fprintf(lg->fp, "\n");
    fflush(lg->fp); // flush for every line
    if (lg->special == XFALSE && lg->fn != NULL) {
      struct stat st;
      if ((stat(lg->fn, &st)) == 0 && st.st_size > lg->max_size) {
        rotate_log(lg);
      }
    }
  }
  return XTRUE;
}

xsuccess xlog_real(int level, const char* code_fn, int code_ln, const char* fmt, ...) {
  xsuccess ret = xlog_ensure_initialized();
  if (ret == XSUCCESS) {
    va_list argp;
    do_log_visitor_param param;
    xstr all_logs = xstr_new();
    xvec lines;
    int i;
    va_start(argp, fmt);
    pthread_mutex_lock(&logging_lock);
    xstr_vprintf(all_logs, fmt, argp);
    lines = xstr_split_xvec(all_logs, "\n");
    param.level = level;
    param.code_fn = code_fn;
    param.code_ln = code_ln;
    //assert (xhash_size(g_xloggers) > 0);
    for (i = 0; i < xvec_size(lines); i++) {
      // call the function for each line
      param.log_line = xvec_get(lines, i);
      xhash_visit(g_xloggers, do_log_visitor, &param);
    }
    xvec_delete(lines);
    pthread_mutex_unlock(&logging_lock);
    va_end(argp);
    xstr_delete(all_logs);
  }
  return ret;
}

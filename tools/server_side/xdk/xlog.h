#ifndef XLOG_H_
#define XLOG_H_

#include <stdio.h>
#include <stdarg.h>

#include "xdef.h"

/**
  @author
    Santa Zhang

  @file
    xlog.h

  @brief
    Provide logging utility.
*/

/**
  @brief
    The default logging file.
*/
#define XLOG_DEFAULT_FILE stderr

/**
  @brief
    Disable logging. No logs will be shown.
*/
#define XLOG_DISABLED     -1

/**
  @brief
    Logging level of fatal errors. The system will typically halt on fatal errors.
*/
#define XLOG_FATAL        0

/**
  @brief
    Logging level of errors. The system will run incorrectly on these errors.
*/
#define XLOG_ERROR        1

/**
  @brief
    Logging level of warnings.
*/
#define XLOG_WARNING      2

/**
  @brief
    Logging level of informations.
*/
#define XLOG_INFO         3

/**
  @brief
    Logging level of debug messages.
*/
#define XLOG_DEBUG        4

/**
  @brief
    Logging level 5. User defined.
*/
#define XLOG_LEVEL5       5

/**
  @brief
    Logging level 6. User defined.
*/
#define XLOG_LEVEL6       6

/**
  @brief
    Logging level 7. User defined.
*/
#define XLOG_LEVEL7       7

/**
  @brief
    The actions to control the logging system.
*/
typedef enum {
  XLOG_CTL_ADD,           ///< @brief Create a new logger.
  XLOG_CTL_REMOVE,        ///< @brief Remove a logger.
  XLOG_CTL_ENABLE,        ///< @brief Enable a logger.
  XLOG_CTL_DISABLE,       ///< @brief Disable a logger.
  XLOG_CTL_SET_FILE,      ///< @brief Set the logger's file pointer.
  XLOG_CTL_SET_LEVELS,    ///< @brief Set the logger's log levels.
  XLOG_CTL_SET_SIZE,      ///< @brief Set the logger's max log size.
  XLOG_CTL_SET_HISTORY    ///< @brief Set the logger's history count.
} xlog_ctl_action;


/**
  @brief
    Initialize logging system, parse command line args concerning the log system.

  The logging system uses the following parameters:<br>
  --log-level=2<br>
  Loging levels are: 0 (fatal), 1 (error), 2 (warning), 3 (info), 4 (debug), 5~7(user defined).<br>

  --log-dir=.<br>
  Where all the logs and log history will be saved.<br>

  --log-maxsize=10M<br>
  Max file size of the log, if it is bigger than this, log file is moved into history.<br>

  --log-history=5<br>
  Max number of 'old' logs to be kept.

  --log-screenonly<br>
  Only write to screen. This is by default disabled.

  @param argc
    The number of command line args. This is provided to main().
  @param argv
    The array of command line args. This is provided to main().

  @deprecated
    This function is deprecated, use xlog_add() instead.

  @return
    Whether the logging system was correctly started.

*/
xsuccess xlog_init(int argc, char* argv[]);

/**
  @brief
    General interface to control the logging system.

  @param logger_name
    The logger that we are dealing with.
  @param action
    The control action we gonna take.

  @return
    If the control action is successful.
*/
xsuccess xlog_ctl(const char* logger_name, xlog_ctl_action action, ...);

/**
  @brief
    Create a new logger.

  This is actually a macro around xlog_ctl(), using the @c XLOG_CTL_ADD action.

  @param logger_name
    Name of the new logger.

  @return
    Whether the logger is successfully created.
*/
#define xlog_add(logger_name) xlog_ctl(logger_name, XLOG_CTL_ADD)


/**
  @brief
    Remove an existing logger.

  This is actually a macro around xlog_ctl(), using the @c XLOG_CTL_REMOVE action.

  @param logger_name
    Name of the new logger.

  @return
    Whether the logger is successfully removed.
*/
#define xlog_remove(logger_name) xlog_ctl(logger_name, XLOG_CTL_REMOVE)


/**
  @brief
    Write logs of a certain logging level.

  @param level
    The logging level, could be 0 (fatal), 1 (error), 2 (warning), 3 (info), 4 (debug), 5~7 (user defined).
  @param fmt
    The formatting string.
  @param code_fn
    The code file name containing logging code.
  @param code_ln
    The line number of logging code.

  @warning
    This function is not intended to be used directly. Use xlog() instead.

  @return
    Whether the log was written successfully.
*/
xsuccess xlog_real(int level, const char* code_fn, int code_ln, const char* fmt, ...);

/**
  @brief
    Write logs of a certain logging level.

  @param level
    The logging level, could be 0 (fatal), 1 (error), 2 (warning), 3 (info), 4 (debug), 5~7 (user defined).
  @param fmt
    The formatting string.

  @return
    Whether the log was written successfully.
*/
#define xlog(level, fmt, ...) xlog_real(level, __FILE__, __LINE__, fmt, ## __VA_ARGS__)

/**
  @brief
    Helper macro, make you type less.
*/
#define xlog_fatal(fmt, ...)    xlog(XLOG_FATAL, fmt, ## __VA_ARGS__)

/**
  @brief
    Helper macro, make you type less.
*/
#define xlog_error(fmt, ...)    xlog(XLOG_ERROR, fmt, ## __VA_ARGS__)

/**
  @brief
    Helper macro, make you type less.
*/
#define xlog_warning(fmt, ...)  xlog(XLOG_WARNING, fmt, ## __VA_ARGS__)

/**
  @brief
    Helper macro, make you type less.
*/
#define xlog_info(fmt, ...)     xlog(XLOG_INFO, fmt, ## __VA_ARGS__)

/**
  @brief
    Helper macro, make you type less.
*/
#define xlog_debug(fmt, ...)    xlog(XLOG_DEBUG, fmt, ## __VA_ARGS__)

/**
  @brief
    Helper macro, make you type less.
*/
#define xlog_level5(fmt, ...)   xlog(XLOG_LEVEL5, fmt, ## __VA_ARGS__)

/**
  @brief
    Helper macro, make you type less.
*/
#define xlog_level6(fmt, ...)   xlog(XLOG_LEVEL6, fmt, ## __VA_ARGS__)

/**
  @brief
    Helper macro, make you type less.
*/
#define xlog_level7(fmt, ...)   xlog(XLOG_LEVEL7, fmt, ## __VA_ARGS__)

#endif  // XLOG_H_


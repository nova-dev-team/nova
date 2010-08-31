#ifndef XDK_XDEF_H_
#define XDK_XDEF_H_

/**
  @author
    Santa Zhang

  @file
    xdef.h

  @brief
    Defines types used in xdk.
*/

/**
  @brief
    Enumeration of bool type.
*/
typedef enum xbool {
  XTRUE = 1,  ///< @brief Stands for "true".
  XFALSE = 0  ///< @brief Stands for "false".
} xbool;


/**
  @brief
    Enumeration of success/failure flag.
*/
typedef enum xsuccess {
  XSUCCESS = 0, ///< @brief Indicates successful action.
  XFAILURE = -1 ///< @brief Indicates a failure.
} xsuccess;

/**
 *  @brief
 *    The byte type, used for storing binary data.
 */
typedef unsigned char xbyte;

/**
  @brief
    When value range is non-negative, -1 could be used to act as a special mark for "unlimited" large value.
*/
#define XUNLIMITED -1

/**
 *  @brief
 *    Indicates the param is used for output (using pointers).
 */
#define XOUT

/**
 *  @brief
 *    Indicates the param is used for input.
 */
#define XIN

/**
 *  @brief
 *    Indicates the param is used both for input and output (using pointers).
 */
#define XINOUT

#endif  // XDK_XDEF_H_

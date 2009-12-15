#ifndef XDK_H_
#define XDK_H_

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
  XTRUE = 1,
  XFALSE = 0
} xbool;


/**
  @brief
    Enumeration of success/failure flag.
*/
typedef enum xsuccess {
  XSUCCESS = 0,
  XFAILURE = -1
} xsuccess;

#define XUNLIMITED -1

#endif

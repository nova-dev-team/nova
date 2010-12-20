#ifndef XSTR_H_
#define XSTR_H_

#include <stdarg.h>

#include "xdef.h"
#include "xvec.h"

/**
  @author
    Santa Zhang

  @file
    xstr.h

  @brief
    String implementation.
*/

// Implementation is hidden in .c file.
struct xstr_impl;

/**
  @brief
    Simple string implementation.
*/
typedef struct xstr_impl* xstr;

/**
  @brief
    Create a new xstr object.

  @return
    New xstr object, already initialized.
*/
xstr xstr_new();

/**
 * @brief
 *  Create a new xstr object from a c-string.
 *
 * @param cs
 *  The c-string.
 *
 * @return
 *  The new xstr object, already set to the c-string.
 */
xstr xstr_new_from_cstr(const char* cs);

/**
  @brief
    Get a substring in an xstr.

  @param xs
    The xstr including the substring.
  @param start
    The start index of the substring. If it is larger than the xstr length, an empty xstr will be returned.

  @return
    A new xstr object, as a substring.
*/
xstr xstr_substr(xstr xs, int start);

/**
  @brief
    Get a substring in an xstr.

  @param xs
    The xstr including the substring.
  @param start
    The start index of the substring. If it is larger than the xstr length, an empty xstr will be returned.
  @param len
    Length of the substring.

  @return
    A new xstr object, as a substring.
*/
xstr xstr_substr2(xstr xs, int start, int len);

/**
  @brief
    Destroy an xstr object.

  @param xs
    The xstr to be destroyed.
*/
void xstr_delete(void* xs);

/**
  @brief
    Get c-string from xstr.

  @param xs
    The xstr from which c-string will be extracted.

  @return
    The extracted c-string.

  @warning
    Do not modify the content of returned c-string!
*/
const char* xstr_get_cstr(const xstr xs);

/**
  @brief
    Set content of xstr to a c-string.

  @param xs
    The xstr to be changed.
  @param cs
    The new content, has c-string type.
*/
void xstr_set_cstr(xstr xs, const char* cs);

/**
  @brief
    Get length of an xstr.

  @param xs
    The xstr whose length we care about.

  @return
    Length of the xstr.
*/
int xstr_len(xstr xs);

/**
  @brief
    This is printf like function, except that it prints into an xstr.

  @param xs
    The xstr that will be printed into. New data will be appended to the end of xs.
  @param fmt
    The format string like in printf. Only support \%d, \%c, \%s.

  @return
    On error (format error, etc.) return -1.
    Otherwise the number of chars appended to xs.
*/
int xstr_printf(xstr xs, const char* fmt, ...);

/**
  @brief
    This is vprintf like function, except that it prints into an xstr.

  @param xs
    The xstr that will be printed into. New data will be appended to the end of xs.
  @param fmt
    The format string like in printf. Only support \%d, \%c, \%s.
  @param va
    The variable arg list to be printed.

  @return
    On error (format error, etc.) return -1.
    Otherwise the number of chars appended to xs.
*/
int xstr_vprintf(xstr xs, const char* fmt, va_list va);

/**
  @brief
    Add a c-string as prefix.

  @param xs
    The xstr where the new c-string will be added as prefix.
  @param prefix
    The prefix to be added.
*/
void xstr_add_prefix_cstr(xstr xs, const char* prefix);

/**
  @brief
    Append a char to xstr.

  @param xs
    The xstr where new char will be appended to.
  @param ch
    The char to be appended. If ch == '\\0', this function will do nothing.
*/
void xstr_append_char(xstr xs, char ch);

/**
  @brief
    Append a c-string to xstr.

  @param xs
    The xstr where new c-string will be appended to.
  @param cs
    The c-string to be appended.
*/
void xstr_append_cstr(xstr xs, const char* cs);

/**
  @brief
    Test if an xstr starts with a c-string.

  @param xs
    The xstring to be tested.
  @param head
    The cstring which might be the head.

  @return
    XTRUE or XFALSE.
*/
xbool xstr_startwith_cstr(xstr xs, const char* head);

/**
  @brief
    Test if an xstr ends with a c-string.

  @param xs
    The xstring to be tested.
  @param tail
    The cstring which might be the tail.

  @return
    XTRUE or XFALSE.
*/
xbool xstr_endwith_cstr(xstr xs, const char* tail);

/**
  @brief
    Deeply copy an xstr object.

  @param orig
    The original xstr object.

  @return
    A deeply copied xstr object, it should be destroyed by xstr_delete later.
*/
xstr xstr_copy(xstr orig);

/**
  @brief
    Return the last char in xstr.

  @param xs
    The xstr object.

  @return
    Will return '\\0' if xstr has length 0.
*/
char xstr_last_char(xstr xs);

/**
  @brief
    Test if 2 xstr is equal.

  @param xstr1
    The xstr to be tested.
  @param xstr2
    The xstr to be tested.

  @return
    Whether the 2 xstr is equal.
*/
xbool xstr_eql(xstr xstr1, xstr xstr2);

/**
  @brief
    Test if an x-string is equal to a c-string.

  @param xs
    The x-string.
  @param cstr
    The c-string.

  @return
    Whether the 2 strings are equal.
*/
xbool xstr_eql_cstr(xstr xs, const char* cstr);

/**
  @brief
    Strip an xstr.

  @param xs
    The xstr object to be stripped.
  @param strip_set
    A c-string containing all the chars to be stripped.
*/
void xstr_strip(xstr xs, char* strip_set);

/**
 * @brief
 *  Compare 2 xstr object.
 *
 * @param xs1
 *  The xstr 1 to be compared.
 * @param xs2
 *  The xstr 2 to be compared.
 *
 * @return
 *  Return value is like strcmp().
 */
int xstr_compare(xstr xs1, xstr xs2);

/**
  @brief
    Split an xstring.
  
  @param xs
    The xstring to be splitted.
  @param sep
    Separators for the xstring.
    If "" is given, the original string will be separated into characters.
  
  @return
    An xvec object, containing all the segments.
*/
xvec xstr_split_xvec(xstr xs, const char* sep);

#endif  // XSTR_H_

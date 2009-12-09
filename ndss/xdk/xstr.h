#ifndef XSTR_H_
#define XSTR_H_

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
typedef struct xstr_impl *xstr;

/**
  @brief
    Create a new xstr object.

  @return
    New xstr object, already initialized.
*/
xstr xstr_new();

/**
  @brief
    Destroy an xstr object.

  @param xs
    The xstr to be destroyed.
*/
void xstr_delete(xstr xs);

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
const char* xstr_get_cstr(xstr xs);

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
    This is printf like function, except that it prints into an xstr. It only supports some basic formats.

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
void xstr_append_cstr(xstr xs, char* cs);

#endif


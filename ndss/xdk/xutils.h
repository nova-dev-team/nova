#ifndef XUTILS_H_
#define XUTILS_H_

/**
  @author
    Santa Zhang

  @file
    xutils.h

  @brief
    Miscellaneous utilities.
*/


/**
  @brief
    Convert int value into a char* string.

  @param value
    The int value to be converted.
  @param buf
    The buffer that will contain text representation of the int value.
  @param base
    The numerical base for text representation, in range [2, 36].

  @return
    The char* string containing text representation of the int value.
    '\0' if error occured.

  @warning
    Make sure buf have enough size!
*/
char* xitoa(int value, char* buf, int base);


/**
  @brief
    Test if a string starts with another string.

  @param str
    The string to be tested.
  @param head
    The begining of the string.

  @return
    1 if true, 0 if false.
*/
int xstr_startwith(char* str, char* head);

/**
  @brief
    Strips white space around a string.

  @param str
    The string to be stripped.

  @return
    The stripped string.
*/
char* xstr_strip(char* str);

#endif


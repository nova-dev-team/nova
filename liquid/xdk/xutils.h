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

#include <netinet/in.h>

#include "xdef.h"

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
    Return empty string if error occured.

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
xbool xcstr_startwith_cstr(const char* str, const char* head);

/**
  @brief
    Strips white space around a string.

  @param str
    The string to be stripped.

  @return
    The stripped string.
*/
char* xcstr_strip(char* str);

/**
  @brief
    Convert an IP value into string.

  @param ip
    The IP value.
  @param str
    The string that will contain text representation of the IP value.
    It must have enough size (>= 16).
  
  @return
    -1 if convert failure, otherwise 0.

  @warning
    str must have enough size!
*/
xsuccess xinet_ip2str(int ip, char* str);

/**
  @brief
    Convert text representation of inet address into socket address.

  @param host
    Text representation of inet address.
  @param port
    The port of socket address.
  @param addr
    Socket address to be filled in.

  @return
    -1 if failure, otherwise 0.
*/
xsuccess xinet_get_sockaddr(const char* host, int port, struct sockaddr_in* addr);

#endif


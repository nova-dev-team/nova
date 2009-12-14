#include <stddef.h>
#include <string.h>
#include <arpa/inet.h>
#include <netdb.h>

#include "xmemory.h"
#include "xutils.h"

// adapted from www.jb.man.ac.uk/~slowe/cpp/itoa.html
char* xitoa(int value, char* buf, int base) {
  char* ptr = buf;
  char* ptr1 = buf;
  char* digits = "zyxwvutsrqponmlkjihgfedcba9876543210123456789abcdefghijklmnopqrstuvwxyz";
  char ch;
  int v;

  if (base < 2 || base > 36) {
    *buf = '\0';
    return buf;
  }

  do {
    v = value;
    value /= base;
    *ptr++ = digits[35 + (v - value * base)];
  } while (value);

  if (v < 0)
    *ptr++ = '-';
  
  *ptr-- = '\0';
  while (ptr1 < ptr) {
    ch = *ptr;
    *ptr-- = *ptr1;
    *ptr1++ = ch;
  }

  return buf;
}


xbool xcstr_startwith_cstr(char* str, char* head) {
  int i;
  for (i = 0; str[i] != '\0' && head[i] != '\0'; i++) {
    if (str[i] != head[i])
      return XFALSE;
  }
  return head[i] == '\0';
}

char* xcstr_strip(char* str) {
  int begin = 0;
  int end = strlen(str) - 1;
  int i;

  while (str[begin] == ' ' || str[begin] == '\t' || str[begin] == '\r' || str[begin] == '\n') {
    begin++;
  }

  while (str[end] == ' ' || str[end] == '\t' || str[end] == '\r' || str[end] == '\n') {
    end--;
  }

  if (end <= begin) {
    // whole string stripped
    str[0] = '\0';
    return str;
  }

  for (i = 0; str[begin + i] != '\0' && begin + i <= end; i++) {
    str[i] = str[begin + i];
  }
  str[i] = '\0';
  
  return str;
}

xsuccess xinet_ip2str(int ip, char* str) {
  unsigned char* p = (unsigned char *) &ip;
  int i;
  char seg_str[4];
  str[0] = '\0';

  // TODO big endian? small endian?
  for (i = 3; i >= 0; i--) {
    int seg = p[i];
    xitoa(seg, seg_str, 10);
    strcat(str, seg_str);
    if (i != 0) {
      strcat(str, ".");
    }
  }
  return XSUCCESS;
}

xsuccess xinet_get_sockaddr(const char* host, int port, struct sockaddr_in* addr) {
  in_addr_t a;
  bzero(addr, sizeof(*addr));
  addr->sin_family = AF_INET;
  a = inet_addr(host);
  if (a != INADDR_NONE) {
    addr->sin_addr.s_addr = a;
  } else {
    struct hostent *hp = gethostbyname(host);
    if (hp == 0 || hp->h_length != 4) {
      return XFAILURE;
    }
  }
  addr->sin_port = htons(port);
  return XSUCCESS;
}


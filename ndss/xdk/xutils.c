#include <stddef.h>
#include <string.h>

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


int xstr_startwith(char* str, char* head) {
  int i;
  for (i = 0; str[i] != '\0' && head[i] != '\0'; i++) {
    if (str[i] != head[i])
      return 0;
  }
  return head[i] == '\0';
}

char* xstr_strip(char* str) {
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


#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "xutils.h"

char g_buf[1024];

void t(char* str) {
  strcpy(g_buf, str);
  xcstr_strip(g_buf);
  printf("strip: '%s' -> '%s'\n", str, g_buf);
}

void test_strip() {
  t("  hallpy ");
  t("  sdf \t\tdood");
  t("");
  t("      \n\n\n\n\n\r");
}

int main() {
  test_strip();
  return 0;
}

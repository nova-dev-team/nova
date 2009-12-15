#include <stdio.h>

#include "xutils.h"

void t(char* str, char* head, int v) {
  if (v != xcstr_startwith_cstr(str, head)) {
    if (v == 0) {
      printf("'%s' is not started with '%s'!\n", str, head);
    } else {
      printf("'%s' is started with '%s'!\n", str, head);
    }
  }
}

void test_xcstr_startwith_cstr() {
  t("", "", 1);
  t("a", "", 1);
  t("a", "a", 1);
  t("abc", "ab", 1);

  t("a", "c", 0);
  t("", "ac", 0);
  t("a", "ac", 0);
}

int main() {
  test_xcstr_startwith_cstr();
  return 0;
}

#include <stdio.h>
#include <string.h>

#include "xstr.h"
#include "xmemory.h"

int main() {
  xstr xs = xstr_new();
  const char* cs = xstr_get_cstr(xs);
  const char* cs2 = "This string is so long that it might require xstr to relocate memory chunk!\n";
  int i;
  printf("good!\n%s\n", cs);
  xstr_set_cstr(xs, cs2);
  printf("result: %s\nxstr_len : %d\n", xstr_get_cstr(xs), xstr_len(xs));
  printf("len by strlen:%d %d\n", (int) strlen(cs2), (int) strlen(xstr_get_cstr(xs)));
  xstr_set_cstr(xs, "");
 // printf("xstr_printf returnd %d\n", xstr_printf(xs, "sdfsdf%^sdfsdf"));
  printf("xstr_printf returnd %d\n", xstr_printf(xs, "%d", 1));
  printf("\n%s\n", xstr_get_cstr(xs));
  printf("xstr_printf returnd %d\n", xstr_printf(xs, "%d", 12));
  printf("\n%s\n", xstr_get_cstr(xs));
  printf("xstr_printf returnd %d\n", xstr_printf(xs, "%d", 123));
  printf("\n%s\n", xstr_get_cstr(xs));
  printf("xstr_printf returnd %d\n", xstr_printf(xs, "%c", 'A'));
  printf("\n%s\n", xstr_get_cstr(xs));
  printf("xstr_printf returnd %d\n", xstr_printf(xs, "%s", "SANTA"));
  printf("\n%s\n", xstr_get_cstr(xs));
  printf("xstr_printf returnd %d\n", xstr_printf(xs, "%d%%", 4));
  printf("\n%s\n", xstr_get_cstr(xs));
  printf("xstr_printf returnd %d\n", xstr_printf(xs, "This is a test %d, %c%c%c %s\n", 3, 'A', 'B', 'C', "String test!!!"));
  printf("\n%s\n", xstr_get_cstr(xs));
  for (i = 0; i < 100; i++) {
    xstr_printf(xs, "crapyy test %d\n", i);
//    xstr_printf(xs, "%d%%", i);
  }
  printf("OK Finally :\n%s", xstr_get_cstr(xs));
  printf("suck");
  xstr_delete(xs);
  return xmem_usage(NULL);
}

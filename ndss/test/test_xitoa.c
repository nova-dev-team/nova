#include <stdio.h>
#include <assert.h>
#include <time.h>
#include <stdlib.h>

#include "xutils.h"

void test_convert_base10() {
  int run_times = 100;
  int i, num, num2, num3;
  char buf[16];
  printf("Testing converting %d random numbers in base 10.\n", run_times);
  for (i = 0; i < run_times; i++) {
    num = rand();
    printf("num=%d, %d\n", num, -num);
    num2 = -num;
    xitoa(num, buf, 10);
    sscanf(buf, "%d", &num3);
    if (num != num3) {
      printf("Error! %d converted to '%s'\n", num, buf);
      exit(1);
    }
    xitoa(num2, buf, 10);
    sscanf(buf, "%d", &num3);
    if (num2 != num3) {
      printf("Error! %d converted to '%s'\n", num2, buf);
      exit(1);
    }
  }
}

int main() {
  srand(time(NULL));
  test_convert_base10();
  return 0;
}

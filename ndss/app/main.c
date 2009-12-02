#include <stdio.h>

#include "ndss.h"
#include "xdk.h"

#include "main_helper.h"

int main(int argc, char *argv[]) {
  printf("NDSS - Nova Distributed Storage System\n");
  printf("Testing helper: %d\n", helper_fun());
  return 0;
}

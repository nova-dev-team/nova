#include <stdio.h>
#include <stdlib.h>

#include "xoption.h"
#include "imgmount.h"
#include "imgmount_instance.h"

void imgmount_print_help() {
  printf("Usage: liquid mount <server_ip:server_port> <mount_home>\n");
  printf("\n");
  printf("The address of a 'imgdir' server is provided by <server_ip:server_port>\n");
  printf("\n");
  printf("The <mount_home> will have the following content:\n");
  printf("  cache/        cached files\n");
  printf("  cntl/         controlling utilities\n");
  printf("  data/         where your data will be located\n");
  printf("  log/          application log data\n");
  printf("  tmp/          temporary files\n");
  printf("\n");
}

xsuccess imgmount_main(int argc, char* argv[]) {
  xsuccess ret = XSUCCESS;
  xoption xopt = xoption_new();
  xoption_parse(xopt, argc, argv);
  // check if need help
  if (xoption_has(xopt, "help") || xoption_has(xopt, "h") || argc < 4) {
    imgmount_print_help();
  } else {
    ret = imgmount_instance_run(argc, argv);
  }
  xoption_delete(xopt);
  return ret;
}

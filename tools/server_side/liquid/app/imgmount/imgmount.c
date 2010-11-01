#include <stdio.h>
#include <stdlib.h>

#include "xutils.h"
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
  printf("  data/         where your data will be located\n");
  printf("  tmp/          temporary files\n");
  printf("  log/          application log data\n");
  printf("\n");
}

static xsuccess imgmount_main_real(int argc, char* argv[]) {
  xsuccess ret = XFAILURE;
  char* ip_port = argv[2];
  xstr server_ip = xstr_new();
  int server_port = -1;
  xstr mount_point = xstr_new_from_cstr(argv[3]);

  if (xinet_split_host_port(ip_port, server_ip, &server_port) == XSUCCESS) {
    imgmount_instance inst = imgmount_instance_new(server_ip, server_port, mount_point);
    if (inst != NULL) {
      ret = imgmount_instance_run(inst);
      imgmount_instance_delete(inst);
    } else {
      ret = XFAILURE;
      fprintf(stderr, "Failed to boot imgmount!\n");
    }
    // server_ip and mount_point is managed by imgmount_instance, no need to delete them
    
  } else {
    fprintf(stderr, "incorrect ip and port info!\n");
    ret = XFAILURE;
    xstr_delete(server_ip);
    xstr_delete(mount_point);
  }
  return ret;
}

xsuccess imgmount_main(int argc, char* argv[]) {
  xsuccess ret = XSUCCESS;
  xoption xopt = xoption_new();
  xoption_parse(xopt, argc, argv);
  // check if need help
  if (xoption_has(xopt, "help") || xoption_has(xopt, "h") || argc < 4) {
    imgmount_print_help();
  } else {
    ret = imgmount_main_real(argc, argv);
  }
  xoption_delete(xopt);
  return ret;
}

#include "liquid_client.h"
#include "ftp_fs.h"

xsuccess ftp_fs_list_into_xstr(xstr path, xstr holder, xstr error_msg) {
  int i;
  for (i = 0; i < 1000; i++) {
    xstr_printf(holder, "drwxr-xr-x 2 user group 4096 Dec 07 07:09 folder_%d\r\n", i);
  }
  for (i = 0; i < 1000; i++) {
    xstr_printf(holder, "-rwxr-xr-x 2 user group 4096 Dec 07 07:09 file_%d\r\n", i);
  }
  return XSUCCESS;
}


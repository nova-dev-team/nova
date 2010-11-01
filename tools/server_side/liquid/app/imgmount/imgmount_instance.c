#include "xnet.h"
#include "xmemory.h"
#include "xutils.h"

#include "imgmount_instance.h"
#include "imgmount_ls.h"
#include "imgmount_conn.h"

struct imgmount_instance_impl {
  xstr imgdir_ip;
  int imgdir_port;
  xstr mount_home;
};

// check existance of the mount_home, and add contents to it
static xsuccess prepare_mount_home(const char* mount_home_cstr) {
  xsuccess ret = XSUCCESS;
  char* sub_dirs[] = {"cache", "data", "tmp", NULL};
  int i = 0;
  xstr sub_dir_path = xstr_new();
  while (sub_dirs[i] != NULL && ret == XSUCCESS) {
    xjoin_path_cstr(sub_dir_path, mount_home_cstr, sub_dirs[i]);
    ret = xfilesystem_mkdir_p(xstr_get_cstr(sub_dir_path), 0755);
    i++;
  }
  xstr_delete(sub_dir_path);
  return ret;
}

imgmount_instance imgmount_instance_new(xstr server_ip, int server_port, xstr mount_home) {
  imgmount_instance inst = xmalloc_ty(1, struct imgmount_instance_impl);
  inst->imgdir_ip = server_ip;
  inst->imgdir_port = server_port;
  inst->mount_home = mount_home;
  if (prepare_mount_home(xstr_get_cstr(mount_home)) == XFAILURE) {
    // failed to prepare mount home, destroy the instance and cancel all operations,
    // then set return value to NULL, indicate a failed operation
    imgmount_instance_delete(inst);
    inst = NULL;
  }
  return inst;
}

// helper function to establish connection to imgdir server
static xsuccess imgmount_instance_connect(imgmount_instance inst) {
  xsuccess ret = XFAILURE;
  // TODO
  return ret;
}

xsuccess imgmount_instance_run(imgmount_instance inst) {
  xsuccess ret;
  
  ret = imgmount_instance_connect(inst);
  // TODO
  return ret;
}

void imgmount_instance_delete(imgmount_instance inst) {
  xstr_delete(inst->imgdir_ip);
  inst->imgdir_ip = NULL;
  xstr_delete(inst->mount_home);
  inst->mount_home = NULL;
  xfree(inst);
}

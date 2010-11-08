#include "xnet.h"
#include "xmemory.h"
#include "xutils.h"
#include "xlog.h"

#include "imgmount_instance.h"
#include "imgmount_fuse.h"
#include "imgmount_fs_cache.h"

// check existance of the mount_home, and add contents to it
static xsuccess prepare_mount_home(const char* mount_home_cstr) {
  xsuccess ret = XSUCCESS;
  char* sub_dirs[] = {"cache", "cntl", "data", "log", "tmp", NULL};
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


static void imgmount_instance_delete(imgmount_instance inst) {
  xsocket_delete(inst->imgdir_sock);
  pthread_mutex_destroy(&(inst->imgdir_sock_lock));
  xstr_delete(inst->mount_home);
  fs_cache_delete(inst->fs_root);
  xfree(inst);
}


static imgmount_instance imgmount_instance_new(xstr server_ip, int server_port, xstr mount_home) {
  imgmount_instance inst = xmalloc_ty(1, struct imgmount_instance_impl);
  inst->imgdir_sock = xsocket_new(server_ip, server_port);
  pthread_mutex_init(&(inst->imgdir_sock_lock), NULL);
  inst->mount_home = mount_home;
  inst->fs_root = fs_cache_new_root();
  if (prepare_mount_home(xstr_get_cstr(mount_home)) == XFAILURE) {
    // failed to prepare mount home, destroy the instance and cancel all operations,
    // then set return value to NULL, indicate a failed operation
    imgmount_instance_delete(inst);
    inst = NULL;
  }
  return inst;
}

// helper function to establish connection to imgdir server
static xsuccess imgmount_instance_connect_imgdir(imgmount_instance inst) {
  xsuccess ret = XFAILURE;
  ret = xsocket_connect(inst->imgdir_sock);
  if (ret == XSUCCESS) {
    // do imgdir handshake
    ret = xsocket_write_line(inst->imgdir_sock, "accept_me\r\n");
    if (ret == XSUCCESS) {
      xstr reply_text = xstr_new();
      if (xsocket_read_line(inst->imgdir_sock, reply_text) == XFAILURE || xstr_eql_cstr(reply_text, "accepted") == XFALSE) {
        ret = XFAILURE;
      }
      xstr_delete(reply_text);
    }
    // handshake done
  }
  return ret;
}

static struct fuse_operations imgmount_filesystem_operations = {
  .access   = imgmount_access,
  .getattr  = imgmount_getattr,
  .mkdir    = imgmount_mkdir,
  .open     = imgmount_open,
  .read     = imgmount_read,
  .readdir  = imgmount_readdir,
  .statfs   = imgmount_statfs,
};

// helper function for handling real running routine in imgmount_instance_run()
static xsuccess instance_real_run(imgmount_instance inst, int argc, char* argv[]) {
  xsuccess ret;
  ret = imgmount_instance_connect_imgdir(inst);
  if (ret == XFAILURE) {
    xlog_fatal("cannot connect to imgdir server!\n");
  } else {
    int fake_argc = argc - 2, i;
    char** fake_argv = xmalloc_ty(fake_argc, char *);
    xstr sub_dir_path = xstr_new();

    // fake argv: liquid_app, mount_data_dir, [optinal_args]
    fake_argv[0] = argv[0];
    xjoin_path_cstr(sub_dir_path, xstr_get_cstr(inst->mount_home), "data");
    fake_argv[1] = (char *) xstr_get_cstr(sub_dir_path);

    // fill in optional FUSE args into argv
    for (i = 2; i + 2 < argc; i++)
      fake_argv[i] = argv[i + 2];

    // leave operations to fuse
    fuse_main(fake_argc, fake_argv, &imgmount_filesystem_operations, inst);
    xstr_delete(sub_dir_path);
    xfree(fake_argv);
  }
  return ret;
}

xsuccess imgmount_instance_run(int argc, char* argv[]) {
  xsuccess ret;
  const char* ip_port = argv[2];
  xstr server_host = xstr_new();
  int server_port = -1;
  xstr mount_home = xstr_new_from_cstr(argv[3]);

  if (xinet_split_host_port(ip_port, server_host, &server_port) == XSUCCESS) {
    imgmount_instance inst = imgmount_instance_new(server_host, server_port, mount_home);
    if (inst != NULL) {
      // call real running handling routine
      ret = instance_real_run(inst, argc, argv);
      imgmount_instance_delete(inst);
    } else {
      ret = XFAILURE;
      fprintf(stderr, "Failed to boot imgmount!\n");
    }
    // server_host and mount_home is managed by imgmount_instance, no need to delete them
  } else {
    fprintf(stderr, "incorrect ip and port info!\n");
    ret = XFAILURE;
    xstr_delete(server_host);
    xstr_delete(mount_home);
  }
  return ret;
}


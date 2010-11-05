#ifndef IMGMOUNT_INSTANCE_H_
#define IMGMOUNT_INSTANCE_H_

/**
  @file
    imgmount_instance.h

  @author
    Santa Zhang

  @brief
    Implementation of a 'imgmount' instance. It represents and handles all operations for a mount point.
*/

#include "xdef.h"
#include "xnet.h"
#include "xstr.h"

#include "imgmount_fs_cache.h"

// hidden implementation
struct imgmount_instance_impl;

/**
  @brief
    A 'imgmount' instance. It represents and handles all operations for a mount point.
*/
typedef struct imgmount_instance_impl* imgmount_instance;

/**
  @brief
    Start a imgmount instance's work.

  @param argc
    The number of args passed to imgmount.
  @param argv
    The array of args passed to imgmount.

  @return
    XSUCCESS if every thing runs ok.
    XFAILURE if failed to run the imgmount instance.
*/
xsuccess imgmount_instance_run(int argc, char* argv[]);

/**
  @brief
    Get the mount home of a imgmount instance.

  @param inst
    The imgmount instance.

  @return
    The mount home as an x-string.
*/
xstr get_mount_home(imgmount_instance inst);

/**
  @brief
    Get the x-socket connecting to imgdir server.

  @param inst
    The imgmount instance.

  @return
    The x-socket to imgdir server.
*/
xsocket get_imgdir_sock(imgmount_instance inst);

/**
  @brief
    Get the cached root.

  @param inst
    The imgmount instance.

  @return
    The cached root.
*/
fs_cache get_root(imgmount_instance inst);

#endif  // IMGMOUNT_INSTANCE_H_


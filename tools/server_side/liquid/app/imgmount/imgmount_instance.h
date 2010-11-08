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

#include <pthread.h>

#include "xdef.h"
#include "xnet.h"
#include "xstr.h"

#include "imgmount_fs_cache.h"

/**
  @brief
    The imgmount instance.
*/
struct imgmount_instance_impl {
  xsocket imgdir_sock;  ///<  @brief Connection to imgdir.
  pthread_mutex_t imgdir_sock_lock;  ///< @brief Protect imgdir_sock in multi-thread communication.
  xstr mount_home;  ///<  @brief The mount home, where the imgmount filesystem is organized.
  fs_cache fs_root;  ///<  @brief The cached root of imgdir filesystem.
};

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


#endif  // IMGMOUNT_INSTANCE_H_


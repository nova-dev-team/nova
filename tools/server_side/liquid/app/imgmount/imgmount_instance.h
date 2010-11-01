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
#include "xstr.h"

// hidden implementation
struct imgmount_instance_impl;

/**
  @brief
    A 'imgmount' instance. It represents and handles all operations for a mount point.
*/
typedef struct imgmount_instance_impl* imgmount_instance;

/**
  @brief
    Create a new 'imgmount' instance.

  @param server_ip
    The ip address of 'imgdir' server.
    This variable is managed by imgmount_instance, and will be deleted when imgmount_instance is deleted.
  @param server_port
    The service port of 'imgdir' server.
  @param mount_home
    The mount home directory of 'imgdir' server.
    This variable is managed by imgmount_instance, and will be deleted when imgmount_instance is deleted.

  @return
    Will return NULL on failure (eg. mount_home not found, sub-folder cannot be created, etc).
    'mount_home' and 'server_ip' will be deleted on failure, so don't worry about memory leak.
*/
imgmount_instance imgmount_instance_new(xstr server_ip, int server_port, xstr mount_home);

/**
  @brief
    Start a imgmount instance's work.

  @param inst
    The imgmount_instance to serve.

  @return
    XSUCCESS if every thing runs ok.
    XFAILURE if failed to start.
*/
xsuccess imgmount_instance_run(imgmount_instance inst);

/**
  @brief
    Destroy a 'imgmount_instance'. All connections will be closed, and mount_home will be unmounted.

  @param inst
    The imgmount_instance to be destroyed.
*/
void imgmount_instance_delete(imgmount_instance inst);

#endif  // IMGMOUNT_INSTANCE_H_

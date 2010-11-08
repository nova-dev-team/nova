#ifndef IMGMOUNT_PROTOCOL_H_
#define IMGMOUNT_PROTOCOL_H_

/**
  @brief
    Communication protocol between imgdir server and imgmount client.

  @author
    Santa Zhang

  @file
    imgmount_protocol.h
*/

#include "imgmount_instance.h"
#include "xstr.h"

/**
  @brief
    Send a 'list' request to imgdir server.

  @param inst
    The imgmount instance.
  @param path
    The path to be updated by 'list' command.
  @param entry
    The entry to be updated by "list" command.

  @return
    Errcode of the 'list' request. If nothing goes wrong, 0 will be returned.
*/
int protocol_request_list(imgmount_instance inst, xstr path, fs_cache entry);

/**
  @brief
    Send a 'mkdir' request to imgdir server.

  @param inst
    The imgmount instance.
  @param parent_dir
    The dir entry under which the new folder should be created.
  @param fullpath
    The fullpath of the child dir.
  @param child_name
    Name of the child dir.

  @return
    Errcode of the 'mkdir' request. If nothing goes wrong, 0 will be returned.
*/
int protocol_request_mkdir(imgmount_instance inst, fs_cache parent_dir, const char* fullpath, const char* child_name);

#endif  // IMGMOUNT_PROTOCOL_H_


#ifndef IMGMOUNT_FUSE_H_
#define IMGMOUNT_FUSE_H_

/**
  @file
    imgmount_fuse.h

  @author
    Santa Zhang

  @brief
    Provide FUSE operations for imgmount utility.
*/

#include <errno.h>
#include <fcntl.h>

#define FUSE_USE_VERSION 26
#include <fuse.h>

/**
  @brief
    Provide file size, permissions, etc.

  @param path
    The path of an entry, relative to the mount point.
  @param stbuf
    The stat structure we should fill in.

  @return
    Return 0 if no error. Else return -ERRNO.
*/
int imgmount_getattr(const char* path, struct stat *stbuf);

int imgmount_open(const char *path, struct fuse_file_info *fi);

int imgmount_read(const char *path, char *buf, size_t size, off_t offset, struct fuse_file_info *fi);

int imgmount_readdir(const char *path, void *buf, fuse_fill_dir_t filler, off_t offset, struct fuse_file_info *fi);

#endif  // IMGMOUNT_FUSE_H_


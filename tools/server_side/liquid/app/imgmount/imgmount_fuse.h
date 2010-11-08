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
#include <sys/statvfs.h>
#include <sys/types.h>

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

/**
  @brief
    Read dir contents.

  @param path
    Absolute path of the dir entry.
  @param buf
    Data to be returned to FUSE.
  @param filler
    Utility provided by FUSE to fill dir entry data into buf.
  @param offset
    Parameter provided by FUSE.
  @param fi
    Parameter provided by FUSE.

  @return
    Return 0 if no error. Else return corresponding -ERRNO.
*/
int imgmount_readdir(const char *path, void *buf, fuse_fill_dir_t filler, off_t offset, struct fuse_file_info *fi);

/**
  @brief
    Stat filesystem status.

  @param path
    The path where the filesystem stat is called.
  @param vfs
    The statvfs structure to hold data.

  @return
    Return 0 if no error. Else return corresponding -ERRNO.
*/
int imgmount_statfs(const char *path, struct statvfs *vfs);

/**
  @brief
    Check access permission.

  @param path
    The path which should be checked.
  @param mode
    The access mode.

  @return
    Return 0 if no error. Else return corresponding -ERRNO.
*/
int imgmount_access(const char* path, int mode);

int imgmount_mkdir(const char* path, mode_t mode);

#endif  // IMGMOUNT_FUSE_H_


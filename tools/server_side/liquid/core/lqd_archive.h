#ifndef CORE_MD5ARCHIVE_H_
#define CORE_MD5ARCHIVE_H_

#include <stdio.h>

#include "xdef.h"
#include "xstr.h"

/**
  @brief
    Helper to store/retrieve contents from disk. File and folder names are determined by the sha-1 value given.

  @file
    lqd_archive.h

  @author
    Santa Zhang
*/

/**
  @brief
    Check if a content exists on disk.

  @param basefolder
    The base directory where all contents are saved.
  @param key
    The hash key value.

  @return
    Whether the file exists.
*/
xbool archive_has(const char* basefolder, xbyte* key);

/**
  @brief
    Get the full path of a file with certain key value.

  @param basefolder
    The base directory where all contents are saved.
  @param key
    The hash key value.
  @param path
    The file path will be returned in this xstr.

  @return
    If file not found, XFIALURE will be returned.
*/
xsuccess archive_path(const char* basefolder, xbyte* key, xstr path);

/**
  @brief
    Open a file with certain hash key value, and return the FILE* pointer.

  @param basefolder
    The base directory where all contents are saved.
  @param key
    The key value.
  @param modes
    File open mode, which will be passed to fopen.

  @return
    If file not found, NULL will be returned. Otherwise the FILE* pointer will be returned.
*/
FILE* archive_open_fp(const char* basefolder, xbyte* key, const char* modes);

/**
  @brief
   Open a file with certain hash key value, and return the file descriptor.

  @param basefolder
    The base directory where all contents are saved.
  @param key
    The key value.
  @param options
    File open options, which will be passed to open().

  @return
    If file not found, -1 will be returned, and errno will be set, same as open(). Otherwise a non-negative integer will be returned as file descriptor.
*/
int archive_open_fd(const char* basefolder, xbyte* key, int options);

#endif  // #define CORE_MD5ARCHIVE_H_


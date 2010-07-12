#ifndef CORE_MD5ARCHIVE_H_
#define CORE_MD5ARCHIVE_H_

#include <stdio.h>

#include "xdef.h"
#include "xstr.h"

/**
  @brief
    Helper to store/retrieve contents from disk. File and folder names are determined by the md5 value given.

  @file
    md5archive.h

  @author
    Santa Zhang
*/

/**
  @brief
    Check if a content exists on disk.

  @param basefolder
    The base directory where all contents are saved.
  @param md5
    The md5 values.

  @return
    Whether the file exists.
*/
xbool md5archive_has(const char* basefolder, unsigned char* md5);

/**
  @brief
    Get the full path of a file with certain md5 value.

  @param basefolder
    The base directory where all contents are saved.
  @param md5
    The md5 values.
  @param path
    The file path will be returned in this xstr.

  @return
    If file not found, XFIALURE will be returned.
*/
xsuccess md5archive_path(const char* basefolder, unsigned char* md5, xstr path);

/**
  @brief
    Open a file with certain md5 value.

  @param basefolder
    The base directory where all contents are saved.
  @param md5
    The md5 values.
  @param modes
    File open mode, which will be passed to fopen.

  @return
    If file not found, NULL will be returned. Otherwise the FILE* pointer will be returned.
*/
FILE* md5archive_open(const char* basefolder, unsigned char* md5, const char* modes);

#endif  // #define CORE_MD5ARCHIVE_H_


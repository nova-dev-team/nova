#ifndef LIQUID_FTP_FS_H_
#define LIQUID_FTP_FS_H_

#include "xnet.h"
#include "xdef.h"
#include "xstr.h"

/**
  @author
    Santa Zhang

  @file
    ftp_fs.h

  @brief
    Filesystem support for FTP server.
*/

/**
  @brief
    Fill directory listing into xstr object.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param path
    The path to be listed.
  @param holder
    Where the listing text will be filled.
  @param error_msg
    When error occurred, error message will be filled into this xstr.

  @return
    Whether the action is successful. If error occurred, check error_msg for error info.
*/
xsuccess ftp_fs_list_into_xstr(const xstr root_jail, const xstr path, xstr holder, xstr error_msg);

/**
  @brief
    Try to change current working directory.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current directory.
  @param new_path
    New path to switch into, could be relative path or absolute path.
  @param error_msg
    When error occurred, error message will be filled into this xstr.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_try_cwd_cstr(const xstr root_jail, const char* current_dir, const char* new_path, xstr error_msg);

/**
  @brief
    Get a file content.

  @param xsock
    The data socket to client.
  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current directory.
  @param filename
    Name of the file to be retrieved, could be relative path or absolute path.
    It could also be an directory name, in which case this function works as if "LIST" was called instead of "RETR".
  @param start_pos
    The starting position of transfer.
  @param error_msg
    When error occurred, error message will be filled into this xstr.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_retr_file(xsocket xsock, const xstr root_jail, const xstr current_dir, const char* filename, off_t start_pos, xstr error_msg);

/**
  @brief
    Get the modification time of a file.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current directory.
  @param filename
    Name of the file to be checked, could be relative path or absolute path.
  @param mdtm_str
    The xstr which will contain reply message to client.
  @param error_msg
    When error occurred, error message will be filled into this xstr.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_mdtm(const xstr root_jail, const char* current_dir, const char* filename, xstr mdtm_str, xstr error_msg);

/**
  @brief
    Get the file size.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current directory.
  @param filename
    Name of the file whose size we care about, could be relative path or absolute path.
  @param size_str
    The xstr which will contain reply message to client.
  @param error_msg
    When error occurred, error message will be filled into this xstr.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_size(const xstr root_jail, const char* current_dir, const char* filename, xstr size_str, xstr error_msg);

/**
  @brief
    Make a new directory.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current directory.
  @param dirname
    Name of the directory to be made.
  @param error_msg
    When error occurred, error message will be filled into this xstr.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_mkdir(const xstr root_jail, const char* current_dir, const char* dirname, xstr error_msg);

/**
  @brief
    Remove a file or directory.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current directory.
  @param dirname
    Name of the file or directory to be removed.
  @param error_msg
    When error occurred, error message will be filled into this xstr.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_dele(const xstr root_jail, const char* current_dir, const char* dirname, xstr error_msg);

/**
  @brief
    Test if could put a file content.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current directory.
  @param filename
    Name of the file to be stored, could be relative path or absolute path.
  @param start_pos
    The starting position of transfer.

  @return
    Whether the file could be stored.
*/
xbool ftp_fs_could_stor_file(const xstr root_jail, const xstr current_dir, const char* filename, off_t start_pos);

/**
  @brief
    Put a file content.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param xsock
    The data socket to client.
  @param current_dir
    Current directory.
  @param filename
    Name of the file to be stored, could be relative path or absolute path.
  @param start_pos
    The starting position of transfer.
  @param error_msg
    When error occurred, error message will be filled into this xstr.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_stor_file(xsocket xsock, const xstr root_jail, const xstr current_dir, const char* filename, off_t start_pos, xstr error_msg);

/**
  @brief
    Rename a file or dir.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current directory.
  @param from_name
    The original name.
  @param to_name
    The new name.
  @param error_msg
    The error message holder.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_rename(const xstr root_jail, const char* current_dir, const char* from_name, const char* to_name, xstr error_msg);

/**
  @brief
    Carry out SITE commands.

  @param root_jail
    The root jail where all actions will be locked inside.
  @param current_dir
    Current working directory (in user's vision).
  @param cmd
    The SITE cmd. "SITE" character is already chopped off.
  @param error_msg
    The error message holder.

  @return
    Whether the action is successful.
*/
xsuccess ftp_fs_site_cmd(const xstr root_jail, const char* current_dir, const char* cmd, xstr error_msg);

#endif  // LIQUID_FTP_FS_H_


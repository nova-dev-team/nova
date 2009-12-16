#ifndef LIQUID_FTP_FS_H_
#define LIQUID_FTP_FS_H_

#include "xdef.h"
#include "xstr.h"

xsuccess ftp_fs_list_into_xstr(xstr path, xstr holder, xstr error_msg);

xsuccess ftp_fs_try_cwd_cstr(const char* current_dir, const char* new_path, xstr error_msg);

#endif  // LIQUID_FTP_FS_H_


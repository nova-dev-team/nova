#ifndef LIQUID_FTP_FS_H_
#define LIQUID_FTP_FS_H_

#include "xnet.h"
#include "xdef.h"
#include "xstr.h"

xsuccess ftp_fs_list_into_xstr(xstr path, xstr holder, xstr error_msg);

xsuccess ftp_fs_try_cwd_cstr(const char* current_dir, const char* new_path, xstr error_msg);

xsuccess ftp_fs_retr_file(xsocket xsock, xstr current_dir, const char* filename, xstr error_msg);

#endif  // LIQUID_FTP_FS_H_


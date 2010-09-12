#ifndef LIQUID_FTP_ACL_H_
#define LIQUID_FTP_ACL_H_

/**
 * @author
 *  Santa Zhang
 *
 * @file
 *  ftp_acl.h
 *
 * @brief
 *  Provide user authentication and ACL protection for filesystem.
 */

#include "xdef.h"
#include "xstr.h"

/**
 * @brief
 *  Indicates the path entry is readable.
 */
#define FTP_ACL_READ_FLAG       0x1

/**
 * @brief
 *  Indicates the path entry is writable.
 */
#define FTP_ACL_WRITE_FLAG      0x2

/**
 * @brief
 *  Indicates the path entry is deletable.
 */
#define FTP_ACL_DEL_FLAG        0x4

/**
 * @brief
 *  Check if the path entry is readable.
 */
#define FTP_ACL_CAN_READ(priv)  (((priv) & FTP_ACL_READ_FLAG) != 0)

/**
 * @brief
 *  Check if the path entry is writable.
 */
#define FTP_ACL_CAN_WRITE(priv) (((priv) & FTP_ACL_WRITE_FLAG) != 0)

/**
 * @brief
 *  Check if the path entry is deletable.
 */
#define FTP_ACL_CAN_DEL(priv)   (((priv) & FTP_ACL_DEL_FLAG) != 0)

/**
 * @brief
 *  Set the ACL to work in single user mode.
 *
 * This function must be called before any ACL functions. And it cannot be mixed with multi user mode.
 *
 * @param user
 *  The user name.
 * @param pwd
 *  The user's password.
 * @param root_jail
 *  The root jail where the user will be locked into, for safety.
 * @param readonly
 *  If the ftp should be served as readonly system.
 *
 * @return
 *  If successfully setup the single user mode.
 */
xsuccess ftp_acl_single_user_mode(const char* user, const char* pwd, const char* root_jail, const xbool readonly);

/**
 * @brief
 *  Set the ACL to work in multi user mode (using an Sqlite3 database).
 *
 * This function must be called before any ACL functions. And it cannot be mixed with single user mode.
 *
 * @param db_fname
 *  The filename of the Sqlite3 database.
 *
 * @return
 *  If failed to open the Sqlite3 database, it will return XFAILURE.
 */
xsuccess ftp_acl_multi_user_mode(const char* db_fname);

/**
 * @brief
 *  Authentication for users.
 *
 * @param user
 *  The user name.
 * @param pwd
 *  The user's password.
 *
 * @return
 *  XSUCCESS if the user is authenticated, otherwise XFAILURE.
 */
xsuccess ftp_auth_user(const char* user, const char* pwd);

/**
 * @brief
 *  Get the root jail of some user.
 *
 * @param user
 *  The user name.
 * @param root_jail
 *  The root jail to be returned.
 *
 * @return
 *  XSUCCESS if the user exists, otherwise XFAILURE.
 */
xsuccess ftp_get_root_jail(const char* user, xstr root_jail);

/**
 * @brief
 *  Check the privilege over a given path.
 *
 * @param user
 *  The user who's privilege will be checked.
 * @param path
 *  The path to be checked.
 * @param priv_flag
 *  The privilege flag to be returned.
 *
 * @return
 *  Whether the checking is successful. It returns XFAILURE if path is not found in db.
 */
xsuccess ftp_path_privilege(const char* user, const char* path, int* priv_flag);

/**
 * @brief
 *  Finalize the ACL system.
 */
xsuccess ftp_acl_finalize();

#endif  // LIQUID_FTP_ACL_H_


#ifndef LIQUID_FTP_SESSION_H_
#define LIQUID_FTP_SESSION_H_

#include "xdef.h"
#include "xstr.h"
#include "xnet.h"

/**
  @author
    Santa Zhang

  @file
    ftp_session.h

  @brief
    User's FTP connection session.
*/

struct ftp_session_impl;

/**
  @brief
    FTP session object.
*/
typedef struct ftp_session_impl* ftp_session;

/**
  @brief
    Create a new FTP session.

  @param cmd_sock
    User's command connection xsocket object. It will NOT be destroyed when deleting the FTP session object.
  @param host_addr
    The host address of FTP server.
  @param root_jail
    The root jail in which the ftp service is running. Every action must be locked inside this jail.

  @return
    A newly created FTP session object.
*/
ftp_session ftp_session_new(xsocket cmd_sock, xstr host_addr, xstr root_jail);

/**
  @brief
    Destroy an FTP session. Note that the command xsocket is not destroyed.

  @param session
    The FTP session to be destroyed.
*/
void ftp_session_delete(ftp_session session);

/**
  @brief
    Write some data on command xsocket.

  @param session
    The FTP session whose command connection will be used.
  @param data
    The data to be written.
  @param len
    Length of data to be written, in bytes.

  @return
    Will return number of bytes written. If error occurred, -1 will be returned instead.
*/
int ftp_session_cmd_write(ftp_session session, void* data, int len);

/**
  @brief
    Read data from command connection.

  @param session
    The FTP session whose command connection will be used.
  @param buf
    Pointer to the input buffer.
  @param max_len
    Maximum number of bytes to be read.

  @return
    Will return number of bytes read. If error occurred, -1 will be returned instead.

  @warning
    Make sure input buffer has enough size.
*/
int ftp_session_cmd_read(ftp_session session, void* buf, int max_len);

/**
  @brief
    Get the root jail of an ftp session.

  @param session
    The ftp session.

  @return
    The root jail of the given ftp session.
*/
const xstr ftp_session_get_root_jail(ftp_session session);


/**
  @brief
    Check if user is logged in.

  @param session
    User's connection session.

  @return
    XTRUE or XFALSE.
*/
xbool ftp_session_is_logged_in(ftp_session session);

/**
  @brief
    Check if username is provided, i.e., if USER command was provided.

  @param session
    User's connection session.

  @return
    XTRUE or XFALSE.
*/
xbool ftp_session_is_username_given(ftp_session session);

/**
  @brief
    Set user's name.

  @param session
    User's connection session.
  @param cstr_username
    User's name, in c-string.
*/
void ftp_session_set_username_cstr(ftp_session session, char* cstr_username);

/**
  @brief
    Get user's name, in c-string.

  @param session
    User's connection session.

  @return
    User's name, in c-string.
*/
const char* ftp_session_get_username_cstr(ftp_session session);

/**
  @brief
    Get user's identifier. It is like "ip:port(username)".

  @param session
    User's connection session.

  @return
    User's identifier in "ip:port(username)" format.
*/
const char* ftp_session_get_user_identifier_cstr(ftp_session session);

/**
  @brief
    Try to authenticate user's account.

  @param session
    User's connection session.
  @param password
    User's password, in plain text.

  @return
    XTRUE or XFALSE indicating if the info is correct.
*/
xbool ftp_session_auth_cstr(ftp_session session, char* password);

/**
  @brief
    Try to change working directory.

  @param session
    User's connection session.
  @param new_path
    New path to be changed. Could be relative path, or absolute path.
  @param error_msg
    If error occurred, error message will be written into this xstr.

  @return
    XSUCCESS if successfully changed working directory, and internal status of FTP session will also be changed.
    XFAILURE if failed to change working directory, internal status of FTP session will NOT be changed.
*/
xsuccess ftp_session_try_cwd_cstr(ftp_session session, char* new_path, xstr error_msg);

/**
  @brief
    Get current working directory, in c-string.

  @param session
    User's connection session.

  @return
    Current working directory, in c-string.
*/
const char* ftp_session_get_cwd_cstr(ftp_session session);

/**
  @brief
    Get current working directory, in xstr.

  @param session
    User's connection session.

  @return
    Current working directory, in xstr.
*/
xstr ftp_session_get_cwd(ftp_session session);

/**
  @brief
    Check if user issued ABOR command.

  @param session
    User's connection session.

  @return
    XTRUE of XFALSE.
*/
xbool ftp_session_is_user_aborted(ftp_session session);

/**
  @brief
    Get current transmission mode.

  @param session
    User's connection session.

  @return
    current transmission mode. 'P' stands for passive mode.
*/
char ftp_session_get_trans_mode(ftp_session session);

/**
  @brief
    Get current transmission type.

  @param session
    User's connection session.

  @return
    Current transmission type, either 'A' (ASCII) or 'I' (BINARY/IMAGE).
*/
char ftp_session_get_trans_type(ftp_session session);

/**
  @brief
    Get current transmission type.

  @param session
    User's connection session.
  @param type
    User's transmission type, 'A' (ASCII) or 'I' (BINARY/IMAGE).
*/
void ftp_session_set_trans_type(ftp_session session, char type);

/**
  @brief
    Prepare to do data connection.

  @param session
    User's connection session.
  @param data_acceptor
    The data service acceptor to be bound to data xserver.
*/
void ftp_session_prepare_data_service(ftp_session session, xserver_acceptor data_acceptor);


/**
  @brief
    Check if data service is ready.

  @param session
    The ftp session.

  @return
    Whether the data service is ready.
*/
xbool ftp_session_is_data_service_ready(ftp_session session);


/**
  @brief
    Start data service for FTP session.

  @param session
    User's connection session.
*/
void ftp_session_trigger_data_service(ftp_session session);

/**
  @brief
    Get service host address.

  @param session
    User's connection session.

  @return
    Host address of the FTP server.
*/
xstr ftp_session_get_host_addr(ftp_session session);

/**
  @brief
    Get host IP, in c-string.

  @param session
    User's connection session.

  @return
    FTP server's IP address, in c-string.
*/
const char* ftp_session_get_host_ip_cstr(ftp_session session);

/**
  @brief
    Get data server's port number.

  @param session
    User's connection session.

  @return
    Data server port number.
*/
int ftp_session_get_data_server_port(ftp_session session);

/**
  @brief
    Get user's data command, in c-string.

  @param session
    User's connection session.

  @return
    User's data command, in c-string.
*/
const char* ftp_session_get_data_cmd_cstr(ftp_session session);

/**
  @brief
    Set user's data command, in c-string.

  @param session
    User's connection session.
  @param data_cmd
    User's data command.
*/
void ftp_session_set_data_cmd_cstr(ftp_session session, char* data_cmd);

/**
  @brief
    Get start offset for STOR and RETR command.

  @param session
    User's connection session.

  @return
    The offset value.

  @warning
    To support files larger than 2gb, must compile it with D_FILE_OFFSET_BITS=64 compiler flag.
*/
off_t ftp_session_get_start_offset(ftp_session session);

/**
  @brief
    Set start offset for STOR and RETR command.

  @param session
    User's connection session.
  @param offset
    Start offset value.
*/
void ftp_session_set_start_offset(ftp_session session, off_t offset);

/**
  @brief
    Change directory to an upper level. When current directory is root, it will NOT go up.

  @param session
    User's connection session.
*/
void ftp_session_cdup(ftp_session session);

#endif  // LIQUID_FTP_SESSION_H_


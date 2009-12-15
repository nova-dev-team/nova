#ifndef LIQUID_FTP_SESSION_H_
#define LIQUID_FTP_SESSION_H_

#include "xdef.h"
#include "xstr.h"
#include "xnet.h"

struct ftp_session_impl;

typedef struct ftp_session_impl* ftp_session;

ftp_session ftp_session_new(xsocket cmd_sock, xstr host_addr);

void ftp_session_delete(ftp_session session);

int ftp_session_cmd_write(ftp_session session, void* data, int len);

int ftp_session_cmd_read(ftp_session session, void* buf, int max_len);

xbool ftp_session_is_logged_in(ftp_session session);

xbool ftp_session_is_username_given(ftp_session session);

void ftp_session_set_username_cstr(ftp_session session, char* cstr_username);

const char* ftp_session_get_username_cstr(ftp_session session);

const char* ftp_session_get_user_identifier_cstr(ftp_session session);

xbool ftp_session_auth_cstr(ftp_session, char* password);

xsuccess ftp_session_try_cwd_cstr(ftp_session session, char* new_path, xstr error_msg);

const char* ftp_session_get_cwd_cstr(ftp_session session);

xstr ftp_session_get_cwd(ftp_session session);

int ftp_session_is_user_aborted();

char ftp_session_get_trans_mode(ftp_session session);

char ftp_session_get_trans_type(ftp_session session);

void ftp_session_set_trans_type(ftp_session session, char type);

void ftp_session_prepare_data_service(ftp_session session, xserver_acceptor data_acceptor);

void ftp_session_wait_data_service();

void ftp_session_trigger_data_service();

xstr ftp_session_get_host_addr(ftp_session session);

char* ftp_session_get_host_ip_cstr(ftp_session session);

int ftp_session_get_data_server_port(ftp_session session);

const char* ftp_session_get_data_cmd_cstr(ftp_session session);

void ftp_session_set_data_cmd_cstr(ftp_session session, char* data_cmd);

#endif


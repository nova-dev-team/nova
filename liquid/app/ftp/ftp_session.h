#ifndef LIQUID_FTP_SESSION_H_
#define LIQUID_FTP_SESSION_H_

#include "xdef.h"
#include "xstr.h"
#include "xnet.h"

struct ftp_session_impl;

typedef struct ftp_session_impl* ftp_session;

ftp_session ftp_session_new(xsocket cmd_sock);

void ftp_session_delete(ftp_session session);

int ftp_session_cmd_write(ftp_session session, void* data, int len);

int ftp_session_cmd_read(ftp_session session, void* buf, int max_len);

xbool ftp_session_is_logged_in(ftp_session session);

void ftp_session_set_username(xstr username);

void ftp_session_auth(xstr password);

int ftp_session_is_user_aborted();

#endif


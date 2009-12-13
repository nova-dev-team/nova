#ifndef NDSS_FTP_SESSION_H_
#define NDSS_FTP_SESSION_H_

#include "xstr.h"

struct ftp_session_impl;

typedef struct ftp_session_impl* ftp_session;

ftp_session ftp_session_new();

void ftp_session_delete(ftp_session sess);

int ftp_session_is_logged_in();

void ftp_session_set_username(xstr username);

void ftp_session_auth(xstr password);

int ftp_session_is_user_aborted();



#endif


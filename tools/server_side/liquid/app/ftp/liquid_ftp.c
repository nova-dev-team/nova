#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <inttypes.h>
#include <sys/time.h>
#include <signal.h>
#include <sys/stat.h>

#include "xdef.h"
#include "xnet.h"
#include "xmemory.h"
#include "xstr.h"
#include "xutils.h"
#include "xkeepalive.h"
#include "xlog.h"
#include "xoption.h"

#include "ftp_acl.h"
#include "ftp_session.h"
#include "ftp_fs.h"

// set to 1 if we need to profile the ftp
#define PROFILE_FTP 0

void liquid_ftp_help() {
  printf("usage: liquid ftp [-p port|--port=port] [-b bind_addr|--bind=bind_addr] [--root=<root_folder>] [--keepalive]\n");
}

static void strip_trailing_crlf(char* str) {
  // abc\r\n -> abc
  int pos = strlen(str) - 1;
  while (pos >= 0 && (str[pos] == '\r' || str[pos] == '\n')) {
    str[pos] = '\0';
    pos--;
  }
}

static void add_comma_separated_data_server_addr(xstr rep, ftp_session session) {
  int data_server_port = ftp_session_get_data_server_port(session);
  char data_server_ip[16];
  int i;
  strcpy(data_server_ip, ftp_session_get_host_ip_cstr(session));

  xstr_printf(rep, "(");
  for (i = 0; data_server_ip[i] != '\0'; i++) {
    char ch = data_server_ip[i];
    if (ch == '.')
      xstr_append_char(rep, ',');
    else
      xstr_append_char(rep, ch);
  }

  xstr_printf(rep, ",%d,%d)\r\n", data_server_port / 256, data_server_port % 256);
}

static void reply(ftp_session session, const char* text) {
  xstr text_no_crlf = xstr_new();
#if PROFILE_FTP == 1
  struct timeval time_now;
  gettimeofday(&time_now, NULL);
  xlog_debug("[prof] ftp-reply at %d, %d (sec, usec)\n", (int) time_now.tv_sec, (int) time_now.tv_usec);
#endif  // #if PROFILE_FPT == 1
  xstr_set_cstr(text_no_crlf, text);
  xstr_strip(text_no_crlf, "\r\n");
  xlog_info("[rep %s] %s", ftp_session_get_user_identifier_cstr(session), xstr_get_cstr(text_no_crlf));
  ftp_session_cmd_write(session, (void *) text, strlen(text));
  xstr_delete(text_no_crlf);
}

static xsuccess get_request(ftp_session session, char* inbuf, int buf_size) {
  int cnt;
  // TODO ignore NOOP request

#if PROFILE_FTP == 1
  struct timeval time_now;
  gettimeofday(&time_now, NULL);
  xlog_debug("[prof] ftp-get_request start at %d, %d (sec, usec)\n", (int) time_now.tv_sec, (int) time_now.tv_usec);
#endif  // #if PROFILE_FPT == 1

  cnt = ftp_session_cmd_read(session, inbuf, buf_size);

#if PROFILE_FTP == 1
  gettimeofday(&time_now, NULL);
  xlog_debug("[prof] ftp-get_request done at %d, %d (sec, usec)\n", (int) time_now.tv_sec, (int) time_now.tv_usec);
#endif  // #if PROFILE_FPT == 1

  if (cnt == buf_size) {
    reply(session, "501 too long request\r\n");
    xlog_info("[ftp] client %s kicked because of too long request\n", ftp_session_get_user_identifier_cstr(session));
    return XFAILURE;
  } else if (cnt == 0) {
    xlog_info("[ftp] client %s prematurely disconnected\n", ftp_session_get_user_identifier_cstr(session));
    return XFAILURE;
  } else if (cnt == -1) {
    xlog_info("[ftp] client %s kicked because of socket error\n", ftp_session_get_user_identifier_cstr(session));
    return XFAILURE;
  }
  inbuf[cnt] = '\0';
  strip_trailing_crlf(inbuf);
  xlog_info("[req %s] %s\n", ftp_session_get_user_identifier_cstr(session), inbuf);

  return XSUCCESS;
}

static xbool can_read(ftp_session session, const char* path) {
  int priv_flag;
  xbool ret = XFALSE;
  if (ftp_path_privilege(ftp_session_get_username_cstr(session), path, &priv_flag) == XSUCCESS) {
    if (FTP_ACL_CAN_READ(priv_flag)) {
      ret = XTRUE;
    }
  }
  return ret;
}

static xbool can_read2(ftp_session session, const char* path, const char* sub_path) {
  xbool ret;
  xstr join_path = xstr_new();
  xjoin_path_cstr(join_path, path, sub_path);
  ret = can_read(session, xstr_get_cstr(join_path));
  xstr_delete(join_path);
  return ret;
}

static xbool can_write(ftp_session session, const char* path) {
  int priv_flag;
  xbool ret = XFALSE;
  if (ftp_path_privilege(ftp_session_get_username_cstr(session), path, &priv_flag) == XSUCCESS) {
    if (FTP_ACL_CAN_WRITE(priv_flag)) {
      ret = XTRUE;
    }
  }
  return ret;
}

static xbool can_write2(ftp_session session, const char* path, const char* sub_path) {
  xbool ret;
  xstr join_path = xstr_new();
  xjoin_path_cstr(join_path, path, sub_path);
  ret = can_write(session, xstr_get_cstr(join_path));
  xstr_delete(join_path);
  return ret;
}

static xbool can_del(ftp_session session, const char* path) {
  int priv_flag;
  xbool ret = XFALSE;
  if (ftp_path_privilege(ftp_session_get_username_cstr(session), path, &priv_flag) == XSUCCESS) {
    if (FTP_ACL_CAN_DEL(priv_flag)) {
      ret = XTRUE;
    }
  }
  return ret;
}

static xbool can_del2(ftp_session session, const char* path, const char* sub_path) {
  xbool ret;
  xstr join_path = xstr_new();
  xjoin_path_cstr(join_path, path, sub_path);
  ret = can_del(session, xstr_get_cstr(join_path));
  xstr_delete(join_path);
  return ret;
}

static void data_acceptor(xsocket data_xsock, void* args) {
  ftp_session session = (ftp_session) args;
  const char* data_cmd = ftp_session_get_data_cmd_cstr(session);
#if PROFILE_FTP == 1
  struct timeval time_now;
  gettimeofday(&time_now, NULL);
  xlog_debug("[prof] ftp-data_acceptor running at %d, %d (sec, usec)\n", (int) time_now.tv_sec, (int) time_now.tv_usec);
#endif  // #if PROFILE_FPT == 1

  // XXX ignore sigpipe, we encountered problems when there is conflicts
  // in ftp data random port
  // NOTE SIGPIPE is per-thread option, so set it in every thread!
  signal(SIGPIPE, SIG_IGN);

  if (xcstr_startwith_cstr(data_cmd, "LIST")) {
    xstr ls_data = xstr_new();
    xstr error_msg = xstr_new();
    if (ftp_fs_list_into_xstr(ftp_session_get_root_jail(session), ftp_session_get_cwd(session), ls_data, error_msg) == XSUCCESS) {
      xsocket_write(data_xsock, (const void *) xstr_get_cstr(ls_data), xstr_len(ls_data));
      reply(session, "226 transfer complete\r\n");
    } else {
      reply(session, xstr_get_cstr(error_msg));
    }
    xstr_delete(error_msg);
    xstr_delete(ls_data);

  } else if (xcstr_startwith_cstr(data_cmd, "RETR")) {
    xstr error_msg = xstr_new();
    if (ftp_fs_retr_file(data_xsock, ftp_session_get_root_jail(session), ftp_session_get_cwd(session), data_cmd + 5, ftp_session_get_start_offset(session), error_msg) == XSUCCESS) {
      reply(session, "226 transfer complete\r\n");
    } else {
      reply(session, xstr_get_cstr(error_msg));
    }
    xstr_delete(error_msg);
  } else if (xcstr_startwith_cstr(data_cmd, "STOR")) {
    xstr error_msg = xstr_new();

    // test if could store the file
    if (ftp_fs_could_stor_file(ftp_session_get_root_jail(session), ftp_session_get_cwd(session), data_cmd + 5, ftp_session_get_start_offset(session)) == XTRUE) {
      reply(session, "150 ok to send data\r\n");
      if (ftp_fs_stor_file(data_xsock, ftp_session_get_root_jail(session), ftp_session_get_cwd(session), data_cmd + 5, ftp_session_get_start_offset(session), error_msg) == XSUCCESS) {
        reply(session, "226 transfer complete\r\n");
      } else {
        reply(session, xstr_get_cstr(error_msg));
      }

    } else {
      xstr_printf(error_msg, "500 failed to create file '%s'\r\n", data_cmd + 5);
      reply(session, xstr_get_cstr(error_msg));
    }

    xstr_delete(error_msg);
  }
}

static void cmd_acceptor(xsocket client_xs, void* args) {
  int buf_size = 8192;
  xstr host_addr = (xstr) args;
  xstr root_jail = xstr_new();
  char* inbuf = xmalloc_ty(buf_size, char);
  char* outbuf = xmalloc_ty(buf_size, char);
  xbool stop_service = XFALSE;

  // XXX ignore sigpipe, we encountered problems when there is conflicts
  // in ftp data random port
  // NOTE SIGPIPE is per-thread option, so set it in every thread!
  signal(SIGPIPE, SIG_IGN);

  // client_xs will NOT be deleted by ftp_session, but will be deleted by xserver
  // host_addr will NOT be deleted by ftp_session, but will be deleted by ftp entry (liquid_ftp_service)
  ftp_session session = ftp_session_new(client_xs, host_addr);
  reply(session, "220 liquid ftp\r\n");

  // pre-login
  while (stop_service == XFALSE && ftp_session_is_logged_in(session) == XFALSE) {
    if (get_request(session, inbuf, buf_size) != XSUCCESS) {
      stop_service = XTRUE;
      break;
    }

    if (xcstr_startwith_cstr(inbuf, "USER")) {
      // check if command is correct
      if (strlen(inbuf) < 6) {
        reply(session, "501 please provide username\r\n");
      } else {
        ftp_session_set_username_cstr(session, inbuf + 5);
        reply(session, "331 password required\r\n");
      }

    } else if (xcstr_startwith_cstr(inbuf, "PASS")) {
      // check if command is correct
      if (strlen(inbuf) < 6) {
        reply(session, "501 please provide password\r\n");
      } else {
        if (ftp_session_auth_cstr(session, inbuf + 5)) {
          reply(session, "230 user logged in\r\n");
        } else {
          reply(session, "530 login incorrect\r\n");
        }
      }

    } else if (xcstr_startwith_cstr(inbuf, "FEAT")) {
      reply(session, "211-Features:\r\n");
      reply(session, " MDTM\r\n");
      reply(session, " REST STREAM\r\n");
      reply(session, " SIZE\r\n");
      reply(session, "211 End\r\n");

    } else if (xcstr_startwith_cstr(inbuf, "QUIT")) {
      reply(session, "211 see you\r\n");
      stop_service = XTRUE;

    } else {
      xstr rep = xstr_new();
      xstr_printf(rep, "500 unknown command '%s'\r\n", inbuf);
      reply(session, xstr_get_cstr(rep));
      xstr_delete(rep);
    }
  }

  // load the root_jail
  if (stop_service == XFALSE) {
    ftp_get_root_jail(ftp_session_get_username_cstr(session), root_jail);
    ftp_session_set_root_jail(session, xstr_get_cstr(root_jail));
    xlog_info("[ftp] root jail is %s\n", xstr_get_cstr(root_jail));
  }

  // post-login
  while (stop_service == XFALSE) {
    if (get_request(session, inbuf, buf_size) != XSUCCESS) {
      stop_service = XTRUE;
      break;
    }

    if (xcstr_startwith_cstr(inbuf, "USER")) {
      reply(session, "500 cannot change username\r\n");
    } else if (xcstr_startwith_cstr(inbuf, "QUIT")) {
      reply(session, "211 see you\r\n");
      stop_service = XTRUE;
    } else if (xcstr_startwith_cstr(inbuf, "SYST")) {
      reply(session, "215 UNIX Type: L8\r\n");
    } else if (xcstr_startwith_cstr(inbuf, "FEAT")) {
      reply(session, "211-Features:\r\n");
      reply(session, " MDTM\r\n");
      reply(session, " REST STREAM\r\n");
      reply(session, " SIZE\r\n");
      reply(session, "211 End\r\n");
    } else if (xcstr_startwith_cstr(inbuf, "PWD")) {
      xstr rep = xstr_new();
      xstr_printf(rep, "257 \"%s\" is current directory\r\n", ftp_session_get_cwd_cstr(session));
      reply(session, xstr_get_cstr(rep));
      xstr_delete(rep);
    } else if (xcstr_startwith_cstr(inbuf, "TYPE")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 please provide type\r\n");
      } else {
        char type = inbuf[5];
        if (type == 'a' || type == 'A') {
          ftp_session_set_trans_type(session, 'A');
          reply(session, "200 type set to A\r\n");
        } else if (type == 'i' || type == 'I') {
          ftp_session_set_trans_type(session, 'I');
          reply(session, "200 type set to I\r\n");
        } else {
          reply(session, "500 invalid TYPE command\r\n");
        }
      }

    } else if (xcstr_startwith_cstr(inbuf, "PASV")) {
      xstr rep = xstr_new();
      ftp_session_set_start_offset(session, 0);
      if (ftp_session_prepare_data_service(session, data_acceptor) == XSUCCESS) {
        xstr_set_cstr(rep, "227 entering passive mode ");
        add_comma_separated_data_server_addr(rep, session);
      } else {
        xstr_set_cstr(rep, "500 failed to open data socket\r\n");
      }
      reply(session, xstr_get_cstr(rep));
      xstr_delete(rep);
    } else if (xcstr_startwith_cstr(inbuf, "LIST")) {
      if (ftp_session_is_data_service_ready(session)) {
        if (can_read(session, ftp_session_get_cwd_cstr(session))) {
          reply(session, "150 here comes the listing\r\n");
          ftp_session_set_data_cmd_cstr(session, inbuf);
          ftp_session_trigger_data_service(session);
        } else {
          reply(session, "550 permission denied\r\n");
          ftp_session_discard_data_service(session);
        }
      } else {
        reply(session, "425 use PASV first\r\n");
      }

    } else if (xcstr_startwith_cstr(inbuf, "RETR")) {
      if (ftp_session_is_data_service_ready(session)) {
        if (strlen(inbuf) < 6) {
          reply(session, "501 please provide filename\r\n");
        } else {
          if (can_read2(session, ftp_session_get_cwd_cstr(session), inbuf + 5)) {
            reply(session, "150 sending file\r\n");
            ftp_session_set_data_cmd_cstr(session, inbuf);
            ftp_session_trigger_data_service(session);
          } else {
            reply(session, "550 permission denied\r\n");
            ftp_session_discard_data_service(session);
          }
        }
      } else {
        reply(session, "425 use PASV first\r\n");
      }
    } else if (xcstr_startwith_cstr(inbuf, "STOR")) {
      if (ftp_session_is_data_service_ready(session)) {
        if (strlen(inbuf) < 6) {
          reply(session, "501 please provide filename\r\n");
        } else {
          xbool file_exists = XFALSE;
          xbool perm_denied = XFALSE;
          xstr file_fullpath = xstr_new();
          struct stat st;
          xjoin_path_cstr(file_fullpath, ftp_session_get_cwd_cstr(session), inbuf + 5);
          if (lstat(xstr_get_cstr(file_fullpath), &st) == 0) {
            file_exists = XTRUE;
          }
          if (file_exists == XTRUE) {
            // appending to file
            if (can_write2(session, ftp_session_get_cwd_cstr(session), inbuf + 5) == XFALSE) {
              perm_denied = XTRUE;
            }
          } else {
            // creating new file
            if (can_write(session, ftp_session_get_cwd_cstr(session)) == XFALSE) {
              perm_denied = XTRUE;
            }
          }
          if (perm_denied == XTRUE) {
            reply(session, "550 permission denied\r\n");
            ftp_session_discard_data_service(session);
          } else { 
            // ok to write file
            ftp_session_set_data_cmd_cstr(session, inbuf);
            ftp_session_trigger_data_service(session);
          }
          xstr_delete(file_fullpath);
        }
      } else {
        reply(session, "425 use PASV first\r\n");
      }

    } else if (xcstr_startwith_cstr(inbuf, "CWD")) {
      if (strlen(inbuf) < 5) {
        reply(session, "501 invalid CWD command\r\n");
      } else {
        if (can_read2(session, ftp_session_get_cwd_cstr(session), inbuf + 4) == XTRUE) {
          xstr error_msg = xstr_new();
          if (ftp_session_try_cwd_cstr(session, inbuf + 4, error_msg) == XSUCCESS) {
            reply(session, "250 CWD command successful\r\n");
          } else {
            reply(session, xstr_get_cstr(error_msg));
          }
          xstr_delete(error_msg);
        } else {
          reply(session, "550 permission denied\r\n");
        }
      }
    } else if (xcstr_startwith_cstr(inbuf, "CDUP")) {
      ftp_session_cdup(session);
      reply(session, "250 directory succesfully changed\r\n");
    } else if (xcstr_startwith_cstr(inbuf, "MDTM")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 invalid MDTM command\r\n");
      } else {
        if (can_read2(session, ftp_session_get_cwd_cstr(session), inbuf + 5) == XTRUE) {
          xstr error_msg = xstr_new();
          xstr mdtm_str = xstr_new();
          if (ftp_fs_mdtm(ftp_session_get_root_jail(session), ftp_session_get_cwd_cstr(session), inbuf + 5, mdtm_str, error_msg) == XSUCCESS) {
            reply(session, xstr_get_cstr(mdtm_str));
          } else {
            reply(session, xstr_get_cstr(error_msg));
          }
          xstr_delete(mdtm_str);
          xstr_delete(error_msg);
        } else {
          reply(session, "550 permission denied\r\n");
        }
      }

    } else if (xcstr_startwith_cstr(inbuf, "SIZE")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 invalid SIZE command\r\n");
      } else {
        if (can_read2(session, ftp_session_get_cwd_cstr(session), inbuf + 5) == XTRUE) {
          xstr error_msg = xstr_new();
          xstr size_str = xstr_new();
          if (ftp_fs_size(ftp_session_get_root_jail(session), ftp_session_get_cwd_cstr(session), inbuf + 5, size_str, error_msg) == XSUCCESS) {
            reply(session, xstr_get_cstr(size_str));
          } else {
            reply(session, xstr_get_cstr(error_msg));
          }
          xstr_delete(size_str);
          xstr_delete(error_msg);
        } else {
          reply(session, "550 permission denied\r\n");
        }
      }

    } else if (xcstr_startwith_cstr(inbuf, "REST")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 invalid REST command\r\n");
      } else {
        off_t offset;
        xstr rep = xstr_new();
        sscanf(inbuf + 5, "%"PRId64"", &offset);
        ftp_session_set_start_offset(session, offset);
        xstr_printf(rep, "350 restarting at %ld\r\n", offset);
        reply(session, xstr_get_cstr(rep));
        xstr_delete(rep);
      }
    } else if (xcstr_startwith_cstr(inbuf, "MKD")) {
      if (strlen(inbuf) < 5) {
        reply(session, "501 invalid MKD command\r\n");
      } else {
        if (can_write(session, ftp_session_get_cwd_cstr(session)) == XTRUE) {
          xstr error_msg = xstr_new();
          if (ftp_fs_mkdir(ftp_session_get_root_jail(session), ftp_session_get_cwd_cstr(session), inbuf + 4, error_msg) == XSUCCESS) {
            reply(session, "257 mkdir success\r\n");
          } else {
            reply(session, xstr_get_cstr(error_msg));
          }
          xstr_delete(error_msg);
        } else {
          reply(session, "550 permission denied\r\n");
        }
      }
    } else if (xcstr_startwith_cstr(inbuf, "DELE")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 invalid DELE command\r\n");
      } else {
        if (can_del2(session, ftp_session_get_cwd_cstr(session), inbuf + 5)) {
          xstr error_msg = xstr_new();
          if (ftp_fs_dele(ftp_session_get_root_jail(session), ftp_session_get_cwd_cstr(session), inbuf + 5, error_msg) == XSUCCESS) {
            reply(session, "250 delete done\r\n");
          } else {
            reply(session, xstr_get_cstr(error_msg));
          }
          xstr_delete(error_msg);
        } else {
          reply(session, "550 permission denied\r\n");
        }
      }
    } else if (xcstr_startwith_cstr(inbuf, "ALLO")) {
      reply(session, "202 ALLO command ignored\r\n");

    } else if (xcstr_startwith_cstr(inbuf, "SITE")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 invalid SITE command\r\n");
      } else {
        // TODO acl on site command
        xstr error_msg = xstr_new();
        if (ftp_fs_site_cmd(ftp_session_get_root_jail(session), ftp_session_get_cwd_cstr(session), inbuf + 5, error_msg) == XSUCCESS) {
          reply(session, "200 SITE command succeeded\r\n");
        } else {
          reply(session, xstr_get_cstr(error_msg));
        }
        xstr_delete(error_msg);
      }

    } else if (xcstr_startwith_cstr(inbuf, "RMD")) {
      if (strlen(inbuf) < 5) {
        reply(session, "501 invalid RMD command!\r\n");
      } else {
        if (can_del2(session, ftp_session_get_cwd_cstr(session), inbuf + 4) == XTRUE) {
          xstr error_msg = xstr_new();
          if (ftp_fs_dele(ftp_session_get_root_jail(session), ftp_session_get_cwd_cstr(session), inbuf + 4, error_msg) == XSUCCESS) {
            reply(session, "250 RMD done\r\n");
          } else {
            reply(session, xstr_get_cstr(error_msg));
          }
          xstr_delete(error_msg);
        } else {
          reply(session, "550 permission denied\r\n");
        }
      }
    } else if (xcstr_startwith_cstr(inbuf, "RNFR")) {
      if (strlen(inbuf) < 6) {
        reply(session, "501 invalid RNFR command\r\n");
      } else {
        if (can_del2(session, ftp_session_get_cwd_cstr(session), inbuf + 5) == XTRUE) {
          xstr error_msg = xstr_new();
          xstr rnfr = xstr_new();
          xstr_set_cstr(rnfr, inbuf + 5);
          reply(session, "350 RNFR ok\r\n");
          if (get_request(session, inbuf, buf_size) != XSUCCESS) {
            stop_service = XTRUE;
          } else {
            if (xcstr_startwith_cstr(inbuf, "RNTO")) {
              // TODO acl on RNTO
              if (strlen(inbuf) < 6) {
                reply(session, "501 invalid RNTO command!\r\n");
              } else {
                if (ftp_fs_rename(ftp_session_get_root_jail(session), ftp_session_get_cwd_cstr(session), xstr_get_cstr(rnfr), inbuf + 5, error_msg) != XSUCCESS) {
                  reply(session, xstr_get_cstr(error_msg));
                } else {
                  reply(session, "250 rename ok\r\n");
                }
              }
            } else {
              reply(session, "500 need RNTO\r\n");
            }
          }
          xstr_delete(rnfr);
          xstr_delete(error_msg);
        } else {
          reply(session, "550 permission denied\r\n");
        }
      }
    } else {
      xstr rep = xstr_new();
      xstr_printf(rep, "500 unknown command '%s'\r\n", inbuf);
      reply(session, xstr_get_cstr(rep));
      xstr_delete(rep);
    }
  }
  ftp_session_delete(session);
  xfree(inbuf);
  xfree(outbuf);
  xstr_delete(root_jail);
}


static xsuccess liquid_ftp_service(xstr host, int port, int backlog) {
  xsuccess ret;
  void* args = host;
  char serv_mode = 't'; // serv in new thread
  xserver xs = xserver_new(host, port, backlog, cmd_acceptor, XUNLIMITED, serv_mode, args);

  // XXX ignore sigpipe, we encountered problems when there is conflicts
  // in ftp data random port
  // NOTE SIGPIPE is per-thread option, so set it in every thread!
  signal(SIGPIPE, SIG_IGN);

  if (xs == NULL) {
    xlog_fatal("[ftp] in liquid_ftp_service(): failed to init xserver!\n");
    ret = XFAILURE;
  } else {
    xlog_info("[ftp] ftp server started on %s:%d\n", xstr_get_cstr(host), port);
    ret = xserver_serve(xs);  // xserver is self destrying, after service, it will destroy it self
  }
  // don't need to release "host", since it is managed by xserver
  return ret;
}

int liquid_ftp_real(int argc, char* argv[]) {
  int port = 8021;
  xstr bind_addr = xstr_new();  // will be sent into liquid_ftp_service(), and work as a component of xserver. will be destroyed when xserver is deleted
  int back_log = 10;
  const char* single_user = "";
  const char* single_pwd = "";
  const char* single_root = "/";
  xsuccess ret = XFAILURE;

  xoption xopt = xoption_new();
  xoption_parse_with_xconf(xopt, argc, argv);

  xstr_set_cstr(bind_addr, "0.0.0.0");

  // set the root jail, where action will be locked inside
  if (strlen(getenv("HOME")) > 0) {
    single_root = getenv("HOME");
  }

  if (xoption_has(xopt, "p")) {
    if (xoption_get_size(xopt, "p") == 0) {
      xlog_error("error in command line args: '-p' must be followed by port number!\n");
    } else {
      port = atoi(xoption_get(xopt, "p"));
    }
  }
  if (xoption_has(xopt, "port")) {
    port = atoi(xoption_get(xopt, "port"));
  }
  if (xoption_has(xopt, "b")) {
    if (xoption_get_size(xopt, "b") == 0) {
      xlog_error("error in command line args: '-b' must be followed by bind address!\n");
    } else {
      xstr_set_cstr(bind_addr, xoption_get(xopt, "b"));
    }
  }
  if (xoption_has(xopt, "bind")) {
    xstr_set_cstr(bind_addr, xoption_get(xopt, "bind"));
  }
  if (xoption_has(xopt, "backlog")) {
    back_log = atoi(xoption_get(xopt, "backlog"));
  }

  if (xoption_has(xopt, "simple")) {
    // single user mode
    xbool readonly = XFALSE;
    if (xoption_has(xopt, "user")) {
      single_user = xoption_get(xopt, "user");
    }
    if (xoption_has(xopt, "passwd")) {
      single_pwd = xoption_get(xopt, "passwd");
    }
    if (xoption_has(xopt, "root")) {
      single_root = xoption_get(xopt, "root");
    }
    if (xoption_has(xopt, "readonly")) {
      if (xoption_get(xopt, "readonly") == NULL
          || xoption_get(xopt, "readonly")[0] == '1'
          || xoption_get(xopt, "readonly")[0] == 't'
          || xoption_get(xopt, "readonly")[0] == 'T') {
        readonly = XTRUE;
      }
    }
    ftp_acl_single_user_mode(single_user, single_pwd, single_root, readonly);
    xlog_info("[ftp] running in simple mode\n");
  } else {
    // multi user mode
    ftp_acl_multi_user_mode("liquid_ftp.sqlite3");
    xlog_info("[ftp] running in advanced mode\n");
  }

  xoption_delete(xopt);
  ret = liquid_ftp_service(bind_addr, port, back_log);
  ftp_acl_finalize();
  return ret;
}

xsuccess liquid_ftp(int argc, char* argv[]) {
  xsuccess ret = XSUCCESS;
  xoption xopt = xoption_new();
  xoption_parse(xopt, argc, argv);
  // check if needed help
  if (xoption_has(xopt, "help") || xoption_has(xopt, "h")) {
    liquid_ftp_help();
  } else {
    // check if keepalive is required
    if (xoption_has(xopt, "keepalive")) {
      ret = xkeep_alive(liquid_ftp_real, argc, argv);
    } else {
      ret = liquid_ftp_real(argc, argv);
    }
  }
  xoption_delete(xopt);
  return ret;
}


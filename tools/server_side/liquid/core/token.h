#ifndef CORE_TOKEN_H_
#define CORE_TOKEN_H_

#include "xdef.h"
#include "xnet.h"

/**
  @author
    Santa Zhang

  @brief
    The token for partitioning data to data servers.

  @file
    token.h
*/

/**
  @brief
    The status of the token. Could be:
    0. invalid (reserved)
    1. holding (holds everything inside the token range)
    2. migrate_out (everything inside the token range is being migrated out to another host)
    3. migrate_in (everything inside the token range is being migrated from another host)
*/
typedef enum {
  TKN_INVALID,  ///< @brief Invalid status, reserved.
  TKN_HOLDING,  ///< @brief Holding contents inside that range.
  TKN_MIGRATE_OUT,  ///< @brief Contents are being migrated out to another host.
  TKN_MIGRATE_IN  ///< @brief Contents are being migrated in from another host.
} token_status;

/**
  @brief
    The token for partitioning data to data servers. MD5 values are partitioned into different sections according to the last 3 bytes.
*/
typedef struct {
  int start;  ///< @brief Start position of the token.
  int end;  ///< @brief End position of the token, inclusive.

  token_status status;  ///< @brief The status of the token range.
} token;

/**
  @brief
    Hidden implementation of token set. The token set is like a search tree.
*/
struct token_set_impl;

/**
  @brief
    The token set interface exposed to users.
*/
typedef struct token_set_impl* token_set;

/**
  @brief
    Check if a md5 value is contained in the token.

  @param tkn
    The token range to be checked.
  @param md5
    The md5 value to be checked.
*/
xbool token_include(token tkn, unsigned char* md5);

/**
  @brief
    Create token set, only consider this node.

  @return
    The generated token set.
*/
token_set create_token_set();

/**
  @brief
    Create token set, according to information shared by other nodes.

  @return
    NULL if errro happened (connection failed, etc), otherwise return the created token set.
*/
token_set create_token_set_from_peer(xsocket xsock);


#endif  // #ifndef CORE_TOKEN_H_

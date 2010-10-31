#include <string.h>
#include <assert.h>

#include "xmemory.h"

#include "lqd_defs.h"
#include "lqd_token.h"

/**
 * @brief
 *  The token mask.
 */
#define TOKEN_MASK 'x'

/**
 * @brief
 *  The hidden implementation of token.
 */
struct token_impl {
  char* start;  ///<  @brief The start mask of the token.
  char* stop; ///<  @brief The stop mask of the token.
};

// create a new token, not initialized with start and stop values
static token token_new_raw() {
  int i;
  token tkn = xmalloc_ty(1, struct token_impl);
  tkn->start = xmalloc_ty(FILE_KEY_HEX_LENGTH + 1, char);
  tkn->stop = xmalloc_ty(FILE_KEY_HEX_LENGTH + 1, char);
  for (i = 0; i < FILE_KEY_HEX_LENGTH; i++) {
    tkn->start[i] = tkn->stop[i] = TOKEN_MASK;
  }
  tkn->start[FILE_KEY_HEX_LENGTH] = tkn->stop[FILE_KEY_HEX_LENGTH] = '\0';
  return tkn;
}

token token_new(XIN const char* start, XIN const char* stop) {
  int start_len, stop_len, i;
  xbool invalid_token = XFALSE;
  token tkn = token_new_raw();
  start_len = strlen(start);
  stop_len = strlen(stop);
  if (start_len != stop_len || start_len > FILE_KEY_HEX_LENGTH) {
    invalid_token = XTRUE;
  } else {
    // fill in the start, stop mask
    for (i = 0; i < start_len; i++) {
      if (!(IS_LOWERCASE_HEX_CHAR(start[i]) && IS_LOWERCASE_HEX_CHAR(stop[i]))) {
        invalid_token = XTRUE;
        break;
      }
      tkn->start[FILE_KEY_HEX_LENGTH - start_len + i] = start[i];
      tkn->stop[FILE_KEY_HEX_LENGTH - start_len + i] = stop[i];
    }
  }
  if (invalid_token == XTRUE) {
    token_delete(tkn);
    tkn = NULL;
  }
  return tkn;
}

// a helper function to get the token mask size
static int token_mask_size(XIN token tkn) {
  int mask_size = -1, i;
  for (i = 0; i < FILE_KEY_HEX_LENGTH; i++) {
    if (tkn->start[i] != TOKEN_MASK) {
      mask_size = i;
      break;
    }
  }
  assert(mask_size >= 0);
  return mask_size;
}

xsuccess token_new_by_split(XIN token tkn, XOUT token* splt1, XOUT token* splt2) {
  xsuccess ret = XFAILURE;
  // TODO
  // algorithm:
  // find the 'pos' where start[pos] != stop[pos]
  // if no such pos // start == stop
  //  report failure if mask_size == 0
  //  sp1.start = '0'..start, sp1.stop = '7'..stop // .. means strcat
  //  sp2.start = '8'..start, sp2.stop = 'f'..stop
  // else // pos found
  //  mid = (start[pos] + stop[pos]) / 2
  //  sp1.start = start, sp1.stop = start[0~pos-1]..mid..'f' until end
  //  sp2.start = start[0~pos-1]..mid+1..'0' until end, sp2.stop = stop
  int pos = -1, i;
  // find the 'pos' where start[pos] != stop[pos]
  for (i = 0; i < FILE_KEY_HEX_LENGTH; i++) {
    if (tkn->start[i] != tkn->stop[i]) {
      pos = i;
      break;
    }
  }
  if (pos == -1) {
    // if no such pos // start == stop
    int mask_size = token_mask_size(tkn);
    if (mask_size == 0) {
      //  report failure if mask_size == 0
      ret = XFAILURE;
    } else {
      //  sp1.start = '0'..start, sp1.stop = '7'..stop // .. means strcat
      //  sp2.start = '8'..start, sp2.stop = 'f'..stop
      *splt1 = token_new_raw();
      strcpy((*splt1)->start, tkn->start);
      strcpy((*splt1)->stop, tkn->stop);
      (*splt1)->start[mask_size - 1] = '0';
      (*splt1)->stop[mask_size - 1] = '7';

      *splt2 = token_new_raw();
      strcpy((*splt2)->start, tkn->start);
      strcpy((*splt2)->stop, tkn->stop);
      (*splt2)->start[mask_size - 1] = '8';
      (*splt2)->stop[mask_size - 1] = 'f';

      ret = XSUCCESS;
    }
  } else {
    // else // pos found
    int i;
    //  mid = (start[pos] + stop[pos]) / 2
    int start_val = HEX_CHAR_TO_DEC(tkn->start[pos]);
    int stop_val = HEX_CHAR_TO_DEC(tkn->stop[pos]);
    int mid = (start_val + stop_val) / 2;

    //  sp1.start = start, sp1.stop = start[0~pos-1]..mid..'f' until end
    *splt1 = token_new_raw();
    strcpy((*splt1)->start, tkn->start);
    strcpy((*splt1)->stop, tkn->stop);
    (*splt1)->stop[pos] = DEC_TO_HEX_CHAR(mid);
    for (i = pos + 1; i < FILE_KEY_HEX_LENGTH; i++) {
      (*splt1)->stop[i] = 'f';
    }

    //  sp2.start = start[0~pos-1]..mid+1..'0' until end, sp2.stop = stop
    *splt2 = token_new_raw();
    strcpy((*splt2)->start, tkn->start);
    strcpy((*splt2)->stop, tkn->stop);
    (*splt2)->start[pos] = DEC_TO_HEX_CHAR(mid + 1);
    for (i = pos + 1; i < FILE_KEY_HEX_LENGTH; i++) {
      (*splt2)->start[i] = '0';
    }

    ret = XSUCCESS;
  }
  return ret;
}

const char* token_get_start_cstr(XIN token tkn) {
  return tkn->start;
}

const char* token_get_stop_cstr(XIN token tkn) {
  return tkn->stop;
}

xbool token_include_cstr(XIN token tkn, XIN const char* key_text) {
  xbool ret = XFALSE;
  int mask_size = token_mask_size(tkn);
  if (strcmp(tkn->start + mask_size, key_text + mask_size) <= 0 &&
      strcmp(key_text + mask_size, tkn->stop + mask_size) <= 0) {

    ret = XTRUE;
  } else {
    ret = XFALSE;
  }
  return ret;
}

// helper function used by token_include_key_bytes()
// return 0 for equal, -1 if hex < bytes, 1 if hex > bytes
static int hex_char_cmp_bytes(XIN char* hex, XIN xbyte* bytes) {
  int i;
  for (i = 0; i < FILE_KEY_HEX_LENGTH; i++) {
    int val_chk = -1;
    int hex_chk = HEX_CHAR_TO_DEC(hex[i]);
    if (hex[i] == TOKEN_MASK) {
      continue;
    }
    if (i % 2 == 0) {
      // higher part of a byte should be checked
      val_chk = ((bytes[i / 2]) >> 4) & 0xf;
    } else {
      val_chk = (bytes[i / 2]) & 0xf;
    }
    assert(val_chk >= 0);
    if (hex_chk > val_chk) {
      return 1;
    } else if (hex_chk < val_chk) {
      return -1;
    }
  }
  return 0;
}

xbool token_include_key_bytes(XIN token tkn, XIN xbyte* key_bytes) {
  xbool ret = XFALSE;
  xbool geq_start = (hex_char_cmp_bytes(tkn->start, key_bytes) <= 0);
  xbool leq_stop = (hex_char_cmp_bytes(tkn->stop, key_bytes) >= 0);
  if (geq_start && leq_stop) {
    ret = XTRUE;
  } else {
    ret = XFALSE;
  }
  return ret;
}

void token_delete(XIN token tkn) {
  xfree(tkn->start);
  xfree(tkn->stop);
  xfree(tkn);
}


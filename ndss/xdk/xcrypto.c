#include "xcrypto.h"
#include "xmemory.h"

#include "crypto/md5/md5.h"
#include "crypto/sha1/sha1.h"

/**
  @brief
    Wrapper around 3rd party md5 lib.
*/
struct xmd5_impl {
  md5_word_t count[2];  ///< @brief Variable for 3rd party lib.
  md5_word_t abcd[4]; ///< @brief Variable for 3rd party lib.
  md5_byte_t buf[64]; ///< @brief Variable for 3rd party lib.
};

xmd5 xmd5_new() {
  xmd5 xm = xmalloc_ty(1, struct xmd5_impl);
  md5_init((md5_state_t *) xm); // call 3rd party lib
  return xm;
}

void xmd5_feed(xmd5 xm, const void* data, int size) {
  md5_append((md5_state_t *) xm, (md5_byte_t *) data, size);
}

void xmd5_result(xmd5 xm, unsigned char* result) {
  md5_finish((md5_state_t *) xm, (md5_byte_t *) result);
}

void xmd5_delete(xmd5 xm) {
  xfree(xm);
}

/**
  @brief
    Wrapper around 3rd party sha1 lib.
*/
struct xsha1_impl {
  unsigned message_digest[5]; ///< @brief Variable for 3rd party lib.
  unsigned length_low;  ///< @brief Variable for 3rd party lib.
  unsigned length_high; ///< @brief Variable for 3rd party lib.
  unsigned char message_block[64];  ///< @brief Variable for 3rd party lib.
  int message_block_index;  ///< @brief Variable for 3rd party lib.
  int computed; ///< @brief Variable for 3rd party lib.
  int corrupted;  ///< @brief Variable for 3rd party lib.
};

xsha1 xsha1_new() {
  xsha1 xsh = xmalloc_ty(1, struct xsha1_impl);
  SHA1Reset((SHA1Context *) xsh);
  return xsh;
}

void xsha1_feed(xsha1 xsh, void* data, int size) {
  SHA1Input((SHA1Context *) xsh, data, size);
}

int xsha1_result(xsha1 xsh, unsigned int* result) {
  int ret = SHA1Result((SHA1Context *) xsh);
  int i;
  for (i = 0; i < 5; i++) {
    result[i] = xsh->message_digest[i];
  }
  return ret;
}

void xsha1_delete(xsha1 xsh) {
  xfree(xsh);
}


#include <stdio.h>
#include <string.h>

#include "xcrypto.h"
#include "xmemory.h"

int main() {
  static const char *const test[7*2] = {
	"", "d41d8cd98f00b204e9800998ecf8427e",
	"a", "0cc175b9c0f1b6a831c399e269772661",
	"abc", "900150983cd24fb0d6963f7d28e17f72",
	"message digest", "f96b697d7cb7938d525a2f31aaf161d0",
	"abcdefghijklmnopqrstuvwxyz", "c3fcd3d76192e4007dfb496cca67e13b",
	"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
				"d174ab98d277d9f5a5611c2c9f419d9f",
	"12345678901234567890123456789012345678901234567890123456789012345678901234567890", "57edf4a22be3c955ac49da2e2107b67a"
  };
  unsigned char digest[16];
  char hex_output[16 * 2 + 1];
  int i, j;
  for (i = 0; i < 7 * 2; i += 2) {
    xmd5 xm = xmd5_new();
    xmd5_feed(xm, test[i], strlen(test[i]));
    xmd5_result(xm, digest);
    for (j = 0; j < 16; j++) {
      sprintf(hex_output + j * 2, "%02x", digest[j]);
    }
    if (strcmp(hex_output, test[i + 1]) == 0) {
      printf("%s %s match success!\n", hex_output, test[i + 1]);
    }
    xmd5_delete(xm);
  }
  return xmem_usage();
}


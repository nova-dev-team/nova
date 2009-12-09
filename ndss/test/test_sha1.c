#include <stdio.h>
#include <string.h>

#include "xmemory.h"
#include "xcrypto.h"

/*
 *  Define patterns for testing
 */
#define TESTA   "abc"
#define TESTB_1 "abcdbcdecdefdefgefghfghighij"
#define TESTB_2 "hijkijkljklmklmnlmnomnopnopq"
#define TESTB   TESTB_1 TESTB_2
#define TESTC   "a"

int main()
{
  
    int i;
    unsigned message_digest[5];
    xsha1 xsh = xsha1_new();

    /*
     *  Perform test A
     */
    printf("\nTest A: 'abc'\n");

    xsha1_feed(xsh, TESTA, strlen(TESTA));
    if (xsha1_result(xsh, message_digest) == 0) {
        fprintf(stderr, "ERROR-- could not compute message digest\n");
    }
    else
    {
        printf("\t");
        for(i = 0; i < 5 ; i++)
        {
            printf("%X ", message_digest[i]);
        }
        printf("\n");
        printf("Should match:\n");
        printf("\tA9993E36 4706816A BA3E2571 7850C26C 9CD0D89D\n");
    }
    xsha1_delete(xsh);

    printf("TESTB\n");
    xsh = xsha1_new();
    xsha1_feed(xsh, TESTB, strlen(TESTB));
    if (xsha1_result(xsh, message_digest) == 0) {
        fprintf(stderr, "ERROR-- could not compute message digest\n");
    }
    else
    {
        printf("\t");
        for(i = 0; i < 5 ; i++)
        {
            printf("%X ", message_digest[i]);
        }
        printf("\n");
        printf("Should match:\n");
        printf("\t84983E44 1C3BD26E BAAE4AA1 F95129E5 E54670F1\n");
    }
    xsha1_delete(xsh);

    xsh = xsha1_new();
    for(i = 1; i <= 1000000; i++) {
        xsha1_feed(xsh, (const unsigned char *) TESTC, 1);
    }
    if (xsha1_result(xsh, message_digest) == 0) {
        fprintf(stderr, "ERROR-- could not compute message digest\n");
    }
    else
    {
        printf("\t");
        for(i = 0; i < 5 ; i++)
        {
            printf("%X ", message_digest[i]);
        }
        printf("\n");
        printf("Should match:\n");
        printf("\t34AA973C D4C4DAA4 F61EEB2B DBAD2731 6534016F\n");
    }
    xsha1_delete(xsh);
    return xmem_usage();
}


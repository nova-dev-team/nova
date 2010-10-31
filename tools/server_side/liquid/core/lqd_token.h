#ifndef LIQUID_CORE_TOKEN_H_
#define LIQUID_CORE_TOKEN_H_

/**
 * @brief
 *  A token represents a range of key values.
 *
 * @author
 *  Santa Zhang
 *
 * @file
 *  lqd_token.h
 */

#include "xdef.h"

/**
* @brief
*  Check if a char is lowercase hex.
*
* @param ch
*  The char to be checked.
*
* @return
*  If the char is lowercase hex.
*/
#define IS_LOWERCASE_HEX_CHAR(ch) (('0' <= (ch) && (ch) <= '9') || ('a' <= (ch) && (ch) <= 'f'))

/**
* @brief
*  Convert a hex char to an dec value. The hex char should be in '0' ~ '9' or 'a' ~ 'f'.
*
* @param ch
*  The hex char to be checked.
*
* @return
*  The dec value.
*/
#define HEX_CHAR_TO_DEC(ch)  ((ch) >= 'a' ? (((ch) - 'a') + 10) : ((ch) - '0'))

/**
 * @brief
 *  Convert a decimal value (0~15) to corresponding hex char.
 *
 * @param val
 *  The decimal value to be checked.
 *
 * @return
 *  The hex char.
 */
#define DEC_TO_HEX_CHAR(val) ((char)((val) >= 10 ? (((val) - 10) + 'a') : ((val) + '0')))


// hidden implementation
struct token_impl;

/**
 * @brief
 *  The token type
 */
typedef struct token_impl* token;

/**
 * @brief
 *  Create a new token with given range.
 *
 * @param start
 *  The start postfix of the range.
 * @param stop
 *  The stop postfix of the range. (included in the range)
 *
 * @return
 *  Return NULL if parameters are not correct. The start and stop parameters
 *  must have same length, not larger than 40 bytes (sha1 hash), only have 0~9, a~f.
 */
token token_new(XIN const char* start, XIN const char* stop);

/**
 * @brief
 *  Split a token into 2 smaller tokens.
 *
 * @param tkn
 *  The token to be splitted.
 * @param splt1
 *  Output of the splitted token 1, not initialized!
 * @param splt2
 *  Output of the splitted token 2, not initialized!
 *
 * @return
 *  Return XSUCCESS if split successful.
 *  If split is not successful, then splt1 and splt2 will not be initialized.
 *
 * @warning
 *  Note that the splitted tokens should not be initialized!
 */
xsuccess token_new_by_split(XIN token tkn, XOUT token* splt1, XOUT token* splt2);

/**
 * @brief
 *  Get the starting hash of a token.
 *
 * @param tkn
 *  The token we are interested in.
 *
 * @return
 *  The starting hash.
 */
const char* token_get_start_cstr(XIN token tkn);

/**
 * @brief
 *  Get the stop hash of a token.
 *
 * @param tkn
 *  The token we are interested in.
 *
 * @return
 *  The stop hash.
 */
const char* token_get_stop_cstr(XIN token tkn);

/**
 * @brief
 *  Test if a key is included in the token.
 *
 * @param tkn
 *  The token we are interested in.
 * @param key_text
 *  The key, in plain text. 40 bytes long, lowercase.
 *
 * @return
 *  XTRUE if included.
 */
xbool token_include_cstr(XIN token tkn, XIN const char* key_text);

/**
 * @brief
 *  Test if a key is included in the token.
 *
 * @param tkn
 *  The token we are interested in.
 * @param key_bytes
 *  The key, in binary bit values. 20 bytes long.
 *
 * @return
 *  XTRUE if included.
 */
xbool token_include_key_bytes(XIN token tkn, XIN xbyte* key_bytes);

/**
 * @brief
 *  Delete a token.
 *
 * @param tkn
 *  The token to be deleted.
 */
void token_delete(XIN token tkn);

#endif  // LIQUID_CORE_TOKEN_H_


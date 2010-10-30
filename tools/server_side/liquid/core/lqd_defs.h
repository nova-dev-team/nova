#ifndef CORE_DEFS_H_
#define CORE_DEFS_H_

/**
  @brief
    Helpful \#define's for liquid node server.

  @author
    Santa Zhang

  @file
    lqd_defs.h
*/

/**
  @brief
    The version of liquid node server, in c-string.
*/
#define LIQUID_VER_CSTR "0.0.1"

/**
  @brief
    The version value of liquid node server, in XX.YY.ZZ format.
 */
#define LIQUID_VER_VALUE 0x000001

/**
  @brief
    Determines number of levels of directory layers, where the files should be hashed into.
    This will prevent too big directory size, speed up filesystem querying.
*/
#define FOLDER_LEVELS 3

/**
  @brief
    Determines the number of bits in the file key. Since we are using SHA-1 to hash file names,
    this is 160 bits.
*/
#define FILE_KEY_BITS 160

/**
  @brief
    Calculates bytes in file key.
*/
#define FILE_KEY_BYTES (FILE_KEY_BITS / 8)

/**
 * @brief
 *  Calculates the string length of the file key, in hex notation.
 */
#define FILE_KEY_HEX_LENGTH (FILE_KEY_BITS / 4)

#endif  // #ifndef CORE_DEFS_H_


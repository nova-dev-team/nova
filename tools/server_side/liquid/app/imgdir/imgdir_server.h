#ifndef IMGDIR_SERVER_H_
#define IMGDIR_SERVER_H_

/**
 * @brief
 *  Service as image info directory.
 *
 * @file
 *  imgdir_server.h
 *
 * @author
 *  Santa Zhang
 */

#include "xdef.h"

#include "imgdir_fsdb.h"

/**
  @brief
    Implementation of imgdir_server model.
*/
struct imgdir_server_impl {
  fsdb fs;  ///< @brief Filesystem database handle.
  fs_cache root;  ///< @brief Root of the filesystem.
};


/**
  @brief
    Defines handler of imgdir_server model.
*/
typedef struct imgdir_server_impl* imgdir_server;

/**
 * @brief
 *  Entry of the imgdir utility.
 *
 * @param argc
 *  Number of command line args.
 * @param argv
 *  Array of command line args.
 *
 * @return
 *  Whether the tool runs successfully.
 */
xsuccess imgdir_server_main(int argc, char* argv[]);

/**
 * @brief
 *  Display help message for imgdir utility.
 */
void imgdir_print_help();

#endif  // IMGDIR_SERVER_H_


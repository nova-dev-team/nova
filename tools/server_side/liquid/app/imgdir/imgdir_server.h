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


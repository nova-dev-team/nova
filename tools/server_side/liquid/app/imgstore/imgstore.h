#ifndef LQD_IMAGE_STORE_H_
#define LQD_IMAGE_STORE_H_

/**
 * @brief
 *  Image storge service node.
 *
 * @author
 *  Santa Zhang
 *
 * @file
 *  imgstore.h
 */

#include "xdef.h"

/**
 * @brief
 *  Entrance to service as a virtual machine disk image storage server.
 *
 * @param argc
 *  Number of command line arguments. Should be directly given from main(argc, argv)
 * @param argv
 *  Array of command line arguments. Should be directly given from main(argc, argv)
 *
 * @return
 *  Whether the service is successful.
 */
xsuccess liquid_imgstore(int argc, char* argv[]);

/**
 * @brief
 *  Print help info.
 */
void liquid_imgstore_help();

#endif  // LQD_IMAGE_STORE_H_


#ifndef IMG_MOUNT_H_
#define IMG_MOUNT_H_

/**
 * @brief
 *  Mount a VM disk filesystem.
 *
 * @file
 *  imgmount.h
 *
 * @author
 *  Santa Zhang
 */

#include "xdef.h"

/**
 * @brief
 *  Entry of the imgmount utility.
 *
 * @param argc
 *  Number of command line arguments.
 * @param argv
 *  Array of command line arguments.
 *
 * @return
 *  Whether the utility runs successfully.
 */
xsuccess imgmount_main(int argc, char* argv[]);

/**
 * @brief
 *  Display help info for imgmount utility.
 */
void imgmount_print_help();

#endif  // IMG_MOUNT_H_


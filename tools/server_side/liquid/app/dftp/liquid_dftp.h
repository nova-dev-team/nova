#ifndef LIQUID_DFTP_H_
#define LIQUID_DFTP_H_

#include "xdef.h"

/**
  @author
    Santa Zhang

  @file
    liquid_dftp.h

  @brief
    Serving as an distributed FTP server.
*/

/**
  @brief
    Distributed FTP service module, must connect to a liquid node server.

  @param argc
    The argc from main().
  @param argv
    The argv from main().

  @return
    On error return XFAILURE. Other wise the function blocks.
*/
xsuccess liquid_dftp(int argc, char* argv[]);

/**
  @brief
    Display help message on DFTP module.
*/
void liquid_dftp_help();

#endif // LIQUID_DFTP_H_


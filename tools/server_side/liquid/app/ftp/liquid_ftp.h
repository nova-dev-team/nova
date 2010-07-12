#ifndef LIQUID_FTP_H_
#define LIQUID_FTP_H_

#include "xdef.h"

/**
  @author
    Santa Zhang

  @file
    liquid_ftp.h

  @brief
    Liquid service as an FTP server.
*/

/**
  @brief
    Entrance to service as FTP server.

  @param argc
    Number of command line arguments. Should be directly given from main(argc, argv).
  @param argv
    Array of command line arguments. Should be directly given from main(argc, argv).

  @return
    Whether the service is successful.
*/
xsuccess liquid_ftp(int argc, char* argv[]);

/**
  @brief
    Print help info.
*/
void liquid_ftp_help();

#endif  // LIQUID_FTP_H_


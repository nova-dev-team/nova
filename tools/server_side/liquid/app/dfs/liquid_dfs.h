#ifndef LIQUID_DFS_H_
#define LIQUID_DFS_H_

#include "xdef.h"

/**
  @author
    Santa Zhang

  @file
    liquid_dfs.h

  @brief
    Serving as an distributed filesystem server.
*/

/**
  @brief
    Distributed file system service.

  @param argc
    The argc from main().
  @param argv
    The argv from main().

  @return
    On error return XFAILURE. Other wise the function blocks.
*/
xsuccess liquid_dfs(int argc, char* argv[]);

/**
  @brief
    Show help message on DFS module.
*/
void liquid_dfs_help();

#endif  // #define LIQUID_DFS_H_

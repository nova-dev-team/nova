#ifndef LIQUID_NODE_H_
#define LIQUID_NODE_H_

#include "xdef.h"

/**
  @author
    Santa Zhang

  @file
    liquid_node.h

  @brief
    Serving as an storage node server.
*/

/**
  @brief
    The liquid node service, in charges of storing data contents.

  @param argc
    The argc from main().
  @param argv
    The argv from main().

  @return
    On error return XFAILURE, other wise the function blocks.
*/
xsuccess liquid_node(int argc, char* argv[]);

/**
  @brief
    Show help message on the liquid node service.
*/
void liquid_node_help();

#endif  // #ifndef LIQUID_NODE_H_


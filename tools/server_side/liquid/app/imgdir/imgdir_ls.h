#ifndef LIQUID_IMGDIR_LS_H_
#define LIQUID_IMGDIR_LS_H_

/**
 * @brief
 *  Handlers for the 'ls' command.
 *
 * @author
 *  Santa Zhang
 *
 * @file
 *  imgdir_ls.h
 */

#include "xdef.h"
#include "xnet.h"

xsuccess imgdir_handle_ls(XIN xsocket client_xs, void* args);

#endif  // LIQUID_IMGDIR_LS_H_


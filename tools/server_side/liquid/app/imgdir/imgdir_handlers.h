#ifndef LIQUID_IMGDIR_HANDLERS_H_
#define LIQUID_IMGDIR_HANDLERS_H_

/**
 * @brief
 *  Handlers for imgmount client connection.
 *
 * @author
 *  Santa Zhang
 *
 * @file
 *  imgdir_handlers.h
 */

#include "xdef.h"
#include "xnet.h"
#include "imgdir_session.h"

/**
  @brief
    Handle list requests.

  If

  @param session
    The client session.
  @param req_head
    The client's request head.

  @return
    Whether the request has been successfully handled.
*/
xsuccess imgdir_handle_ls(XIN imgdir_session session, xstr req_head);

xsuccess imgdir_handle_mkdir(XIN imgdir_session session, xstr req_head);

#endif  // LIQUID_IMGDIR_HANDLERS_H_

#ifndef XDK_XCONF_H_
#define XDK_XCONF_H_

#include "xdef.h"
#include "xstr.h"
#include "xhash.h"

/**
  @brief
    Utility for parsing configuration files.

  @file
    xconf.h

  @author
    Santa Zhang
*/

struct xconf_impl;

typedef struct xconf_imple* xconf;

xconf xconf_new();

xconf xconf_load(xconf xcf, const char* fname);

xsuccess xconf_save(xconf xcf, const char* fname);

xhash xconf_get_hash(xconf xcf, const char* section_name);

xstr xconf_get_value(xconf xcf, const char* section_name, const char* item_name);

void xconf_set_value(xconf xcf, const char* section_name, const char* item_name, const char* value);

void xconf_delete(xconf xcf);

#endif  // #ifndef XDK_XCONF_H_


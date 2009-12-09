#ifndef XSTR_H_
#define XSTR_H_

struct _xstr;

typedef struct _xstr *xstr;

xstr xstr_new();

void xstr_delete(xstr xs);

const char* xstr_as_cstr(xstr xs);

#endif



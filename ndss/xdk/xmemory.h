#ifndef XDK_XMEMORY_H_
#define XDK_XMEMORY_H_

// allocate new memory
void* xmalloc(int size);

// release allocated memory
void xfree(void* ptr);

void* xrealloc(void* ptr, int new_size);

// report memory usage count
int xmem_use();

#endif


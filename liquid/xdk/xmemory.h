#ifndef XDK_XMEMORY_H_
#define XDK_XMEMORY_H_

#include <stdio.h>

/**
  @author
    Santa Zhang

  @file
    xmemory.h

  @brief
    Memory management. Some tricks are applied to detect memory leak.
*/

/**
  @brief
    Helper macro for xmalloc().

  @param cnt
    Number of allocation.
  @param ty
    Type of allocated objects.
*/
#define xmalloc_ty(cnt, ty) ((ty *) xmalloc((cnt) * sizeof(ty), __FILE__, __LINE__))

/**
  @brief
    Allocate memory chunk.

  This is merely a wrapper around malloc().
  Allocation counts will be recorded.

  @param size
    Size of the memory chunk.

  @return
    Pointer to alloc'ed memory chunk.

  @warning
    xmalloc'ed memory chunk must be freed by xfree().
*/

void* xmalloc(int size, ...);

/**
  @brief
    Free memory chunk.

  This is merely a wrapper around free().
  Allocation counts will be decreased.

  @param ptr
    Pointer to memory chunk.

  @warning
    Do not xfree() an xmalloc'ed memory chunk more than once!
*/
void xfree(void* ptr);

/**
  @brief
    Resize an xmalloc'ed chunk size

  This is merely a wrapper around realloc().
  Allocation counts will not be changed.

  @param ptr
    Pointer to current memory chunk.
  @param new_size
    New memory chunk size.
  
  @return
    Pointer to new memory chunk.
*/
void* xrealloc(void* ptr, int new_size);

/**
  @brief
    Report number of allocated memory chunks.

  At the end of execution, this function should return 0, indicating that
  all xmalloc'ed memory are released. This means there is no memory leak.

  @param fp
    File pointer to which the output message will be printed.
    stderr is advocated. If NULL is given, nothing will be printed.

  @return
    Number of allocated memory chunks.
*/
int xmem_usage(FILE* fp);

void xmem_reset_counter();

#endif  // XDK_XMEMORY_H_


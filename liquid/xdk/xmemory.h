#ifndef XDK_XMEMORY_H_
#define XDK_XMEMORY_H_

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


#define XMEM_DEBUG

#ifdef XMEM_DEBUG
#define xmalloc_ty(cnt, ty) ((ty *) xmalloc_debug((cnt) * sizeof(ty), __FILE__, __LINE__))
#else
#define xmalloc_ty(cnt, ty) ((ty *) xmalloc((cnt) * sizeof(ty)))
#endif

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

void* xmalloc(int size);

void* xmalloc_debug(int size, const char* file, int line);

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

  @return
    Number of allocated memory chunks.
*/
int xmem_usage();

void xmem_print_usage();

void xmem_reset_counter();

#endif


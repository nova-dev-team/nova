#ifndef XHASH_H_
#define XHASH_H_


/**
  @author
    Santa Zhang

  @file
    xhash.h

  @brief
    Simple linear hash table implementation.
    It will adaptively adjust its slot size to meet storage requirements.
*/

/**
  @brief
    Function type for hashcode calculator
*/
typedef int (*xhash_hash)(void* key);

/**
  @brief
    Function type for hash entry equality checking
*/
typedef int (*xhash_eql)(void* key1, void* key2);

/**
  @brief
    Function type for hash entry destructor
*/
typedef void (*xhash_free)(void* key, void* value);

/**
  @brief
    Hash entry type
*/
typedef struct hash_entry {
  void* key;  /**< key of the hash entry */
  void* value;  /**< value of the hash entry */
  struct xhash_entry* next; /**< link to next hash entry (linked hashtable entry) */
} xhash_entry;


/**
  @brief
    Linear hash table type

    TODO add detail info about the xhash table
*/
typedef struct xhash {
  xhash_entry** slot; /**< @brief slots used to store hash entry (as link list) */

  int entry_count; /**< @brief for calculating load average */
  int extend_ptr;  /**< @brief  index of next element to be expanded */
  int extend_level; /**< @brief how many times the table get expanded */

  /**
    @brief
      The size of the hash table in the begining.
      Current hash table's "appearing" size is base_size * 2 ^ level
      Actuall size could be calculated by: extend_ptr + base_size * 2 ^ level
  */
  int base_size;

  xhash_hash hash_func; /**< @brief hashcode calculator */
  xhash_eql eql_func; /**< @brief hash entry equality checker */
  xhash_free free_func; /**< @brief hash entry destructor */
} xhash;


/**
  @brief
    Initialize hash table

  @param xh
    The hash table to be initialized
  @param arg_hash
    The hashcode calculator function
  @param arg_eql
    The hash entry equality checker
  @param arg_free
    The hash entry destructor

  @warning
    Do not initialize a hash table more than once!
*/
void xhash_init(xhash* xh, xhash_hash arg_hash, xhash_eql arg_eql, xhash_free arg_free);


/**
  @brief
    Destruct a hash table

    The hash table itself will be destructed by calling xfree().
    free_func() will be invoked on each entry.

  @param xh
    The hash table to be destructed
*/
void xhash_release(xhash* xh);

/**
  @brief
    Put a new entry (key, value) into hash table

  @param xh
    The hash table in which new entry will be put
  @param key
    Pointer to the key element
  @param value
    Pointer to the value element
*/
void xhash_put(xhash* xh, void* key, void* value);

/**
  @brief
    Get an entry from hash table

  @param xh
    The hash table from which the value will be fetched
  @param key
    Pointer to the key element

  @return
    NULL if not found, otherwise corresponding *value will be returned
*/
void* xhash_get(xhash* xh, void* key);

/**
  @brief
    Remove an entry from hash table

  @param xh
    The hash table from which entry will be removed
  @param key
    Pointer to the key which will be removed

  @return
    0 if successful, -1 if failed
*/
int xhash_remove(xhash* xh, void* key);

#endif


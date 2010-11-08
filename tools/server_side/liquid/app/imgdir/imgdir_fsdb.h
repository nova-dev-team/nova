#ifndef IMGDIR_FSDB_H_
#define IMGDIR_FSDB_H_

/**
  @brief
    Storage for filesystem layout.

  @author
    Santa Zhang

  @file
    imgdir_fsdb.h
*/

#include "xdef.h"
#include "xstr.h"

// reuse the fs_cache type defined in imgmount utility
#include "imgmount/imgmount_fs_cache.h"

// hidden implementation
struct fsdb_impl;

/**
  @brief
    Interface for the filesystem layout database.
*/
typedef struct fsdb_impl* fsdb;

/**
  @brief
    Load the root node from filesystem layout database.

  @param db_fn
    The path of database file. If it does not exist, a new database will be created.
  @param p_db
    Pointer to database handle.

  @return
    NULL if failed to open database file, and p_db will be set to NULL, too.
    Other wise the root node will be returned, and database handle will be returned by p_db.
*/
fs_cache fsdb_load_root(XIN const char* db_fn, XOUT fsdb* p_db);

/**
  @brief
    Create a new entry under a dir.

  @param db
    Handle of the database.
  @param parent
    The parent node, under which the new entry will be added.
  @param name
    Name of the new entry.
  @param type
    Type of the new entry.
  @param p_node
    Where new child node will be returned.

  @warning
    Do not use fs_cache_new_* functions, since they only create node in memory, but not sync'ed to database.
    Use this function to create new node in memory and also sync them to database.

  @return
    If operation successed, 0 will be returned, and p_node will be set to the new child node.
    Otherwise corresponding errno will be returned, and p_node will be set to NULL.
*/
int fsdb_mknode(XIN fsdb db, XIN fs_cache parent, XIN const char* name, XIN fs_cache_type type, XOUT fs_cache *p_node);

/**
  @brief
    Raname (and possibly move) an entry.

  @param db
    Handle of the database.
  @param node
    The node to be renamed (or moved).
  @param new_parent
    Set to NULL if only need to rename.
    Set to new parent node if need to move the entry to a new dir.
  @param new_name
    Set to NULL if only move operation is required.
    Set to new name if need to do rename operations.

  @return
    If operation successed, 0 will be returned.
    Otherwise corresponding errno will be returned.
*/
int fsdb_rename(XIN fsdb db, XIN fs_cache node, XIN fs_cache new_parent, XIN const char* new_name);

/**
  @brief
    Delete an entry from database.

  @param db
    Handle of database.
  @param node
    A fs_cache representing the file to be deleted.

  @return
    If operation successed, 0 will be returned.
    Otherwise corresponding errno will be returned.
*/
int fsdb_unlink(XIN fsdb db, XIN fs_cache node);

/**
  @brief
    Close connection to a database system.
    The handle it self will be released.

  @param db
    Handle of the database. If it is NULL, then nothing will be done.

  @return
    Whether successfully closed the filesystem database.
*/
xsuccess fsdb_close(XIN fsdb db);

#endif  // IMGDIR_FSDB_H_

#ifndef IMGMOUNT_FS_CACHE_H_
#define IMGMOUNT_FS_CACHE_H_

/**
  @brief
    A cache of imgdir's filesystem structure.

  @author
    Santa Zhang

  @file
    imgmount_fs_cache.h
*/

#include <sys/types.h>
#include <sys/time.h>
#include <pthread.h>

#include "xstr.h"
#include "xhash.h"

/**
  @brief
    The uid of root node in fsdb.
*/
#define FS_CACHE_ROOT_UID 1

/**
  @brief
    A special mark for invalid uid.
*/
#define FS_CACHE_INVALID_UID -1

/**
  @brief
    Enumeration of fs_cache types.
*/
typedef enum {
  FS_CACHE_DIR,  ///< @brief Indicates a dir.
  FS_CACHE_FILE  ///< @brief Indicates a normal file.
} fs_cache_type;


/**
  @brief
    Implementation of fs_cache.
*/
struct fs_cache_impl {
  xstr name;  ///< @brief Entry name. For root, this is '/'.
  fs_cache_type type; ///< @brief Entry type, could be FS_CACHE_FILE or FS_CACHE_DIR.
  xhash child;  ///< @brief A hash of child files. It is a (xstr -> fs_cache) hash.
  struct fs_cache_impl* parent;  ///< @brief Pointer to parent folder. For root, this is it self.
  int perm; ///< @brief Permission of the entry.
  int mtime;  ///< @brief Last modification time of the entry.
  off_t size; ///< @brief The size of the entry. (only for normal files)
  pthread_mutex_t mutex;  ///< @brief Mutex to protect the node in multi-thread app.
  int uid;  ///< @brief Unique id for this fs node. For 'not-in-fsdb' node (like in imgdir), this will be -1.
  int parent_uid; ///< @brief Parent's uid. Only used by imgdir.
  struct timeval sync_time;  ///< @brief The time that this info is updated. When fs_cache is newly created, this is set to an "invalid" value.
  struct timeval sync_expire;  ///< @brief The maximum expire interval for a node. It is by default set to 1 sec.
};


/**
  @brief
    Cached imgdir filesystem structure.
*/
typedef struct fs_cache_impl* fs_cache;


/**
  @brief
    Create a new raw fs_cache node.

  @warning
    Don't use this function unless you clearly know what you are doing.

  @return
    A new raw fs_cache node, most fields are not set.
*/
fs_cache fs_cache_new_raw();

/**
  @brief
    Create a new cached dir entry as root.

  @return
    A new cached entry, as root dir.
*/
fs_cache fs_cache_new_root();

/**
  @brief
    Create a new cached dir entry.
    The new entry will be added to child list of the parent node.

  @param name
    Name of the new entry.
  @param parent
    Parent of the entry. The new entry will be added to the child list of the parent node.

  @return
    A new cached entry, as a dir.
*/
fs_cache fs_cache_new_dir(const char* name, fs_cache parent);


/**
  @brief
    Create a new cached file entry.
    The new entry will be added to child list of the parent node.

  @param name
    Name of the new entry.
  @param parent
    Parent of the entry. The new entry will be added to the child list of the parent node.

  @return
    A new cached entry, as a normal file.
*/
fs_cache fs_cache_new_file(const char* name, fs_cache parent);


/**
  @brief
    Clear all the child of a fs_cache entry. This could only be used by for a dir entry.

  @param entry
    The fs_entry whose child should be cleared().

  @warning
    Only use it on dir entry.
*/
void fs_cache_clear_child(fs_cache entry);


/**
  @brief
    Delete a cached filesystem entry. Will recursively delete child items if it is a dir.

  @param entry
    The cached entry to be deleted.
*/
void fs_cache_delete(fs_cache entry);

/**
  @brief
    Check if an fs_entry has been synced.
    Only newly created fs_entry is synced.

  @param entry
    The cached entry to be checked.

  @return
    XTRUE if the entry is synced. Otherwise return XFALSE.
*/
xbool fs_cache_is_synced(fs_cache entry);

/**
  @brief
    Get fullpath of an fs_entry.

  @param entry
    The cache entry we are interested in.
  @param fullpath
    Where the fullpath will be filled in.

  @return
    XSUCCESS if nothing goes wrong.
*/
xsuccess fs_cache_get_fullpath(XIN fs_cache entry, XOUT xstr fullpath);

/**
  @brief
    Count all nodes in a subtree.

  @param root
    The root of the subtree.

  @return
    The number of nodes in the subtree.
*/
int fs_cache_tree_count(XIN fs_cache root);

#endif  // IMGMOUNT_FS_CACHE_H_


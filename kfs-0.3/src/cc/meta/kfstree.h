/*!
 * $Id: kfstree.h 300 2009-04-05 00:16:59Z sriramsrao $ 
 *
 * \file kfstree.h
 * \brief Search tree for the KFS metadata server.
 * \author Blake Lewis (Kosmix Corp.)
 *
 * The tree is a B+ tree with leaves representing the structure
 * of the file system, e.g., directory entries, file attributes, etc.
 *
 * Copyright 2008 Quantcast Corp.
 * Copyright 2006-2008 Kosmix Corp.
 *
 * This file is part of Kosmos File System (KFS).
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

#if !defined(KFS_KFSTREE_H)
#define KFS_KFSTREE_H

#include <string>
#include <vector>
#include <algorithm>
#include "base.h"
#include "meta.h"

using std::string;
using std::vector;
using std::lower_bound;

namespace KFS {

class Tree;

/*!
 * \brief an internal node in the KFS search tree.
 *
 * Nodes contain an array of keys together with links either
 * to nodes lower in the tree or to metadata at the leaves.
 * Each is linked to the following node at the same level in
 * the tree to allow linear traversal.
 */
class Node: public MetaNode {
	static const int NKEY = 32;
	// should size the node to near 4k; 120 gets us there...
	// static const int NKEY = 120;
	static const int NSPLIT = NKEY / 2;
	static const int NFEWEST = NKEY - NSPLIT;

	int count;			//!< how many children
	Key childKey[NKEY];		//!< children's key values
	MetaNode *childNode[NKEY];	//!< and pointers to them

	Node *next;			//!< following peer node

	void placeChild(Key k, MetaNode *n, int p)
	{
		childKey[p] = k;
		childNode[p] = n;
	}
	void appendChild(Key k, MetaNode *n)
	{
		placeChild(k, n, count);
		++count;
	}
	void moveChildren(Node *dest, int start, int n);
	void insertChildren(Node *dest, int start, int n);
	void absorb(Node *dest);
	int excess() { return (count - NFEWEST) / 2; }
	void linkToPeer(Node *n) { next = n; }
	void openHole(int pos, int skip);
	void closeHole(int pos, int skip);
	Node *leftNeighbor(int pos)
	{
		return (pos == 0) ? NULL : child(pos - 1);
	}
	Node *rightNeighbor(int pos)
	{
		return (pos == count - 1) ? NULL : child(pos + 1);
	}
	void shiftLeft(Node *dest, int nshift);
	void shiftRight(Node *dest, int nshift);
public:
	Node(int f): MetaNode(KFS_INTERNAL, f), count(0), next(NULL) { }
	bool hasleaves() const { return testflag(META_LEVEL1); }
	bool isroot() const { return testflag(META_ROOT); }
	bool isfull() const { return (count == NKEY); } //!< full
	bool isdepleted() const { return (count < NFEWEST); } //!< underfull
	int cpbit() const { return testflag(META_CPBIT) ? 1 : 0; }
	void markparity(int count)
	{
		if (count & 1)
			setflag(META_CPBIT);
		else
			clearflag(META_CPBIT);
	}
	/*!
 	* \brief binary search to locate key within node
 	* \param[in] test	the key that we are looking for
 	* \return		the position of first key >= test;
	* 			can be off the end of the array
	*/
	int findplace(const Key &test)
	{
		Key *p = lower_bound(childKey, childKey + count, test);
		return p - childKey;
	}
	//! \brief rightmost (largest) key in node
	const Key key() const { return childKey[count - 1]; }
	Node *child(int n) const		//! \brief accessor
	{
		return static_cast <Node *> (childNode[n]);
	}
	Meta *leaf(int n) const			//! \brief accessor
	{
		return static_cast <Meta *> (childNode[n]);
	}
	const Key &getkey(int n) const { return childKey[n]; } //!< accessor
	Node *split(Tree *t, Node *father, int pos);	//!< split full node
	void addChild(Key *k, MetaNode *child, int pos); //!< insert child node
	void insertData(Key *key, Meta *item, int pos); //!< insert data item
	Node *peer() const { return next; }	//!< return adjacent node
	int children() const { return count; } //!< how many children
	bool mergeNeighbor(int pos);		//!< merge underfull nodes
	bool balanceNeighbor(int pos);		//!< borrow from full node
	void resetKey(int pos);			//!< update key from child
	void remove(int pos);			//!< delete child node
	/*!
	 * \brief return metadata with the specified key
	 * \param[in] k	key that we are looking for
	 * \return	pointer to corresponding metadata
	 * \warning	only use in cases where the key is unique
	 */
	template <typename T> T *extractMeta(const Key &k)
	{
		return refine<T>(leaf(findplace(k)));
	}
	const string show() const;
	void showChildren() const;
};

/*!
 * \brief for iterating through leaf nodes
 */
class LeafIter {
	Node *dad;	//!< node containing child pointers
	int pos;	//!< index of current child
public:
	LeafIter(Node *d, int p): dad(d), pos(p) { }
	Meta *current() const { return dad->leaf(pos); }
	Node *parent() const { return dad; }
	int index() const { return pos; }
	void next()
	{
		if (++pos == dad->children()) {
			pos = 0;
			dad = dad->peer();
		}
	}
	void reset(Node *d, int p) {
		dad = d;
		pos = p;
	}
};

/*!
 * \brief the KFS search tree.
 *
 * A tree is just a pointer to the root node and a pointer
 * to the first (leftmost) leaf node.
 */
class Tree {
	Node *root;			//!< root node
	Node *first;			//!< leftmost level-1 node
	int hgt;			//!< height of tree
	struct pathlink {		//!< for recording descent path
		Node *n;		//!< parent node
		int pos;		//!< index of child
		pathlink(Node *nn, int p): n(nn), pos(p) {}
		pathlink(): n(0), pos(-1) { }
	};
	bool allowFidToPathConversion;	//!< fid->path translation is enabled?
	Node *findLeaf(const Key &k) const;
	void unlink(fid_t dir, const string fname, MetaFattr *fa, bool save_fa);
	int link(fid_t dir, const string fname, FileType type, fid_t myID, 
		int16_t numReplicas);
	MetaDentry *getDentry(fid_t dir, const string &fname);
	bool emptydir(fid_t dir);
	bool is_descendant(fid_t src, fid_t dst);
	void shift_path(vector <pathlink> &path);
	off_t recomputeDirSize(fid_t dir);
	int changeFileReplication(MetaFattr *fa, int16_t numReplicas);
	int changeDirReplication(MetaFattr *dirattr, int16_t numReplicas);
public:
	Tree()
	{
		root = new Node(META_ROOT|META_LEVEL1);
		Key *sentinel = new Key(KFS_SENTINEL, 0);
		root->insertData(sentinel, NULL, 0);
		first = root;
		hgt = 1;
		allowFidToPathConversion = true;
	}
	int new_tree()			//!< create a directory namespace
	{
		fid_t dummy = 0;
		return mkdir(KFS::ROOTFID, "/", &dummy);
	}
	int insert(Meta *m);			//!< add data item
	int del(Meta *m);			//!< remove data item
	Node *getroot() { return root; }	//!< return root node
	Node *firstLeaf() { return first; }	//!< leftmost leaf
	void pushroot(Node *rootbro);		//!< insert new root
	void poproot();				//!< discard current root
	int height() { return hgt; }		//!< return tree height
	void printleaves();			//!< print debugging info
	MetaFattr *getFattr(fid_t fid);		//!< return attributes
	MetaDentry *getDentry(fid_t fid);	//!< return dentry attributes
	//!< turn off conversion from file-id to pathname---useful when we
	//!< are going to compute the size of "/" and thereby each dir. in the tree
	void disableFidToPathname() { allowFidToPathConversion = false; }
	void enableFidToPathname() { allowFidToPathConversion = true; }
	std::string getPathname(fid_t fid);	//!< return full pathname for a given file id
	void recomputeDirSize();		//!< re-compute the size of each dir. in tree

	int create(fid_t dir, const string &fname, fid_t *newFid, 
			int16_t numReplicas, bool exclusive);
	//!< final argument is optional: when non-null, this call will return
	//!< the size of the file (if known)
	int remove(fid_t dir, const string &fname, const string &pathname, off_t *filesize = NULL);
	int mkdir(fid_t dir, const string &dname, fid_t *newFid);
	int rmdir(fid_t dir, const string &dname, const string &pathname);
	int readdir(fid_t dir, vector <MetaDentry *> &result);
	int getalloc(fid_t file, vector <MetaChunkInfo *> &result);
	int getalloc(fid_t file, chunkOff_t offset, MetaChunkInfo **c);
	int rename(fid_t dir, const string &oldname, string &newname, 
			const string &oldpath, bool once);
	MetaFattr *lookup(fid_t dir, const string &fname);
	MetaFattr *lookupPath(fid_t rootdir, const string &path);
	void updateSpaceUsageForPath(const string &path, off_t nbytes);
	int getChunkVersion(fid_t file, chunkId_t chunkId, seq_t *chunkVersion);
	int changePathReplication(fid_t file, int16_t numReplicas);

	int moveToDumpster(fid_t dir, const string &fname);
	void cleanupDumpster();

	/*!
	 * \brief Write-allocation 
	 * On receiveing a request from a client for space allocation,
	 * the  request is handled in two steps:
	 * 1. The metaserver picks a chunk-id and sends a RPC to
	 *    to a chunkserver to create the chunk.
	 * 2. After the chunkserver ack's the RPC, the metaserver
	 *    updates the metatree to reflect the allocation of
	 *    the chunkId to the file.
	 * The following two functions, respectively, perform the above two steps.
	 */

	/*
	 * \brief Allocate a unique chunk identifier
	 * \param[in] file	The id of the file for which need to allocate space
	 * \param[in] offset	The offset in the file at which space should be allocated
	 * \param[out] chunkId  The chunkId that is allocated
	 * \param[out] numReplicas The # of replicas to be created for the chunk.  This
	 *  parameter is inhreited from the file's attributes.
	 * \retval 0 on success; -errno on failure
	 */
	int allocateChunkId(fid_t file, chunkOff_t offset, chunkId_t *chunkId,
				seq_t *version, int16_t *numReplicas);

	/*
	 * \brief Assign a chunk identifier to a file.  Update the metatree
	 * to reflect the assignment.
	 * \param[in] file	The id of the file for which need to allocate space
	 * \param[in] offset	The offset in the file at which space should be allocated
	 * \param[in] chunkId   The chunkId that is assigned to file/offset
	 * \retval 0 on success; -errno on failure
	 */
	int assignChunkId(fid_t file, chunkOff_t offset, 
			  chunkId_t chunkId, seq_t version);
	
	/*
	 * \brief Truncate a file to the specified file offset.  Due
	 * to truncation, chunks past the desired offset will be
	 * deleted.  When there are holes in the file or if the file
	 * is extended past the last chunk, a truncation can cause
	 * a chunk allocation to occur.  This chunk will be used to
	 * track the file's size.
	 * \param[in] file	The id of the file being truncated
	 * \param[in] offset	The offset to which the file should be
	 *			truncate to
         * \param[out] allocOffset	The offset at which an allocation
	 * 				should be done
	 * \retval 0 on success; -errno on failure; 1 if an allocation
	 * is needed
	 */
	int truncate(fid_t file, chunkOff_t offset, chunkOff_t *allocOffset);
};

/*!
 * \brief return all metadata with the specified key
 * \param[in] node	leftmost leaf node in which the key appears
 * \param[in] k		the key itself
 * \param[out] result	vector of all items with that key
 *
 * Finds the first instance of the key, then looks at adjacent
 * items (possibly jumping to a new leaf node), gathering up all
 * with the same key.
 */
template <typename T> void
extractAll(Node *n, const Key &k, vector <T *> &result)
{
	int p = n->findplace(k);
	while (n != NULL && k == n->getkey(p)) {
		result.push_back(refine<T>(n->leaf(p)));
		if (++p == n->children()) {
			p = 0;
			n = n->peer();
		}
	}
}

extern Tree metatree;
extern void makeDumpsterDir();
extern void emptyDumpsterDir();
}
#endif // !defined(KFS_KFSTREE_H)

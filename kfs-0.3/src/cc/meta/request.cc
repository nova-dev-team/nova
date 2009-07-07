/*!
 * $Id: request.cc 300 2009-04-05 00:16:59Z sriramsrao $ 
 *
 * \file request.cc
 * \brief process queue of outstanding metadata requests
 * \author Blake Lewis and Sriram Rao
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

#include <map>

#include "common/Version.h"

#include "kfstree.h"
#include "queue.h"
#include "request.h"
#include "logger.h"
#include "checkpoint.h"
#include "util.h"
#include "LayoutManager.h"

#include "libkfsIO/Globals.h"

using std::map;
using std::string;
using std::istringstream;
using std::ifstream;
using std::min;

using namespace KFS;
using namespace KFS::libkfsio;

MetaQueue <MetaRequest> requestList;

typedef void (*ReqHandler)(MetaRequest *r);
map <MetaOp, ReqHandler> handler;

typedef int (*ParseHandler)(Properties &, MetaRequest **);

static int parseHandlerLookup(Properties &prop, MetaRequest **r);
static int parseHandlerLookupPath(Properties &prop, MetaRequest **r);
static int parseHandlerCreate(Properties &prop, MetaRequest **r);
static int parseHandlerRemove(Properties &prop, MetaRequest **r);
static int parseHandlerRename(Properties &prop, MetaRequest **r);
static int parseHandlerMkdir(Properties &prop, MetaRequest **r);
static int parseHandlerRmdir(Properties &prop, MetaRequest **r);
static int parseHandlerReaddir(Properties &prop, MetaRequest **r);
static int parseHandlerReaddirPlus(Properties &prop, MetaRequest **r);
static int parseHandlerGetalloc(Properties &prop, MetaRequest **r);
static int parseHandlerGetlayout(Properties &prop, MetaRequest **r);
static int parseHandlerAllocate(Properties &prop, MetaRequest **r);
static int parseHandlerTruncate(Properties &prop, MetaRequest **r);
static int parseHandlerChangeFileReplication(Properties &prop, MetaRequest **r);
static int parseHandlerRetireChunkserver(Properties &prop, MetaRequest **r);
static int parseHandlerToggleRebalancing(Properties &prop, MetaRequest **r);
static int parseHandlerExecuteRebalancePlan(Properties &prop, MetaRequest **r);

static int parseHandlerLeaseAcquire(Properties &prop, MetaRequest **r);
static int parseHandlerLeaseRenew(Properties &prop, MetaRequest **r);
static int parseHandlerLeaseRelinquish(Properties &prop, MetaRequest **r);
static int parseHandlerChunkCorrupt(Properties &prop, MetaRequest **r);

static int parseHandlerHello(Properties &prop, MetaRequest **r);

static int parseHandlerPing(Properties &prop, MetaRequest **r);
static int parseHandlerStats(Properties &prop, MetaRequest **r);
static int parseHandlerDumpChunkToServerMap(Properties &prop, MetaRequest **r);
static int parseHandlerDumpChunkReplicationCandidates(Properties &prop, MetaRequest **r);
static int parseHandlerOpenFiles(Properties &prop, MetaRequest **r);
static int parseHandlerToggleWORM(Properties &prop, MetaRequest **r);
static int parseHandlerUpServers(Properties &prop, MetaRequest **r);

/// command -> parsehandler map
typedef map<string, ParseHandler> ParseHandlerMap;
typedef map<string, ParseHandler>::iterator ParseHandlerMapIter;

// handlers for parsing
ParseHandlerMap gParseHandlers;

// mapping for the counters
typedef map<MetaOp, Counter *> OpCounterMap;
typedef map<MetaOp, Counter *>::iterator OpCounterMapIter;
OpCounterMap gCounters;
Counter *gNumFiles, *gNumDirs, *gNumChunks;

// see the comments in setClusterKey()
string gClusterKey;
string gMD5SumFn;
bool gWormMode = false;
static int16_t gMaxReplicasPerFile = MAX_REPLICAS_PER_FILE;

static bool
file_exists(fid_t fid)
{
	return metatree.getFattr(fid) != NULL;
}

static bool
path_exists(const string &pathname)
{
	MetaFattr *fa = metatree.lookupPath(KFS::ROOTFID, pathname);
	return fa != NULL;
}

static bool
is_dir(fid_t fid)
{
	MetaFattr *fa = metatree.getFattr(fid);
	return fa != NULL && fa->type == KFS_DIR;
}

static void
AddCounter(const char *name, MetaOp opName)
{
	Counter *c = new Counter(name);
	globals().counterManager.AddCounter(c);
	gCounters[opName] = c;
}

void
KFS::RegisterCounters()
{
	static int calledOnce = 0;
	if (calledOnce)
		return;
	calledOnce = 1;

	AddCounter("Get alloc", META_GETALLOC);
	AddCounter("Get layout", META_GETLAYOUT);
	AddCounter("Lookup", META_LOOKUP);
	AddCounter("Lookup Path", META_LOOKUP_PATH);
	AddCounter("Allocate", META_ALLOCATE);
	AddCounter("Truncate", META_TRUNCATE);
	AddCounter("Create", META_CREATE);
	AddCounter("Remove", META_REMOVE);
	AddCounter("Rename", META_RENAME);
	AddCounter("Mkdir", META_MKDIR);
	AddCounter("Rmdir", META_RMDIR);
	AddCounter("Change File Replication", META_CHANGE_FILE_REPLICATION);
	AddCounter("Lease Acquire", META_LEASE_ACQUIRE);
	AddCounter("Lease Renew", META_LEASE_RENEW);
	AddCounter("Lease Cleanup", META_LEASE_CLEANUP);
	AddCounter("Corrupt Chunk ", META_CHUNK_CORRUPT);
	AddCounter("Chunkserver Hello ", META_HELLO);
	AddCounter("Chunkserver Bye ", META_BYE);
	AddCounter("Chunkserver Retire Start", META_RETIRE_CHUNKSERVER);
	// AddCounter("Chunkserver Retire Done", META_CHUNK_RETIRE_DONE);
	AddCounter("Replication Checker ", META_CHUNK_REPLICATION_CHECK);
	AddCounter("Replication Done ", META_CHUNK_REPLICATE);

	gNumFiles = new Counter("Number of Files");
	gNumDirs = new Counter("Number of Directories");
	gNumChunks = new Counter("Number of Chunks");

	globals().counterManager.AddCounter(gNumFiles);
	globals().counterManager.AddCounter(gNumDirs);
	globals().counterManager.AddCounter(gNumChunks);
}

static void
UpdateCounter(MetaOp opName)
{
	Counter *c;
	OpCounterMapIter iter;

	iter = gCounters.find(opName);
	if (iter == gCounters.end())
		return;
	c = iter->second;
	c->Update(1);
}

void
KFS::UpdateNumDirs(int count)
{
	if (gNumDirs == NULL)
		return;

	if ((int64_t) gNumDirs->GetValue() + count < 0)
		gNumDirs->Reset();
	else
		gNumDirs->Update(count);
}

void
KFS::UpdateNumFiles(int count)
{
	if (gNumFiles == NULL)
		return;

	if ((int64_t) gNumDirs->GetValue() + count < 0)
		gNumFiles->Reset();
	else
		gNumFiles->Update(count);
}

void
KFS::UpdateNumChunks(int count)
{
	if (gNumChunks == NULL)
		return;

	if ((int64_t) gNumDirs->GetValue() + count < 0)
		gNumChunks->Reset();
	else
		gNumChunks->Update(count);
}

/*
 * Submit a request to change the increment used for bumping up chunk version #.
 * @param[in] r  The request that depends on chunk-version-increment being written
 * out to disk as part of completing the request processing.
 */
void
KFS::ChangeIncarnationNumber(MetaRequest *r)
{
	if (chunkVersionInc < 1)
		// disable this bumping for now
		++chunkVersionInc;
	MetaChangeChunkVersionInc *ccvi = new MetaChangeChunkVersionInc(chunkVersionInc, r);

	submit_request(ccvi);
}

/*
 * Set the "key" for this cluster.  All chunkservers connecting to the meta-data
 * server should provide this key in the hello message.
 * @param[in] key  The desired cluster key
*/
void
KFS::setClusterKey(const char *key)
{
	gClusterKey = key;
}

/*
 * A way of doing admission control on chunkservers is to ensure that they run
 * an "approved" binary.  The approved list is defined by the MD5Sum of the
 * binary in an MD5Sum file.  All chunkservers connecting to 
 * the metaserver should provide their md5sum in the hello message.  If the
 * md5sum is in the approved list, then the chunkserver is admitted to the
 * system.
 * @param[in] md5sumFn  The filename with the list of MD5Sums
*/
void
KFS::setMD5SumFn(const char *md5sumFn)
{
	gMD5SumFn = md5sumFn;
}

/*
 * Set WORM mode. In WORM mode, deletes are disabled.
 */
void
KFS::setWORMMode(bool value)
{
	gWormMode = value;
}

void
KFS::setMaxReplicasPerFile(int16_t val)
{
	gMaxReplicasPerFile = val;
}

/*
 * Boilerplate code for specific request types.  Cast to the
 * appropriate type, call the corresponding KFS tree routine,
 * then use the callback to return the results.
 */
static void
handle_lookup(MetaRequest *r)
{
	MetaLookup *req = static_cast <MetaLookup *>(r);
	MetaFattr *fa = metatree.lookup(req->dir, req->name);
	req->status = (fa == NULL) ? -ENOENT : 0;
	if (fa != NULL)
		req->result = *fa;
}

static void
handle_lookup_path(MetaRequest *r)
{
	MetaLookupPath *req = static_cast <MetaLookupPath *>(r);
	MetaFattr *fa = metatree.lookupPath(req->root, req->path);
	req->status = (fa == NULL) ? -ENOENT : 0;
	if (fa != NULL)
		req->result = *fa;
}

static void
handle_create(MetaRequest *r)
{
	MetaCreate *req = static_cast <MetaCreate *>(r);
	fid_t fid = 0;
	if (!is_dir(req->dir)) {
		req->status = -ENOTDIR;
		return;
	}
	req->status = metatree.create(req->dir, req->name, &fid,
					req->numReplicas, req->exclusive);
	req->fid = fid;
}

static void
handle_mkdir(MetaRequest *r)
{
	MetaMkdir *req = static_cast <MetaMkdir *>(r);
	if (!is_dir(req->dir)) {
		req->status = -ENOTDIR;
		return;
	}
	fid_t fid = 0;
	req->status = metatree.mkdir(req->dir, req->name, &fid);
	req->fid = fid;
}


/*!
 * Specially named files (such as, those that end with ".tmp") can be
 * mutated by remove/rename.  Otherwise, in WORM no deletes/renames are allowed.
 */
static bool
isWormMutationAllowed(const string &pathname)
{
	string::size_type pos;

	pos = pathname.rfind(".tmp");
	return pos != string::npos;
}

/*!
 * \brief Remove a file in a directory.  Also, remove the chunks
 * associated with the file.  For removing chunks, we send off
 * RPCs to the appropriate chunkservers.
 */

static void
handle_remove(MetaRequest *r)
{
	MetaRemove *req = static_cast <MetaRemove *>(r);
	if (gWormMode && (!isWormMutationAllowed(req->name))) {
		// deletes are disabled in WORM mode except for specially named
		// files
		req->status = -EPERM;
		return;
	}
	req->status = metatree.remove(req->dir, req->name, req->pathname, &req->filesize);
}

static void
handle_rmdir(MetaRequest *r)
{
	MetaRmdir *req = static_cast <MetaRmdir *>(r);
	if (gWormMode && (!isWormMutationAllowed(req->name))) {
		// deletes are disabled in WORM mode
		req->status = -EPERM;
		return;
	}
	req->status = metatree.rmdir(req->dir, req->name, req->pathname);
}

static void
handle_readdir(MetaRequest *r)
{
	MetaReaddir *req = static_cast <MetaReaddir *>(r);
	if (!file_exists(req->dir))
		req->status = -ENOENT;
	else if (!is_dir(req->dir))
		req->status = -ENOTDIR;
	else {
		vector<MetaDentry *> res;
		req->status = metatree.readdir(req->dir, res);
		//
		// Previously, req->v used to be vector<MetaDentry *>.  This
		// meant that req->v carried out a pointer for something in the
		// metatree.  Now, if the pointer was deleted, then req->v
		// contains dangling references and can corrupt memory.
		// Instead, make a copy of the dentry and we are good.
		//
		if (req->status == 0) {
			for (uint32_t i = 0; i < res.size(); i++)
				req->v.push_back(res[i]);
		}
	}
}

class EnumerateLocations {
	vector <ServerLocation> &v;
public:
	EnumerateLocations(vector <ServerLocation> &result): v(result) { }
	void operator () (ChunkServerPtr c)
	{
		ServerLocation l = c->GetServerLocation();
		v.push_back(l);
	}
};

class ListServerLocations {
	ostringstream &os;
public:
	ListServerLocations(ostringstream &out): os(out) { }
	void operator () (const ServerLocation &s)
	{
		os << " " <<  s.ToString();
	}
};

class EnumerateReaddirPlusInfo {
	ostringstream &os;
public:
	EnumerateReaddirPlusInfo(ostringstream &o) : os(o) { }
	void operator()(MetaDentry *entry) {
		static string fname[] = { "empty", "file", "dir" };
		MetaFattr *fa = metatree.lookup(entry->getDir(), entry->getName());

		os << "Begin-entry" << "\r\n";

		if (fa == NULL) {
			return;
		}
		// when we readdir on "/", we get an entry for "/".  We need to
		// supress that from the listing for "/".
		if ((fa->id() == ROOTFID) && (entry->getName() == "/"))
			return;

		os << "Name: " << entry->getName() << "\r\n";
		os << "File-handle: " << toString(fa->id()) << "\r\n";
		os << "Type: " << fname[fa->type] << "\r\n";
		sendtime(os, "M-Time:", fa->mtime, "\r\n");
		sendtime(os, "C-Time:", fa->ctime, "\r\n");
		sendtime(os, "CR-Time:", fa->crtime, "\r\n");
		if (fa->type == KFS_DIR) {
			return;
		}
		// for a file, get the layout and provide location of last chunk
		// so that the client can compute filesize
		vector<MetaChunkInfo*> chunkInfo;
		vector<ChunkServerPtr> c;
		int status = metatree.getalloc(fa->id(), chunkInfo);

		if ((status != 0) || (chunkInfo.size() == 0)) {
			os << "Chunk-count: 0\r\n";
			os << "File-size: 0\r\n";
			os << "Replication: " << toString(fa->numReplicas) << "\r\n";
			return;
		}
		MetaChunkInfo* lastChunk = chunkInfo.back();
		ChunkLayoutInfo l;

		l.offset = lastChunk->offset;
		l.chunkId = lastChunk->chunkId;
		l.chunkVersion = lastChunk->chunkVersion;
		if (gLayoutManager.GetChunkToServerMapping(l.chunkId, c) != 0) {
			// if all the servers hosting the chunk are
			// down...sigh...
			os << "Chunk-count: 0\r\n";
			os << "File-size: 0\r\n";
			os << "Replication: " << toString(fa->numReplicas) << "\r\n";
			return;
		}
		//
		// we give the client all the info about the last block of the
		// file; we also tell the client what we know about the
		// filesize.  if the value we send is -1, the client will figure
		// out the size.
		//
		for_each(c.begin(), c.end(), EnumerateLocations(l.locations));
		os << "Chunk-count: " << toString(fa->chunkcount) << "\r\n";
		os << "File-size: " << toString(fa->filesize) << "\r\n";
		os << "Replication: " << toString(fa->numReplicas) << "\r\n";
		os << "Chunk-offset: " << l.offset << "\r\n";
		os << "Chunk-handle: " << l.chunkId << "\r\n";
		os << "Chunk-version: " << l.chunkVersion << "\r\n";
		os << "Num-replicas: " << l.locations.size() << "\r\n";
		os << "Replicas: ";
		for_each(l.locations.begin(), l.locations.end(), ListServerLocations(os));
		os << "\r\n";
	}

};

static void
handle_readdirplus(MetaRequest *r)
{
	MetaReaddirPlus *req = static_cast <MetaReaddirPlus *>(r);
	if (!file_exists(req->dir)) {
		req->status = -ENOENT;
		return;
	}
	else if (!is_dir(req->dir)) {
		req->status = -ENOTDIR;
		return;
	}
	vector<MetaDentry *> res;
	req->status = metatree.readdir(req->dir, res);
	if (req->status != 0)
		return;
	// now that we have the entire directory read, for each entry in the
	// directory, get the attributes out.
	req->numEntries = res.size();
	for_each(res.begin(), res.end(), EnumerateReaddirPlusInfo(req->v));
}

/*!
 * \brief Get the allocation information for a specific chunk in a file.
 */
static void
handle_getalloc(MetaRequest *r)
{
	MetaGetalloc *req = static_cast <MetaGetalloc *>(r);
	MetaChunkInfo *chunkInfo;
	vector<ChunkServerPtr> c;

	if (!file_exists(req->fid)) {
		KFS_LOG_VA_DEBUG("handle_getalloc: no such file %lld", req->fid);
		req->status = -ENOENT;
		return;
	}

	req->status = metatree.getalloc(req->fid, req->offset, &chunkInfo);
	if (req->status != 0) {
		KFS_LOG_VA_DEBUG(
			"handle_getalloc(%lld, %lld) = %d: kfsop failed",
			req->fid, req->offset, req->status);
		return;
	}

	req->chunkId = chunkInfo->chunkId;
	req->chunkVersion = chunkInfo->chunkVersion;
	if (gLayoutManager.GetChunkToServerMapping(req->chunkId, c) != 0) {
		KFS_LOG_DEBUG("handle_getalloc: no chunkservers");
		req->status = -EAGAIN;
		return;
	}
	for_each(c.begin(), c.end(), EnumerateLocations(req->locations));
	req->status = 0;
}

/*!
 * \brief Get the allocation information for a file.  Determine
 * how many chunks there and where they are located.
 */
static void
handle_getlayout(MetaRequest *r)
{
	MetaGetlayout *req = static_cast <MetaGetlayout *>(r);
	vector<MetaChunkInfo*> chunkInfo;
	vector<ChunkServerPtr> c;

	if (!file_exists(req->fid)) {
		req->status = -ENOENT;
		return;
	}

	req->status = metatree.getalloc(req->fid, chunkInfo);
	if (req->status != 0)
		return;

	for (vector<ChunkLayoutInfo>::size_type i = 0; i < chunkInfo.size(); i++) {
		ChunkLayoutInfo l;

		l.offset = chunkInfo[i]->offset;
		l.chunkId = chunkInfo[i]->chunkId;
		l.chunkVersion = chunkInfo[i]->chunkVersion;
		if (gLayoutManager.GetChunkToServerMapping(l.chunkId, c) != 0) {
			req->status = -EHOSTUNREACH;
			return;
		}
		for_each(c.begin(), c.end(), EnumerateLocations(l.locations));
		req->v.push_back(l);
	}
	req->status = 0;
}

class ChunkVersionChanger {
	fid_t fid;
	chunkId_t chunkId;
	seq_t chunkVers;
public:
	ChunkVersionChanger(fid_t f, chunkId_t c, seq_t v) :
		fid(f), chunkId(c), chunkVers(v) { }
	void operator() (ChunkServerPtr p) {
		p->NotifyChunkVersChange(fid, chunkId, chunkVers);
	}
};

/*!
 * \brief handle an allocation request for a chunk in a file.
 * \param[in] r		write allocation request
 *
 * Write allocation proceeds as follows:
 *  1. The client has sent a write allocation request which has been
 * parsed and turned into an RPC request (which is handled here).
 *  2. We first get a unique chunk identifier (after validating the
 * fileid).
 *  3. We send the request to the layout manager to pick a location
 * for the chunk.
 *  4. The layout manager picks a location and sends an RPC to the
 * corresponding chunk server to create the chunk.
 *  5. When the RPC is going on, processing for this request is
 * suspended.
 *  6. When the RPC reply is received, this request gets re-activated
 * and we come back to this function.
 *  7. Assuming that the chunk server returned a success,  we update
 * the metatree to link the chunkId with the fileid (from this
 * request).
 *  8. Processing for this request is now complete; it is logged and
 * a reply is sent back to the client.
 *
 * Versioning/Leases introduces a few wrinkles to the above steps:
 * In step #2, the metatree could return -EEXIST if an allocation
 * has been done for the <fid, offset>.  In such a case, we need to
 * check with the layout manager to see if a new lease is required.
 * If a new lease is required, the layout manager bumps up the version
 * # for the chunk and notifies the chunkservers.  The message has to
 * be suspended until the chunkservers ack.  After the message is
 * restarted, we need to update the metatree to reflect the newer
 * version # before notifying the client.
 *
 * On the other hand, if a new lease isn't required, then the layout
 * manager tells us where the data has been placed; the process for
 * the request is therefore complete.
 */
static void
handle_allocate(MetaRequest *r)
{
	MetaAllocate *req = static_cast<MetaAllocate *>(r);

	if (!req->layoutDone) {
		KFS_LOG_VA_DEBUG("Starting layout for req:%lld", req->opSeqno);
		// force an allocation
		req->chunkId = 0;
		// start at step #2 above.
		req->status = metatree.allocateChunkId(
				req->fid, req->offset, &req->chunkId,
				&req->chunkVersion, &req->numReplicas);
		if ((req->status != 0) && (req->status != -EEXIST)) {
			// we have a problem
			return;
		}
		if (req->status == -EEXIST) {
			bool isNewLease = false;
			// Get a (new) lease if possible
			req->status = gLayoutManager.GetChunkWriteLease(req, isNewLease);
			if (req->status != 0) {
				// couln't get the lease...bail
				return;
			}
			if (!isNewLease) {
				KFS_LOG_VA_DEBUG("Got valid lease for req:%lld",
						req->opSeqno);
				// we got a valid lease.  so, return
				return;
			}
			// new lease and chunkservers have been notified
			// so, wait for them to ack

		} else if (gLayoutManager.AllocateChunk(req) != 0) {
			// we have a problem
			req->status = -ENOSPC;
			return;
		}
		// we have queued an RPC to the chunkserver.  so, hold
		// off processing (step #5)
		req->suspended = true;
		return;
	}
	KFS_LOG_VA_DEBUG("Layout is done for req:%lld", req->opSeqno);

	if (req->status != 0) {
		// we have a problem: it is possible that the server
		// went down.  ask the client to retry....
		req->status = -KFS::EALLOCFAILED;

		metatree.getChunkVersion(req->fid, req->chunkId,
					&req->chunkVersion);
		if (req->chunkVersion > 0) {
			// reset version #'s at the chunkservers
			for_each(req->servers.begin(), req->servers.end(),
				ChunkVersionChanger(req->fid, req->chunkId,
						req->chunkVersion));
		} else {
			// this is the first time the chunk was allocated.
			// since the allocation failed, remove existence of this chunk
			// on the metaserver.
			gLayoutManager.RemoveChunkToServerMapping(req->chunkId);
		}
		req->suspended = true;
		ChangeIncarnationNumber(req);
		return;
	}
	// layout is complete (step #6)
	req->suspended = false;

	// update the tree (step #7) and since we turned off the
	// suspend flag, the request will be logged and go on its
	// merry way.
	req->status = metatree.assignChunkId(req->fid, req->offset,
					req->chunkId, req->chunkVersion);
	if (req->status != 0)
		KFS_LOG_VA_DEBUG("Assign chunk id failed for %lld,%lld", req->fid, req->offset);
}

static void
handle_truncate(MetaRequest *r)
{
	MetaTruncate *req = static_cast <MetaTruncate *>(r);
	chunkOff_t allocOffset = 0;

	if (gWormMode) {
		req->status = -EPERM;
		return;
	}

	req->status = metatree.truncate(req->fid, req->offset, &allocOffset);
	if (req->status > 0) {
		// an allocation is needed
		MetaAllocate *alloc = new MetaAllocate(req->opSeqno, req->fid,
							allocOffset);

		KFS_LOG_VA_DEBUG("Suspending truncation due to alloc at offset: %lld",
				allocOffset);

		// tie things together
		alloc->req = r;
		req->suspended = true;
		handle_allocate(alloc);
	}
}

static void
handle_rename(MetaRequest *r)
{
	MetaRename *req = static_cast <MetaRename *>(r);
	if (gWormMode && ((!isWormMutationAllowed(req->oldname)) ||
                          path_exists(req->newname))) {
		// renames are disabled in WORM mode: otherwise, we could
		// overwrite an existing file
		req->status = -EPERM;
		return;
	}
	req->status = metatree.rename(req->dir, req->oldname, req->newname,
					req->oldpath, req->overwrite);
}

static void
handle_change_file_replication(MetaRequest *r)
{
	MetaChangeFileReplication *req = static_cast <MetaChangeFileReplication *>(r);
	if (file_exists(req->fid))
		req->status = metatree.changePathReplication(req->fid, req->numReplicas);
	else
		req->status = -ENOENT;
}

static void
handle_retire_chunkserver(MetaRequest *r)
{
	MetaRetireChunkserver *req = static_cast <MetaRetireChunkserver *>(r);

	req->status = gLayoutManager.RetireServer(req->location, req->nSecsDown);
}

static void
handle_toggle_rebalancing(MetaRequest *r)
{
	MetaToggleRebalancing *req = static_cast <MetaToggleRebalancing *>(r);

	gLayoutManager.ToggleRebalancing(req->value);
	req->status = 0;
}

static void
handle_toggle_worm(MetaRequest *r) {
	MetaToggleWORM *req = static_cast <MetaToggleWORM *>(r);
   	setWORMMode(req->value);
	req->status = 0;
}

static void
handle_execute_rebalanceplan(MetaRequest *r)
{
	MetaExecuteRebalancePlan *req = static_cast <MetaExecuteRebalancePlan *>(r);

	req->status = gLayoutManager.LoadRebalancePlan(req->planPathname);
}

static void
handle_log_rollover(MetaRequest *r)
{
	r->status = 0;
}

static void
handle_hello(MetaRequest *r)
{
	MetaHello *req = static_cast <MetaHello *>(r);

	if (req->status < 0) {
		// bad hello request...possible cluster key mismatch
		return;
	}

	gLayoutManager.AddNewServer(req);
	req->status = 0;
}

static void
handle_bye(MetaRequest *r)
{
	MetaBye *req = static_cast <MetaBye *>(r);

	gLayoutManager.ServerDown(req->server.get());
	req->status = 0;
}

static void
handle_lease_acquire(MetaRequest *r)
{
	MetaLeaseAcquire *req = static_cast <MetaLeaseAcquire *>(r);

	req->status = gLayoutManager.GetChunkReadLease(req);
}

static void
handle_lease_renew(MetaRequest *r)
{
	MetaLeaseRenew *req = static_cast <MetaLeaseRenew *>(r);

	req->status = gLayoutManager.LeaseRenew(req);
}

static void
handle_lease_relinquish(MetaRequest *r)
{
	MetaLeaseRelinquish *req = static_cast <MetaLeaseRelinquish *>(r);

	req->status = gLayoutManager.LeaseRelinquish(req);
	KFS_LOG_VA_INFO("Lease relinquish: %s, status = %d", r->Show().c_str(),
			req->status);
}

static void
handle_lease_cleanup(MetaRequest *r)
{
	MetaLeaseCleanup *req = static_cast <MetaLeaseCleanup *>(r);

	gLayoutManager.LeaseCleanup();
	// some leases are gone.  so, cleanup dumpster
	metatree.cleanupDumpster();
	req->status = 0;
}

static void
handle_chunk_corrupt(MetaRequest *r)
{
	MetaChunkCorrupt *req = static_cast <MetaChunkCorrupt *>(r);

	gLayoutManager.ChunkCorrupt(req);
	req->status = 0;
}

static void
handle_chunk_replication_check(MetaRequest *r)
{
	MetaChunkReplicationCheck *req = static_cast <MetaChunkReplicationCheck *>(r);

	gLayoutManager.ChunkReplicationChecker();
	req->status = 0;
}

static void
handle_chunk_size_done(MetaRequest *r)
{
	MetaChunkSize *req = static_cast <MetaChunkSize *>(r);

	if (req->chunkSize < 0) {
		req->status = -1;
		return;
	}

	MetaFattr *fa = metatree.getFattr(req->fid);
	if ((fa != NULL) && (fa->type == KFS_FILE)) {
		vector<MetaChunkInfo*> chunkInfo;
                int status = metatree.getalloc(fa->id(), chunkInfo);
		off_t spaceUsageDelta = 0;

                if ((status != 0) || (chunkInfo.size() == 0)) {
                        return;
                }
		if (fa->filesize > 0) {
			// stash the value for doing the delta calc
			spaceUsageDelta = fa->filesize;
		}
		// only if we are looking at the last chunk of the file can we
		// set the size.
                MetaChunkInfo* lastChunk = chunkInfo.back();
		if (req->chunkId == lastChunk->chunkId) {
			fa->filesize = (fa->chunkcount - 1) * CHUNKSIZE +
					req->chunkSize;
		}
		// stash the value away so that we can log it.
		req->filesize = fa->filesize;
		//
		// we possibly updated fa->filesize; so, compute the delta from
		// before and after
		//
		spaceUsageDelta = fa->filesize - spaceUsageDelta;
		if (spaceUsageDelta > 0) {
			if (req->pathname == "") {
				req->pathname = metatree.getPathname(req->fid);
			}
			metatree.updateSpaceUsageForPath(req->pathname, spaceUsageDelta);
		}
	}
	req->status = 0;
}

static void
handle_chunk_replication_done(MetaRequest *r)
{
	MetaChunkReplicate *req = static_cast <MetaChunkReplicate *>(r);

	gLayoutManager.ChunkReplicationDone(req);
}

static void
handle_change_chunkVersionInc(MetaRequest *r)
{
	r->status = 0;
}

static void
handle_ping(MetaRequest *r)
{
	MetaPing *req = static_cast <MetaPing *>(r);

	req->status = 0;

	gLayoutManager.Ping(req->systemInfo, req->servers, req->downServers, req->retiringServers);

}

static void
handle_upservers(MetaRequest *r)
{
    MetaUpServers *req = static_cast <MetaUpServers *>(r);

    req->status = 0;
    gLayoutManager.UpServers(req->stringStream);
}

static void
handle_dump_chunkToServerMap(MetaRequest *r)
{
	MetaDumpChunkToServerMap *req = static_cast <MetaDumpChunkToServerMap *>(r);

	req->status = 0;

	gLayoutManager.DumpChunkToServerMap();
}

static void
handle_dump_chunkReplicationCandidates(MetaRequest *r)
{
	MetaDumpChunkReplicationCandidates *req = static_cast <MetaDumpChunkReplicationCandidates *>(r);
	ostringstream os;

	req->status = 0;

	gLayoutManager.DumpChunkReplicationCandidates(os);
	req->blocks = os.str();
}

static void
handle_stats(MetaRequest *r)
{
	MetaStats *req = static_cast <MetaStats *>(r);
	ostringstream os;

	req->status = 0;

	globals().counterManager.Show(os);
	req->stats = os.str();

}

static void
handle_open_files(MetaRequest *r)
{
	MetaOpenFiles *req = static_cast <MetaOpenFiles *>(r);

	req->status = 0;

	gLayoutManager.GetOpenFiles(req->openForRead, req->openForWrite);
}

/*
 * Map request types to the functions that handle them.
 */
static void
setup_handlers()
{
	handler[META_LOOKUP] = handle_lookup;
	handler[META_LOOKUP_PATH] = handle_lookup_path;
	handler[META_CREATE] = handle_create;
	handler[META_MKDIR] = handle_mkdir;
	handler[META_REMOVE] = handle_remove;
	handler[META_RMDIR] = handle_rmdir;
	handler[META_READDIR] = handle_readdir;
	handler[META_READDIRPLUS] = handle_readdirplus;
	handler[META_GETALLOC] = handle_getalloc;
	handler[META_GETLAYOUT] = handle_getlayout;
	handler[META_ALLOCATE] = handle_allocate;
	handler[META_TRUNCATE] = handle_truncate;
	handler[META_RENAME] = handle_rename;
	handler[META_CHANGE_FILE_REPLICATION] = handle_change_file_replication;
	handler[META_LOG_ROLLOVER] = handle_log_rollover;
	handler[META_CHUNK_SIZE] = handle_chunk_size_done;
	handler[META_CHUNK_REPLICATE] = handle_chunk_replication_done;
	handler[META_CHUNK_REPLICATION_CHECK] = handle_chunk_replication_check;
	handler[META_RETIRE_CHUNKSERVER] = handle_retire_chunkserver;
	handler[META_TOGGLE_REBALANCING] = handle_toggle_rebalancing;
	handler[META_EXECUTE_REBALANCEPLAN] = handle_execute_rebalanceplan;
	handler[META_TOGGLE_WORM] = handle_toggle_worm;
	// Chunk server -> Meta server op
	handler[META_HELLO] = handle_hello;
	handler[META_BYE] = handle_bye;

	// Lease related ops
	handler[META_LEASE_ACQUIRE] = handle_lease_acquire;
	handler[META_LEASE_RENEW] = handle_lease_renew;
	handler[META_LEASE_RELINQUISH] = handle_lease_relinquish;
	handler[META_LEASE_CLEANUP] = handle_lease_cleanup;

	// Chunk version # increment/corrupt chunk
	handler[META_CHANGE_CHUNKVERSIONINC] = handle_change_chunkVersionInc;
	handler[META_CHUNK_CORRUPT] = handle_chunk_corrupt;

	// Monitoring RPCs
	handler[META_PING] = handle_ping;
	handler[META_STATS] = handle_stats;
	handler[META_DUMP_CHUNKTOSERVERMAP] = handle_dump_chunkToServerMap;
	handler[META_DUMP_CHUNKREPLICATIONCANDIDATES] = handle_dump_chunkReplicationCandidates;
	handler[META_OPEN_FILES] = handle_open_files;
	handler[META_UPSERVERS] = handle_upservers;

	gParseHandlers["LOOKUP"] = parseHandlerLookup;
	gParseHandlers["LOOKUP_PATH"] = parseHandlerLookupPath;
	gParseHandlers["CREATE"] = parseHandlerCreate;
	gParseHandlers["MKDIR"] = parseHandlerMkdir;
	gParseHandlers["REMOVE"] = parseHandlerRemove;
	gParseHandlers["RMDIR"] = parseHandlerRmdir;
	gParseHandlers["READDIR"] = parseHandlerReaddir;
	gParseHandlers["READDIRPLUS"] = parseHandlerReaddirPlus;
	gParseHandlers["GETALLOC"] = parseHandlerGetalloc;
	gParseHandlers["GETLAYOUT"] = parseHandlerGetlayout;
	gParseHandlers["ALLOCATE"] = parseHandlerAllocate;
	gParseHandlers["TRUNCATE"] = parseHandlerTruncate;
	gParseHandlers["RENAME"] = parseHandlerRename;
	gParseHandlers["CHANGE_FILE_REPLICATION"] = parseHandlerChangeFileReplication;
	gParseHandlers["RETIRE_CHUNKSERVER"] = parseHandlerRetireChunkserver;
	gParseHandlers["EXECUTE_REBALANCEPLAN"] = parseHandlerExecuteRebalancePlan;
	gParseHandlers["TOGGLE_REBALANCING"] = parseHandlerToggleRebalancing;

	// Lease related ops
	gParseHandlers["LEASE_ACQUIRE"] = parseHandlerLeaseAcquire;
	gParseHandlers["LEASE_RENEW"] = parseHandlerLeaseRenew;
	gParseHandlers["LEASE_RELINQUISH"] = parseHandlerLeaseRelinquish;
	gParseHandlers["CORRUPT_CHUNK"] = parseHandlerChunkCorrupt;

	// Meta server <-> Chunk server ops
	gParseHandlers["HELLO"] = parseHandlerHello;

	gParseHandlers["PING"] = parseHandlerPing;
	gParseHandlers["UPSERVERS"] = parseHandlerUpServers;
	gParseHandlers["TOGGLE_WORM"] = parseHandlerToggleWORM;
	gParseHandlers["STATS"] = parseHandlerStats;
	gParseHandlers["DUMP_CHUNKTOSERVERMAP"] = parseHandlerDumpChunkToServerMap;
	gParseHandlers["DUMP_CHUNKREPLICATIONCANDIDATES"] = parseHandlerDumpChunkReplicationCandidates;
	gParseHandlers["OPEN_FILES"] = parseHandlerOpenFiles;
}

/*!
 * \brief request queue initialization
 */
void
KFS::initialize_request_handlers()
{
	setup_handlers();
}

/*!
 * \brief remove successive requests for the queue and carry them out.
 */
void
KFS::process_request()
{
	MetaRequest *r = requestList.dequeue();
	map <MetaOp, ReqHandler>::iterator h = handler.find(r->op);
	if (h == handler.end())
		r->status = -ENOSYS;
	else
		((*h).second)(r);

	if (!r->suspended) {
		UpdateCounter(r->op);
		oplog.add_pending(r);
	}
}

/*!
 * \brief add a new request to the queue
 * \param[in] r the request
 */
void
KFS::submit_request(MetaRequest *r)
{
	requestList.enqueue(r);
}

/*!
 * \brief print out the leaf nodes for debugging
 */
void
KFS::printleaves()
{
	metatree.printleaves();
}

/*!
 * \brief log lookup request (nop)
 */
int
MetaLookup::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log lookup path request (nop)
 */
int
MetaLookupPath::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log a file create
 */
int
MetaCreate::log(ofstream &file) const
{
	// use the log entry time as a proxy for when the file was created
	struct timeval t;
	gettimeofday(&t, NULL);

	file << "create/dir/" << dir << "/name/" << name <<
		"/id/" << fid << "/numReplicas/" << (int) numReplicas << 
		"/ctime/" << showtime(t) << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief log a directory create
 */
int
MetaMkdir::log(ofstream &file) const
{
	struct timeval t;
	gettimeofday(&t, NULL);

	file << "mkdir/dir/" << dir << "/name/" << name <<
		"/id/" << fid << "/ctime/" << showtime(t) << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief log a file deletion
 */
int
MetaRemove::log(ofstream &file) const
{
	file << "remove/dir/" << dir << "/name/" << name << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief log a directory deletion
 */
int
MetaRmdir::log(ofstream &file) const
{
	file << "rmdir/dir/" << dir << "/name/" << name << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief log directory read (nop)
 */
int
MetaReaddir::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log directory read (nop)
 */
int
MetaReaddirPlus::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log getalloc (nop)
 */
int
MetaGetalloc::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log getlayout (nop)
 */
int
MetaGetlayout::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log a chunk allocation
 */
int
MetaAllocate::log(ofstream &file) const
{
	// use the log entry time as a proxy for when the block was created/file
	// was modified
	struct timeval t;
	gettimeofday(&t, NULL);

	file << "allocate/file/" << fid << "/offset/" << offset
	     << "/chunkId/" << chunkId
	     << "/chunkVersion/" << chunkVersion 
	     << "/mtime/" << showtime(t) << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief log a file truncation
 */
int
MetaTruncate::log(ofstream &file) const
{
	// use the log entry time as a proxy for when the file was modified
	struct timeval t;
	gettimeofday(&t, NULL);

	file << "truncate/file/" << fid << "/offset/" << offset 
	     << "/mtime/" << showtime(t) << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief log a rename
 */
int
MetaRename::log(ofstream &file) const
{
	file << "rename/dir/" << dir << "/old/" <<
		oldname << "/new/" << newname << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief Log a chunk-version-increment change to disk.
*/
int
MetaChangeChunkVersionInc::log(ofstream &file) const
{
	file << "chunkVersionInc/" << cvi << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief log change file replication
 */
int
MetaChangeFileReplication::log(ofstream &file) const
{
	file << "setrep/file/" << fid << "/replicas/" << numReplicas << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief log retire chunkserver (nop)
 */
int
MetaRetireChunkserver::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log toggling of chunkserver rebalancing state (nop)
 */
int
MetaToggleRebalancing::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log toggling of metaserver WORM state (nop)
 */
int
MetaToggleWORM::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log execution of rebalance plan (nop)
 */
int
MetaExecuteRebalancePlan::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief close log and open a new one
 */
int
MetaLogRollover::log(ofstream &file) const
{
	return oplog.finishLog();
}

/*!
 * \brief for a chunkserver hello, there is nothing to log
 */
int
MetaHello::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a chunkserver's death, there is nothing to log
 */
int
MetaBye::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a chunkserver allocate, there is nothing to log
 */
int
MetaChunkAllocate::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log a chunk delete; (nop)
 */
int
MetaChunkDelete::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log a chunk truncation; (nop)
 */
int
MetaChunkTruncate::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log a heartbeat to a chunk server; (nop)
 */
int
MetaChunkHeartbeat::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log a stale notify to a chunk server; (nop)
 */
int
MetaChunkStaleNotify::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief log a chunk server retire; (nop)
 */
int
MetaChunkRetire::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief when a chunkserver tells us of a corrupted chunk, there is nothing to log
 */
int
MetaChunkCorrupt::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief When notifying a chunkserver of a version # change, there is
 * nothing to log.
 */
int
MetaChunkVersChange::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief When asking a chunkserver to replicate a chunk, there is
 * nothing to log.
 */
int
MetaChunkReplicate::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief When asking a chunkserver for a chunk's size, there is
 * write out the estimate of the file's size.
 */
int
MetaChunkSize::log(ofstream &file) const
{
	if (filesize < 0)
		return 0;

	file << "size/file/" << fid << "/filesize/" << filesize << '\n';
	return file.fail() ? -EIO : 0;
}

/*!
 * \brief for a ping, there is nothing to log
 */
int
MetaPing::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a request of upserver, there is nothing to log
 */
int
MetaUpServers::log(ofstream &file) const
{
    return 0;
}

/*!
 * \brief for a stats request, there is nothing to log
 */
int
MetaStats::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a map dump request, there is nothing to log
 */
int
MetaDumpChunkToServerMap::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a dump chunk replication candidates request, there is nothing to log
 */
int
MetaDumpChunkReplicationCandidates::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for an open files request, there is nothing to log
 */
int
MetaOpenFiles::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a lease acquire request, there is nothing to log
 */
int
MetaLeaseAcquire::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a lease renew request, there is nothing to log
 */
int
MetaLeaseRenew::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a lease renew relinquish, there is nothing to log
 */
int
MetaLeaseRelinquish::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief for a lease cleanup request, there is nothing to log
 */
int
MetaLeaseCleanup::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief This is an internally generated op.  There is
 * nothing to log.
 */
int
MetaChunkReplicationCheck::log(ofstream &file) const
{
	return 0;
}

/*!
 * \brief parse a command sent by a client
 *
 * Commands are of the form:
 * <COMMAND NAME> \r\n
 * {header: value \r\n}+\r\n
 *
 * The general model in parsing the client command:
 * 1. Each command has its own parser
 * 2. Extract out the command name and find the parser for that
 * command
 * 3. Dump the header/value pairs into a properties object, so that we
 * can extract the header/value fields in any order.
 * 4. Finally, call the parser for the command sent by the client.
 *
 * @param[in] cmdBuf: buffer containing the request sent by the client
 * @param[in] cmdLen: length of cmdBuf
 * @param[out] res: A piece of memory allocated by calling new that
 * contains the data for the request.  It is the caller's
 * responsibility to delete the memory returned in res.
 * @retval 0 on success;  -1 if there is an error
 */
int
KFS::ParseCommand(char *cmdBuf, int cmdLen, MetaRequest **res)
{
	const char *delims = " \r\n";
	// header/value pairs are separated by a :
	const char separator = ':';
	string cmdStr;
	string::size_type cmdEnd;
	Properties prop;
	istringstream ist(cmdBuf);
	ParseHandlerMapIter entry;
	ParseHandler handler;

	*res = NULL;

	// get the first line and find the command name
	ist >> cmdStr;
	// trim the command
	cmdEnd = cmdStr.find_first_of(delims);
	if (cmdEnd != cmdStr.npos) {
		cmdStr.erase(cmdEnd);
	}

	// find the parse handler and parse the thing
	entry = gParseHandlers.find(cmdStr);
	if (entry == gParseHandlers.end())
		return -1;
	handler = entry->second;

	prop.loadProperties(ist, separator, false);

	return (*handler)(prop, res);
}

/*!
 * \brief Various parse handlers.  All of them follow the same model:
 * @param[in] prop: A properties table filled with values sent by the client
 * @param[out] r: If parse is successful, returns a dynamically
 * allocated meta request object. It is the callers responsibility to get rid
 * of this pointer.
 * @retval 0 if parse is successful; -1 otherwise.
 *
 * XXX: Need to make MetaRequest a smart pointer
 */

static int
parseHandlerLookup(Properties &prop, MetaRequest **r)
{
	fid_t dir;
	const char *name;
	seq_t seq;

	seq = prop.getValue("Cseq", (seq_t) -1);
	dir = prop.getValue("Parent File-handle", (fid_t) -1);
	if (dir < 0)
		return -1;
	name = prop.getValue("Filename", (const char*) NULL);
	if (name == NULL)
		return -1;
	*r = new MetaLookup(seq, dir, name);
	return 0;
}

static int
parseHandlerLookupPath(Properties &prop, MetaRequest **r)
{
	fid_t root;
	const char *path;
	seq_t seq;

	seq = prop.getValue("Cseq", (seq_t) -1);
	root = prop.getValue("Root File-handle", (fid_t) -1);
	if (root < 0)
		return -1;
	path = prop.getValue("Pathname", (const char *) NULL);
	if (path == NULL)
		return -1;
	*r = new MetaLookupPath(seq, root, path);
	return 0;
}

static int
parseHandlerCreate(Properties &prop, MetaRequest **r)
{
	fid_t dir;
	const char *name;
	seq_t seq;
	int16_t numReplicas;
	bool exclusive;

	seq = prop.getValue("Cseq", (seq_t) -1);
	dir = prop.getValue("Parent File-handle", (fid_t) -1);
	if (dir < 0)
		return -1;
	name = prop.getValue("Filename", (const char *) NULL);
	if (name == NULL)
		return -1;
	// cap replication
	numReplicas = min((int16_t) prop.getValue("Num-replicas", 1), gMaxReplicasPerFile);
	if (numReplicas <= 0)
		return -1;
	// by default, create overwrites the file; when it is turned off,
	// it is for supporting O_EXCL
	exclusive = (prop.getValue("Exclusive", 1)) == 1;

	*r = new MetaCreate(seq, dir, name, numReplicas, exclusive);
	return 0;
}

static int
parseHandlerRemove(Properties &prop, MetaRequest **r)
{
	fid_t dir;
	const char *name;
	seq_t seq;

	seq = prop.getValue("Cseq", (seq_t) -1);
	dir = prop.getValue("Parent File-handle", (fid_t) -1);
	if (dir < 0)
		return -1;
	name = prop.getValue("Filename", (const char *) NULL);
	if (name == NULL)
		return -1;
	MetaRemove *rm = new MetaRemove(seq, dir, name);
	rm->pathname = prop.getValue("Pathname", "");
	*r = rm;
	return 0;
}

static int
parseHandlerMkdir(Properties &prop, MetaRequest **r)
{
	fid_t dir;
	const char *name;
	seq_t seq;

	seq = prop.getValue("Cseq", (seq_t) -1);
	dir = prop.getValue("Parent File-handle", (fid_t) -1);
	if (dir < 0)
		return -1;
	name = prop.getValue("Directory", (const char *) NULL);
	if (name == NULL)
		return -1;
	*r = new MetaMkdir(seq, dir, name);
	return 0;
}

static int
parseHandlerRmdir(Properties &prop, MetaRequest **r)
{
	fid_t dir;
	const char *name;
	seq_t seq;

	seq = prop.getValue("Cseq", (seq_t) -1);
	dir = prop.getValue("Parent File-handle", (fid_t) -1);
	if (dir < 0)
		return -1;
	name = prop.getValue("Directory", (const char *) NULL);
	if (name == NULL)
		return -1;
	MetaRmdir *rm = new MetaRmdir(seq, dir, name);
	rm->pathname = prop.getValue("Pathname", "");
	*r = rm;
	return 0;
}

static int
parseHandlerReaddir(Properties &prop, MetaRequest **r)
{
	fid_t dir;
	seq_t seq;

	seq = prop.getValue("Cseq", (seq_t) -1);
	dir = prop.getValue("Directory File-handle", (fid_t) -1);
	if (dir < 0)
		return -1;
	*r = new MetaReaddir(seq, dir);
	return 0;
}

static int
parseHandlerReaddirPlus(Properties &prop, MetaRequest **r)
{
	fid_t dir;
	seq_t seq;

	seq = prop.getValue("Cseq", (seq_t) -1);
	dir = prop.getValue("Directory File-handle", (fid_t) -1);
	if (dir < 0)
		return -1;
	*r = new MetaReaddirPlus(seq, dir);
	return 0;
}

static int
parseHandlerGetalloc(Properties &prop, MetaRequest **r)
{
	fid_t fid;
	seq_t seq;
	chunkOff_t offset;
	const char *pathname;

	seq = prop.getValue("Cseq", (seq_t) -1);
	fid = prop.getValue("File-handle", (fid_t) -1);
	offset = prop.getValue("Chunk-offset", (chunkOff_t) -1);
	pathname = prop.getValue("Pathname", (const char *) NULL);
	if ((fid < 0) || (offset < 0))
		return -1;
	*r = new MetaGetalloc(seq, fid, offset);
	if (pathname != NULL) {
		MetaGetalloc *mg = static_cast<MetaGetalloc *> (*r);
		mg->pathname = pathname;
	}
	return 0;
}

static int
parseHandlerGetlayout(Properties &prop, MetaRequest **r)
{
	fid_t fid;
	seq_t seq;

	seq = prop.getValue("Cseq", (seq_t) -1);
	fid = prop.getValue("File-handle", (fid_t) -1);
	if (fid < 0)
		return -1;
	*r = new MetaGetlayout(seq, fid);
	return 0;
}

static int
parseHandlerAllocate(Properties &prop, MetaRequest **r)
{
	fid_t fid;
	seq_t seq;
	chunkOff_t offset;

	seq = prop.getValue("Cseq", (seq_t) -1);
	fid = prop.getValue("File-handle", (fid_t) -1);
	offset = prop.getValue("Chunk-offset", (chunkOff_t) -1);
	if ((fid < 0) || (offset < 0))
		return -1;
	MetaAllocate *m = new MetaAllocate(seq, fid, offset);
	m->pathname = prop.getValue("Pathname", "");
	m->clientHost = prop.getValue("Client-host", "");
	*r = m;
	return 0;
}

static int
parseHandlerTruncate(Properties &prop, MetaRequest **r)
{
	fid_t fid;
	seq_t seq;
	chunkOff_t offset;

	seq = prop.getValue("Cseq", (seq_t) -1);
	fid = prop.getValue("File-handle", (fid_t) -1);
	offset = prop.getValue("Offset", (chunkOff_t) -1);
	if ((fid < 0) || (offset < 0))
		return -1;
	MetaTruncate *mt = new MetaTruncate(seq, fid, offset);
	mt->pathname = prop.getValue("Pathname", "");
	*r = mt;
	return 0;
}

static int
parseHandlerRename(Properties &prop, MetaRequest **r)
{
	fid_t fid;
	seq_t seq;
	const char *oldname;
	const char *newpath;
	bool overwrite;

	seq = prop.getValue("Cseq", (seq_t) -1);
	fid = prop.getValue("Parent File-handle", (fid_t) -1);
	oldname = prop.getValue("Old-name", (const char *) NULL);
	newpath = prop.getValue("New-path", (const char *) NULL);
	overwrite = (prop.getValue("Overwrite", 0)) == 1;
	if ((fid < 0) || (oldname == NULL) || (newpath == NULL))
		return -1;

	MetaRename *rn = new MetaRename(seq, fid, oldname, newpath, overwrite);
	rn->oldpath = prop.getValue("Old-path", "");
	*r = rn;
	return 0;
}

static int
parseHandlerChangeFileReplication(Properties &prop, MetaRequest **r)
{
	fid_t fid;
	seq_t seq;
	int16_t numReplicas;

	seq = prop.getValue("Cseq", (seq_t) -1);
	fid = prop.getValue("File-handle", (fid_t) -1);
	numReplicas = min((int16_t) prop.getValue("Num-replicas", 1), gMaxReplicasPerFile);
	if (numReplicas <= 0)
		return -1;
	*r = new MetaChangeFileReplication(seq, fid, numReplicas);
	return 0;
}

/*!
 * \brief Message that initiates the retiring of a chunkserver.
*/
static int
parseHandlerRetireChunkserver(Properties &prop, MetaRequest **r)
{
	ServerLocation location;
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	int downtime;

	location.hostname = prop.getValue("Chunk-server-name", "");
	location.port = prop.getValue("Chunk-server-port", -1);
	if (!location.IsValid()) {
		return -1;
	}
	downtime = prop.getValue("Downtime", -1);
	*r = new MetaRetireChunkserver(seq, location, downtime);
	return 0;
}

static int
parseHandlerToggleRebalancing(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	// 1 is enable; 0 is disable
	int value = prop.getValue("Toggle-rebalancing", 0);
	bool v = (value == 1);

	*r = new MetaToggleRebalancing(seq, v);
	KFS_LOG_VA_INFO("Toggle rebalancing: %d", value);
	return 0;
}

/*!
 * \brief Message that initiates the execution of a rebalance plan.
*/
static int
parseHandlerExecuteRebalancePlan(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	string pathname = prop.getValue("Pathname", "");

	*r = new MetaExecuteRebalancePlan(seq, pathname);
	return 0;
}

/*!
 * \brief Validate that the md5 sent by a chunkserver matches one of the
 * acceptable md5's.
 */
static int
isValidMD5Sum(const string &md5sum)
{
	if (!file_exists(gMD5SumFn)) {
		KFS_LOG_VA_INFO("MD5Sum file %s doesn't exist; no admission control", gMD5SumFn.c_str());
		return 1;
	}
	ifstream ifs;
	const int MAXLINE = 512;
	char line[MAXLINE];

	ifs.open(gMD5SumFn.c_str());

	if (ifs.fail()) {
		KFS_LOG_VA_INFO("Unable to open MD5Sum file %s; no admission control", gMD5SumFn.c_str());
		return 1;
	}

	while (!ifs.eof()) {
		ifs.getline(line, MAXLINE);
		string key = line;
		// remove trailing white space
		string::size_type spc = key.find(' ');
		if (spc != string::npos)
			key.erase(spc);
		if (key == md5sum)
			return 1;
	}

	return 0;
}

/*!
 * \brief Parse out the headers from a HELLO message.  The message
 * body contains the id's of the chunks hosted on the server.
 */
static int
parseHandlerHello(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	MetaHello *hello;
	string key;

	hello = new MetaHello(seq);
	hello->location.hostname = prop.getValue("Chunk-server-name", "");
	hello->location.port = prop.getValue("Chunk-server-port", -1);
	if (!hello->location.IsValid()) {
		delete hello;
		return -1;
	}
	key = prop.getValue("Cluster-key", "");
	if (key != gClusterKey) {
		KFS_LOG_VA_INFO("cluster key mismatch: we have %s, chunkserver sent us %s",
				gClusterKey.c_str(), key.c_str());
		hello->status = -EBADCLUSTERKEY;
	}
	key = prop.getValue("MD5Sum", "");
	if (!isValidMD5Sum(key)) {
		KFS_LOG_VA_INFO("MD5sum mismatch from chunkserver %s:%d: it sent us %s",
				hello->location.hostname.c_str(), hello->location.port, key.c_str());
		hello->status = -EBADCLUSTERKEY;
	}
	hello->totalSpace = prop.getValue("Total-space", (long long) 0);
	hello->usedSpace = prop.getValue("Used-space", (long long) 0);
	hello->rackId = prop.getValue("Rack-id", (int) -1);
	// # of chunks hosted on this server
	hello->numChunks = prop.getValue("Num-chunks", 0);
	// The chunk names follow in the body.  This field tracks
	// the length of the message body
	hello->contentLength = prop.getValue("Content-length", 0);

	*r = hello;
	return 0;
}

/*!
 * \brief Parse out the headers from a LEASE_ACQUIRE message.
 */
int
parseHandlerLeaseAcquire(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	chunkId_t chunkId = prop.getValue("Chunk-handle", (chunkId_t) -1);
	const char *pathname = prop.getValue("Pathname", (const char *) NULL);

	*r = new MetaLeaseAcquire(seq, chunkId);
	if (pathname != NULL) {
		MetaLeaseAcquire *mla = static_cast<MetaLeaseAcquire *> (*r);
		mla->pathname = pathname;
	}
	return 0;
}

/*!
 * \brief Parse out the headers from a LEASE_RENEW message.
 */
int
parseHandlerLeaseRenew(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	chunkId_t chunkId = prop.getValue("Chunk-handle", (chunkId_t) -1);
	int64_t leaseId = prop.getValue("Lease-id", (int64_t) -1);
	string leaseTypeStr = prop.getValue("Lease-type", "READ_LEASE");
	const char *pathname = prop.getValue("Pathname", (const char *) NULL);
	LeaseType leaseType;

	if (leaseTypeStr == "WRITE_LEASE")
		leaseType = WRITE_LEASE;
	else
		leaseType = READ_LEASE;

	*r = new MetaLeaseRenew(seq, leaseType, chunkId, leaseId);
	if (pathname != NULL) {
		MetaLeaseRenew *mlr = static_cast<MetaLeaseRenew *> (*r);
		mlr->pathname = pathname;
	}
	return 0;
}

/*!
 * \brief Parse out the headers from a LEASE_RELINQUISH message.
 */
int
parseHandlerLeaseRelinquish(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	chunkId_t chunkId = prop.getValue("Chunk-handle", (chunkId_t) -1);
	int64_t leaseId = prop.getValue("Lease-id", (int64_t) -1);
	string leaseTypeStr = prop.getValue("Lease-type", "READ_LEASE");
	LeaseType leaseType;

	if (leaseTypeStr == "WRITE_LEASE")
		leaseType = WRITE_LEASE;
	else
		leaseType = READ_LEASE;

	*r = new MetaLeaseRelinquish(seq, leaseType, chunkId, leaseId);
	return 0;
}

/*!
 * \brief Parse out the headers from a CORRUPT_CHUNK message.
 */
int
parseHandlerChunkCorrupt(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	fid_t fid = prop.getValue("File-handle", (chunkId_t) -1);
	chunkId_t chunkId = prop.getValue("Chunk-handle", (chunkId_t) -1);

	*r = new MetaChunkCorrupt(seq, fid, chunkId);
	return 0;
}

/*!
 * \brief Parse out the headers from a PING message.
 */
int
parseHandlerPing(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);

	*r = new MetaPing(seq);
	return 0;
}

/*!
 * \brief Parse out the headers for a UPSERVER message.
 */
int
parseHandlerUpServers(Properties &prop, MetaRequest **r)
{
    seq_t seq = prop.getValue("Cseq", (seq_t) -1);

    *r = new MetaUpServers(seq);
    return 0;
}

/*!
 * \brief Parse out the headers from a TOGGLE_WORM message.
 */
int
parseHandlerToggleWORM(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);
	// 1 is enable; 0 is disable
	int value = prop.getValue("Toggle-WORM", 0);
	bool v = (value == 1);

	*r = new MetaToggleWORM(seq, v);
	KFS_LOG_VA_INFO("Toggle WORM: %d", value);
	return 0;
}

/*!
 * \brief Parse out the headers from a STATS message.
 */
int
parseHandlerStats(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);

	*r = new MetaStats(seq);
	return 0;
}

/*!
 * \brief Parse out a dump server map request.
 */
int
parseHandlerDumpChunkToServerMap(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);

	*r = new MetaDumpChunkToServerMap(seq);
	return 0;
}

/*!
 * \brief Parse out a dump chunk replication candidates request.
 */
int
parseHandlerDumpChunkReplicationCandidates(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);

	*r = new MetaDumpChunkReplicationCandidates(seq);
	return 0;
}

/*!
 * \brief Parse out the headers from a STATS message.
 */
int
parseHandlerOpenFiles(Properties &prop, MetaRequest **r)
{
	seq_t seq = prop.getValue("Cseq", (seq_t) -1);

	*r = new MetaOpenFiles(seq);
	return 0;
}

/*!
 * \brief Generate response (a string) for various requests that
 * describes the result of the request execution.  The generated
 * response string is based on the KFS protocol.  All follow the same
 * model:
 * @param[out] os: A string stream that contains the response.
 */
void
MetaLookup::response(ostringstream &os)
{
	static string fname[] = { "empty", "file", "dir" };

	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	os << "File-handle: " << toString(result.id()) << "\r\n";
	os << "Type: " << fname[result.type] << "\r\n";
	os << "Chunk-count: " << toString(result.chunkcount) << "\r\n";
	os << "File-size: " << toString(result.filesize) << "\r\n";
	os << "Replication: " << toString(result.numReplicas) << "\r\n";
	sendtime(os, "M-Time:", result.mtime, "\r\n");
	sendtime(os, "C-Time:", result.ctime, "\r\n");
	sendtime(os, "CR-Time:", result.crtime, "\r\n\r\n");
}

void
MetaLookupPath::response(ostringstream &os)
{
	static string fname[] = { "empty", "file", "dir" };

	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	os << "File-handle: " << toString(result.id()) << "\r\n";
	os << "Type: " << fname[result.type] << "\r\n";
	os << "Chunk-count: " << toString(result.chunkcount) << "\r\n";
	os << "File-size: " << toString(result.filesize) << "\r\n";
	os << "Replication: " << toString(result.numReplicas) << "\r\n";
	sendtime(os, "M-Time:", result.mtime, "\r\n");
	sendtime(os, "C-Time:", result.ctime, "\r\n");
	sendtime(os, "CR-Time:", result.crtime, "\r\n\r\n");
}

void
MetaCreate::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	os << "File-handle: " << toString(fid) << "\r\n\r\n";
}

void
MetaRemove::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaMkdir::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	os << "File-handle: " << toString(fid) << "\r\n\r\n";
}

void
MetaRmdir::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaReaddir::response(ostringstream &os)
{
	vector<MetaDentry>::iterator iter;
	string res;
	int numEntries = 0;

	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	// Send over the names---1 name per line so it is easy to
	// extract it out
	// XXX This should include the file id also, and probably
	// the other NFS READDIR elements, namely a cookie and
	// eof indicator to support reading less than a whole
	// directory at a time.
	for (iter = v.begin(); iter != v.end(); ++iter) {
		// "/" doesn't have "/" as an entry in it.
		if ((dir == ROOTFID) && (iter->getName() == "/"))
			continue;

		res = res + iter->getName() + "\n";
		++numEntries;
	}
	os << "Num-Entries: " << numEntries << "\r\n";
	os << "Content-length: " << res.length() << "\r\n\r\n";
	if (res.length() > 0)
		os << res;
}

void
MetaReaddirPlus::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	os << "Num-Entries: " << numEntries << "\r\n";
	os << "Content-length: " << v.str().length() << "\r\n\r\n";
	os << v.str();
}

void
MetaRename::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaGetalloc::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	os << "Chunk-handle: " << chunkId << "\r\n";
	os << "Chunk-version: " << chunkVersion << "\r\n";
	os << "Num-replicas: " << locations.size() << "\r\n";

	assert(locations.size() > 0);

	os << "Replicas:";
	for_each(locations.begin(), locations.end(), ListServerLocations(os));
	os << "\r\n\r\n";
}

void
MetaGetlayout::response(ostringstream &os)
{
	vector<ChunkLayoutInfo>::iterator iter;
	ChunkLayoutInfo l;
	string res;

	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	os << "Num-chunks: " << v.size() << "\r\n";
	// Send over the layout info
	for (iter = v.begin(); iter != v.end(); ++iter) {
		l = *iter;
		res = res + l.toString();
	}
	os << "Content-length: " << res.length() << "\r\n\r\n";

	if (res.length() > 0)
		os << res;
}

class PrintChunkServerLocations {
	ostringstream &os;
public:
	PrintChunkServerLocations(ostringstream &out): os(out) { }
	void operator () (ChunkServerPtr &s)
	{
		os << " " <<  s->ServerID();
	}
};

void
MetaAllocate::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status < 0) {
		os << "\r\n";
		return;
	}
	os << "Chunk-handle: " << chunkId << "\r\n";
	os << "Chunk-version: " << chunkVersion << "\r\n";

	os << "Master: " << master->ServerID() << "\r\n";
	os << "Num-replicas: " << servers.size() << "\r\n";

	assert(servers.size() > 0);
	os << "Replicas:";
	for_each(servers.begin(), servers.end(), PrintChunkServerLocations(os));
	os << "\r\n\r\n";
}

void
MetaLeaseAcquire::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	if (status >= 0) {
		os << "Lease-id: " << leaseId << "\r\n";
	}
	os << "\r\n";
}

void
MetaLeaseRenew::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaLeaseRelinquish::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaHello::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaChunkCorrupt::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaTruncate::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaChangeFileReplication::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Num-replicas: " << numReplicas << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaRetireChunkserver::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaToggleRebalancing::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaToggleWORM::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaExecuteRebalancePlan::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";
}

void
MetaPing::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	os << "Build-version: " << KFS::KFS_BUILD_VERSION_STRING << "\r\n";
	os << "Source-version: " << KFS::KFS_SOURCE_REVISION_STRING << "\r\n";
	if (gWormMode)
		os << "WORM: " << 1 << "\r\n";
	else
		os << "WORM: " << 0 << "\r\n";
	os << "System Info: " << systemInfo << "\r\n";
	os << "Servers: " << servers << "\r\n";
	os << "Retiring Servers: " << retiringServers << "\r\n";
	os << "Down Servers: " << downServers << "\r\n\r\n";
}

void
MetaUpServers::response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << opSeqno << "\r\n";
    os << "Status: " << status << "\r\n";
    os << "Content-length: " << stringStream.str().length() << "\r\n\r\n";
    if (stringStream.str().length() > 0)
        os << stringStream.str();
}

void
MetaStats::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	os << stats << "\r\n";
}

void
MetaDumpChunkToServerMap::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n\r\n";

	/*
    	ostringstream v;
	gLayoutManager.DumpChunkToServerMap(v);
	os << "Content-length: " << v.str().length() << "\r\n\r\n";
	if (v.str().length() > 0)
	    os << v.str();
	*/
}

void
MetaDumpChunkReplicationCandidates::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	os << "Content-length: " << blocks.length() << "\r\n\r\n";
	if (blocks.length() > 0)
		os << blocks;
}

void
MetaOpenFiles::response(ostringstream &os)
{
	os << "OK\r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Status: " << status << "\r\n";
	os << "Read: " << openForRead << "\r\n";
	os << "Write: " << openForWrite << "\r\n\r\n";
}

/*!
 * \brief Generate request (a string) that should be sent to the chunk
 * server.  The generated request string is based on the KFS
 * protocol.  All follow the same model:
 * @param[out] os: A string stream that contains the response.
 */
void
MetaChunkAllocate::request(ostringstream &os)
{
	MetaAllocate *allocOp = static_cast<MetaAllocate *>(req);
	assert(allocOp != NULL);

	os << "ALLOCATE \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n";
	os << "File-handle: " << allocOp->fid << "\r\n";
	os << "Chunk-handle: " << allocOp->chunkId << "\r\n";
	os << "Chunk-version: " << allocOp->chunkVersion << "\r\n";
	if (leaseId >= 0) {
		os << "Lease-id: " << leaseId << "\r\n";
	}

	os << "Num-servers: " << allocOp->servers.size() << "\r\n";
	assert(allocOp->servers.size() > 0);

	os << "Servers:";
	for_each(allocOp->servers.begin(), allocOp->servers.end(),
			PrintChunkServerLocations(os));
	os << "\r\n\r\n";
}

void
MetaChunkDelete::request(ostringstream &os)
{
	os << "DELETE \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n";
	os << "Chunk-handle: " << chunkId << "\r\n\r\n";
}

void
MetaChunkTruncate::request(ostringstream &os)
{
	os << "TRUNCATE \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n";
	os << "Chunk-handle: " << chunkId << "\r\n";
	os << "Chunk-size: " << chunkSize << "\r\n\r\n";
}

void
MetaChunkHeartbeat::request(ostringstream &os)
{
	os << "HEARTBEAT \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n\r\n";
}

void
MetaChunkStaleNotify::request(ostringstream &os)
{
	string s;
	vector<chunkId_t>::size_type i;

	os << "STALE_CHUNKS \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n";
	os << "Num-chunks: " << staleChunkIds.size() << "\r\n";
	for (i = 0; i < staleChunkIds.size(); ++i) {
		s += toString(staleChunkIds[i]);
		s += " ";
	}
	os << "Content-length: " << s.length() << "\r\n\r\n";
	os << s;
}

void
MetaChunkRetire::request(ostringstream &os)
{
	os << "RETIRE \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n\r\n";
}

void
MetaChunkVersChange::request(ostringstream &os)
{
	os << "CHUNK_VERS_CHANGE \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n";
	os << "File-handle: " << fid << "\r\n";
	os << "Chunk-handle: " << chunkId << "\r\n";
	os << "Chunk-version: " << chunkVersion << "\r\n\r\n";
}

void
MetaChunkReplicate::request(ostringstream &os)
{
	os << "REPLICATE \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n";
	os << "File-handle: " << fid << "\r\n";
	os << "Chunk-handle: " << chunkId << "\r\n";
	os << "Chunk-version: " << chunkVersion << "\r\n";
	os << "Chunk-location: " << srcLocation.ToString() << "\r\n\r\n";
}

void
MetaChunkSize::request(ostringstream &os)
{
	os << "SIZE \r\n";
	os << "Cseq: " << opSeqno << "\r\n";
	os << "Version: KFS/1.0\r\n";
	os << "File-handle: " << fid << "\r\n";
	os << "Chunk-handle: " << chunkId << "\r\n\r\n";
}

/*!
 * $Id: request.h 297 2009-04-04 21:11:58Z sriramsrao $ 
 *
 * \file request.h
 * \brief protocol requests to KFS metadata server
 * \author Blake Lewis (Kosmix Corp.)
 *
 * The model is that various receiver threads handle network
 * connections and extract RPC parameters, then queue a request
 * of the appropriate type for the metadata server to process.
 * When the operation is finished, the server calls back to the
 * receiver with status and any results.
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
#if !defined(KFS_REQUEST_H)
#define KFS_REQUEST_H

#include "common/kfsdecls.h"
#include "kfstypes.h"
#include "meta.h"
#include "thread.h"
#include "util.h"
#include <deque>
#include <fstream>
#include <sstream>
#include <vector>

#include "libkfsIO/KfsCallbackObj.h"

using std::ofstream;
using std::vector;
using std::ostringstream;

namespace KFS {

/*!
 * \brief Metadata server operations
 */
enum MetaOp {
	// Client -> Metadata server ops
	META_LOOKUP,
	META_LOOKUP_PATH,
	META_CREATE,
	META_MKDIR,
	META_REMOVE,
	META_RMDIR,
	META_READDIR,
	META_READDIRPLUS,
	META_GETALLOC,
	META_GETLAYOUT,
	META_ALLOCATE,
	META_TRUNCATE,
	META_RENAME,
	META_LOG_ROLLOVER,
	META_CHANGE_FILE_REPLICATION, //! < Client is asking for a change in file's replication factor
	//!< Admin is notifying us to retire a chunkserver
	META_RETIRE_CHUNKSERVER,
	//!< Admin is notifying us to toggle rebalancing
	META_TOGGLE_REBALANCING,
	//!< Admin is notifying us to execute a rebalance plan
	META_EXECUTE_REBALANCEPLAN,
	META_TOGGLE_WORM, //!< Toggle metaserver's WORM mode
	//!< Metadata server <-> Chunk server ops
	META_HELLO,  //!< Hello RPC sent by chunkserver on startup
	META_BYE,  //!< Internally generated op whenever a chunkserver goes down
	META_CHUNK_HEARTBEAT, //!< Periodic heartbeat from meta->chunk
	META_CHUNK_ALLOCATE, //!< Allocate chunk RPC from meta->chunk
	META_CHUNK_DELETE,  //!< Delete chunk RPC from meta->chunk
	META_CHUNK_TRUNCATE, //!< Truncate chunk RPC from meta->chunk
	META_CHUNK_STALENOTIFY, //!< Stale chunk notification RPC from meta->chunk
	META_CHUNK_VERSCHANGE, //!< Notify chunkserver of version # change from meta->chunk
	META_CHUNK_REPLICATE, //!< Ask chunkserver to replicate a chunk
	META_CHUNK_SIZE, //!< Ask chunkserver for the size of a chunk
	META_CHUNK_REPLICATION_CHECK, //!< Internally generated
	META_CHUNK_CORRUPT, //!< Chunkserver is notifying us that a chunk is corrupt
	//!< All the blocks on the retiring server have been evacuated and the
	//!< server can safely go down.  We are asking the server to take a graceful bow
	META_CHUNK_RETIRE,
	//!< Lease related messages
	META_LEASE_ACQUIRE,
	META_LEASE_RENEW,
	META_LEASE_RELINQUISH,
	//!< Internally generated to cleanup leases
	META_LEASE_CLEANUP,
	//!< Internally generated to update the increment for chunk version #'s
	META_CHANGE_CHUNKVERSIONINC,

	//!< Metadata server monitoring
	META_PING, //!< Print out chunkserves and their configs
	META_STATS, //!< Print out whatever statistics/counters we have
	META_DUMP_CHUNKTOSERVERMAP, //! < Dump out the chunk -> location map
	META_DUMP_CHUNKREPLICATIONCANDIDATES, //! < Dump out the list of chunks being re-replicated
	META_OPEN_FILES, //!< Print out open files---for which there is a valid read/write lease
	META_UPSERVERS //!< Print out live chunk servers

};

/*!
 * \brief Meta request base class
 */
struct MetaRequest {
	const MetaOp op; //!< type of request
	int status;	//!< returned status
	seq_t opSeqno;	//!< command sequence # sent by the client
	seq_t seqno;	//!< sequence no. in log
	const bool mutation; //!< mutates metatree
	bool suspended;  //!< is this request suspended somewhere
	KfsCallbackObj *clnt; //!< a handle to the client that generated this request.
	MetaRequest(MetaOp o, seq_t ops, bool mu):
		op(o), status(0), opSeqno(ops), seqno(0), mutation(mu),
		suspended(false), clnt(NULL) { }
	virtual ~MetaRequest() { }

	//!< when an op finishes execution, we send a response back to
	//!< the client.  This function should generate the appropriate
	//!< response to be sent back as per the KFS protocol.
	virtual void response(ostringstream &os)
	{
		(void) os; // XXX avoid spurious compiler warnings
	};
	virtual int log(ofstream &file) const = 0; //!< write request to log
	virtual string Show() { return ""; }
};

/*!
 * \brief look up a file name
 */
struct MetaLookup: public MetaRequest {
	fid_t dir;	//!< parent directory fid
	string name;	//!< name to look up
	MetaFattr result; //!< result of lookup
	MetaLookup(seq_t s, fid_t d, string n):
		MetaRequest(META_LOOKUP, s, false), dir(d), name(n) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "lookup: name = " << name;
		os << " (parent fid = " << dir << ")";
		return os.str();
	}
};

/*!
 * \brief look up a complete path
 */
struct MetaLookupPath: public MetaRequest {
	fid_t root;	//!< fid of starting directory
	string path;	//!< path to look up
	MetaFattr result; //!< result of lookup;
	MetaLookupPath(seq_t s, fid_t r, string p):
		MetaRequest(META_LOOKUP_PATH, s, false), root(r), path(p) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "lookup_path: path = " << path;
		os << " (root fid = " << root << ")";
		return os.str();
	}
};

/*!
 * \brief create a file
 */
struct MetaCreate: public MetaRequest {
	fid_t dir;	//!< parent directory fid
	string name;	//!< name to create
	fid_t fid;	//!< file ID of new file
	int16_t numReplicas; //!< desired degree of replication
	bool exclusive;  //!< model the O_EXCL flag
	MetaCreate(seq_t s, fid_t d, string n, int16_t r, bool e):
		MetaRequest(META_CREATE, s, true), dir(d),
		name(n), numReplicas(r), exclusive(e) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "create: name = " << name;
		os << " (parent fid = " << dir << ")";
		os << " desired replication = " << numReplicas;
		return os.str();
	}
};

/*!
 * \brief create a directory
 */
struct MetaMkdir: public MetaRequest {
	fid_t dir;	//!< parent directory fid
	string name;	//!< name to create
	fid_t fid;	//!< file ID of new directory
	MetaMkdir(seq_t s, fid_t d, string n):
		MetaRequest(META_MKDIR, s, true), dir(d), name(n) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "mkdir: name = " << name;
		os << " (parent fid = " << dir << ")";
		return os.str();
	}
};

/*!
 * \brief remove a file
 */
struct MetaRemove: public MetaRequest {
	fid_t dir;	//!< parent directory fid
	string name;	//!< name to remove
	string pathname; //!< full pathname to remove
	off_t filesize;	//!< size of file that was freed (debugging info)
	MetaRemove(seq_t s, fid_t d, string n):
		MetaRequest(META_REMOVE, s, true), dir(d), name(n), filesize(0) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "remove: path = " << pathname << " (name = " << name << ")";
		os << " (parent fid = " << dir << ") : filesize: " << filesize;
		return os.str();
	}
};

/*!
 * \brief remove a directory
 */
struct MetaRmdir: public MetaRequest {
	fid_t dir;	//!< parent directory fid
	string name;	//!< name to remove
	string pathname; //!< full pathname to remove
	MetaRmdir(seq_t s, fid_t d, string n):
		MetaRequest(META_RMDIR, s, true), dir(d), name(n) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "rmdir: path = " << pathname << " (name = " << name << ")";
		os << " (parent fid = " << dir << ")";
		return os.str();
	}
};

/*!
 * \brief read directory contents
 */
struct MetaReaddir: public MetaRequest {
	fid_t dir;	//!< directory to read
	vector <MetaDentry> v; //!< vector of results
	MetaReaddir(seq_t s, fid_t d):
		MetaRequest(META_READDIR, s, false), dir(d) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "readdir: dir fid = " << dir;
		return os.str();
	}
};

/*!
 * \brief read directory contents and get file attributes
 */
struct MetaReaddirPlus: public MetaRequest {
	fid_t dir;	//!< directory to read
	ostringstream v; //!< results built out into a string
	int numEntries; //!< # of entries in the directory
	MetaReaddirPlus(seq_t s, fid_t d):
		MetaRequest(META_READDIRPLUS, s, false), dir(d) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "readdir: dir fid = " << dir;
		return os.str();
	}
};

/*!
 * \brief get allocation info. a chunk for a file
 */
struct MetaGetalloc: public MetaRequest {
	fid_t fid;	//!< file for alloc info is needed
	chunkOff_t offset; //!< offset of chunk within file
	chunkId_t chunkId; //!< Id of the chunk corresponding to offset
	seq_t chunkVersion; //!< version # assigned to this chunk
	vector<ServerLocation> locations; //!< where the copies of the chunks are
	std::string pathname; //!< pathname of the file (useful to print in debug msgs)
	MetaGetalloc(seq_t s, fid_t f, chunkOff_t o):
		MetaRequest(META_GETALLOC, s, false), fid(f), offset(o) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "getalloc: " << pathname << " (fid = " << fid << ")";
		os << " offset = " << offset;
		return os.str();
	}
};

/*!
 * \brief layout information for a chunk
 */
struct ChunkLayoutInfo {
	chunkOff_t offset; //!< offset of chunk within file
	chunkId_t chunkId; //!< Id of the chunk corresponding to offset
	seq_t chunkVersion; //!< version # assigned to this chunk
	vector<ServerLocation> locations; //!< where the copies of the chunks are
	string toString()
	{
		ostringstream os;

		os << offset << " " << chunkId << " " ;
		os << chunkVersion << " " << locations.size() << " ";
		for (vector<ServerLocation>::size_type i = 0;
			i < locations.size(); ++i) {
			os << locations[i].hostname << " " << locations[i].port << " ";
		}
		return os.str();
	}
};

/*!
 * \brief get allocation info. for all chunks of a file
 */
struct MetaGetlayout: public MetaRequest {
	fid_t fid;	//!< file for layout info is needed
	vector <ChunkLayoutInfo> v; //!< vector of results
	MetaGetlayout(seq_t s, fid_t f):
		MetaRequest(META_GETLAYOUT, s, false), fid(f) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "getlayout: fid = " << fid;
		return os.str();
	}
};

class ChunkServer;
typedef boost::shared_ptr<ChunkServer> ChunkServerPtr;

/*!
 * \brief allocate a chunk for a file
 */
struct MetaAllocate: public MetaRequest {
	MetaRequest *req; //!< req that triggered allocation (e.g., truncate)
	fid_t fid;		//!< file for which space has to be allocated
	chunkOff_t offset;	//!< offset of chunk within file
	chunkId_t chunkId;	//!< Id of the chunk that was allocated
	seq_t chunkVersion;	//!< version # assigned to this chunk
	std::string pathname;   //!< full pathname that corresponds to fid
	std::string clientHost; //!< the host from which request was received
	int16_t  numReplicas;	//!< inherited from file's fattr
	bool layoutDone;	//!< Has layout of chunk been done
	//!< Server(s) on which this chunk has been placed
	vector <ChunkServerPtr> servers;
	//!< For replication, the master that runs the transaction
	//!< for completing the write.
	ChunkServerPtr master;
	uint32_t numServerReplies;
	MetaAllocate(seq_t s, fid_t f, chunkOff_t o):
		MetaRequest(META_ALLOCATE, s, true), req(NULL), fid(f),
		offset(o), layoutDone(false), numServerReplies(0) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "allocate: path = " << pathname << " fid = " << fid;
		os << " offset = " << offset;
		return os.str();
	}
};

/*!
 * \brief truncate a file
 */
struct MetaTruncate: public MetaRequest {
	fid_t fid;	//!< file for which space has to be allocated
	chunkOff_t offset; //!< offset to truncate the file to
	string pathname; //!< full pathname for file being truncated
	MetaTruncate(seq_t s, fid_t f, chunkOff_t o):
		MetaRequest(META_TRUNCATE, s, true), fid(f), offset(o) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "truncate: path = " << pathname << " fid = " << fid;
		os << " offset = " << offset;
		return os.str();
	}
};

/*!
 * \brief rename a file or directory
 */
struct MetaRename: public MetaRequest {
	fid_t dir;	//!< parent directory
	string oldname;	//!< old file name
	string newname;	//!< new file name
	string oldpath; //!< fully-qualified old pathname
	bool overwrite; //!< overwrite newname if it exists
	MetaRename(seq_t s, fid_t d, const char *o, const char *n, bool c):
		MetaRequest(META_RENAME, s, true), dir(d),
			oldname(o), newname(n), overwrite(c) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "rename: oldname = " << oldpath << "(name = " << oldname << ")";
		os << " (fid = " << dir << ")";
		os << " newname = " << newname;
		return os.str();
	}
};

/*!
 * \brief change a file's replication factor
 */
struct MetaChangeFileReplication: public MetaRequest {
	fid_t fid;	//!< fid whose replication has to be changed
	int16_t numReplicas; //!< desired degree of replication
	MetaChangeFileReplication(seq_t s, fid_t f, int16_t n):
		MetaRequest(META_CHANGE_FILE_REPLICATION, s, true), fid(f), numReplicas(n) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "change-file-replication: fid = " << fid;
		os << "new # of replicas: " << numReplicas << ' ';
		return os.str();
	}
};

/*!
 * \brief Notification to hibernate/retire a chunkserver:
 * Hibernation: when the server is put
 * in hibernation mode, the server is taken down temporarily with a promise that
 * it will come back N secs later; if the server doesnt' come up as promised
 * then re-replication starts.
 *
 * Retirement: is extended downtime.  The server is taken down and we don't know
 * if it will ever come back.  In this case, we use this server (preferably)
 * to evacuate/re-replicate all the blocks off it before we take it down.
 */

struct MetaRetireChunkserver : public MetaRequest {
	ServerLocation location; //<! Location of this server
	int nSecsDown; //<! set to -1, we retire; otherwise, # of secs of down time
	MetaRetireChunkserver(seq_t s, const ServerLocation &l, int d) :
		MetaRequest(META_RETIRE_CHUNKSERVER, s, false), location(l),
		nSecsDown(d) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		if (nSecsDown > 0)
			return "Hibernating server: " + location.ToString();
		else
			return "Retiring server: " + location.ToString();
	}
};

/*!
 * \brief Notification to toggle rebalancing state.
 */

struct MetaToggleRebalancing : public MetaRequest {
	bool value; // !< Enable/disable rebalancing
	MetaToggleRebalancing(seq_t s, bool v) :
		MetaRequest(META_TOGGLE_REBALANCING, s, false), value(v) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		if (value)
			return "Toggle rebalancing: Enable";
		else
			return "Toggle rebalancing: Disable";
	}
};

/*!
 * \brief Execute a rebalance plan that was constructed offline.
*/

struct MetaExecuteRebalancePlan : public MetaRequest {
	std::string planPathname; //<! full path to the file with the plan
	MetaExecuteRebalancePlan(seq_t s, const std::string &p) :
		MetaRequest(META_EXECUTE_REBALANCEPLAN, s, false), planPathname(p) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		return "Execute rebalance plan : " + planPathname;
	}
};


/*!
 * \brief close a log file and start a new one
 */
struct MetaLogRollover: public MetaRequest {
	MetaLogRollover():
		MetaRequest(META_LOG_ROLLOVER, 0, true) { }
	int log(ofstream &file) const;
	string Show()
	{
		return "log rollover";
	}
};

/*!
 * \brief change the number which is used to increment
 * chunk version numbers.  This is an internally
 * generated op whenever (1) the system restarts after a failure
 * or (2) a write-allocation fails because a replica went down.
*/
struct MetaChangeChunkVersionInc : public MetaRequest {
	seq_t cvi;
	//!< The request that depends on the chunk-version-inc # being
	//!< logged to disk.  Once the logging is done, the request
	//!< processing can resume.
	MetaRequest *req;
	MetaChangeChunkVersionInc(seq_t n, MetaRequest *r):
		MetaRequest(META_CHANGE_CHUNKVERSIONINC, 0, true),
		cvi(n), req(r) { }
	int log(ofstream &file) const;
	string Show()
	{
		ostringstream os;

		os << "changing chunk version inc: value = " << cvi;
		return os.str();
	}
};

struct ChunkInfo {
	fid_t fileId;
	chunkId_t chunkId;
	seq_t chunkVersion;
};

/*!
 * \brief hello RPC from a chunk server on startup
 */
struct MetaHello: public MetaRequest {
	ChunkServerPtr server; //!< The chunkserver that sent the hello message
	ServerLocation location; //<! Location of this server
	uint64_t totalSpace; //!< How much storage space does the
			//!< server have (bytes)
	uint64_t usedSpace; //!< How much storage space is used up (in bytes)
	int rackId; //!< the rack on which the server is located
	int numChunks; //!< # of chunks hosted on this server
	int contentLength; //!< Length of the message body
	vector<ChunkInfo> chunks; //!< Chunks  hosted on this server
	MetaHello(seq_t s): MetaRequest(META_HELLO, s, false) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		return "Chunkserver Hello";
	}
};

/*!
 * \brief whenever a chunk server goes down, this message is used to clean up state.
 */
struct MetaBye: public MetaRequest {
	ChunkServerPtr server; //!< The chunkserver that went down
	MetaBye(seq_t s, ChunkServerPtr c):
		MetaRequest(META_BYE, s, false), server(c) { }
	int log(ofstream &file) const;
	string Show()
	{
		return "Chunkserver Bye";
	}
};

/*!
 * \brief RPCs that go from meta server->chunk server are
 * MetaRequest's that define a method to generate the RPC
 * request.
 */
struct MetaChunkRequest: public MetaRequest {
	MetaRequest *req; //!< The request that triggered this RPC
	ChunkServer *server; //!< The chunkserver to send this RPC to

	MetaChunkRequest(MetaOp o, seq_t s, bool mu,
			 MetaRequest *r, ChunkServer *c):
		MetaRequest(o, s, mu), req(r), server(c) { }

	//!< generate a request message (in string format) as per the
	//!< KFS protocol.
	virtual void request(ostringstream &os) = 0;
};

/*!
 * \brief Allocate RPC from meta server to chunk server
 */
struct MetaChunkAllocate: public MetaChunkRequest {
	int64_t leaseId;
	MetaChunkAllocate(seq_t n, MetaAllocate *r, ChunkServer *s, int64_t l):
		MetaChunkRequest(META_CHUNK_ALLOCATE, n, false, r, s),
		leaseId(l) { }
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	int log(ofstream &file) const;
	string Show()
	{
		return  "meta->chunk allocate: ";
	}
};

/*!
 * \brief Chunk version # change RPC from meta server to chunk server
 */
struct MetaChunkVersChange: public MetaChunkRequest {
	fid_t fid;
	chunkId_t chunkId; //!< The chunk id to free
	seq_t chunkVersion;	//!< version # assigned to this chunk
	MetaChunkVersChange(seq_t n, ChunkServer *s, fid_t f, chunkId_t c, seq_t v):
		MetaChunkRequest(META_CHUNK_VERSCHANGE, n, false, NULL, s),
		fid(f), chunkId(c), chunkVersion(v) { }
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	int log(ofstream &file) const;
	string Show()
	{
		ostringstream os;

		os <<  "meta->chunk vers change: ";
		os << " fid = " << fid;
		os << " chunkId = " << chunkId;
		os << " chunkVersion = " << chunkVersion;
		return os.str();
	}
};

/*!
 * \brief Delete RPC from meta server to chunk server
 */
struct MetaChunkDelete: public MetaChunkRequest {
	chunkId_t chunkId; //!< The chunk id to free
	MetaChunkDelete(seq_t n, ChunkServer *s, chunkId_t c):
		MetaChunkRequest(META_CHUNK_DELETE, n, false, NULL, s), chunkId(c) { }
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	int log(ofstream &file) const;
	string Show()
	{
		ostringstream os;

		os <<  "meta->chunk delete: ";
		os << " chunkId = " << chunkId;
		return os.str();
	}
};

/*!
 * \brief Truncate chunk RPC from meta server to chunk server
 */
struct MetaChunkTruncate: public MetaChunkRequest {
	chunkId_t chunkId; //!< The id of chunk to be truncated
	size_t chunkSize; //!< The size to which chunk should be truncated
	MetaChunkTruncate(seq_t n, ChunkServer *s, chunkId_t c, size_t sz):
		MetaChunkRequest(META_CHUNK_TRUNCATE, n, false, NULL, s),
		chunkId(c), chunkSize(sz) { }
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	int log(ofstream &file) const;
	string Show()
	{
		ostringstream os;

		os <<  "meta->chunk truncate: ";
		os << " chunkId = " << chunkId;
		os << " chunkSize = " << chunkSize;
		return os.str();
	}
};

/*!
 * \brief Replicate RPC from meta server to chunk server.  This
 * message is sent to a "destination" chunk server---that is, a chunk
 * server is told to create a copy of chunk from some source that is
 * already hosting the chunk.  This model allows the destination to
 * replicate the chunk at its convenieance.
 */
struct MetaChunkReplicate: public MetaChunkRequest {
	fid_t fid;  //!< input: we tell the chunkserver what it is
	chunkId_t chunkId; //!< The chunk id to replicate
	seq_t chunkVersion; //!< output: the chunkservers tells us what it did
	ServerLocation srcLocation; //!< where to get a copy from
	ChunkServerPtr server;  //!< "dest" on which we put a copy
	MetaChunkReplicate(seq_t n, ChunkServer *s,
			fid_t f, chunkId_t c, seq_t v,
			const ServerLocation &l):
		MetaChunkRequest(META_CHUNK_REPLICATE, n, false, NULL, s),
		fid(f), chunkId(c), chunkVersion(v), srcLocation(l) { }
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	int log(ofstream &file) const;
	string Show()
	{
		ostringstream os;

		os <<  "meta->chunk replicate: ";
		os << " fileId = " << fid;
		os << " chunkId = " << chunkId;
		os << " chunkVersion = " << chunkVersion;
		return os.str();
	}
};

/*!
 * \brief As a chunkserver for the size of a particular chunk.  We use this RPC
 * to compute the filesize: whenever the lease on the last chunk of the file
 * expires, we get the chunk's size and then determine the filesize.
 */
struct MetaChunkSize: public MetaChunkRequest {
	fid_t fid;  //!< input: we use the tuple <fileid, chunkid> to
			//!< find the entry we need.
	chunkId_t chunkId; //!< input: the chunk whose size we need
	off_t chunkSize; //!< output: the chunk size
	off_t filesize; //!< for logging purposes: the size of the file
	/// input: given the pathname, we can update space usage for the path
	/// hierarchy corresponding to pathname; this will enable us to make "du"
	/// instantaneous.
	std::string pathname; 
	MetaChunkSize(seq_t n, ChunkServer *s, fid_t f, chunkId_t c, 
			const std::string &p) :
		MetaChunkRequest(META_CHUNK_SIZE, n, true, NULL, s),
		fid(f), chunkId(c), chunkSize(-1), filesize(-1), pathname(p) { }
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	int log(ofstream &file) const;
	string Show()
	{
		ostringstream os;

		os <<  "meta->chunk size: " << pathname;
		os << " fileId = " << fid;
		os << " chunkId = " << chunkId;
		return os.str();
	}
};

/*!
 * \brief Heartbeat RPC from meta server to chunk server.  We can
 * ask the chunk server for lots of stuff; for now, we ask it
 * how much is available/used up.
 */
struct MetaChunkHeartbeat: public MetaChunkRequest {
	MetaChunkHeartbeat(seq_t n, ChunkServer *s):
		MetaChunkRequest(META_CHUNK_HEARTBEAT, n, false, NULL, s) { }
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	int log(ofstream &file) const;
	string Show()
	{
		return "meta->chunk heartbeat";
	}
};

/*!
 * \brief Stale chunk notification message from meta->chunk.  This
 * tells the chunk servers the id's of stale chunks, which the chunk
 * server should get rid of.
 */
struct MetaChunkStaleNotify: public MetaChunkRequest {
	MetaChunkStaleNotify(seq_t n, ChunkServer *s):
		MetaChunkRequest(META_CHUNK_STALENOTIFY, n, false, NULL, s) { }
	vector<chunkId_t> staleChunkIds; //!< chunk ids that are stale
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	int log(ofstream &file) const;
	string Show()
	{
		return "meta->chunk stale notify";
	}
};

/*!
 * For scheduled downtime, we evacaute all the chunks on a server; when
 * we know that the evacuation is finished, we tell the chunkserver to retire.
 */
struct MetaChunkRetire: public MetaChunkRequest {
	MetaChunkRetire(seq_t n, ChunkServer *s):
		MetaChunkRequest(META_CHUNK_RETIRE, n, false, NULL, s) { }
	int log(ofstream &file) const;
	//!< generate the request string that should be sent out
	void request(ostringstream &os);
	string Show()
	{
		return "chunkserver retire";
	}
};

/*!
 * \brief For monitoring purposes, a client/tool can send a PING
 * request.  In response, the server replies with the list of all
 * connected chunk servers and their locations as well as some state
 * about each of those servers.
 */
struct MetaPing: public MetaRequest {
	string systemInfo; //!< result that describes system info (space etc)
	string servers; //!< result that contains info about chunk servers
	string retiringServers; //!< info about servers that are being retired
	string downServers; //!< info about servers that have gone down
	MetaPing(seq_t s):
		MetaRequest(META_PING, s, false) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		return "ping";
	}
};

/*!
 * \brief For monitoring purposes, a client/tool can request metaserver
 * to provide a list of live chunkservers.
 */
struct MetaUpServers: public MetaRequest {
	ostringstream stringStream;
	MetaUpServers(seq_t s):
		MetaRequest(META_UPSERVERS, s, false) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		return "upservers";
	}
};

/*!
 * \brief To toggle WORM mode of metaserver a client/tool can send a
 * TOGGLE_WORM request. In response, the server changes its WORM state.
 */
struct MetaToggleWORM: public MetaRequest {
	bool value; // !< Enable/disable WORM
	MetaToggleWORM(seq_t s, bool v):
		MetaRequest(META_TOGGLE_WORM, s, false), value(v) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		if (value)
			return "Toggle WORM: Enabled";
		else
			return "Toggle WORM: Disabled";
	}
};
/*!
 * \brief For monitoring purposes, a client/tool can send a STATS
 * request.  In response, the server replies with the list of all
 * counters it keeps.
 */
struct MetaStats: public MetaRequest {
	string stats; //!< result
	MetaStats(seq_t s):
		MetaRequest(META_STATS, s, false) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		return "stats";
	}
};

/*!
 * \brief For debugging purposes, dump out the chunk->location map
 * to a file.
 */
struct MetaDumpChunkToServerMap: public MetaRequest {
	MetaDumpChunkToServerMap(seq_t s):
		MetaRequest(META_DUMP_CHUNKTOSERVERMAP, s, false) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		return "dump chunk2server map";
	}
};

/*!
 * \brief For debugging purposes, dump out the set of blocks that are currently
 * being re-replicated.
 */
struct MetaDumpChunkReplicationCandidates: public MetaRequest {
	MetaDumpChunkReplicationCandidates(seq_t s):
		MetaRequest(META_DUMP_CHUNKREPLICATIONCANDIDATES, s, false) { }
	// list of blocks that are being re-replicated
	std::string blocks;
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		return "dump chunk replication candidates";
	}
};



/*!
 * \brief For monitoring purposes, a client/tool can send a OPEN FILES
 * request.  In response, the server replies with the list of all
 * open files---files for which there is a valid lease
 */
struct MetaOpenFiles: public MetaRequest {
	string openForRead; //!< result
	string openForWrite; //!< result
	MetaOpenFiles(seq_t s):
		MetaRequest(META_OPEN_FILES, s, false) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		return "open files";
	}
};

/*!
 * \brief Op for handling a notify of a corrupt chunk
 */
struct MetaChunkCorrupt: public MetaRequest {
	fid_t fid; //!< input
	chunkId_t chunkId; //!< input
	ChunkServerPtr server; //!< The chunkserver that sent us this message
	MetaChunkCorrupt(seq_t s, fid_t f, chunkId_t c):
		MetaRequest(META_CHUNK_CORRUPT, s, false),
		fid(f), chunkId(c) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "corrupt chunk: fid = " << fid << " chunkid = " << chunkId;
		return os.str();
	}
};

/*!
 * \brief Op for acquiring a lease on a chunk of a file.
 */
struct MetaLeaseAcquire: public MetaRequest {
	LeaseType leaseType; //!< input
	std::string pathname;   //!< full pathname of the file that owns chunk
	chunkId_t chunkId; //!< input
	int64_t leaseId; //!< result
	MetaLeaseAcquire(seq_t s, chunkId_t c):
		MetaRequest(META_LEASE_ACQUIRE, s, false),
		leaseType(READ_LEASE), chunkId(c), leaseId(-1) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "lease acquire: " << pathname << " ";
		if (leaseType == READ_LEASE)
			os << "read lease ";
		else
			os << "write lease ";

		os << " chunkId = " << chunkId;
		return os.str();
	}
};

/*!
 * \brief Op for renewing a lease on a chunk of a file.
 */
struct MetaLeaseRenew: public MetaRequest {
	LeaseType leaseType; //!< input
	std::string pathname;   //!< full pathname of the file that owns chunk
	chunkId_t chunkId; //!< input
	int64_t leaseId; //!< input
	MetaLeaseRenew(seq_t s, LeaseType t, chunkId_t c, int64_t l):
		MetaRequest(META_LEASE_RENEW, s, false),
		leaseType(t), chunkId(c), leaseId(l) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "lease renew: " << pathname << " ";
		if (leaseType == READ_LEASE)
			os << "read lease ";
		else
			os << "write lease ";

		os << " chunkId = " << chunkId;
		return os.str();
	}
};

/*!
 * \brief Op for relinquishing a lease on a chunk of a file.
 */
struct MetaLeaseRelinquish: public MetaRequest {
	LeaseType leaseType; //!< input
	chunkId_t chunkId; //!< input
	int64_t leaseId; //!< input
	MetaLeaseRelinquish(seq_t s, LeaseType t, chunkId_t c, int64_t l):
		MetaRequest(META_LEASE_RELINQUISH, s, false),
		leaseType(t), chunkId(c), leaseId(l) { }
	int log(ofstream &file) const;
	void response(ostringstream &os);
	string Show()
	{
		ostringstream os;

		os << "lease relinquish: ";
		if (leaseType == READ_LEASE)
			os << "read lease ";
		else
			os << "write lease ";

		os << " chunkId = " << chunkId << " leaseId = " << leaseId;
		return os.str();
	}
};

/*!
 * \brief An internally generated op to force the cleanup of
 * dead leases thru the main event processing loop.
 */
struct MetaLeaseCleanup: public MetaRequest {
	MetaLeaseCleanup(seq_t s, KfsCallbackObj *c):
		MetaRequest(META_LEASE_CLEANUP, s, false) { clnt = c; }

	int log(ofstream &file) const;
	string Show()
	{
		return "lease cleanup";
	}
};

/*!
 * \brief An internally generated op to check that the degree
 * of replication for each chunk is satisfactory.  This op goes
 * thru the main event processing loop.
 */
struct MetaChunkReplicationCheck : public MetaRequest {
	MetaChunkReplicationCheck(seq_t s, KfsCallbackObj *c):
		MetaRequest(META_CHUNK_REPLICATION_CHECK, s, false) { clnt = c; }

	int log(ofstream &file) const;
	string Show()
	{
		return "chunk replication check";
	}
};

extern int ParseCommand(char *cmdBuf, int cmdLen, MetaRequest **res);

extern void initialize_request_handlers();
extern void process_request();
extern void submit_request(MetaRequest *r);
extern void printleaves();

extern void ChangeIncarnationNumber(MetaRequest *r);
extern void RegisterCounters();
extern void setClusterKey(const char *key);
extern void setMD5SumFn(const char *md5sumFn);
extern void setWORMMode(bool value);
extern void setMaxReplicasPerFile(int16_t value);

/* update counters for # of files/dirs/chunks in the system */
extern void UpdateNumDirs(int count);
extern void UpdateNumFiles(int count);
extern void UpdateNumChunks(int count);

}
#endif /* !defined(KFS_REQUEST_H) */

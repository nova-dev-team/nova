//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: KfsOps.h 278 2009-02-23 08:28:33Z mikeov $
//
// Created 2006/05/26
// Author: Sriram Rao
//
// Copyright 2008 Quantcast Corp.
// Copyright 2006-2008 Kosmix Corp.
//
// This file is part of Kosmos File System (KFS).
//
// Licensed under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License. You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
// implied. See the License for the specific language governing
// permissions and limitations under the License.
//
// Declarations for the various Chunkserver ops.
//
// 
//----------------------------------------------------------------------------

#ifndef _CHUNKSERVER_KFSOPS_H
#define _CHUNKSERVER_KFSOPS_H

#include <string>
#include <fstream>
#include <sstream>
#include <vector>
#include <sys/time.h>

namespace KFS
{
// forward declaration to get compiler happy
struct KfsOp;
}

#include "libkfsIO/KfsCallbackObj.h"
#include "libkfsIO/IOBuffer.h"
#include "libkfsIO/DiskConnection.h"

#include "common/properties.h"
#include "common/kfsdecls.h"
#include "Chunk.h"

namespace KFS
{

enum KfsOp_t {
    CMD_UNKNOWN,
    // Meta server->Chunk server ops
    CMD_ALLOC_CHUNK,
    CMD_DELETE_CHUNK,
    CMD_TRUNCATE_CHUNK,
    CMD_REPLICATE_CHUNK,
    CMD_CHANGE_CHUNK_VERS,
    CMD_HEARTBEAT,
    CMD_STALE_CHUNKS,
    CMD_RETIRE,
    // Chunk server->Meta server ops
    CMD_META_HELLO,
    CMD_CORRUPT_CHUNK,
    CMD_LEASE_RENEW,
    CMD_LEASE_RELINQUISH,

    // Client -> Chunkserver ops
    CMD_SYNC,
    CMD_OPEN,
    CMD_CLOSE,
    CMD_READ,
    CMD_WRITE_ID_ALLOC,
    CMD_WRITE_PREPARE,
    CMD_WRITE_PREPARE_FWD,
    CMD_WRITE_SYNC,
    CMD_SIZE,
    // when data is loaded KFS, we need a way to verify that what was
    // copied in matches the source.  analogous to md5 model, client
    // can issue this RPC and get the checksums stored for a chunk;
    // the client can comptue checksum on input data and verify that
    // they both match
    CMD_GET_CHUNK_METADATA,
    // Monitoring ops
    CMD_PING,
    CMD_STATS,
    CMD_DUMP_CHUNKMAP,
    // Internally generated ops
    CMD_CHECKPOINT,
    CMD_WRITE,
    CMD_WRITE_CHUNKMETA, // write out the chunk meta-data
    CMD_READ_CHUNKMETA, // read out the chunk meta-data
    // op sent by the network thread to event thread to kill a
    // "RemoteSyncSM".
    CMD_KILL_REMOTE_SYNC,
    // this op is to periodically "kick" the event processor thread
    CMD_TIMEOUT,
    // op to signal the disk manager that some disk I/O has finished
    CMD_DISKIO_COMPLETION,
    CMD_NCMDS
};

enum OpType_t {
    OP_REQUEST,
    OP_RESPONSE
};

struct KfsOp : public KfsCallbackObj {
    KfsOp_t op;
    OpType_t type;
    kfsSeq_t   seq;
    int32_t   status;
    bool      cancelled;
    bool      done;
    KfsCallbackObj  *clnt;
    // keep statistics
    struct timeval startTime;
    KfsOp (KfsOp_t o, kfsSeq_t s, KfsCallbackObj *c = NULL) :
        op(o), seq(s), status(0), cancelled(false), done(false),
        clnt(c)
    {
        SET_HANDLER(this, &KfsOp::HandleDone);
        gettimeofday(&startTime, NULL);
    }
    void Cancel() {
        cancelled = true;
    }
    // to allow dynamic-type-casting, make the destructor virtual
    virtual ~KfsOp();
    virtual void Request(std::ostringstream &os) {
        // fill this method if the op requires a message to be sent to a server.
        (void) os;
    };
    // After an op finishes execution, this method generates the
    // response that should be sent back to the client.  The response
    // string that is generated is based on the KFS protocol.
    virtual void Response(std::ostringstream &os);
    virtual void Execute() = 0;
    virtual void Log(std::ofstream &ofs) { };
    // Return info. about op for debugging
    virtual std::string Show() const = 0;
    // If the execution of an op suspends and then resumes and
    // finishes, this method should be invoked to signify completion.
    virtual int HandleDone(int code, void *data);
};

//
// Model used in all the c'tor's of the ops: we do minimal
// initialization and primarily init the fields that are used for
// output.  The fields that are "input" are set when they are parsed
// from the input stream.
//
struct AllocChunkOp : public KfsOp {
    kfsFileId_t fileId; // input
    kfsChunkId_t chunkId; // input
    int64_t chunkVersion; // input
    int64_t leaseId; // input
    AllocChunkOp(kfsSeq_t s) :
        KfsOp(CMD_ALLOC_CHUNK, s), leaseId(-1)
    {
        // All inputs will be parsed in
    }
    void Response(std::ostringstream &os);
    void Execute();
    void Log(std::ofstream &ofs);
    // handlers for reading/writing out the chunk meta-data
    int HandleChunkMetaReadDone(int code, void *data);
    int HandleChunkMetaWriteDone(int code, void *data);
    std::string Show() const {
        std::ostringstream os;

        os << "alloc-chunk: fileid = " << fileId << " chunkid = " << chunkId;
        os << " chunkvers = " << chunkVersion << " leaseid = " << leaseId;
        return os.str();
    }
};

struct ChangeChunkVersOp : public KfsOp {
    kfsFileId_t fileId; // input
    kfsChunkId_t chunkId; // input
    int64_t chunkVersion; // input
    ChangeChunkVersOp(kfsSeq_t s) :
        KfsOp(CMD_CHANGE_CHUNK_VERS, s)
    {

    }
    void Execute();
    void Log(std::ofstream &ofs);
    // handler for reading in the chunk meta-data
    int HandleChunkMetaReadDone(int code, void *data);
    // handler for writing out the chunk meta-data
    int HandleChunkMetaWriteDone(int code, void *data);
    std::string Show() const {
        std::ostringstream os;

        os << "change-chunk-vers: fileid = " << fileId << " chunkid = " << chunkId;
        os << " chunkvers = " << chunkVersion;
        return os.str();
    }
};

struct DeleteChunkOp : public KfsOp {
    kfsChunkId_t chunkId; // input
    DeleteChunkOp(kfsSeq_t s) :
        KfsOp(CMD_DELETE_CHUNK, s)
    {

    }
    void Execute();
    void Log(std::ofstream &ofs);
    std::string Show() const {
        std::ostringstream os;

        os << "delete-chunk: " << " chunkid = " << chunkId;
        return os.str();
    }
};

struct TruncateChunkOp : public KfsOp {
    kfsChunkId_t chunkId;  // input
    size_t	 chunkSize; // size to which file should be truncated to
    TruncateChunkOp(kfsSeq_t s) :
        KfsOp(CMD_TRUNCATE_CHUNK, s)
    {

    }
    void Execute();
    void Log(std::ofstream &ofs);
    // handler for reading in the chunk meta-data
    int HandleChunkMetaReadDone(int code, void *data);
    // handler for writing out the chunk meta-data
    int HandleChunkMetaWriteDone(int code, void *data);
    std::string Show() const {
        std::ostringstream os;

        os << "truncate-chunk: " << " chunkid = " << chunkId;
        os << " chunksize: " << chunkSize;
        return os.str();
    }
};

class Replicator;
typedef boost::shared_ptr<Replicator> ReplicatorPtr;

// Op for replicating the chunk.  The metaserver is asking this
// chunkserver to create a copy of a chunk.  We replicate the chunk
// and then notify the server upon completion.
//
struct ReplicateChunkOp : public KfsOp {
    kfsChunkId_t chunkId;  // input
    ServerLocation location; // input: where to get the chunk from
    kfsFileId_t fid; // output: we tell the metaserver what we replicated
    int64_t chunkVersion; // output: we tell the metaserver what we replicated
    ReplicatorPtr replicator;
    ReplicateChunkOp(kfsSeq_t s) :
        KfsOp(CMD_REPLICATE_CHUNK, s) { }
    void Execute();
    void Response(std::ostringstream &os);
    int HandleDone(int code, void *data);
    void Log(std::ofstream &ofs);
    std::string Show() const {
        std::ostringstream os;

        os << "replicate-chunk: " << " chunkid = " << chunkId;
        return os.str();
    }
};

struct HeartbeatOp : public KfsOp {
    int64_t totalSpace;
    int64_t usedSpace;
    long numChunks;
    HeartbeatOp(kfsSeq_t s) :
        KfsOp(CMD_HEARTBEAT, s)
    {
        // the fields will be filled in when we execute
    }
    void Execute();
    void Response(std::ostringstream &os);
    std::string Show() const {
        return "meta-server heartbeat";
    }
};

struct StaleChunksOp : public KfsOp {
    int contentLength; /* length of data that identifies the stale chunks */
    int numStaleChunks; /* what the server tells us */
    std::vector<kfsChunkId_t> staleChunkIds; /* data we parse out */
    StaleChunksOp(kfsSeq_t s) :
        KfsOp(CMD_STALE_CHUNKS, s)
    { 
    
    }
    void Execute();
    void Response(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;
        
        os << "stale chunks: " << " # stale: " << numStaleChunks;
        return os.str();
    }
};

struct RetireOp : public KfsOp {
    RetireOp(kfsSeq_t s) : KfsOp(CMD_RETIRE, s) { }
    void Execute();
    std::string Show() const {
        return "meta-server is telling us to retire";
    }
};

struct OpenOp : public KfsOp {
    kfsChunkId_t chunkId;  // input
    int openFlags;  // either O_RDONLY, O_WRONLY
    OpenOp(kfsSeq_t s) :
        KfsOp(CMD_OPEN, s)
    {

    }
    void Execute();
    std::string Show() const {
        std::ostringstream os;
        
        os << "open: chunkId = " << chunkId;
        return os.str();
    }
};

struct CloseOp : public KfsOp {
    kfsChunkId_t chunkId; // input
    CloseOp(kfsSeq_t s) :
        KfsOp(CMD_CLOSE, s)
    {

    }
    void Execute();
    std::string Show() const {
        std::ostringstream os;
        
        os << "close: chunkId = " << chunkId;
        return os.str();
    }
};

struct ReadOp;
struct WriteOp;
struct WriteSyncOp;
struct WritePrepareFwdOp;

struct WriteIdAllocOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t	 chunkVersion;
    off_t 	 offset;   /* input */
    size_t 	 numBytes; /* input */
    int64_t      writeId; /* output */
    std::string  writeIdStr; /* output */
    uint32_t     numServers; /* input */
    std::string  servers; /* input: set of servers on which to write */
    WriteIdAllocOp *fwdedOp; /* if we did any fwd'ing, this is the op that tracks it */
    WriteIdAllocOp(kfsSeq_t s) :
        KfsOp(CMD_WRITE_ID_ALLOC, s), writeId(-1), fwdedOp(NULL)
    {
        SET_HANDLER(this, &WriteIdAllocOp::HandleDone);
    }
    
    WriteIdAllocOp(kfsSeq_t s, const WriteIdAllocOp *other) :
        KfsOp(CMD_WRITE_ID_ALLOC, s), chunkId(other->chunkId),
        chunkVersion(other->chunkVersion), offset(other->offset),
        numBytes(other->numBytes), writeId(-1), numServers(other->numServers),
        servers(other->servers), fwdedOp(NULL) 
    {
        SET_HANDLER(this, &WriteIdAllocOp::HandleDone);
    }

    ~WriteIdAllocOp();

    void Request(std::ostringstream &os);
    void Response(std::ostringstream &os);
    void Execute();
    // should the chunk metadata get paged out, then we use the
    // write-id alloc op as a hint to page the data back in---writes
    // are coming.
    void ReadChunkMetadata();

    int ForwardToPeer(const ServerLocation &peer);
    int HandlePeerReply(int code, void *data);
    int HandleDone(int code, void *data);

    std::string Show() const {
        std::ostringstream os;
        
        os << "write-id-alloc: seq = " << seq << " chunkId = " << chunkId 
           << " chunkversion = " << chunkVersion << " servers = " << servers;
        return os.str();
    }
};

struct WritePrepareOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t	 chunkVersion;
    off_t 	 offset;   /* input */
    size_t 	 numBytes; /* input */
    int64_t      writeId; /* value for the local server */
    uint32_t     numServers; /* input */
    uint32_t     checksum; /* input: as computed by the sender; 0 means sender didn't send */
    std::string  servers; /* input: set of servers on which to write */
    IOBuffer *dataBuf; /* buffer with the data to be written */
    WritePrepareFwdOp *writeFwdOp; /* op that tracks the data we
                                      fwd'ed to a peer */
    WriteOp *writeOp; /* the underlying write that is queued up locally */
    uint32_t numDone; // if we did forwarding, we wait for
                      // local/remote to be done; otherwise, we only
                      // wait for local to be done

    WritePrepareOp(kfsSeq_t s) :
        KfsOp(CMD_WRITE_PREPARE, s), writeId(-1), checksum(0), 
        dataBuf(NULL), writeFwdOp(NULL), writeOp(NULL), numDone(0)
    {
        SET_HANDLER(this, &WritePrepareOp::HandleDone);
    }
    ~WritePrepareOp();

    void Response(std::ostringstream &os);
    void Execute();

    int ForwardToPeer(const ServerLocation &peer, IOBuffer *data);
    int HandleDone(int code, void *data);

    std::string Show() const {
        std::ostringstream os;
        
        os << "write-prepare: seq = " << seq << " chunkId = " << chunkId 
           << " chunkversion = " << chunkVersion;
        os << " offset: " << offset << " numBytes: " << numBytes;
        return os.str();
    }
};

struct WritePrepareFwdOp : public KfsOp {
    WritePrepareOp *owner;
    ServerLocation location; /* for debugging purposes */
    std::string  writeIdStr; /* input */
    IOBuffer *dataBuf; /* buffer with the data to be written */
    WritePrepareFwdOp(kfsSeq_t s, WritePrepareOp *o, IOBuffer *d, const ServerLocation &l) :
        KfsOp(CMD_WRITE_PREPARE_FWD, s), owner(o), location(l), dataBuf(d)
    {
        SET_HANDLER(this, &WritePrepareFwdOp::HandleDone);
    }

    ~WritePrepareFwdOp() {
        delete dataBuf;
    }

    void Request(std::ostringstream &os);
    int HandleDone(int code, void *data);

    // nothing to do...we send the data to peer and wait. have a
    // decl. to keep compiler happy
    void Execute() { }

    std::string Show() const {
        std::ostringstream os;
        
        os << "write-prepare-fwd: " << location.ToString() << ' ' << owner->Show();
        return os.str();
    }
};

struct WriteOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t	 chunkVersion;
    off_t 	 offset;   /* input */
    size_t 	 numBytes; /* input */
    ssize_t	 numBytesIO; /* output: # of bytes actually written */
    DiskConnectionPtr diskConnection; /* disk connection used for writing data */
    IOBuffer *dataBuf; /* buffer with the data to be written */
    off_t	 chunkSize; /* store the chunk size for logging purposes */
    std::vector<uint32_t> checksums; /* store the checksum for logging purposes */
    /* 
     * for writes that are smaller than a checksum block, we need to
     * read the whole block in, compute the new checksum and then write
     * out data.  This buffer holds the data read in from disk.
    */
    ReadOp *rop;
    /*
     * The owning write prepare op
     */
    WritePrepareOp *wpop;
    /*
     * Should we wait for aio_sync() to finish before replying to
     * upstream clients? By default, we don't
     */
    bool waitForSyncDone;
    /* Set if the write was triggered due to re-replication */
    bool isFromReReplication;
    int64_t      writeId;
    // time at which the write was enqueued at the ChunkManager
    time_t	 enqueueTime;

    // for statistics purposes, have a "holder" op that tracks how long it took a write to finish. 
    bool isWriteIdHolder;

    WriteOp(kfsChunkId_t c, int64_t v) :
        KfsOp(CMD_WRITE, 0), chunkId(c), chunkVersion(v),
        dataBuf(NULL), rop(NULL), wpop(NULL), waitForSyncDone(false),
        isFromReReplication(false), isWriteIdHolder(false)
    {
        SET_HANDLER(this, &WriteOp::HandleWriteDone);
    }

    WriteOp(kfsSeq_t s, kfsChunkId_t c, int64_t v, off_t o, size_t n, 
            IOBuffer *b, int64_t id) :
        KfsOp(CMD_WRITE, s), chunkId(c), chunkVersion(v),
        offset(o), numBytes(n), numBytesIO(0),
        dataBuf(b), chunkSize(0), rop(NULL), wpop(NULL), 
        waitForSyncDone(false), isFromReReplication(false),
        writeId(id), isWriteIdHolder(false)
    {
        SET_HANDLER(this, &WriteOp::HandleWriteDone);
    }
    ~WriteOp();

    void Reset() {
        status = numBytesIO = 0;
        SET_HANDLER(this, &WriteOp::HandleWriteDone);
    }
    void Response(std::ostringstream &os) { };
    void Execute();
    void Log(std::ofstream &ofs);
    int HandleWriteDone(int code, void *data);    
    int HandleSyncDone(int code, void *data);
    int HandleLoggingDone(int code, void *data);
    
    std::string Show() const {
        std::ostringstream os;
        
        os << "write: chunkId = " << chunkId << " chunkversion = " << chunkVersion;
        os << " offset: " << offset << " numBytes: " << numBytes;
        return os.str();
    }
};

// sent by the client to force data to disk
struct WriteSyncOp : public KfsOp {
    kfsChunkId_t chunkId;    
    int64_t chunkVersion;
    int64_t writeId; /* corresponds to the local write */
    uint32_t numServers;
    std::string servers;
    WriteSyncOp *fwdedOp;
    WriteOp *writeOp; // the underlying write that needs to be pushed to disk
    uint32_t numDone; // if we did forwarding, we wait for
                      // local/remote to be done; otherwise, we only
                      // wait for local to be done
    bool writeMaster; // infer from the server list if we are the "master" for doing the writes

    WriteSyncOp(kfsSeq_t s, kfsChunkId_t c, int64_t v) :
        KfsOp(CMD_WRITE_SYNC, s), chunkId(c), chunkVersion(v), writeId(-1),
        numServers(0), fwdedOp(NULL), writeOp(NULL), numDone(0), writeMaster(false)
    { 
        SET_HANDLER(this, &WriteSyncOp::HandleDone);        
    }
    ~WriteSyncOp();

    void Request(std::ostringstream &os);
    void Response(std::ostringstream &os);
    void Execute();

    int ForwardToPeer(const ServerLocation &peer);
    int HandlePeerReply(int code, void *data);
    int HandleDone(int code, void *data);    

    std::string Show() const {
        std::ostringstream os;
        
        os << "write-sync: seq = " << seq << " chunkId = " << chunkId << " chunkversion = " << chunkVersion;
        os << " write-id info: " << servers;
        return os.str();
    }
};


// OP for reading/writing out the meta-data associated with each chunk.  This
// is an internally generated op (ops that generate this one are
// allocate/write/truncate/change-chunk-vers). 
struct WriteChunkMetaOp : public KfsOp {
    kfsChunkId_t chunkId;
    DiskConnectionPtr diskConnection; /* disk connection used for writing data */
    IOBuffer *dataBuf; /* buffer with the data to be written */

    WriteChunkMetaOp(kfsChunkId_t c, KfsCallbackObj *o) : 
        KfsOp(CMD_WRITE_CHUNKMETA, 0, o), chunkId(c), dataBuf(NULL)  
    {
        SET_HANDLER(this, &WriteChunkMetaOp::HandleDone);
    }
    ~WriteChunkMetaOp() {
        delete dataBuf;
    }
    void Execute() { }
    std::string Show() const {
        std::ostringstream os;

        os << "write-chunk-meta: chunkid = " << chunkId;
        return os.str();

    }
    // Notify the op that is waiting for the write to finish that all
    // is done
    int HandleDone(int code, void *data) {
        clnt->HandleEvent(EVENT_CMD_DONE, NULL);
        delete this;
        return 0;
    }
};

struct ReadChunkMetaOp : public KfsOp {
    kfsChunkId_t chunkId;
    DiskConnectionPtr diskConnection; /* disk connection used for reading data */

    // others ops that are also waiting for this particular meta-data
    // read to finish; they'll get notified when the read is done
    std::list<KfsOp *> waiters;
    ReadChunkMetaOp(kfsChunkId_t c, KfsCallbackObj *o) : 
        KfsOp(CMD_READ_CHUNKMETA, 0, o), chunkId(c)
    {
        SET_HANDLER(this, &ReadChunkMetaOp::HandleDone);
    }

    void Execute() { }
    std::string Show() const {
        std::ostringstream os;

        os << "read-chunk-meta: chunkid = " << chunkId;
        return os.str();
    }

    void AddWaiter(KfsOp *op) {
        waiters.push_back(op);
    }
    // Update internal data structures and then notify the waiting op
    // that read of meta-data is done.
    int HandleDone(int code, void *data);
};

struct ReadOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t	 chunkVersion;
    off_t 	 offset;   /* input */
    size_t 	 numBytes; /* input */
    ssize_t	 numBytesIO; /* output: # of bytes actually read */
    DiskConnectionPtr diskConnection; /* disk connection used for reading data */
    IOBuffer *dataBuf; /* buffer with the data read */
    std::vector<uint32_t> checksum; /* checksum over the data that is sent back to client */
    float diskIOTime; /* how long did the AIOs take */
    /*
     * for writes that require the associated checksum block to be
     * read in, store the pointer to the associated write op.
    */
    WriteOp *wop;
    ReadOp(kfsSeq_t s) :
        KfsOp(CMD_READ, s), numBytesIO(0), dataBuf(NULL),
        wop(NULL)
    {
        SET_HANDLER(this, &ReadOp::HandleDone);
    }
    ReadOp(WriteOp *w, off_t o, size_t n) :
        KfsOp(CMD_READ, w->seq), chunkId(w->chunkId),
        chunkVersion(w->chunkVersion), offset(o), numBytes(n),
        numBytesIO(0), dataBuf(NULL), wop(w)
    {
        clnt = w;
        SET_HANDLER(this, &ReadOp::HandleDone);
    }
    ~ReadOp() {
        assert(wop == NULL);
        delete dataBuf;
        if (diskConnection)
            diskConnection->Close();
    }

    void Request(std::ostringstream &os);
    void Response(std::ostringstream &os);
    void Execute();
    int HandleDone(int code, void *data);
    // handler for reading in the chunk meta-data
    int HandleChunkMetaReadDone(int code, void *data);
    // handler for dealing with re-replication events
    int HandleReplicatorDone(int code, void *data);
    std::string Show() const {
        std::ostringstream os;
        
        os << "read: chunkId = " << chunkId << " chunkversion = " << chunkVersion;
        os << " offset: " << offset << " numBytes: " << numBytes;
        return os.str();
    }
};

// used for retrieving a chunk's size
struct SizeOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t	 chunkVersion;
    off_t     size; /* result */
    SizeOp(kfsSeq_t s) :
        KfsOp(CMD_SIZE, s) { }
    SizeOp(kfsSeq_t s, kfsChunkId_t c, int64_t v) :
        KfsOp(CMD_SIZE, s), chunkId(c), chunkVersion(v) { }

    void Request(std::ostringstream &os);
    void Response(std::ostringstream &os);
    void Execute();
    std::string Show() const {
        std::ostringstream os;
        
        os << "size: chunkId = " << chunkId << " chunkversion = " << chunkVersion;
        return os.str();
    }
    int HandleDone(int code, void *data);
};

struct GetChunkMetadataOp : public KfsOp {
    kfsChunkId_t chunkId;  // input
    int64_t chunkVersion; // output
    off_t chunkSize; // output
    IOBuffer *dataBuf; // buffer with the checksum info
    size_t numBytesIO;
    GetChunkMetadataOp(kfsSeq_t s) :
        KfsOp(CMD_GET_CHUNK_METADATA, s), chunkVersion(0), chunkSize(0), dataBuf(NULL), numBytesIO(0)
    {

    }
    ~GetChunkMetadataOp() 
    {
        delete dataBuf;
    }
    void Execute();
    // handler for reading in the chunk meta-data
    int HandleChunkMetaReadDone(int code, void *data);

    void Request(std::ostringstream &os);
    void Response(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;

        os << "get-chunk-metadata: " << " chunkid = " << chunkId;
        return os.str();
    }
    int HandleDone(int code, void *data);
};

// used for pinging the server and checking liveness
struct PingOp : public KfsOp {
    int64_t totalSpace;
    int64_t usedSpace;
    PingOp(kfsSeq_t s) :
        KfsOp(CMD_PING, s) { }
    void Response(std::ostringstream &os);
    void Execute();
    std::string Show() const {
        return "monitoring ping";
    }
};

// used to dump chunk map
struct DumpChunkMapOp : public KfsOp {
    DumpChunkMapOp(kfsSeq_t s) :
       KfsOp(CMD_DUMP_CHUNKMAP, s) { }
    void Response(std::ostringstream &os);
    void Execute();
    std::string Show() const {
       return "dumping chunk map";
    }
};

// used to extract out all the counters we have
struct StatsOp : public KfsOp {
    std::string stats; // result
    StatsOp(kfsSeq_t s) :
        KfsOp(CMD_STATS, s) { }
    void Response(std::ostringstream &os);
    void Execute();
    std::string Show() const {
        return "monitoring stats";
    }
};

/// Checkpoint op is a means of communication between the main thread
/// and the logger thread.  The main thread sends this op to the logger
/// thread and the logger threads gets rid of it after taking a
/// checkpoint.
// XXX: We may want to allow users to submit checkpoint requests.  At
// that point code will need to come in for ops and such.

struct CheckpointOp : public KfsOp {
    std::ostringstream data; // the data that needs to be checkpointed
    CheckpointOp(kfsSeq_t s) :
        KfsOp(CMD_CHECKPOINT, s) { }
    void Response(std::ostringstream &os) { };
    void Execute() { };
    std::string Show() const {
        return "internal: checkpoint";
    }
};

struct LeaseRenewOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t leaseId;
    std::string leaseType;
    LeaseRenewOp(kfsSeq_t s, kfsChunkId_t c, int64_t l, std::string t) :
        KfsOp(CMD_LEASE_RENEW, s), chunkId(c), leaseId(l), leaseType(t)
    {
        SET_HANDLER(this, &LeaseRenewOp::HandleDone);
    }
    void Request(std::ostringstream &os);
    // To be called whenever we get a reply from the server
    int HandleDone(int code, void *data);
    void Execute() { };
    std::string Show() const {
        std::ostringstream os;

        os << "lease-renew: " << " chunkid = " << chunkId;
        os << " leaseId: " << leaseId << " type: " << leaseType;
        return os.str();
    }
};

// Whenever we want to give up a lease early, we notify the metaserver
// using this op.
struct LeaseRelinquishOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t leaseId;
    std::string leaseType;
    LeaseRelinquishOp(kfsSeq_t s, kfsChunkId_t c, int64_t l, std::string t) :
        KfsOp(CMD_LEASE_RELINQUISH, s), chunkId(c), leaseId(l), leaseType(t)
    {
        SET_HANDLER(this, &LeaseRelinquishOp::HandleDone);
    }
    void Request(std::ostringstream &os);
    // To be called whenever we get a reply from the server
    int HandleDone(int code, void *data);
    void Execute() { };
    std::string Show() const {
        std::ostringstream os;

        os << "lease-relinquish: " << " chunkid = " << chunkId;
        os << " leaseId: " << leaseId << " type: " << leaseType;
        return os.str();
    }
};

// This is just a helper op for building a hello request to the metaserver.
struct HelloMetaOp : public KfsOp {
    ServerLocation myLocation;
    std::string clusterKey;
    std::string md5sum;
    int rackId;
    int64_t totalSpace;
    int64_t usedSpace;
    std::vector<ChunkInfo_t> chunks;
    HelloMetaOp(kfsSeq_t s, ServerLocation &l, std::string &k, std::string &m, int r) :
        KfsOp(CMD_META_HELLO, s), myLocation(l),  clusterKey(k), md5sum(m), rackId(r) {  }
    void Execute();
    void Request(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;

        os << "meta-hello: " << " mylocation = " << myLocation.ToString();
        os << "cluster key: " << clusterKey;
        return os.str();
    }
};

struct CorruptChunkOp : public KfsOp {
    kfsFileId_t fid; // input: fid whose chunk is bad
    kfsChunkId_t chunkId; // input: chunkid of the corrupted chunk

    CorruptChunkOp(kfsSeq_t s, kfsFileId_t f, kfsChunkId_t c) :
        KfsOp(CMD_CORRUPT_CHUNK, s), fid(f), chunkId(c)
    {
        SET_HANDLER(this, &CorruptChunkOp::HandleDone);
    }
    void Request(std::ostringstream &os);
    // To be called whenever we get a reply from the server
    int HandleDone(int code, void *data);
    void Execute() { };
    std::string Show() const {
        std::ostringstream os;

        os << "corrupt chunk: " << " fileid = " << fid 
           << " chunkid = " << chunkId;
        return os.str();
    }
};

struct TimeoutOp : public KfsOp {

    TimeoutOp(kfsSeq_t s) :
        KfsOp(CMD_TIMEOUT, s)
    {

    }
    void Request(std::ostringstream &os) { }
    void Execute();
    std::string Show() const { return "timeout"; }
};

struct KillRemoteSyncOp : public KfsOp {

    // pass in the remote sync SM that needs to be nuked
    KillRemoteSyncOp(kfsSeq_t s, KfsCallbackObj *owner) :
        KfsOp(CMD_KILL_REMOTE_SYNC, s, owner)
    {

    }
    void Request(std::ostringstream &os) { }
    void Execute();
    std::string Show() const { return "kill remote sync"; }
};

// Helper functor that matches ops based on seq #

class OpMatcher {
    kfsSeq_t seqNum;
public:
    OpMatcher(kfsSeq_t s) : seqNum(s) { };
    bool operator() (KfsOp *op) {
        return op->seq == seqNum;
    }
};

extern void InitParseHandlers();
extern void RegisterCounters();

extern int ParseCommand(char *cmdBuf, int cmdLen, KfsOp **res);

extern void SubmitOp(KfsOp *op);
extern void SubmitOpResponse(KfsOp *op);

}

#endif // CHUNKSERVER_KFSOPS_H

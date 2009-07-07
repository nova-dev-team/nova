//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: KfsOps.cc 317 2009-04-23 19:47:19Z sriramsrao $
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
// Code for parsing commands sent to the Chunkserver and generating
// responses that summarize the result of their execution.
//
// 
//----------------------------------------------------------------------------

#include "KfsOps.h"
#include "common/Version.h"
#include "common/kfstypes.h"
#include "libkfsIO/Globals.h"
#include "meta/thread.h"
#include "meta/queue.h"
#include "libkfsIO/Checksum.h"

#include "ChunkManager.h"
#include "ChunkServer.h"
#include "LeaseClerk.h"
#include "Replicator.h"
#include "Utils.h"

#include <algorithm>
#include <boost/lexical_cast.hpp>

using std::map;
using std::string;
using std::ofstream;
using std::istringstream;
using std::ostringstream;
using std::for_each;
using std::vector;
using std::min;

using namespace KFS;
using namespace KFS::libkfsio;

typedef int (*ParseHandler)(Properties &, KfsOp **);

/// command -> parsehandler map
typedef map<string, ParseHandler> ParseHandlerMap;
typedef map<string, ParseHandler>::iterator ParseHandlerMapIter;

// handlers for parsing
ParseHandlerMap	gParseHandlers;

// Counters for the various ops
typedef map<KfsOp_t, Counter *> OpCounterMap;
typedef map<KfsOp_t, Counter *>::iterator OpCounterMapIter;

OpCounterMap gCounters;
Counter gCtrWriteMaster("Write Master");
Counter gCtrWriteDuration("Write Duration");

const char *KFS_VERSION_STR = "KFS/1.0";

// various parse handlers
int parseHandlerOpen(Properties &prop, KfsOp **c);
int parseHandlerClose(Properties &prop, KfsOp **c);
int parseHandlerRead(Properties &prop, KfsOp **c);
int parseHandlerWriteIdAlloc(Properties &prop, KfsOp **c);
int parseHandlerWritePrepare(Properties &prop, KfsOp **c);
int parseHandlerWriteSync(Properties &prop, KfsOp **c);
int parseHandlerSize(Properties &prop, KfsOp **c);
int parseHandlerGetChunkMetadata(Properties &prop, KfsOp **c);
int parseHandlerAllocChunk(Properties &prop, KfsOp **c);
int parseHandlerDeleteChunk(Properties &prop, KfsOp **c);
int parseHandlerTruncateChunk(Properties &prop, KfsOp **c);
int parseHandlerReplicateChunk(Properties &prop, KfsOp **c);
int parseHandlerHeartbeat(Properties &prop, KfsOp **c);
int parseHandlerChangeChunkVers(Properties &prop, KfsOp **c);
int parseHandlerStaleChunks(Properties &prop, KfsOp **c);
int parseHandlerRetire(Properties &prop, KfsOp **c);
int parseHandlerPing(Properties &prop, KfsOp **c);
int parseHandlerDumpChunkMap(Properties &prop, KfsOp **c);
int parseHandlerStats(Properties &prop, KfsOp **c);

static TimeoutOp timeoutOp(0);

void
KFS::SubmitOp(KfsOp *op)
{
    op->type = OP_REQUEST;
    op->Execute();
}

void
KFS::SubmitOpResponse(KfsOp *op)
{
    op->type = OP_RESPONSE;
    op->HandleEvent(EVENT_CMD_DONE, op);
}

void
KFS::verifyExecutingOnEventProcessor()
{
    return;
}


void
KFS::InitParseHandlers()
{
    gParseHandlers["OPEN"] = parseHandlerOpen;
    gParseHandlers["CLOSE"] = parseHandlerClose;
    gParseHandlers["READ"] = parseHandlerRead;
    gParseHandlers["WRITE_ID_ALLOC"] = parseHandlerWriteIdAlloc;
    gParseHandlers["WRITE_PREPARE"] = parseHandlerWritePrepare;
    gParseHandlers["WRITE_SYNC"] = parseHandlerWriteSync;
    gParseHandlers["SIZE"] = parseHandlerSize;
    gParseHandlers["GET_CHUNK_METADATA"] = parseHandlerGetChunkMetadata;
    gParseHandlers["ALLOCATE"] = parseHandlerAllocChunk;
    gParseHandlers["DELETE"] = parseHandlerDeleteChunk;
    gParseHandlers["TRUNCATE"] = parseHandlerTruncateChunk;
    gParseHandlers["REPLICATE"] = parseHandlerReplicateChunk;
    gParseHandlers["HEARTBEAT"] = parseHandlerHeartbeat;
    gParseHandlers["STALE_CHUNKS"] = parseHandlerStaleChunks;
    gParseHandlers["CHUNK_VERS_CHANGE"] = parseHandlerChangeChunkVers;
    gParseHandlers["RETIRE"] = parseHandlerRetire;
    gParseHandlers["PING"] = parseHandlerPing;
    gParseHandlers["DUMP_CHUNKMAP"] = parseHandlerDumpChunkMap;
    gParseHandlers["STATS"] = parseHandlerStats;
}

static void
AddCounter(const char *name, KfsOp_t opName)
{
    Counter *c;
    
    c = new Counter(name);
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
    AddCounter("Open", CMD_OPEN);
    AddCounter("Read", CMD_READ);
    AddCounter("Write Prepare", CMD_WRITE_PREPARE);
    AddCounter("Write Sync", CMD_WRITE_SYNC);
    AddCounter("Write (AIO)", CMD_WRITE);
    AddCounter("Size", CMD_SIZE);
    AddCounter("Get Chunk Metadata", CMD_GET_CHUNK_METADATA);
    AddCounter("Alloc", CMD_ALLOC_CHUNK);
    AddCounter("Delete", CMD_DELETE_CHUNK);
    AddCounter("Truncate", CMD_TRUNCATE_CHUNK);
    AddCounter("Replicate", CMD_REPLICATE_CHUNK);
    AddCounter("Heartbeat", CMD_HEARTBEAT);
    AddCounter("Change Chunk Vers", CMD_CHANGE_CHUNK_VERS);

    globals().counterManager.AddCounter(&gCtrWriteMaster);
    globals().counterManager.AddCounter(&gCtrWriteDuration);
}

static void
UpdateCounter(KfsOp_t opName)
{
    Counter *c;
    OpCounterMapIter iter;
    
    iter = gCounters.find(opName);
    if (iter == gCounters.end())
        return;
    c = iter->second;
    c->Update(1);
}

#if 0
static void
DecrementCounter(KfsOp_t opName)
{
    Counter *c;
    OpCounterMapIter iter;
    
    iter = gCounters.find(opName);
    if (iter == gCounters.end())
        return;
    c = iter->second;
    c->Update(-1);
}
#endif


static void
UpdateMsgProcessingTime(const KfsOp *op) 
{
    struct timeval timeNow;
    float timeSpent;

    gettimeofday(&timeNow, NULL);

    timeSpent = ComputeTimeDiff(op->startTime, timeNow);

    OpCounterMapIter iter = gCounters.find(op->op);
    if (iter == gCounters.end())
        return;
    Counter *c = iter->second;
    c->Update(timeSpent);
}

KfsOp::~KfsOp()
{
    UpdateMsgProcessingTime(this);
}

///
/// Given a command in a buffer, parse it out and build a "Command"
/// structure which can then be executed.  For parsing, we take the
/// string representation of a command and build a Properties object
/// out of it; we can then pull the various headers in whatever order
/// we choose.
/// Commands are of the form:
/// <COMMAND NAME> \r\n
/// {header: value \r\n}+\r\n
///
///  The general model in parsing the client command:
/// 1. Each command has its own parser
/// 2. Extract out the command name and find the parser for that
/// command
/// 3. Dump the header/value pairs into a properties object, so that we
/// can extract the header/value fields in any order.
/// 4. Finally, call the parser for the command sent by the client.
///
/// @param[in] cmdBuf: buffer containing the request sent by the client
/// @param[in] cmdLen: length of cmdBuf
/// @param[out] res: A piece of memory allocated by calling new that
/// contains the data for the request.  It is the caller's
/// responsibility to delete the memory returned in res.
/// @retval 0 on success;  -1 if there is an error
/// 
int
KFS::ParseCommand(char *cmdBuf, int cmdLen, KfsOp **res)
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
    
    // get the first line and find the command name
    ist >> cmdStr;
    // trim the command
    cmdEnd = cmdStr.find_first_of(delims);
    if (cmdEnd != string::npos) {
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

void
parseCommon(Properties &prop, kfsSeq_t &seq)
{
    seq = prop.getValue("Cseq", (kfsSeq_t) -1);
}

int
parseHandlerOpen(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    OpenOp *oc;
    string openMode;

    parseCommon(prop, seq);

    oc = new OpenOp(seq);
    oc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    openMode = prop.getValue("Intent", "");
    // XXX: need to do a string compare
    oc->openFlags = O_RDWR;
    *c = oc;

    return 0;
}

int
parseHandlerClose(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    CloseOp *cc;

    parseCommon(prop, seq);

    cc = new CloseOp(seq);
    cc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    *c = cc;

    return 0;
}

int
parseHandlerRead(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    ReadOp *rc;

    parseCommon(prop, seq);

    rc = new ReadOp(seq);
    rc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    rc->chunkVersion = prop.getValue("Chunk-version", (int64_t) -1);
    rc->offset = prop.getValue("Offset", (off_t) 0);
    rc->numBytes = prop.getValue("Num-bytes", (long long) 0);
    if (rc->numBytes > CHUNKSIZE)
        rc->numBytes = 131072;
    *c = rc;

    return 0;
}

int
parseHandlerWriteIdAlloc(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    WriteIdAllocOp *wi;

    parseCommon(prop, seq);

    wi = new WriteIdAllocOp(seq);
    wi->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    wi->chunkVersion = prop.getValue("Chunk-version", (int64_t) -1);
    wi->offset = prop.getValue("Offset", (off_t) 0);
    wi->numBytes = prop.getValue("Num-bytes", (long long) 0);
    wi->numServers = prop.getValue("Num-servers", 0);
    wi->servers = prop.getValue("Servers", "");

    *c = wi;

    return 0;
}

int
parseHandlerWritePrepare(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    WritePrepareOp *wp;

    parseCommon(prop, seq);

    wp = new WritePrepareOp(seq);
    wp->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    wp->chunkVersion = prop.getValue("Chunk-version", (int64_t) -1);
    wp->offset = prop.getValue("Offset", (off_t) 0);
    wp->numBytes = prop.getValue("Num-bytes", (long long) 0);
    wp->numServers = prop.getValue("Num-servers", 0);
    wp->servers = prop.getValue("Servers", "");
    wp->checksum = (uint32_t) prop.getValue("Checksum", (off_t) 0);

    *c = wp;

    return 0;
}

int
parseHandlerWriteSync(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    WriteSyncOp *ws;
    kfsChunkId_t cid;
    int64_t chunkVers;

    parseCommon(prop, seq);
    cid = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    chunkVers = prop.getValue("Chunk-version", (int64_t) -1);
    
    ws = new WriteSyncOp(seq, cid, chunkVers);
    ws->numServers = prop.getValue("Num-servers", 0);
    ws->servers = prop.getValue("Servers", "");

    *c = ws;

    return 0;
}

int
parseHandlerSize(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    SizeOp *sc;

    parseCommon(prop, seq);

    sc = new SizeOp(seq);
    sc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    sc->chunkVersion = prop.getValue("Chunk-version", (int64_t) -1);
    *c = sc;

    return 0;
}

int
parseHandlerGetChunkMetadata(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    GetChunkMetadataOp *gcm;

    parseCommon(prop, seq);

    gcm = new GetChunkMetadataOp(seq);
    gcm->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    *c = gcm;

    return 0;
}

int
parseHandlerAllocChunk(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    AllocChunkOp *cc;

    parseCommon(prop, seq);

    cc = new AllocChunkOp(seq);
    cc->fileId = prop.getValue("File-handle", (kfsFileId_t) -1);
    cc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    cc->chunkVersion = prop.getValue("Chunk-version", (int64_t) -1);
    // if the leaseId is a positive value, then this server has the
    // lease on this chunk.
    cc->leaseId = prop.getValue("Lease-id", (int64_t) -1);
    *c = cc;

    return 0;
}

int
parseHandlerDeleteChunk(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    DeleteChunkOp *cc;

    parseCommon(prop, seq);

    cc = new DeleteChunkOp(seq);
    cc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    *c = cc;

    return 0;
}

int
parseHandlerTruncateChunk(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    TruncateChunkOp *tc;

    parseCommon(prop, seq);

    tc = new TruncateChunkOp(seq);
    tc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    tc->chunkSize = prop.getValue("Chunk-size", (long long) 0);

    *c = tc;

    return 0;
}

int
parseHandlerReplicateChunk(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    ReplicateChunkOp *rc;
    string s;

    parseCommon(prop, seq);

    rc = new ReplicateChunkOp(seq);
    rc->fid = prop.getValue("File-handle", (kfsFileId_t) -1);
    rc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    rc->chunkVersion = prop.getValue("Chunk-version", (int64_t) -1);
    s = prop.getValue("Chunk-location", "");
    if (s != "") {
        rc->location.FromString(s);
    }

    *c = rc;

    return 0;
}

int
parseHandlerChangeChunkVers(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    ChangeChunkVersOp *rc;

    parseCommon(prop, seq);

    rc = new ChangeChunkVersOp(seq);
    rc->fileId = prop.getValue("File-handle", (kfsFileId_t) -1);
    rc->chunkId = prop.getValue("Chunk-handle", (kfsChunkId_t) -1);
    rc->chunkVersion = prop.getValue("Chunk-version", (int64_t) -1);

    *c = rc;

    return 0;
}

int
parseHandlerHeartbeat(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    HeartbeatOp *hb;

    parseCommon(prop, seq);

    hb = new HeartbeatOp(seq);
    *c = hb;

    return 0;
}

int
parseHandlerRetire(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;

    parseCommon(prop, seq);

    *c = new RetireOp(seq);

    return 0;
}

int
parseHandlerStaleChunks(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    StaleChunksOp *sc;

    parseCommon(prop, seq);

    sc = new StaleChunksOp(seq);
    sc->contentLength = prop.getValue("Content-length", 0);
    sc->numStaleChunks = prop.getValue("Num-chunks", 0);
    *c = sc;
    return 0;
}

int
parseHandlerPing(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    PingOp *po;

    parseCommon(prop, seq);

    po = new PingOp(seq);
    *c = po;
    return 0;
}

int
parseHandlerDumpChunkMap(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    DumpChunkMapOp *po;

    parseCommon(prop, seq);

    po = new DumpChunkMapOp(seq);
    *c = po;
    return 0;
}

int
parseHandlerStats(Properties &prop, KfsOp **c)
{
    kfsSeq_t seq;
    StatsOp *so;

    parseCommon(prop, seq);

    so = new StatsOp(seq);
    *c = so;
    return 0;
}

///
/// Generic event handler for tracking completion of an event
/// execution.  Push the op to the logger and the net thread will pick
/// it up and dispatch it.
///
int
KfsOp::HandleDone(int code, void *data)
{
    gLogger.Submit(this);
    return 0;
}

///
/// A read op finished.  Set the status and the # of bytes read
/// alongwith the data and notify the client.
///
int
ReadOp::HandleDone(int code, void *data)
{
    IOBuffer *b;
    off_t chunkSize = 0;

    // DecrementCounter(CMD_READ);

#ifdef DEBUG
    verifyExecutingOnEventProcessor();
#endif

    if (code == EVENT_DISK_ERROR) {
        status = -1;
        if (data != NULL) {
            status = *(int *) data;
            KFS_LOG_VA_INFO("Disk error: errno = %d, chunkid = %lld", status, chunkId);
        }
        gChunkManager.ChunkIOFailed(chunkId, status);
    }
    else if (code == EVENT_DISK_READ) {
        if (dataBuf == NULL) {
            dataBuf = new IOBuffer();
        }
        b = (IOBuffer *) data;
        // Order matters...when we append b, we take the data from b
        // and put it into our buffer.
        dataBuf->Append(b);
        // verify checksum
        gChunkManager.ReadChunkDone(this);
        numBytesIO = dataBuf->BytesConsumable();
        if (status == 0)
            // checksum verified
            status = numBytesIO;
    }

    if (status >= 0) {
        if (numBytesIO < (int) CHECKSUM_BLOCKSIZE) {
            uint32_t cks;
            cks = ComputeBlockChecksum(dataBuf, numBytesIO);
            checksum.push_back(cks);
        } else {
            checksum = ComputeChecksums(dataBuf, numBytesIO);
        }
        // send the disk IO time back to client for telemetry reporting
        struct timeval timeNow;

        gettimeofday(&timeNow, NULL);
        diskIOTime = ComputeTimeDiff(startTime, timeNow);
    }

    gChunkManager.ChunkSize(chunkId, &chunkSize);

    if (wop != NULL) {
        // if the read was triggered by a write, then resume execution of write
        wop->Execute();
        return 0;
    }

    if ((chunkSize > 0 && (offset + numBytesIO >= (off_t) chunkSize)) &&
        (!gLeaseClerk.IsLeaseValid(chunkId))) {
        // If we have read the full chunk, close out the fd.  The
        // observation is that reads are sequential and when we
        // finished a chunk, the client will move to the next one.
        gChunkManager.CloseChunk(chunkId);
    }

    gLogger.Submit(this);

    return 0;
}

int
ReadOp::HandleReplicatorDone(int code, void *data)
{
    if ((status >= 0) && (checksum.size() > 0)) {
        vector<uint32_t> datacksums = ComputeChecksums(dataBuf, numBytesIO);

        for (uint32_t i = 0; i < min(datacksums.size(), checksum.size()); i++) 
            if (datacksums[i] != checksum[i]) {
                KFS_LOG_VA_INFO("Checksum mismatch in re-replication: expect %ld, got: %ld\n",
                                datacksums[i], checksum[i]);
                status = -EBADCKSUM;
                break;
            }
    }
    // notify the replicator object that the read it had submitted to
    // the peer has finished. 
    return clnt->HandleEvent(code, data);
}

int
WriteOp::HandleWriteDone(int code, void *data)
{
    // DecrementCounter(CMD_WRITE);

    if (isFromReReplication) {
        if (code == EVENT_DISK_WROTE) {
            status = *(int *) data;
            numBytesIO = status;
        }
        else {
            status = -1;
        }
        return clnt->HandleEvent(code, this);
    }
    assert(wpop != NULL);

    if (code == EVENT_DISK_ERROR) {
        // eat up everything that was sent
        dataBuf->Consume(numBytes);
        status = -1;
        if (data != NULL) {
            status = *(int *) data;
            KFS_LOG_VA_INFO("Disk error: errno = %d, chunkid = %lld", status, chunkId);
        }
        gChunkManager.ChunkIOFailed(chunkId, status);

        wpop->HandleEvent(EVENT_CMD_DONE, this);
        return 0;
    }
    else if (code == EVENT_DISK_WROTE) {
        status = *(int *) data;
        numBytesIO = status;
        SET_HANDLER(this, &WriteOp::HandleSyncDone);
        // queue the sync op only if we are all done with writing to
        // this chunk:

        if ((size_t) numBytesIO != (size_t) numBytes) {
            // write didn't do everything that was asked; we need to retry
            KFS_LOG_VA_INFO("Write on chunk did less: asked = %ld, did = %ld; asking clnt to retry",
                            numBytes, numBytesIO);
            status = -EAGAIN;
        }

        waitForSyncDone = false;

        if (offset + numBytesIO >= (off_t) KFS::CHUNKSIZE) {
            // If we have written till the end of the chunk, close out the
            // fd.  The observation is that writes are sequential and when
            // we finished a chunk, the client will move to the next
            // one.
#if 0
            if (gChunkManager.Sync(this) < 0) {
                KFS_LOG_DEBUG("Sync failed...");
                // eat up everything that was sent
                dataBuf->Consume(numBytes);
                // Sync failed
                status = -1;
                // clnt->HandleEvent(EVENT_CMD_DONE, this);
                return wsop->HandleEvent(EVENT_CMD_DONE, this);
            }
#endif
        }

        if (!waitForSyncDone) {
            // KFS_LOG_DEBUG("Queued sync; not waiting for sync to finish...");
            // sync is queued; no need to wait for it to finish
            return HandleSyncDone(EVENT_SYNC_DONE, 0);
        }
    }
    return 0;
}

///
/// A write op finished.  Set the status and the # of bytes written
/// and notify the owning write commit op.
///
int
WriteOp::HandleSyncDone(int code, void *data)
{
    // eat up everything that was sent
    dataBuf->Consume(numBytes);

    if (code != EVENT_SYNC_DONE) {
        status = -1;
    }

    if (status >= 0) {
        gChunkManager.ChunkSize(chunkId, &chunkSize);
        SET_HANDLER(this, &WriteOp::HandleLoggingDone);
        gLogger.Submit(this);
    }
    else {
        wpop->HandleEvent(EVENT_CMD_DONE, this);
    }
    
    return 0;
}

int
WriteOp::HandleLoggingDone(int code, void *data)
{
    assert(wpop != NULL);
    return wpop->HandleEvent(EVENT_CMD_DONE, this);
}

///
/// Handlers for ops that need logging.  This method is invoked by the
/// logger thread.  So, don't access any globals here---otherwise, we
/// need to add locking.
///
void
AllocChunkOp::Log(ofstream &ofs)
{
    ofs << "ALLOCATE " << chunkId << ' ' << fileId << ' ';
    ofs << chunkVersion << "\n";
    assert(!ofs.fail());
}

/// Resetting a chunk's version # is equivalent to doing an allocation
/// of an existing chunk.
void
ChangeChunkVersOp::Log(ofstream &ofs)
{
    ofs << "CHANGE_CHUNK_VERS " << chunkId << ' ' << fileId << ' ';
    ofs << chunkVersion << "\n";
    assert(!ofs.fail());
}

void
DeleteChunkOp::Log(ofstream &ofs)
{
    ofs << "DELETE " << chunkId << "\n";
    assert(!ofs.fail());
}

void
WriteOp::Log(ofstream &ofs)
{
    ofs << "WRITE " << chunkId << ' ' << chunkSize << ' ';
    ofs << offset << ' ';
    ofs << checksums.size();
    for (vector<uint32_t>::size_type i = 0; i < checksums.size(); i++) {
        ofs << ' ' << checksums[i];
    }
    ofs << "\n";
    assert(!ofs.fail());
}

void
TruncateChunkOp::Log(ofstream &ofs)
{
    ofs << "TRUNCATE " << chunkId << ' ' << chunkSize << "\n";
    assert(!ofs.fail());
}

// For replicating a chunk, we log nothing.  We don't write out info
// about the chunk in checkpoint files until replication is complete.
// This way, if we ever crash during chunk-replication, we'll simply
// nuke out the chunk on startup.
void
ReplicateChunkOp::Log(ofstream &ofs)
{

}

///
/// Handlers for executing the various ops.  If the op execution is
/// "in-line", that is the op doesn't block, then when the execution
/// is finished, the op is handed off to the logger; the net thread
/// will drain the logger and then notify the client.  Otherwise, the op is queued
/// for execution and the client gets notified whenever the op
/// finishes execution.
///
void
OpenOp::Execute()
{
    status = gChunkManager.OpenChunk(chunkId, openFlags);

    UpdateCounter(CMD_OPEN);

    //clnt->HandleEvent(EVENT_CMD_DONE, this);
    gLogger.Submit(this);
}

void
CloseOp::Execute()
{
    UpdateCounter(CMD_CLOSE);

    gChunkManager.CloseChunk(chunkId);
    status = 0;
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    gLogger.Submit(this);
}

void
AllocChunkOp::Execute()
{
    UpdateCounter(CMD_ALLOC_CHUNK);
    int res;

    // page in the chunk meta-data if needed
    if (!gChunkManager.NeedToReadChunkMetadata(chunkId)) {
        HandleChunkMetaReadDone(0, NULL);
        return;
    }
    
    SET_HANDLER(this, &AllocChunkOp::HandleChunkMetaReadDone);
    if ((res = gChunkManager.ReadChunkMetadata(chunkId, this)) < 0) {
        KFS_LOG_VA_INFO("Unable read chunk metadata for chunk: %lld; error = %d", chunkId, res);
        status = -EINVAL;
        gLogger.Submit(this);
        return;
    }
}

int
AllocChunkOp::HandleChunkMetaReadDone(int code, void *data)
{
    status = gChunkManager.AllocChunk(fileId, chunkId, chunkVersion);
    // if (status < 0) {
    // failed; nothing to log
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    // return;
    // }
    if (leaseId >= 0) {
        gCtrWriteMaster.Update(1);
        gLeaseClerk.RegisterLease(chunkId, leaseId);
    }
    if (status < 0)
        gLogger.Submit(this);

    // Submit the op and wait to be notified
    SET_HANDLER(this, &AllocChunkOp::HandleChunkMetaWriteDone);
    status = gChunkManager.WriteChunkMetadata(chunkId, this);
    if (status < 0)
        gLogger.Submit(this);
    return 0;
}

void
DeleteChunkOp::Execute()
{
    UpdateCounter(CMD_DELETE_CHUNK);

    status = gChunkManager.DeleteChunk(chunkId);
    //     if (status < 0)
    //         // failed; nothing to log
    //         clnt->HandleEvent(EVENT_CMD_DONE, this);
    //     else
    gLogger.Submit(this);
}

void
TruncateChunkOp::Execute()
{
    UpdateCounter(CMD_TRUNCATE_CHUNK);

    SET_HANDLER(this, &TruncateChunkOp::HandleChunkMetaReadDone);
    if (gChunkManager.ReadChunkMetadata(chunkId, this) < 0) {
        status = -EINVAL;
        gLogger.Submit(this);
    }
}

int
TruncateChunkOp::HandleChunkMetaReadDone(int code, void *data)
{
    status = *(int *) data;
    if (status < 0) {
        gLogger.Submit(this);
        return 0;
    }

    status = gChunkManager.TruncateChunk(chunkId, chunkSize);
    if (status < 0) {
        gLogger.Submit(this);
        return 0;
    }

    SET_HANDLER(this, &TruncateChunkOp::HandleChunkMetaWriteDone);
    status = gChunkManager.WriteChunkMetadata(chunkId, this);
    if (status < 0)
        gLogger.Submit(this);
    return 0;
}

void
ReplicateChunkOp::Execute()
{
    RemoteSyncSMPtr peer = gChunkServer.FindServer(location);

    UpdateCounter(CMD_REPLICATE_CHUNK);

#ifdef DEBUG
    string s = location.ToString();
    KFS_LOG_VA_DEBUG("Replicating chunk: %ld from %s",
                     chunkId, s.c_str());
#endif

    if (!peer) {
        string s = location.ToString();
        KFS_LOG_VA_INFO("Unable to find peer: %s", s.c_str());
        status = -1;
        gLogger.Submit(this);
        return;
    }

    replicator.reset(new Replicator(this));
    // Get the animation going...
    SET_HANDLER(this, &ReplicateChunkOp::HandleDone);

    replicator->Start(peer);
}

void
ChangeChunkVersOp::Execute()
{
    UpdateCounter(CMD_CHANGE_CHUNK_VERS);

    SET_HANDLER(this, &ChangeChunkVersOp::HandleChunkMetaReadDone);
    if (gChunkManager.ReadChunkMetadata(chunkId, this) < 0) {
        status = -EINVAL;
        gLogger.Submit(this);
    }
}

int
ChangeChunkVersOp::HandleChunkMetaReadDone(int code, void *data)
{
    status = *(int *) data;
    if (status < 0) {
        gLogger.Submit(this);
        return 0;
    }

    status = gChunkManager.ChangeChunkVers(fileId, chunkId, 
                                           chunkVersion);
    if (status < 0) {
        gLogger.Submit(this);
        return 0;
    }
        
    SET_HANDLER(this, &ChangeChunkVersOp::HandleChunkMetaWriteDone);
    status = gChunkManager.WriteChunkMetadata(chunkId, this);
    if (status < 0)
        gLogger.Submit(this);
    return 0;
}

// This is the heartbeat sent by the meta server
void
HeartbeatOp::Execute()
{
    UpdateCounter(CMD_HEARTBEAT);

    totalSpace = gChunkManager.GetTotalSpace();
    usedSpace = gChunkManager.GetUsedSpace();
    numChunks = gChunkManager.GetNumChunks();
    status = 0;
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    gLogger.Submit(this);
}

void
RetireOp::Execute()
{
    // we are told to retire...so, bow out
    KFS_LOG_INFO("We have been asked to retire...bye");
    StopNetProcessor(0);
}

void
StaleChunksOp::Execute()
{
    vector<kfsChunkId_t>::size_type i;

    for (i = 0; i < staleChunkIds.size(); ++i) {
        gChunkManager.StaleChunk(staleChunkIds[i]);
    }
    status = 0;
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    gLogger.Submit(this);
}

void
ReadOp::Execute()
{
    UpdateCounter(CMD_READ);

    if (numBytes > CHUNKSIZE) {
        status = -EINVAL;
        gLogger.Submit(this);
        return;
    }
   
    SET_HANDLER(this, &ReadOp::HandleChunkMetaReadDone);
    if (gChunkManager.ReadChunkMetadata(chunkId, this) < 0) {
        status = -EINVAL;
        gLogger.Submit(this);
    }
}

int
ReadOp::HandleChunkMetaReadDone(int code, void *data)
{
    status = *(int *) data;
    if (status < 0) {
        gLogger.Submit(this);
        return 0;
    }

    SET_HANDLER(this, &ReadOp::HandleDone);    
    status = gChunkManager.ReadChunk(this);

    if (status < 0) {
        // clnt->HandleEvent(EVENT_CMD_DONE, this);
        if (wop == NULL) {
            // we are done with this op; this needs draining
            gLogger.Submit(this);
        }
        else {
            // resume execution of write
            wop->Execute();
        }
    }
    return 0;
}

//
// Handling of writes is done in multiple steps:
// 1. The client allocates a chunk from the metaserver; the metaserver
// picks a set of hosting chunkservers and nominates one of the
// server's as the "master" for the transaction.
// 2. The client pushes data for a write via a WritePrepareOp to each
// of the hosting chunkservers (in any order).
// 3. The chunkserver in turn enqueues the write with the ChunkManager
// object.  The ChunkManager assigns an id to the write.   NOTE:
// nothing is written out to disk at this point.
// 4. After the client has pushed out data to replica chunk-servers
// and gotten write-id's, the client does a WriteSync to the master.
// 5. The master retrieves the write corresponding to the write-id and
// commits the write to disk.
// 6. The master then sends out a WriteCommit to each of the replica
// chunkservers asking them to commit the write; this commit message
// is sent concurrently to all the replicas.
// 7. After the replicas reply, the master replies to the client with
// status from individual servers and how much got written on each.
//

static bool
needToForwardWrite(string &serverInfo, uint32_t numServers, int &myPos,
                   ServerLocation &peerLoc, 
                   bool isWriteIdPresent, int64_t &writeId)
{
    istringstream ist(serverInfo);
    ServerLocation loc;
    bool foundLocal = false;
    int64_t id;
    bool needToForward = false;

    // the list of servers is ordered: we forward to the next one
    // in the list.
    for (uint32_t i = 0; i < numServers; i++) {
        ist >> loc.hostname;
        ist >> loc.port;
        if (isWriteIdPresent)
            ist >> id;
            
        if (gChunkServer.IsLocalServer(loc)) {
            // return the position of where this server is present in the list
            myPos = i; 
            foundLocal = true;
            if (isWriteIdPresent)
                writeId = id;
            continue;
        }
        // forward if we are not the last in the list
        if (foundLocal) {
            needToForward = true;
            break;
        }
    }
    peerLoc = loc;
    return needToForward;
}

void
WriteIdAllocOp::Execute()
{
    // check if we need to forward anywhere
    bool needToForward = false, writeMaster = false;
    int64_t dummy;
    int myPos = 1;
    ServerLocation peerLoc;

    if (numServers > 0) {
        needToForward = needToForwardWrite(servers, numServers, myPos, peerLoc, false, dummy);
        writeMaster = (myPos == 0);
        if (needToForward) {
            status = ForwardToPeer(peerLoc);
            if (status < 0) {
                if (gLeaseClerk.IsLeaseValid(chunkId)) {
                    // The write-id allocation has failed; we don't want to renew the lease.
                    // Now, when the client forces a re-allocation, the
                    // metaserver will do a version bump; when the node that
                    // was dead comes back, we can detect it has missed a write
                    gLeaseClerk.RelinquishLease(chunkId);
                }

                // can't forward to peer...so fail the write-id allocation
                gLogger.Submit(this);
                return;
            }
        }
    }


    if (writeMaster) {
        // Notify the lease clerk that we are doing write.  This is to
        // signal the lease clerk to renew the lease for the chunk when appropriate.
        gLeaseClerk.DoingWrite(chunkId);
    }
    
    status = gChunkManager.AllocateWriteId(this);
    if (status == 0)
        writeIdStr = gChunkServer.GetMyLocation() + " " + 
            boost::lexical_cast<string>(writeId);
    // don't need to forward: we are done
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    if (!needToForward) {
        KFS_LOG_VA_INFO("Write-id alloc: %s", Show().c_str());
        ReadChunkMetadata();
    }
}

int
WriteIdAllocOp::ForwardToPeer(const ServerLocation &loc)
{
    ClientSM *client = static_cast<ClientSM *>(clnt);
    assert(client != NULL);

    RemoteSyncSMPtr peer = client->FindServer(loc);
    
    if (!peer) {
        KFS_LOG_VA_DEBUG("Unable to find syncSM to peer: %s", loc.ToString().c_str());
        // flag the error
        return -EHOSTUNREACH;
    }

    fwdedOp = new WriteIdAllocOp(peer->NextSeqnum(), this);
    // when this op comes, need to notify "this"
    fwdedOp->clnt = this;

    // KFS_LOG_VA_INFO("Sending write to peer: %s",
    // writeFwdOp->Show().c_str());
    KFS_LOG_VA_INFO("Fwd'ing write-id alloc to peer: %s", fwdedOp->Show().c_str());

    SET_HANDLER(this, &WriteIdAllocOp::HandlePeerReply);

    peer->Enqueue(fwdedOp);
    
    return 0;
}

int
WriteIdAllocOp::HandlePeerReply(int code, void *data)
{
    WriteIdAllocOp *op = static_cast<WriteIdAllocOp *> (data);

    if (op->status < 0) {
        status = op->status;
        // return clnt->HandleEvent(EVENT_CMD_DONE, this);
        if (gLeaseClerk.IsLeaseValid(chunkId)) {
            // The write has failed; we don't want to renew the lease.
            // Now, when the client forces a re-allocation, the
            // metaserver will do a version bump; when the node that
            // was dead comes back, we can detect it has missed a write
            gLeaseClerk.RelinquishLease(chunkId);
        }
        KFS_LOG_VA_INFO("write-id alloc failed: %s, code = %d", Show().c_str(), status);        
        gLogger.Submit(this);
        return 0;
    }

    writeIdStr.append(" ");
    writeIdStr.append(op->writeIdStr);

    ReadChunkMetadata();
    return 0;
}

void
WriteIdAllocOp::ReadChunkMetadata()
{
    int res;
    // Now, we are all done pending metadata read
    SET_HANDLER(this, &WriteIdAllocOp::HandleDone);

    // page in the chunk meta-data if needed
    if (!gChunkManager.NeedToReadChunkMetadata(chunkId)) {
        HandleDone(0, NULL);
        return;
    }

    // if the read was successful, the call to read will callback handle-done
    if ((res = gChunkManager.ReadChunkMetadata(chunkId, this)) < 0) {
        KFS_LOG_VA_INFO("Unable read chunk metadata for chunk: %lld; error = %d", chunkId, res);
        status = -EINVAL;
        HandleDone(0, NULL);        
        return;
    }
}

int
WriteIdAllocOp::HandleDone(int code, void *data)
{
    KFS_LOG_VA_INFO("Sending write-id alloc back (status = %d): %s", status, Show().c_str());
    gLogger.Submit(this);
    return 0;
}

void 
WritePrepareOp::Execute()
{
    ServerLocation peerLoc;
    int myPos;

    UpdateCounter(CMD_WRITE_PREPARE);

    SET_HANDLER(this, &WritePrepareOp::HandleDone);
        
    // check if we need to forward anywhere
    bool needToForward = false, writeMaster;

    needToForward = needToForwardWrite(servers, numServers, myPos, peerLoc, true, writeId);
    writeMaster = (myPos == 0);

    if (!gChunkManager.IsValidWriteId(writeId)) {
        status = -EINVAL;
        gLogger.Submit(this);
        return;
    }

    if (!gChunkManager.IsChunkMetadataLoaded(chunkId)) {

        if (gLeaseClerk.IsLeaseValid(chunkId)) {
            // The write-id allocation has failed; we don't want to renew the lease.
            // Now, when the client forces a re-allocation, the
            // metaserver will do a version bump; when the node that
            // was dead comes back, we can detect it has missed a write
            gLeaseClerk.RelinquishLease(chunkId);
        }
        
        KFS_LOG_VA_INFO("Write prepare failed...checksums are not loaded; so lease expired for %ld",
                        chunkId);

        status = -KFS::ELEASEEXPIRED;
        gLogger.Submit(this);
        return;
    }

    if (writeMaster) {
        // if we are the master, check the lease...
        if (!gLeaseClerk.IsLeaseValid(chunkId)) {
            KFS_LOG_VA_INFO("Write prepare failed...as lease expired for %ld",
                             chunkId);
            gLeaseClerk.RelinquishLease(chunkId);
            status = -KFS::ELEASEEXPIRED;
            gLogger.Submit(this);
            return;
        }

        // Notify the lease clerk that we are doing write.  This is to
        // signal the lease clerk to renew the lease for the chunk when appropriate.
        gLeaseClerk.DoingWrite(chunkId);
    }

    if (checksum != 0) {
        uint32_t val = ComputeBlockChecksum(dataBuf, numBytes);

        if (val != checksum) {
            KFS_LOG_VA_INFO("Checksum mismatch: sent = %u, computed = %u for %s",
                            checksum, val, Show().c_str());
            status = -EBADCKSUM;
            // so that the error goes out on a sync
            gChunkManager.SetWriteStatus(writeId, status);
            gLogger.Submit(this);
            return;
        }
    }

    if (needToForward) {
        IOBuffer *clonedData = dataBuf->Clone();

        status = ForwardToPeer(peerLoc, clonedData);
        if (status < 0) {
            delete clonedData;
            // so that the error goes out on a sync
            gChunkManager.SetWriteStatus(writeId, status);

            if (gLeaseClerk.IsLeaseValid(chunkId)) {
                // The write has failed; we don't want to renew the lease.
                // Now, when the client forces a re-allocation, the
                // metaserver will do a version bump; when the node that
                // was dead comes back, we can detect it has missed a write
                gLeaseClerk.RelinquishLease(chunkId);
            }

            // can't forward to peer...so fail the write
            gLogger.Submit(this);
            return;
        }
    }

    // will clone only when the op is good
    writeOp = gChunkManager.CloneWriteOp(writeId);
    UpdateCounter(CMD_WRITE);

    assert(writeOp != NULL);

    writeOp->offset = offset;
    writeOp->numBytes = numBytes;
    writeOp->dataBuf = dataBuf;
    writeOp->wpop = this;
    writeOp->checksums.push_back(checksum);
    dataBuf = NULL;

    writeOp->enqueueTime = time(NULL);

    KFS_LOG_VA_INFO("Writing to chunk=%lld, @offset=%lld, nbytes=%lld, checksum=%u",
                    chunkId, offset, numBytes, checksum);

    status = gChunkManager.WriteChunk(writeOp);
    
    if (status < 0)
        gLogger.Submit(this);
}

int
WritePrepareOp::ForwardToPeer(const ServerLocation &loc, IOBuffer *dataBuf)
{
    ClientSM *client = static_cast<ClientSM *>(clnt);
    assert(client != NULL);

    RemoteSyncSMPtr peer = client->FindServer(loc);
    
    if (!peer) {
        KFS_LOG_VA_INFO("Unable to find syncSM to peer: %s", loc.ToString().c_str());
        // flag the error
        return -EHOSTUNREACH;
    }

    writeFwdOp = new WritePrepareFwdOp(peer->NextSeqnum(), this, dataBuf, loc);

    // KFS_LOG_VA_INFO("Sending write to peer: %s",
    // writeFwdOp->Show().c_str());
    KFS_LOG_VA_DEBUG("Fwd'ing write to peer: %s", writeFwdOp->Show().c_str());

    peer->Enqueue(writeFwdOp);
    
    return 0;
}

int
WritePrepareOp::HandleDone(int code, void *data)
{
    WritePrepareFwdOp *op = static_cast<WritePrepareFwdOp *> (data);

#ifdef DEBUG
    verifyExecutingOnEventProcessor();    
#endif

    if ((op != NULL) && (op->status < 0)) {
        status = op->status;
        if (gLeaseClerk.IsLeaseValid(chunkId)) {
            // The write has failed; we don't want to renew the lease.
            // Now, when the client forces a re-allocation, the
            // metaserver will do a version bump; when the node that
            // was dead comes back, we can detect it has missed a write
            gLeaseClerk.RelinquishLease(chunkId);
        }
    }

    numDone++;

    if ((writeFwdOp == NULL) || (numDone >= 2)) {
        gLogger.Submit(this);
    }
    return 0;
}

void 
WriteSyncOp::Execute()
{
    ServerLocation peerLoc;
    int myPos;
    bool needToWriteMetadata = true;

    UpdateCounter(CMD_WRITE_SYNC);

    KFS_LOG_VA_INFO("Executing write sync: %s", Show().c_str());
    // check if we need to forward anywhere
    bool needToForward = false;
    if (numServers > 0) {
        needToForward = needToForwardWrite(servers, numServers, myPos, peerLoc, true, writeId);
        if (myPos == 0)
            writeMaster = true;
    }

    writeOp = gChunkManager.CloneWriteOp(writeId);
    if (writeOp == NULL) {
        KFS_LOG_VA_INFO("Write sync failed...unable to find write-id: %ld", writeId);
        status = -EINVAL;
        gLogger.Submit(this);
        return;
    }
    
    writeOp->enqueueTime = time(NULL);

    if (writeOp->status < 0) {
        // due to failures with data fwd'ing/checksum errors and such
        status = writeOp->status;
        gLogger.Submit(this);
        return;
    }

    if (!gChunkManager.IsChunkMetadataLoaded(chunkId)) {
        off_t csize;

        gChunkManager.ChunkSize(chunkId, &csize);
        if (csize > 0 && (csize >= (off_t) KFS::CHUNKSIZE)) {
            // the metadata block could be paged out by a previous sync
            needToWriteMetadata = false;
        } else {
            KFS_LOG_VA_INFO("Write sync failed...checksums got paged out?; so lease expired for %ld",
                            chunkId);

            status = -KFS::ELEASEEXPIRED;
            gLogger.Submit(this);
            return;
        }
    }

    if (writeMaster) {
        // if we are the master, check the lease...
        if (!gLeaseClerk.IsLeaseValid(chunkId)) {
            KFS_LOG_VA_INFO("Write sync failed...lease expired for %ld",
                            chunkId);
            status = -KFS::ELEASEEXPIRED;
            gLogger.Submit(this);
            return;
        }

        // Notify the lease clerk that we are doing write.  This is to
        // signal the lease clerk to renew the lease for the chunk when appropriate.
        gLeaseClerk.DoingWrite(chunkId);
    }

    if (needToForward) {
        status = ForwardToPeer(peerLoc);
        if (status < 0) {
            // write can't be forwarded; so give up the lease, so that
            // we can force re-allocation
            gLeaseClerk.RelinquishLease(chunkId);
            // can't forward to peer...so fail the write
            gLogger.Submit(this);
            return;
        }
    }

    // commit writes on local/remote servers
    SET_HANDLER(this, &WriteSyncOp::HandleDone);

    if (!needToWriteMetadata) {
        status = 0;
        HandleDone(0, NULL);
        return;
    }
        
    status = gChunkManager.WriteChunkMetadata(chunkId, this);
    assert(status >= 0);
    if (status < 0)
        HandleDone(0, NULL);
    
    // writeOp->wsop = this;

    // XXX: validate id/version?
    // validate the # of bytes is everything we got....otherwise, fail the op

    // writeOp->Execute();
}

int
WriteSyncOp::ForwardToPeer(const ServerLocation &loc)
{
    ClientSM *client = static_cast<ClientSM *>(clnt);
    assert(client != NULL);

    RemoteSyncSMPtr peer = client->FindServer(loc);
    
    if (!peer) {
        KFS_LOG_VA_INFO("Unable to find syncSM to peer: %s", loc.ToString().c_str());
        // flag the error
        return -EHOSTUNREACH;
    }

    fwdedOp = new WriteSyncOp(peer->NextSeqnum(), chunkId, chunkVersion);
    fwdedOp->numServers = numServers;
    fwdedOp->servers = servers;
    fwdedOp->clnt = this;
    SET_HANDLER(fwdedOp, &WriteSyncOp::HandlePeerReply);

    KFS_LOG_VA_DEBUG("Fwd'ing write-sync to peer: %s", fwdedOp->Show().c_str());

    peer->Enqueue(fwdedOp);
    
    return 0;
}

int 
WriteSyncOp::HandlePeerReply(int code, void *data)
{
    assert(clnt != NULL);
    return clnt->HandleEvent(code, this);
}

int
WriteSyncOp::HandleDone(int code, void *data)
{
    KfsOp *op = static_cast<KfsOp *> (data);

#ifdef DEBUG
    verifyExecutingOnEventProcessor();    
#endif

    if (op && (op->status < 0)) {
        status = op->status;
        KFS_LOG_VA_INFO("Peer (%s) returned: %d", op->Show().c_str(), op->status);
    }

    if ((status < 0) && (gLeaseClerk.IsLeaseValid(chunkId))) {
        // The write has failed; we don't want to renew the lease.
        // Now, when the client forces a re-allocation, the
        // metaserver will do a version bump; when the node that
        // was dead comes back, we can detect it has missed a write
        gLeaseClerk.RelinquishLease(chunkId);
    }

    numDone++;

    if ((fwdedOp == NULL) || (numDone >= 2)) {
        // either no forwarding or local/fwding is also done...so, we are done
        gLogger.Submit(this);
    }
    return 0;
}

void 
WriteOp::Execute()
{
    UpdateCounter(CMD_WRITE);

    status = gChunkManager.WriteChunk(this);

    if (status < 0) {
        assert(wpop != NULL);
        wpop->HandleEvent(EVENT_CMD_DONE, this);
    }
}

void
SizeOp::Execute()
{
    UpdateCounter(CMD_SIZE);

    status = gChunkManager.ChunkSize(chunkId, &size);
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    gLogger.Submit(this);
}

void
GetChunkMetadataOp::Execute()
{
    ChunkInfo_t ci;

    UpdateCounter(CMD_GET_CHUNK_METADATA);

    SET_HANDLER(this, &GetChunkMetadataOp::HandleChunkMetaReadDone);
    if (gChunkManager.ReadChunkMetadata(chunkId, this) < 0) {
        status = -EINVAL;
        gLogger.Submit(this);
    }
}

int
GetChunkMetadataOp::HandleChunkMetaReadDone(int code, void *data)
{
    status = *(int *) data;
    if (status < 0) {
        gLogger.Submit(this);
        return 0;
    }

    uint32_t *checksums = NULL;
    status = gChunkManager.GetChunkChecksums(chunkId, &checksums);
    if ((status == 0) && (checksums != NULL)) {
        chunkVersion = gChunkManager.GetChunkVersion(chunkId);
        gChunkManager.ChunkSize(chunkId, &chunkSize);
        dataBuf = new IOBuffer();
        dataBuf->CopyIn((const char *) checksums, MAX_CHUNK_CHECKSUM_BLOCKS * sizeof(uint32_t));
        numBytesIO = dataBuf->BytesConsumable();
    }
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    gLogger.Submit(this);
    return 0;
}

void
PingOp::Execute()
{
    totalSpace = gChunkManager.GetTotalSpace();
    usedSpace = gChunkManager.GetUsedSpace();
    status = 0;
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    gLogger.Submit(this);
}

void
DumpChunkMapOp::Execute()
{
   // Dump chunk map
   gChunkManager.DumpChunkMap();
   status = 0;
   gLogger.Submit(this);
}

void
StatsOp::Execute()
{
    ostringstream os;

    os << "Num aios: " << globals().diskManager.NumDiskIOOutstanding() << "\r\n";
    os << "Num ops: " << gChunkServer.GetNumOps() << "\r\n";
    globals().counterManager.Show(os);
    stats = os.str();
    status = 0;
    // clnt->HandleEvent(EVENT_CMD_DONE, this);
    gLogger.Submit(this);
}

int
AllocChunkOp::HandleChunkMetaWriteDone(int code, void *data)
{
    gLogger.Submit(this);
    return 0;
}

int
TruncateChunkOp::HandleChunkMetaWriteDone(int code, void *data)
{
    gLogger.Submit(this);
    return 0;
}

int
ChangeChunkVersOp::HandleChunkMetaWriteDone(int code, void *data)
{
    gLogger.Submit(this);
    return 0;
}

///
/// Generate response for an op based on the KFS protocol.
///
void
KfsOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n\r\n";
}

void
SizeOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    if (status < 0) {
        os << "Status: " << status << "\r\n\r\n";
        return;
    }
    os << "Status: " << status << "\r\n";    
    os << "Size: " << size << "\r\n\r\n";
}

void
GetChunkMetadataOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    if (status < 0) {
        os << "Status: " << status << "\r\n\r\n";
        return;
    }
    os << "Status: " << status << "\r\n";    

    os << "Chunk-handle: " << chunkId << "\r\n";
    os << "Chunk-version: " << chunkVersion << "\r\n";
    os << "Size: " << chunkSize << "\r\n";
    os << "Content-length: " << numBytesIO << "\r\n\r\n";
}

void
ReadOp::Response(ostringstream &os)
{
    // DecrementCounter(CMD_READ);

    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    if (status < 0) {
        os << "Status: " << status << "\r\n\r\n";
        return;
    }
    os << "Status: " << status << "\r\n";
    os << "DiskIOtime: " << diskIOTime << "\r\n";
    os << "Checksum-entries: " << checksum.size() << "\r\n";
    if (checksum.size() == 0) {
        os << "Checksums: " << 0 << "\r\n";
    } else {
        os << "Checksums: ";
        for (uint32_t i = 0; i < checksum.size(); i++)
            os << checksum[i] << ' ';
        os << "\r\n";
    }
    os << "Content-length: " << numBytesIO << "\r\n\r\n";
}

void
WriteIdAllocOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    if (status < 0) {
        os << "Status: " << status << "\r\n\r\n";    
        return;
    }
    os << "Status: " << status << "\r\n";
    // os << "Write-id: " << writeId << "\r\n\r\n";
    os << "Write-id: " << writeIdStr << "\r\n\r\n";
}

void
WritePrepareOp::Response(ostringstream &os)
{
    // no reply for a prepare...the reply is covered by sync
    if (1)
        return;

    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    if (status < 0) {
        os << "Status: " << status << "\r\n\r\n";    
        return;
    }
    os << "Status: " << status << "\r\n\r\n";
}

void
SizeOp::Request(ostringstream &os)
{
    os << "SIZE \r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Chunk-handle: " << chunkId << "\r\n";
    os << "Chunk-version: " << chunkVersion << "\r\n\r\n";
}

void
GetChunkMetadataOp::Request(ostringstream &os)
{
    os << "GET_CHUNK_METADATA \r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Chunk-handle: " << chunkId << "\r\n\r\n";
}

void
ReadOp::Request(ostringstream &os)
{
    os << "READ \r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Chunk-handle: " << chunkId << "\r\n";
    os << "Chunk-version: " << chunkVersion << "\r\n";
    os << "Offset: " << offset << "\r\n";
    os << "Num-bytes: " << numBytes << "\r\n\r\n";
}

void
WriteIdAllocOp::Request(ostringstream &os)
{
    os << "WRITE_ID_ALLOC\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Chunk-handle:" << chunkId << "\r\n";
    os << "Chunk-version:" << chunkVersion << "\r\n";
    os << "Offset: " << offset << "\r\n";
    os << "Num-bytes: " << numBytes << "\r\n";
    os << "Num-servers: " << numServers << "\r\n";
    os << "Servers: " << servers << "\r\n\r\n";
}

void
WritePrepareFwdOp::Request(ostringstream &os)
{
    os << "WRITE_PREPARE\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Chunk-handle:" << owner->chunkId << "\r\n";
    os << "Chunk-version:" << owner->chunkVersion << "\r\n";
    os << "Offset: " << owner->offset << "\r\n";
    os << "Num-bytes: " << owner->numBytes << "\r\n";
    os << "Checksum: " << owner->checksum << "\r\n";
    os << "Num-servers: " << owner->numServers << "\r\n";
    os << "Servers: " << owner->servers << "\r\n\r\n";
}

void
WriteSyncOp::Request(ostringstream &os)
{
    os << "WRITE_SYNC\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Chunk-handle:" << chunkId << "\r\n";
    os << "Chunk-version:" << chunkVersion << "\r\n";
    os << "Num-servers: " << numServers << "\r\n";
    os << "Servers: " << servers << "\r\n\r\n";
}

void
WriteSyncOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n\r\n";    
}

void
AllocChunkOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n\r\n";
}

void
HeartbeatOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n";
    os << "Total-space: " << totalSpace << "\r\n";
    os << "Used-space: " << usedSpace << "\r\n";
    os << "Num-chunks: " << numChunks << "\r\n\r\n";
}

void
StaleChunksOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n\r\n";
}

void
ReplicateChunkOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n";
    if (status == 0) {
        os << "File-handle: " << fid << "\r\n";
        os << "Chunk-version: " << chunkVersion << "\r\n";
    }
    os << "\r\n";
}

void
PingOp::Response(ostringstream &os)
{
    ServerLocation loc = gMetaServerSM.GetLocation();

    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n";
    os << "Meta-server-host: " << loc.hostname << "\r\n";
    os << "Meta-server-port: " << loc.port << "\r\n";
    os << "Total-space: " << totalSpace << "\r\n";
    os << "Used-space: " << usedSpace << "\r\n\r\n";
}

void
DumpChunkMapOp::Response(ostringstream &os)
{
    ostringstream v;
    gChunkManager.DumpChunkMap(v);
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n";
    os << "Content-length: " << v.str().length() << "\r\n\r\n";
    if (v.str().length() > 0)
       os << v.str();
}

void
StatsOp::Response(ostringstream &os)
{
    os << "OK\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Status: " << status << "\r\n";
    os << stats << "\r\n";
}

////////////////////////////////////////////////
// Now the handle done's....
////////////////////////////////////////////////

int
SizeOp::HandleDone(int code, void *data)
{
    // notify the owning object that the op finished
    clnt->HandleEvent(EVENT_CMD_DONE, this);
    return 0;
}

int
GetChunkMetadataOp::HandleDone(int code, void *data)
{
    // notify the owning object that the op finished
    clnt->HandleEvent(EVENT_CMD_DONE, this);
    return 0;
}

int
ReplicateChunkOp::HandleDone(int code, void *data)
{
    if (data != NULL)
        chunkVersion = * (kfsSeq_t *) data;
    else
        chunkVersion = -1;
    gLogger.Submit(this);
    return 0;
}

int
WritePrepareFwdOp::HandleDone(int code, void *data)
{
    // data fwding is finished; notify owner
    return owner->HandleEvent(EVENT_CMD_DONE, this);
}

class ReadChunkMetaNotifier {
    int res;
public:
    ReadChunkMetaNotifier(int r) : res(r) { }
    void operator()(KfsOp *op) {
        op->HandleEvent(EVENT_CMD_DONE, &res);
    }
};

int
ReadChunkMetaOp::HandleDone(int code, void *data)
{
    int res = -EINVAL;

    if (code == EVENT_DISK_ERROR) {
        status = -1;
        if (data != NULL) {
            status = *(int *) data;
            KFS_LOG_VA_INFO("Disk error: errno = %d, chunkid = %lld", status, chunkId);
            gChunkManager.ChunkIOFailed(chunkId, status);
            res = status;
        }
    }
    else if (code == EVENT_DISK_READ) {
        IOBuffer *dataBuf = (IOBuffer *) data;
    
        if (dataBuf->BytesConsumable() >= (int) sizeof(DiskChunkInfo_t)) {
            DiskChunkInfo_t dci;
            
            dataBuf->CopyOut((char *) &dci, sizeof(DiskChunkInfo_t));
            res = gChunkManager.SetChunkMetadata(dci, chunkId);
        }
    }
    
    gChunkManager.ReadChunkMetadataDone(chunkId);
    clnt->HandleEvent(EVENT_CMD_DONE, (void *) &res);

    for_each(waiters.begin(), waiters.end(), ReadChunkMetaNotifier(res));

    delete this;
    return 0;
}

WriteOp::~WriteOp()
{
    if (isWriteIdHolder) {
        // track how long it took for the write to finish up:
        // enqueueTime tracks when the last write was done to this
        // writeid
        struct timeval lastWriteTime;

        lastWriteTime.tv_sec = enqueueTime;
        lastWriteTime.tv_usec = 0;
        float timeSpent = ComputeTimeDiff(startTime, lastWriteTime);
        if (timeSpent < 1e-6)
            timeSpent = 0.0;

        if (timeSpent > 5.0) {
            gChunkServer.SendTelemetryReport(CMD_WRITE, timeSpent);
        }
        
        // we don't want write id's to pollute stats
        gettimeofday(&startTime, NULL);
        gCtrWriteDuration.Update(1);
        gCtrWriteDuration.Update(timeSpent);
    }

    delete dataBuf;
    if (rop != NULL) {
        rop->wop = NULL;
        // rop->dataBuf can be non null when read completes but WriteChunk
        // fails, and returns before using this buff.
        // Read op destructor deletes dataBuf.
        delete rop;
    }
    if (diskConnection)
        diskConnection->Close();
}

WriteIdAllocOp::~WriteIdAllocOp()
{
    delete fwdedOp;
}

WritePrepareOp::~WritePrepareOp()
{
    // on a successful prepare, dataBuf should be moved to a write op.
    assert((status != 0) || (dataBuf == NULL));

    delete dataBuf;
    delete writeFwdOp;
    delete writeOp;
}

WriteSyncOp::~WriteSyncOp()
{
    off_t chunkSize = 0;

    delete fwdedOp;
    delete writeOp;

    gChunkManager.ChunkSize(chunkId, &chunkSize);
    if ((chunkSize > 0) && 
        (chunkSize >= (off_t) KFS::CHUNKSIZE)) {
        // we are done with all the writing to this chunk; so, close it
        gChunkManager.CloseChunk(chunkId);
    }
}

void
LeaseRenewOp::Request(ostringstream &os)
{
    os << "LEASE_RENEW\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Chunk-handle:" << chunkId << "\r\n";
    os << "Lease-id: " << leaseId << "\r\n";
    os << "Lease-type: " << leaseType << "\r\n\r\n";
}

int
LeaseRenewOp::HandleDone(int code, void *data)
{
    KfsOp *op = (KfsOp *) data;

    assert(op == this);
    return op->clnt->HandleEvent(EVENT_CMD_DONE, data);
}

void
LeaseRelinquishOp::Request(ostringstream &os)
{
    os << "LEASE_RELINQUISH\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Chunk-handle:" << chunkId << "\r\n";
    os << "Lease-id: " << leaseId << "\r\n";
    os << "Lease-type: " << leaseType << "\r\n\r\n";
}

int
LeaseRelinquishOp::HandleDone(int code, void *data)
{
    KfsOp *op = (KfsOp *) data;

    assert(op == this);
    delete this;
    return 0;
}

void
CorruptChunkOp::Request(ostringstream &os)
{
    os << "CORRUPT_CHUNK\r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "File-handle:" << fid << "\r\n";
    os << "Chunk-handle:" << chunkId << "\r\n\r\n";
}

int
CorruptChunkOp::HandleDone(int code, void *data)
{
    // Thank you metaserver for replying :-)
    delete this;
    return 0;
}

class PrintChunkInfo {
    ostringstream &os;
public:
    PrintChunkInfo(ostringstream &o) : os(o) { }
    void operator() (ChunkInfo_t &c) {
        os << c.fileId << ' ';
        os << c.chunkId << ' ';
        os << c.chunkVersion << ' ';
    }
};

void
HelloMetaOp::Request(ostringstream &os)
{
    ostringstream chunkInfo;

    os << "HELLO \r\n";
    os << "Version: " << KFS_VERSION_STR << "\r\n";
    os << "Cseq: " << seq << "\r\n";
    os << "Chunk-server-name: " << myLocation.hostname << "\r\n";
    os << "Chunk-server-port: " << myLocation.port << "\r\n";
    os << "Cluster-key: " << clusterKey << "\r\n";
    os << "MD5Sum: " << md5sum << "\r\n";
    os << "Rack-id: " << rackId << "\r\n";
    os << "Total-space: " << totalSpace << "\r\n";
    os << "Used-space: " << usedSpace << "\r\n";

    // now put in the chunk information
    os << "Num-chunks: " << chunks.size() << "\r\n";
    
    // figure out the content-length first...
    for_each(chunks.begin(), chunks.end(), PrintChunkInfo(chunkInfo));

    os << "Content-length: " << chunkInfo.str().length() << "\r\n\r\n";
    os << chunkInfo.str().c_str();
}

// timeout op to the event processor going
void
TimeoutOp::Execute()
{
    gChunkManager.Timeout();
    gLeaseClerk.Timeout();
    // do not delete "this" since it is either a member variable of
    // the ChunkManagerTimeoutImpl or a static object.  
    // bump the seq # so we know how many times it got executed
    seq++;
}

void
KillRemoteSyncOp::Execute()
{
    RemoteSyncSM *remoteSyncSM = static_cast<RemoteSyncSM *>(clnt);
    assert(remoteSyncSM != NULL);

    remoteSyncSM->Finish();
}

void
HelloMetaOp::Execute()
{
    totalSpace = gChunkManager.GetTotalSpace();
    usedSpace = gChunkManager.GetUsedSpace();
    gChunkManager.GetHostedChunks(chunks);
    status = 0;
    gLogger.Submit(this);
}


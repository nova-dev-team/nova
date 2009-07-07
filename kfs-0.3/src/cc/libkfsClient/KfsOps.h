//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: KfsOps.h 291 2009-02-27 22:04:13Z sriramsrao $ 
//
// Created 2006/05/24
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
//
//----------------------------------------------------------------------------

#ifndef _LIBKFSCLIENT_KFSOPS_H
#define _LIBKFSCLIENT_KFSOPS_H

#include <algorithm>
#include <string>
#include <sstream>
#include <vector>

#include "common/kfstypes.h"
#include "KfsAttr.h"

#include "common/properties.h"

namespace KFS {

enum KfsOp_t {
    CMD_UNKNOWN,
    // Meta-data server RPCs
    CMD_GETALLOC,
    CMD_GETLAYOUT,
    CMD_ALLOCATE,
    CMD_TRUNCATE,
    CMD_LOOKUP,
    CMD_MKDIR,
    CMD_RMDIR,
    CMD_READDIR,
    CMD_READDIRPLUS,
    CMD_GETDIRSUMMARY,
    CMD_CREATE,
    CMD_REMOVE,
    CMD_RENAME,
    CMD_LEASE_ACQUIRE,
    CMD_LEASE_RENEW,
    CMD_LEASE_RELINQUISH,
    CMD_CHANGE_FILE_REPLICATION,
    CMD_DUMP_CHUNKTOSERVERMAP,
    CMD_UPSERVERS,
    // Chunkserver RPCs
    CMD_OPEN,
    CMD_CLOSE,
    CMD_READ,
    CMD_WRITE_ID_ALLOC,
    CMD_WRITE_PREPARE,
    CMD_WRITE_SYNC,
    CMD_SIZE,
    CMD_GET_CHUNK_METADATA,
    CMD_NCMDS,
    CMD_DUMP_CHUNKMAP
};

struct KfsOp {
    KfsOp_t op;
    kfsSeq_t   seq;
    int32_t   status;
    uint32_t  checksum; // a checksum over the data
    size_t    contentLength;
    size_t    contentBufLen;
    char      *contentBuf;
    KfsOp (KfsOp_t o, kfsSeq_t s) :
        op(o), seq(s), status(0), checksum(0), contentLength(0),
        contentBufLen(0), contentBuf(NULL)
    {

    }
    // to allow dynamic-type-casting, make the destructor virtual
    virtual ~KfsOp() {
        if (contentBuf != NULL)
            delete [] contentBuf;
    }
    void AttachContentBuf(const char *buf, size_t len) {
        AttachContentBuf((char *) buf, len);
    }

    void AttachContentBuf(char *buf, size_t len) {
        contentBuf = buf;
        contentBufLen = len;
    }
    void ReleaseContentBuf() {
        contentBuf = NULL;
        contentBufLen = 0;
    }
    // Build a request RPC that can be sent to the server
    virtual void Request(std::ostringstream &os) = 0;

    // Common parsing code: parse the response from string and fill
    // that into a properties structure.
    void ParseResponseHeaderCommon(std::string &resp, Properties &prop);

    // Parse a response header from the server: This does the
    // default parsing of OK/Cseq/Status/Content-length.
    virtual void ParseResponseHeader(char *buf, int len);

    // Return information about op that can printed out for debugging.
    virtual std::string Show() const = 0;
};

struct CreateOp : public KfsOp {
    kfsFileId_t parentFid; // input parent file-id
    const char *filename;
    kfsFileId_t fileId; // result
    int numReplicas; // desired degree of replication
    bool exclusive; // O_EXCL flag
    CreateOp(kfsSeq_t s, kfsFileId_t p, const char *f, int n, bool e) :
        KfsOp(CMD_CREATE, s), parentFid(p), filename(f),
        numReplicas(n), exclusive(e)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "create: " << filename << " (parentfid = " << parentFid << ")";
        return os.str();
    }
};

struct RemoveOp : public KfsOp {
    kfsFileId_t parentFid; // input parent file-id
    const char *filename;
    const char *pathname;
    RemoveOp(kfsSeq_t s, kfsFileId_t p, const char *f, const char *pn) :
        KfsOp(CMD_REMOVE, s), parentFid(p), filename(f), pathname(pn)
    {

    }
    void Request(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;

        os << "remove: " << filename << " (parentfid = " << parentFid << ")";
        return os.str();
    }
};

struct MkdirOp : public KfsOp {
    kfsFileId_t parentFid; // input parent file-id
    const char *dirname;
    kfsFileId_t fileId; // result
    MkdirOp(kfsSeq_t s, kfsFileId_t p, const char *d) :
        KfsOp(CMD_MKDIR, s), parentFid(p), dirname(d)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "mkdir: " << dirname << " (parentfid = " << parentFid << ")";
        return os.str();
    }
};

struct RmdirOp : public KfsOp {
    kfsFileId_t parentFid; // input parent file-id
    const char *dirname;
    const char *pathname; // input: full pathname
    RmdirOp(kfsSeq_t s, kfsFileId_t p, const char *d, const char *pn) :
        KfsOp(CMD_RMDIR, s), parentFid(p), dirname(d), pathname(pn)
    {

    }
    void Request(std::ostringstream &os);
    // default parsing of OK/Cseq/Status/Content-length will suffice.

    std::string Show() const {
        std::ostringstream os;

        os << "rmdir: " << dirname << " (parentfid = " << parentFid << ")";
        return os.str();
    }
};

struct RenameOp : public KfsOp {
    kfsFileId_t parentFid; // input parent file-id
    const char *oldname;  // old file name/dir
    const char *newpath;  // new path to be renamed to
    const char *oldpath;  // old path (starting from /)
    bool overwrite; // set if the rename can overwrite newpath
    RenameOp(kfsSeq_t s, kfsFileId_t p, const char *o,
             const char *n, const char *op, bool c) :
        KfsOp(CMD_RENAME, s), parentFid(p), oldname(o),
        newpath(n), oldpath(op), overwrite(c)
    {

    }
    void Request(std::ostringstream &os);

    // default parsing of OK/Cseq/Status/Content-length will suffice.

    std::string Show() const {
        std::ostringstream os;

        if (overwrite)
            os << "rename_overwrite: ";
        else
            os << "rename: ";
        os << " old=" << oldname << " (parentfid = " << parentFid << ")";
        os << " new = " << newpath;
        return os.str();
    }
};

struct ReaddirOp : public KfsOp {
    kfsFileId_t fid; // fid of the directory
    int numEntries; // # of entries in the directory
    ReaddirOp(kfsSeq_t s, kfsFileId_t f):
        KfsOp(CMD_READDIR, s), fid(f), numEntries(0)
    {

    }
    void Request(std::ostringstream &os);
    // This will only extract out the default+num-entries.  The actual
    // dir. entries are in the content-length portion of things
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "readdir: fid = " << fid;
        return os.str();
    }
};

struct DumpChunkServerMapOp : public KfsOp {
	DumpChunkServerMapOp(kfsSeq_t s):
		KfsOp(CMD_DUMP_CHUNKTOSERVERMAP, s)
	{
	}
	void Request(std::ostringstream &os);
	void ParseResponseHeader(char *buf, int len);
	std::string Show() const {
		std::ostringstream os;
		os << "dumpchunktoservermap";
		return os.str();
	}
};

struct UpServersOp : public KfsOp {
    UpServersOp(kfsSeq_t s):
        KfsOp(CMD_UPSERVERS, s)
    {
    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;
        os << "upservers";
        return os.str();
    }
};

struct DumpChunkMapOp : public KfsOp {
	DumpChunkMapOp(kfsSeq_t s):
		KfsOp(CMD_DUMP_CHUNKMAP, s)
	{
	}
	void Request(std::ostringstream &os);
	void ParseResponseHeader(char *buf, int len);
	std::string Show() const {
		std::ostringstream os;
		os << "dumpchunkmap";
		return os.str();
	}
};

struct ReaddirPlusOp : public KfsOp {
    kfsFileId_t fid; // fid of the directory
    int numEntries; // # of entries in the directory
    ReaddirPlusOp(kfsSeq_t s, kfsFileId_t f):
        KfsOp(CMD_READDIRPLUS, s), fid(f), numEntries(0)
    {

    }
    void Request(std::ostringstream &os);
    // This will only extract out the default+num-entries.  The actual
    // dir. entries are in the content-length portion of things
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "readdirplus: fid = " << fid;
        return os.str();
    }
};

struct GetDirSummaryOp : public KfsOp {
    kfsFileId_t fid; // fid of the directory
    uint64_t numFiles; // output
    uint64_t numBytes; // output
    GetDirSummaryOp(kfsSeq_t s, kfsFileId_t f):
        KfsOp(CMD_GETDIRSUMMARY, s), fid(f), numFiles(0), numBytes(0)
    {

    }
    void Request(std::ostringstream &os);
    // This will only extract out the default+num-entries.  The actual
    // dir. entries are in the content-length portion of things
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "getdirsummary: fid = " << fid;
        return os.str();
    }
};

// Lookup the attributes of a file in a directory
struct LookupOp : public KfsOp {
    kfsFileId_t parentFid; // fid of the parent dir
    const char *filename; // file in the dir
    KfsServerAttr fattr; // result
    LookupOp(kfsSeq_t s, kfsFileId_t p, const char *f) :
        KfsOp(CMD_LOOKUP, s), parentFid(p), filename(f)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);

    std::string Show() const {
        std::ostringstream os;

        os << "lookup: " << filename << " (parentfid = " << parentFid << ")";
        return os.str();
    }
};

// Lookup the attributes of a file relative to a root dir.
struct LookupPathOp : public KfsOp {
    kfsFileId_t rootFid; // fid of the root dir
    const char *filename; // path relative to root
    KfsServerAttr fattr; // result
    LookupPathOp(kfsSeq_t s, kfsFileId_t r, const char *f) :
        KfsOp(CMD_LOOKUP, s), rootFid(r), filename(f)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);

    std::string Show() const {
        std::ostringstream os;

        os << "lookup_path: " << filename << " (rootFid = " << rootFid << ")";
        return os.str();
    }
};

/// Get the allocation information for a chunk in a file.
struct GetAllocOp: public KfsOp {
    kfsFileId_t fid;
    off_t fileOffset;
    kfsChunkId_t chunkId; // result
    int64_t chunkVersion; // result
    // result: where the chunk is hosted name/port
    std::vector<ServerLocation> chunkServers;
    std::string filename; // input
    GetAllocOp(kfsSeq_t s, kfsFileId_t f, off_t o) :
        KfsOp(CMD_GETALLOC, s), fid(f), fileOffset(o)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "getalloc: fid=" << fid << " offset: " << fileOffset;
        return os.str();
    }
};


struct ChunkLayoutInfo {
    ChunkLayoutInfo() : fileOffset(-1), chunkId(0) { };
    off_t fileOffset;
    kfsChunkId_t chunkId; // result
    int64_t chunkVersion; // result
    std::vector<ServerLocation> chunkServers; // where the chunk lives
};

/// Get the layout information for all chunks in a file.
struct GetLayoutOp: public KfsOp {
    kfsFileId_t fid;
    int numChunks;
    std::vector<ChunkLayoutInfo> chunks;
    GetLayoutOp(kfsSeq_t s, kfsFileId_t f) :
        KfsOp(CMD_GETLAYOUT, s), fid(f)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);
    int ParseLayoutInfo();
    std::string Show() const {
        std::ostringstream os;

        os << "getlayout: fid=" << fid;
        return os.str();
    }
};

// Get the chunk metadata (aka checksums) stored on the chunkservers
struct GetChunkMetadataOp: public KfsOp {
    kfsChunkId_t chunkId;
    GetChunkMetadataOp(kfsSeq_t s, kfsChunkId_t c) :
        KfsOp(CMD_GET_CHUNK_METADATA, s), chunkId(c) { }
    void Request(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;

        os << "get chunk metadata: chunkId=" << chunkId;
        return os.str();
    }
};

struct AllocateOp : public KfsOp {
    kfsFileId_t fid;
    off_t fileOffset;
    std::string pathname; // input: the full pathname corresponding to fid
    kfsChunkId_t chunkId; // result
    int64_t chunkVersion; // result---version # for the chunk
    std::string clientHost; // our hostname
    // where is the chunk hosted name/port
    ServerLocation masterServer; // master for running the write transaction
    std::vector<ServerLocation> chunkServers;
    AllocateOp(kfsSeq_t s, kfsFileId_t f, const std::string &p) :
        KfsOp(CMD_ALLOCATE, s), fid(f), fileOffset(0), pathname(p)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);
    string Show() const {
        std::ostringstream os;

        os << "allocate: fid=" << fid << " offset: " << fileOffset;
        return os.str();
    }
};

struct TruncateOp : public KfsOp {
    const char *pathname;
    kfsFileId_t fid;
    off_t fileOffset;
    TruncateOp(kfsSeq_t s, const char *p, kfsFileId_t f, off_t o) :
        KfsOp(CMD_TRUNCATE, s), pathname(p), fid(f), fileOffset(o)
    {

    }
    void Request(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;

        os << "truncate: fid=" << fid << " offset: " << fileOffset;
        return os.str();
    }
};

struct OpenOp : public KfsOp {
    kfsChunkId_t chunkId;
    int openFlags;  // either O_RDONLY, O_WRONLY or O_RDWR
    OpenOp(kfsSeq_t s, kfsChunkId_t c) :
        KfsOp(CMD_OPEN, s), chunkId(c)
    {

    }
    void Request(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;

        os << "open: chunkid=" << chunkId;
        return os.str();
    }
};

struct CloseOp : public KfsOp {
    kfsChunkId_t chunkId;
    CloseOp(kfsSeq_t s, kfsChunkId_t c) :
        KfsOp(CMD_CLOSE, s), chunkId(c)
    {

    }
    void Request(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;

        os << "close: chunkid=" << chunkId;
        return os.str();
    }
};

// used for retrieving a chunk's size
struct SizeOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t chunkVersion;
    off_t     size; /* result */
    SizeOp(kfsSeq_t s, kfsChunkId_t c, int64_t v) :
        KfsOp(CMD_SIZE, s), chunkId(c), chunkVersion(v)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "size: chunkid=" << chunkId << " version=" << chunkVersion;
        return os.str();
    }
};


struct ReadOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t      chunkVersion; /* input */
    off_t 	 offset;   /* input */
    size_t 	 numBytes; /* input */
    struct timeval submitTime; /* when the client sent the request to the server */
    std::vector<uint32_t> checksums; /* checksum for each 64KB block */
    float   diskIOTime; /* as reported by the server */
    float   elapsedTime; /* as measured by the client */

    ReadOp(kfsSeq_t s, kfsChunkId_t c, int64_t v) :
        KfsOp(CMD_READ, s), chunkId(c), chunkVersion(v),
        offset(0), numBytes(0), diskIOTime(0.0), elapsedTime(0.0)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);

    std::string Show() const {
        std::ostringstream os;

        os << "read: chunkid=" << chunkId << " version=" << chunkVersion;
        os << " offset=" << offset << " numBytes=" << numBytes;
        os << " checksum = " << checksum;
        return os.str();
    }
};

// op that defines the write that is going to happen
struct WriteIdAllocOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t      chunkVersion; /* input */
    off_t 	 offset;   /* input */
    size_t 	 numBytes; /* input */
    std::string	 writeIdStr;  /* output */
    std::vector<ServerLocation> chunkServerLoc;
    WriteIdAllocOp(kfsSeq_t s, kfsChunkId_t c, int64_t v, off_t o, size_t n) :
        KfsOp(CMD_WRITE_ID_ALLOC, s), chunkId(c), chunkVersion(v),
        offset(o), numBytes(n)
    {

    }
    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "write-id-alloc: chunkid=" << chunkId << " version=" << chunkVersion;
        return os.str();
    }
};

struct WriteInfo {
    ServerLocation serverLoc;
    int64_t	 writeId;
    WriteInfo() : writeId(-1) { }
    WriteInfo(ServerLocation loc, int64_t w) :
        serverLoc(loc), writeId(w) { }
    WriteInfo & operator = (const WriteInfo &other) {
        serverLoc = other.serverLoc;
        writeId = other.writeId;
        return *this;
    }
    std::string Show() const {
        std::ostringstream os;

        os << " location= " << serverLoc.ToString() << " writeId=" << writeId;
        return os.str();
    }
};

class ShowWriteInfo {
    std::ostringstream &os;
public:
    ShowWriteInfo(std::ostringstream &o) : os(o) { }
    void operator() (WriteInfo w) {
        os << w.Show() << ' ';
    }
};

struct WritePrepareOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t      chunkVersion; /* input */
    off_t 	 offset;   /* input */
    size_t 	 numBytes; /* input */
    std::vector<WriteInfo> writeInfo; /* input */
    WritePrepareOp(kfsSeq_t s, kfsChunkId_t c, int64_t v, std::vector<WriteInfo> &w) :
        KfsOp(CMD_WRITE_PREPARE, s), chunkId(c), chunkVersion(v),
        offset(0), numBytes(0), writeInfo(w)
    {

    }
    void Request(std::ostringstream &os);
    // void ParseResponseHeader(char *buf, int len);
    std::string Show() const {
        std::ostringstream os;

        os << "write-prepare: chunkid=" << chunkId << " version=" << chunkVersion;
        os << " offset=" << offset << " numBytes=" << numBytes;
        for_each(writeInfo.begin(), writeInfo.end(), ShowWriteInfo(os));
        return os.str();
    }
};

struct WriteSyncOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t chunkVersion;
    std::vector<WriteInfo> writeInfo;
    WriteSyncOp() : KfsOp(CMD_WRITE_SYNC, 0) { }
    WriteSyncOp(kfsSeq_t s, kfsChunkId_t c, int64_t v, std::vector<WriteInfo> &w) :
        KfsOp(CMD_WRITE_SYNC, s), chunkId(c), chunkVersion(v), writeInfo(w)
    { }
    void Init(kfsSeq_t s, kfsChunkId_t c, int64_t v, std::vector<WriteInfo> &w) {
        seq = s;
        chunkId = c;
        chunkVersion = v;
        writeInfo = w;
    }
    void Request(std::ostringstream &os);
    std::string Show() const {
        std::ostringstream os;

        os << "write-sync: chunkid=" << chunkId << " version=" << chunkVersion;
	std::for_each(writeInfo.begin(), writeInfo.end(), ShowWriteInfo(os));
        return os.str();
    }
};

struct LeaseAcquireOp : public KfsOp {
    kfsChunkId_t chunkId; // input
    const char *pathname; // input    
    int64_t leaseId; // output
    LeaseAcquireOp(kfsSeq_t s, kfsChunkId_t c, const char *p) :
        KfsOp(CMD_LEASE_ACQUIRE, s), chunkId(c), pathname(p), leaseId(-1)
    {

    }

    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);

    std::string Show() const {
        std::ostringstream os;
        os << "lease-acquire: chunkid=" << chunkId;
        return os.str();
    }
};

struct LeaseRenewOp : public KfsOp {
    kfsChunkId_t chunkId; // input
    int64_t leaseId; // input
    const char *pathname; // input    
    LeaseRenewOp(kfsSeq_t s, kfsChunkId_t c, int64_t l, const char *p) :
        KfsOp(CMD_LEASE_RENEW, s), chunkId(c), leaseId(l), pathname(p)
    {

    }

    void Request(std::ostringstream &os);
    // default parsing of status is sufficient

    std::string Show() const {
        std::ostringstream os;
        os << "lease-renew: chunkid=" << chunkId << " leaseId=" << leaseId;
        return os.str();
    }
};

// Whenever we want to give up a lease early, we notify the metaserver
// using this op.
struct LeaseRelinquishOp : public KfsOp {
    kfsChunkId_t chunkId;
    int64_t leaseId;
    std::string leaseType;
    LeaseRelinquishOp(kfsSeq_t s, kfsChunkId_t c, int64_t l) :
        KfsOp(CMD_LEASE_RELINQUISH, s), chunkId(c), leaseId(l)
    {

    }
    void Request(std::ostringstream &os);
    // defaut parsing of status is sufficient
    std::string Show() const {
        std::ostringstream os;

        os << "lease-relinquish: " << " chunkid = " << chunkId;
        os << " leaseId: " << leaseId << " type: " << leaseType;
        return os.str();
    }
};

struct ChangeFileReplicationOp : public KfsOp {
    kfsFileId_t fid; // input
    int16_t numReplicas; // desired replication
    ChangeFileReplicationOp(kfsSeq_t s, kfsFileId_t f, int16_t r) :
        KfsOp(CMD_CHANGE_FILE_REPLICATION, s), fid(f), numReplicas(r)
    {

    }

    void Request(std::ostringstream &os);
    void ParseResponseHeader(char *buf, int len);

    std::string Show() const {
        std::ostringstream os;
        os << "change-file-replication: fid=" << fid
           << " # of replicas: " << numReplicas;
        return os.str();
    }
};

}

#endif // _LIBKFSCLIENT_KFSOPS_H

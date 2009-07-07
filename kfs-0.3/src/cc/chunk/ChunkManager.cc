//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: ChunkManager.cc 315 2009-04-22 18:58:19Z sjakub $ 
//
// Created 2006/03/28
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

extern "C" {
#include <dirent.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/statvfs.h>
}

#include "common/log.h"
#include "common/kfstypes.h"

#include "ChunkManager.h"
#include "ChunkServer.h"
#include "MetaServerSM.h"
#include "LeaseClerk.h"
#include "Utils.h"

#include "libkfsIO/Counter.h"
#include "libkfsIO/Checksum.h"
#include "libkfsIO/Globals.h"

#include <fstream>
#include <sstream>
#include <algorithm>
#include <string>
#include <set>

#include <boost/lexical_cast.hpp>

using std::ofstream;
using std::ifstream;
using std::istringstream;
using std::ostringstream;
using std::ios_base;
using std::list;
using std::min;
using std::max;
using std::endl;
using std::find_if;
using std::string;
using std::vector;
using std::set;

using namespace KFS;
using namespace KFS::libkfsio;

ChunkManager KFS::gChunkManager;

// Cleanup fds on which no I/O has been done for the past N secs
const int INACTIVE_FDS_CLEANUP_INTERVAL_SECS = 300;

// The # of fd's that we allow to be open before cleanup kicks in.
// This value will be set to : # of files that the process can open / 2
int OPEN_FDS_LOW_WATERMARK = 0;

ChunkManager::ChunkManager()
{
    mTotalSpace = mUsedSpace = 0;
    mNumChunks = 0;
    mChunkManagerTimeoutImpl = new ChunkManagerTimeoutImpl(this);
    // we want a timeout once in 10 secs
    // mChunkManagerTimeoutImpl->SetTimeoutInterval(10 * 1000);
    mIsChunkTableDirty = false;
    mLastDriveChosen = -1;
    time_t now = time(NULL);
    srand48(now);
}

ChunkManager::~ChunkManager()
{
    ChunkInfoHandle_t *cih;

    for (CMI iter = mChunkTable.begin(); iter != mChunkTable.end(); ++iter) {
        cih = iter->second;
        delete cih;
    }
    mChunkTable.clear();
    globals().netManager.UnRegisterTimeoutHandler(mChunkManagerTimeoutImpl);
    delete mChunkManagerTimeoutImpl;
}

void 
ChunkManager::Init(const vector<string> &chunkDirs, int64_t totalSpace)
{
    mTotalSpace = totalSpace;
    for (uint32_t i = 0; i < chunkDirs.size(); i++) {
        ChunkDirInfo_t c;

        c.dirname = chunkDirs[i];
        mChunkDirs.push_back(c);
    }
    // force a stat of the dirs and update space usage counts
    GetTotalSpace();
}

int
ChunkManager::AllocChunk(kfsFileId_t fileId, kfsChunkId_t chunkId, 
                         kfsSeq_t chunkVersion,
                         bool isBeingReplicated)
{
    string s, chunkdir;
    int fd;
    ChunkInfoHandle_t *cih;
    CMI tableEntry = mChunkTable.find(chunkId);

    mIsChunkTableDirty = true;

    if (tableEntry != mChunkTable.end()) {
        cih = tableEntry->second;
        ChangeChunkVers(cih->chunkInfo.fileId, cih->chunkInfo.chunkId, chunkVersion);
        return 0;
    }

    // Find the directory to use
    chunkdir = GetDirForChunk();
    if (chunkdir == "") {
        KFS_LOG_VA_INFO("No directory has space to host chunk %ld", chunkId);
        return -ENOSPC;
    }
        
    s = MakeChunkPathname(chunkdir, fileId, chunkId, chunkVersion);
    
    KFS_LOG_VA_INFO("Creating chunk: %s", s.c_str());

    CleanupInactiveFds();

    if ((fd = creat(s.c_str(), S_IRUSR | S_IWUSR)) < 0) {
        perror("Create failed: ");
        return -KFS::ESERVERBUSY;
    }
    close(fd);

    mNumChunks++;

    cih = new ChunkInfoHandle_t();
    cih->chunkInfo.Init(fileId, chunkId, chunkVersion);
    cih->chunkInfo.SetDirname(chunkdir);
    cih->isBeingReplicated = isBeingReplicated;
    mChunkTable[chunkId] = cih;
    return 0;
}

int
ChunkManager::DeleteChunk(kfsChunkId_t chunkId)
{
    string s;
    ChunkInfoHandle_t *cih;
    CMI tableEntry = mChunkTable.find(chunkId);

    if (tableEntry == mChunkTable.end()) 
        return -EBADF;

    mIsChunkTableDirty = true;

    cih = tableEntry->second;

    s = MakeChunkPathname(cih);
    unlink(s.c_str());

    KFS_LOG_VA_INFO("Deleting chunk: %s", s.c_str());

    mNumChunks--;
    assert(mNumChunks >= 0);
    if (mNumChunks < 0)
        mNumChunks = 0;

    UpdateDirSpace(cih, -cih->chunkInfo.chunkSize);

    mUsedSpace -= cih->chunkInfo.chunkSize;
    mChunkTable.erase(chunkId);
    delete cih;
    return 0;
}

void
ChunkManager::DumpChunkMap()
{
    ChunkInfoHandle_t *cih;
    ofstream ofs;

    ofs.open("chunkdump.txt");
    // Dump chunk map in the format of
    // chunkID fileID chunkSize
    for (CMI tableEntry = mChunkTable.begin(); tableEntry != mChunkTable.end();
         ++tableEntry) {
        cih = tableEntry->second;
        ofs << cih->chunkInfo.chunkId << " " << cih->chunkInfo.fileId << " " << cih->chunkInfo.chunkSize << endl;
    }

    ofs.flush();
    ofs.close();
}

void
ChunkManager::DumpChunkMap(ostringstream &ofs)
{
   ChunkInfoHandle_t *cih;

   // Dump chunk map in the format of
   // chunkID fileID chunkSize
   for (CMI tableEntry = mChunkTable.begin(); tableEntry != mChunkTable.end();
       ++tableEntry) {
       cih = tableEntry->second;
       ofs << cih->chunkInfo.chunkId << " " << cih->chunkInfo.fileId << " " << cih->chunkInfo.chunkSize << endl;
   }
}

int
ChunkManager::WriteChunkMetadata(kfsChunkId_t chunkId, KfsOp *cb)
{
    CMI tableEntry = mChunkTable.find(chunkId);
    int res;

    if (tableEntry == mChunkTable.end()) 
        return -EBADF;

    ChunkInfoHandle_t *cih = tableEntry->second;
    WriteChunkMetaOp *wcm = new WriteChunkMetaOp(chunkId, cb);
    DiskConnection *d = SetupDiskConnection(chunkId, wcm);
    if (d == NULL)
        return -KFS::ESERVERBUSY;

    wcm->diskConnection.reset(d);
    wcm->dataBuf = new IOBuffer();
    cih->chunkInfo.Serialize(wcm->dataBuf);
    cih->lastIOTime = time(0);

    res = wcm->diskConnection->Write(0, wcm->dataBuf->BytesConsumable(), wcm->dataBuf);
    if (res < 0) {
        delete wcm;
    }
    return res >= 0 ? 0 : res;
}

int
ChunkManager::ReadChunkMetadata(kfsChunkId_t chunkId, KfsOp *cb)
{
    CMI tableEntry = mChunkTable.find(chunkId);
    int res = 0;

    if (tableEntry == mChunkTable.end()) 
        return -EBADF;

    ChunkInfoHandle_t *cih = tableEntry->second;

    cih->lastIOTime = time(0);
    if (cih->chunkInfo.AreChecksumsLoaded())
        return cb->HandleEvent(EVENT_CMD_DONE, (void *) &res);

    if (cih->isMetadataReadOngoing) {
        // if we have issued a read request for this chunk's metadata,
        // don't submit another one; otherwise, we will simply drive
        // up memory usage for useless IO's
        cih->readChunkMetaOp->AddWaiter(cb);
        return 0;
    }

    ReadChunkMetaOp *rcm = new ReadChunkMetaOp(chunkId, cb);
    DiskConnection *d = SetupDiskConnection(chunkId, rcm);
    if (d == NULL)
        return -KFS::ESERVERBUSY;

    rcm->diskConnection.reset(d);

    res = rcm->diskConnection->Read(0, KFS_CHUNK_HEADER_SIZE);
    if (res < 0) {
        delete rcm;
    }

    cih->isMetadataReadOngoing = true;
    cih->readChunkMetaOp = rcm;
    
    return res >= 0 ? 0 : res;
}

void
ChunkManager::ReadChunkMetadataDone(kfsChunkId_t chunkId)
{
    CMI tableEntry = mChunkTable.find(chunkId);

    if (tableEntry == mChunkTable.end()) 
        return;

    ChunkInfoHandle_t *cih = tableEntry->second;

    cih->lastIOTime = time(0);
    cih->isMetadataReadOngoing = false;
    cih->readChunkMetaOp = NULL;
}

int
ChunkManager::SetChunkMetadata(const DiskChunkInfo_t &dci, kfsChunkId_t chunkId)
{
    ChunkInfoHandle_t *cih;
    int res;

    if ((res = dci.Validate(chunkId)) < 0) {
        KFS_LOG_VA_ERROR("chunk metadata validation mismatch on chunk %ld", chunkId);
        NotifyMetaCorruptedChunk(chunkId);
        StaleChunk(chunkId);
        return res;
    }

    if (GetChunkInfoHandle(dci.chunkId, &cih) < 0)
        return -EBADF;

    cih->chunkInfo.SetChecksums(dci.chunkBlockChecksum);

    return 0;
}

void
ChunkManager::MarkChunkStale(ChunkInfoHandle_t *cih)
{
    string s = MakeChunkPathname(cih);
    string staleChunkPathname = MakeStaleChunkPathname(cih);
    
    rename(s.c_str(), staleChunkPathname.c_str());
    KFS_LOG_VA_INFO("Moving chunk %ld to staleChunks dir", cih->chunkInfo.chunkId);            
}

int
ChunkManager::StaleChunk(kfsChunkId_t chunkId)
{
    ChunkInfoHandle_t *cih;
    CMI tableEntry = mChunkTable.find(chunkId);

    if (tableEntry == mChunkTable.end()) 
        return -EBADF;

    mIsChunkTableDirty = true;

    cih = tableEntry->second;

    MarkChunkStale(cih);

    mNumChunks--;
    assert(mNumChunks >= 0);
    if (mNumChunks < 0)
        mNumChunks = 0;

    UpdateDirSpace(cih, -cih->chunkInfo.chunkSize);
    
    mUsedSpace -= cih->chunkInfo.chunkSize;
    mChunkTable.erase(chunkId);
    delete cih;
    return 0;
}

int
ChunkManager::TruncateChunk(kfsChunkId_t chunkId, off_t chunkSize)
{
    string chunkPathname;
    ChunkInfoHandle_t *cih;
    int res;
    uint32_t lastChecksumBlock;
    CMI tableEntry = mChunkTable.find(chunkId);

    // the truncated size should not exceed chunk size.
    if (chunkSize > (off_t) KFS::CHUNKSIZE)
        return -EINVAL;

    if (tableEntry == mChunkTable.end())
        return -EBADF;

    mIsChunkTableDirty = true;

    cih = tableEntry->second;
    chunkPathname = MakeChunkPathname(cih);
    
    res = truncate(chunkPathname.c_str(), chunkSize);
    if (res < 0) {
        res = errno;
        return -res;
    }

    UpdateDirSpace(cih, -cih->chunkInfo.chunkSize);

    mUsedSpace -= cih->chunkInfo.chunkSize;
    mUsedSpace += chunkSize;
    cih->chunkInfo.chunkSize = chunkSize;

    UpdateDirSpace(cih, cih->chunkInfo.chunkSize);

    lastChecksumBlock = OffsetToChecksumBlockNum(chunkSize);

    // XXX: Could do better; recompute the checksum for this last block
    cih->chunkInfo.chunkBlockChecksum[lastChecksumBlock] = 0;

    return 0;
}

int
ChunkManager::ChangeChunkVers(kfsFileId_t fileId,
                              kfsChunkId_t chunkId, int64_t chunkVersion)
{
    string chunkPathname;
    ChunkInfoHandle_t *cih;
    CMI tableEntry = mChunkTable.find(chunkId);
    string oldname, newname, s;

    if (tableEntry == mChunkTable.end()) {
        return -1;
    }

    cih = tableEntry->second;
    oldname = MakeChunkPathname(cih);

    mIsChunkTableDirty = true;

    if (cih->chunkInfo.AreChecksumsLoaded()) 
        s = "Checksums are loaded";
    else
        s = "Checksums are not loaded";
    KFS_LOG_VA_INFO("Chunk %s already exists; changing version # to %ld; %s",
                    oldname.c_str(), chunkVersion, s.c_str());

    cih->chunkInfo.chunkVersion = chunkVersion;

    newname = MakeChunkPathname(cih);

    rename(oldname.c_str(), newname.c_str());

    mChunkTable[chunkId] = cih;
    return 0;
}

void
ChunkManager::ReplicationDone(kfsChunkId_t chunkId)
{
    ChunkInfoHandle_t *cih;
    CMI tableEntry = mChunkTable.find(chunkId);

    if (tableEntry == mChunkTable.end()) {
        return;
    }

    cih = tableEntry->second;

#ifdef DEBUG
    string chunkPathname = MakeChunkPathname(cih);
    KFS_LOG_VA_DEBUG("Replication for chunk %s is complete...", chunkPathname.c_str());
#endif

    mIsChunkTableDirty = true;
    cih->isBeingReplicated = false;
    mChunkTable[chunkId] = cih;
}

void
ChunkManager::Start()
{
    globals().netManager.RegisterTimeoutHandler(mChunkManagerTimeoutImpl);
}

void
ChunkManager::UpdateDirSpace(ChunkInfoHandle_t *cih, off_t nbytes)
{
    for (uint32_t i = 0; i < mChunkDirs.size(); i++) {
        if (mChunkDirs[i].dirname == cih->chunkInfo.GetDirname()) {
            mChunkDirs[i].usedSpace += nbytes;
            if (mChunkDirs[i].usedSpace < 0)
                mChunkDirs[i].usedSpace = 0;
        }
    }
}

string
ChunkManager::GetDirForChunk()
{
    if (mChunkDirs.size() == 1)
        return mChunkDirs[0].dirname;

    // do weighted random, so that we can fill all drives
    off_t totalFreeSpace = 0;
    for (uint32_t i = 0; i < mChunkDirs.size(); i++) {
        if ((mChunkDirs[i].availableSpace == 0) ||
            ((mChunkDirs[i].availableSpace - mChunkDirs[i].usedSpace) < (off_t) CHUNKSIZE)) {
            continue;
        }
        totalFreeSpace += (mChunkDirs[i].availableSpace - mChunkDirs[i].usedSpace);
    }

    bool found = false;
    int dirToUse;
    double bucketLow = 0.0, bucketHi = 0.0;
    double randVal = drand48();
    for (uint32_t i = 0; i < mChunkDirs.size(); i++) {
        if ((mChunkDirs[i].availableSpace == 0) ||
            ((mChunkDirs[i].availableSpace - mChunkDirs[i].usedSpace) < (off_t) CHUNKSIZE)) {
            continue;
        }
        bucketHi = bucketLow + ((double) (mChunkDirs[i].availableSpace - mChunkDirs[i].usedSpace) / (double) totalFreeSpace);
        if ((bucketLow <= randVal) && (randVal <= bucketHi)) {
            dirToUse = i;
            found = true;
            break;
        }
        bucketLow = bucketHi;
    }

    if (!found) {
        // to account for rounding errors, if we didn't pick a drive, but some drive has space, use it.
        for (uint32_t i = 0; i < mChunkDirs.size(); i++) {
            if ((mChunkDirs[i].availableSpace == 0) ||
                ((mChunkDirs[i].availableSpace - mChunkDirs[i].usedSpace) < (off_t) CHUNKSIZE)) {
                continue;
            }
            dirToUse = i;
            found = true;
            break;
        }
    }        

    if (!found) {
        KFS_LOG_INFO("All drives are full; dir allocation failed");
        return "";
    }

    mLastDriveChosen = dirToUse;
    return mChunkDirs[dirToUse].dirname;
}

string
ChunkManager::MakeChunkPathname(ChunkInfoHandle_t *cih)
{
    ostringstream os;

    os << cih->chunkInfo.GetDirname() << '/' << cih->chunkInfo.fileId << '.' << cih->chunkInfo.chunkId 
       << '.' << cih->chunkInfo.chunkVersion;
    return os.str();
}

string
ChunkManager::MakeChunkPathname(const string &chunkdir, kfsFileId_t fid, kfsChunkId_t chunkId, kfsSeq_t chunkVersion)
{
    ostringstream os;

    os << chunkdir << '/' << fid << '.' << chunkId << '.' << chunkVersion;
    return os.str();
}

string
ChunkManager::MakeStaleChunkPathname(ChunkInfoHandle_t *cih)
{
    ostringstream os;
    string staleChunkDir = GetStaleChunkPath(cih->chunkInfo.GetDirname());

    os << staleChunkDir << '/' << cih->chunkInfo.fileId << '.' << cih->chunkInfo.chunkId << '.' << cih->chunkInfo.chunkVersion;

    return os.str();
}

void
ChunkManager::MakeChunkInfoFromPathname(const string &pathname, off_t filesz, ChunkInfoHandle_t **result)
{
    string::size_type slash = pathname.rfind('/');
    ChunkInfoHandle_t *cih;

    if (slash == string::npos) {
        *result = NULL;
        return;
    }
    
    string chunkFn, dirname;
    vector<string> component;

    dirname.assign(pathname, 0, slash);
    chunkFn.assign(pathname, slash + 1, string::npos);
    split(component, chunkFn, '.');
    assert(component.size() == 3);

    chunkId_t chunkId = atoll(component[1].c_str());
    if (GetChunkInfoHandle(chunkId, &cih) == 0) {
        KFS_LOG_VA_INFO("Duplicate chunk (%lld) with path: %s", chunkId, pathname.c_str());
        *result = NULL;
        return;
    }

    cih = new ChunkInfoHandle_t();    
    cih->chunkInfo.fileId = atoll(component[0].c_str());
    cih->chunkInfo.chunkId = atoll(component[1].c_str());
    cih->chunkInfo.chunkVersion = atoll(component[2].c_str());
    if (filesz >= (off_t) KFS_CHUNK_HEADER_SIZE)
        cih->chunkInfo.chunkSize = filesz - KFS_CHUNK_HEADER_SIZE;
    cih->chunkInfo.SetDirname(dirname);
    *result = cih;
    /*
    KFS_LOG_VA_DEBUG("From %s restored: %d, %d, %d", chunkFn.c_str(), 
                     cih->chunkInfo.fileId, cih->chunkInfo.chunkId, 
                     cih->chunkInfo.chunkVersion); 
    */
}

int
ChunkManager::OpenChunk(kfsChunkId_t chunkId, 
                        int openFlags)
{
    string fn;
    int fd;
    ChunkInfoHandle_t *cih;
    CMI tableEntry = mChunkTable.find(chunkId);

    if (tableEntry == mChunkTable.end()) {
        KFS_LOG_VA_DEBUG("No such chunk: %s", fn.c_str());
        return -EBADF;
    }
    cih = tableEntry->second;

    fn = MakeChunkPathname(cih);

    if ((!cih->dataFH) || (cih->dataFH->mFd < 0)) {
        fd = open(fn.c_str(), openFlags, S_IRUSR|S_IWUSR);
        if (fd < 0) {
            perror("open: ");
            return -EBADF;
        }
        globals().ctrOpenDiskFds.Update(1);

        // the checksums will be loaded async
        cih->Init(fd);

    } else {
        fd = cih->dataFH->mFd;
    }

    return 0;
}

void
ChunkManager::CloseChunk(kfsChunkId_t chunkId)
{
    ChunkInfoHandle_t *cih;
    CMI tableEntry = mChunkTable.find(chunkId);

    if (tableEntry == mChunkTable.end()) {
        return;
    }

    cih = tableEntry->second;
    // If there are at most 2 references to the handle---a reference
    // from mChunkTable and a reference from cih->chunkHandle, then
    // we can safely close the fileid.
    if (cih->dataFH.use_count() <= 2) {
        if ((!cih->dataFH) || (cih->dataFH->mFd < 0))
            return;
        cih->Release();
    }
}

int
ChunkManager::ChunkSize(kfsChunkId_t chunkId, off_t *chunkSize)
{
    ChunkInfoHandle_t *cih;

    if (GetChunkInfoHandle(chunkId, &cih) < 0)
        return -EBADF;

    *chunkSize = cih->chunkInfo.chunkSize;

    return 0;
}

int
ChunkManager::ReadChunk(ReadOp *op)
{
    ssize_t res;
    DiskConnection *d;
    ChunkInfoHandle_t *cih;
    off_t offset;
    size_t numBytesIO;

    if (GetChunkInfoHandle(op->chunkId, &cih) < 0)
        return -EBADF;

    d = SetupDiskConnection(op->chunkId, op);
    if (d == NULL)
        return -KFS::ESERVERBUSY;

    // the checksums should be loaded...
    cih->chunkInfo.VerifyChecksumsLoaded();

    if (op->chunkVersion != cih->chunkInfo.chunkVersion) {
        KFS_LOG_VA_INFO("Version # mismatch(have=%u vs asked=%ld...failing a read",
                         cih->chunkInfo.chunkVersion, op->chunkVersion);
        return -KFS::EBADVERS;
    }
    op->diskConnection.reset(d);

    // schedule a read based on the chunk size
    if (op->offset >= cih->chunkInfo.chunkSize) {
        op->numBytesIO = 0;
    } else if ((off_t) (op->offset + op->numBytes) > cih->chunkInfo.chunkSize) {
        op->numBytesIO = cih->chunkInfo.chunkSize - op->offset;
    } else {
        op->numBytesIO = op->numBytes;
    }

    if (op->numBytesIO == 0)
        return -EIO;

    // for checksumming to work right, reads should be in terms of
    // checksum-blocks.
    offset = OffsetToChecksumBlockStart(op->offset);

    numBytesIO = (op->numBytesIO / CHECKSUM_BLOCKSIZE) * CHECKSUM_BLOCKSIZE;
    if (op->numBytesIO % CHECKSUM_BLOCKSIZE)
        numBytesIO += CHECKSUM_BLOCKSIZE;

    // Make sure we don't try to read past EOF; the checksumming will
    // do the necessary zero-padding. 
    if ((off_t) (offset + numBytesIO) > cih->chunkInfo.chunkSize)
        numBytesIO = cih->chunkInfo.chunkSize - offset;
    
    if ((res = op->diskConnection->Read(offset + KFS_CHUNK_HEADER_SIZE, numBytesIO)) < 0)
        return -EIO;

    // read was successfully scheduled
    return 0;
}

int
ChunkManager::WriteChunk(WriteOp *op)
{
    ChunkInfoHandle_t *cih;
    int res;

    if (GetChunkInfoHandle(op->chunkId, &cih) < 0)
        return -EBADF;

    // the checksums should be loaded...
    cih->chunkInfo.VerifyChecksumsLoaded();
    
    // schedule a write based on the chunk size.  Make sure that a
    // write doesn't overflow the size of a chunk.
    op->numBytesIO = min((size_t) (KFS::CHUNKSIZE - op->offset), op->numBytes);

    if (op->numBytesIO == 0)
        return -EINVAL;

#if defined(__APPLE__)
    size_t addedBytes = max((long long) 0,
		    op->offset + op->numBytesIO - cih->chunkInfo.chunkSize);
#else
    size_t addedBytes = max((size_t) 0,
			    (size_t) (op->offset + op->numBytesIO - cih->chunkInfo.chunkSize));
#endif

    if ((off_t) (mUsedSpace + addedBytes) >= mTotalSpace)
	return -ENOSPC;

    if ((OffsetToChecksumBlockStart(op->offset) == op->offset) &&
        ((size_t) op->numBytesIO >= (size_t) CHECKSUM_BLOCKSIZE)) {
        assert(op->numBytesIO % CHECKSUM_BLOCKSIZE == 0);
        if (op->numBytesIO % CHECKSUM_BLOCKSIZE != 0) {
            return -EINVAL;
        }
#if 0
        // checksum was computed when we got data from client..so, skip
        // Hopefully, common case: write covers an entire block and
        // so, we just compute checksum and get on with the write.
        op->checksums = ComputeChecksums(op->dataBuf, op->numBytesIO);
#endif
        if (!op->isFromReReplication) {
            assert(op->checksums[0] == op->wpop->checksum);
        } else {
            op->checksums = ComputeChecksums(op->dataBuf, op->numBytesIO);
        }
    } else {

        assert((size_t) op->numBytesIO < (size_t) CHECKSUM_BLOCKSIZE);

        op->checksums.clear();
        // The checksum block we are after is beyond the current
        // end-of-chunk.  So, treat that as a 0-block and splice in.
        if (OffsetToChecksumBlockStart(op->offset) >= cih->chunkInfo.chunkSize) {
            IOBuffer *data = new IOBuffer();

            data->ZeroFill(CHECKSUM_BLOCKSIZE);
            data->Splice(op->dataBuf,
                         op->offset % CHECKSUM_BLOCKSIZE,
                         op->numBytesIO);
            delete op->dataBuf;
            op->dataBuf = data;
            goto do_checksum;
            
        }
        // Need to read the data block over which the checksum is
        // computed. 
        if (op->rop == NULL) {
            // issue a read
            ReadOp *rop = new ReadOp(op, OffsetToChecksumBlockStart(op->offset),
                                     CHECKSUM_BLOCKSIZE);
            KFS_LOG_VA_DEBUG("Write triggered a read for offset = %ld",
                             op->offset);

            op->rop = rop;

            rop->Execute();

            if (rop->status < 0) {
                int res = rop->status;

                op->rop = NULL;
                rop->wop = NULL;
                delete rop;
                return res;
            }

            return 0;
        }
        // If the read failed, cleanup and bail
        if (op->rop->status < 0) {
            op->status = op->rop->status;
            op->rop->wop = NULL;
            delete op->rop;
            op->rop = NULL;
            return op->HandleDone(EVENT_DISK_ERROR, NULL);
        }

        // All is good.  So, get on with checksumming
        op->rop->dataBuf->Splice(op->dataBuf,
                                 op->offset % CHECKSUM_BLOCKSIZE,
                                 op->numBytesIO);

        delete op->dataBuf;
        op->dataBuf = op->rop->dataBuf;
        op->rop->dataBuf = NULL;
        // If the buffer doesn't have a full CHECKSUM_BLOCKSIZE worth
        // of data, zero-pad the end.  We don't need to zero-pad the
        // front because the underlying filesystem will zero-fill when
        // we read a hole.
        ZeroPad(op->dataBuf);

      do_checksum:
        assert(op->dataBuf->BytesConsumable() == (int) CHECKSUM_BLOCKSIZE);

        uint32_t cksum = ComputeBlockChecksum(op->dataBuf, CHECKSUM_BLOCKSIZE); 
        op->checksums.push_back(cksum);

        // eat away the stuff at the beginning, so that we write out
        // exactly where we were asked from.
        off_t extra = op->offset - OffsetToChecksumBlockStart(op->offset);
	
	// OFF_TYPE_CAST: off_t casted to int.
        if (extra > 0)
            op->dataBuf->Consume(extra);
    }

    DiskConnection *d = SetupDiskConnection(op->chunkId, op);
    if (d == NULL)
        return -KFS::ESERVERBUSY;

    op->diskConnection.reset(d);

    /*
    KFS_LOG_VA_DEBUG("Checksum for chunk: %ld, offset = %ld, bytes = %ld, # of cksums = %u",
                  op->chunkId, op->offset, op->numBytesIO, op->checksums.size());
    */

    res = op->diskConnection->Write(op->offset + KFS_CHUNK_HEADER_SIZE, op->numBytesIO, op->dataBuf);
    if (res >= 0)
        UpdateChecksums(cih, op);
    return res;
}

void
ChunkManager::UpdateChecksums(ChunkInfoHandle_t *cih, WriteOp *op)
{
    mIsChunkTableDirty = true;

    off_t endOffset = op->offset + op->numBytesIO;

    // the checksums should be loaded...
    cih->chunkInfo.VerifyChecksumsLoaded();

    for (vector<uint32_t>::size_type i = 0; i < op->checksums.size(); i++) {
        off_t offset = op->offset + i * CHECKSUM_BLOCKSIZE;
        uint32_t checksumBlock = OffsetToChecksumBlockNum(offset);

        cih->chunkInfo.chunkBlockChecksum[checksumBlock] = op->checksums[i];
    }

    if (cih->chunkInfo.chunkSize < endOffset) {

        UpdateDirSpace(cih, endOffset - cih->chunkInfo.chunkSize);

	mUsedSpace += endOffset - cih->chunkInfo.chunkSize;
        cih->chunkInfo.chunkSize = endOffset;

    }
    assert(0 <= mUsedSpace && mUsedSpace <= mTotalSpace);
}

void
ChunkManager::ReadChunkDone(ReadOp *op)
{
    ChunkInfoHandle_t *cih = NULL;
    
    if ((GetChunkInfoHandle(op->chunkId, &cih) < 0) ||
        (op->chunkVersion != cih->chunkInfo.chunkVersion)) {
        AdjustDataRead(op);
        if (cih) {
            KFS_LOG_VA_INFO("Version # mismatch(have=%u vs asked=%ld...",
                             cih->chunkInfo.chunkVersion, op->chunkVersion);
        }
        op->status = -KFS::EBADVERS;
        return;
    }

    ZeroPad(op->dataBuf);

    assert(op->dataBuf->BytesConsumable() >= (int) CHECKSUM_BLOCKSIZE);

    // either nothing to verify or it better match

    bool mismatch = false;

    // figure out the block we are starting from and grab all the checksums
    vector<uint32_t>::size_type i, checksumBlock = OffsetToChecksumBlockNum(op->offset);
    vector<uint32_t> checksums = ComputeChecksums(op->dataBuf, op->dataBuf->BytesConsumable());

    // the checksums should be loaded...
    if (!cih->chunkInfo.AreChecksumsLoaded()) {
        // the read took too long; the checksums got paged out.  ask the client to retry
        KFS_LOG_VA_INFO("Checksums for chunk %lld got paged out; returning EAGAIN to client",
                        cih->chunkInfo.chunkId);
        op->status = -EAGAIN;
        return;
    }

    cih->chunkInfo.VerifyChecksumsLoaded();

    for (i = 0; i < checksums.size() &&
             checksumBlock < MAX_CHUNK_CHECKSUM_BLOCKS;
         checksumBlock++, i++) {
        if ((cih->chunkInfo.chunkBlockChecksum[checksumBlock] == 0) ||
            (checksums[i] == cih->chunkInfo.chunkBlockChecksum[checksumBlock])) {
            continue;
        }
        mismatch = true;
        break;
    }

    if (!mismatch) {
        // for checksums to verify, we did reads in multiples of
        // checksum block sizes.  so, get rid of the extra
        AdjustDataRead(op);
        return;
    }

    KFS_LOG_VA_ERROR("Checksum mismatch for chunk=%ld, offset=%ld, bytes = %ld: expect: %u, computed: %u ",
                  op->chunkId, op->offset, op->numBytesIO,
                  cih->chunkInfo.chunkBlockChecksum[checksumBlock],
                  checksums[i]);

    op->status = -KFS::EBADCKSUM;

    // Notify the metaserver that the chunk we have is "bad"; the
    // metaserver will re-replicate this chunk.
    NotifyMetaCorruptedChunk(op->chunkId);
    
    // Take out the chunk from our side
    StaleChunk(op->chunkId);
}

void
ChunkManager::NotifyMetaCorruptedChunk(kfsChunkId_t chunkId)
{
    ChunkInfoHandle_t *cih;

    if (GetChunkInfoHandle(chunkId, &cih) < 0) {
        KFS_LOG_VA_ERROR("Unable to notify metaserver of corrupt chunk: %lld",
                      chunkId);
        return;
    }

    KFS_LOG_VA_INFO("Notifying metaserver of corrupt chunk (%ld) in file %lld",
                 cih->chunkInfo.fileId, chunkId);

    // This op will get deleted when we get an ack from the metaserver
    CorruptChunkOp *ccop = new CorruptChunkOp(0, cih->chunkInfo.fileId, 
                                              chunkId);
    gMetaServerSM.EnqueueOp(ccop);
}

//
// directory with dirname is unaccessable; maybe drive failed.  so,
// notify metaserver of lost blocks.  the metaserver will then
// re-replicate.
//
void
ChunkManager::NotifyMetaChunksLost(const string &dirname)
{
    ChunkInfoHandle_t *cih;
    CMI iter = mChunkTable.begin();
    
    while (iter != mChunkTable.end()) {
        cih = iter->second;

        if (cih->chunkInfo.GetDirname() != dirname) {
            ++iter;
            continue;
        }

        KFS_LOG_VA_INFO("Notifying metaserver of lost chunk (%ld) in file %lld in dir %s",
                        cih->chunkInfo.fileId, cih->chunkInfo.chunkId, dirname.c_str());

        // This op will get deleted when we get an ack from the metaserver
        CorruptChunkOp *ccop = new CorruptChunkOp(0, cih->chunkInfo.fileId, 
                                                  cih->chunkInfo.chunkId);
        gMetaServerSM.EnqueueOp(ccop);

        // get rid of chunkid from our list
        CMI prev = iter;
        ++iter;
        mChunkTable.erase(cih->chunkInfo.chunkId);
        delete cih;
    }
    
}

void
ChunkManager::ZeroPad(IOBuffer *buffer)
{
    int bytesFilled = buffer->BytesConsumable();
    if ((bytesFilled % CHECKSUM_BLOCKSIZE) == 0)
        return;

    int numToZero = CHECKSUM_BLOCKSIZE - (bytesFilled % CHECKSUM_BLOCKSIZE);
    if (numToZero > 0) {
        // pad with 0's
        buffer->ZeroFill(numToZero);
    }
}

void
ChunkManager::AdjustDataRead(ReadOp *op)
{
    size_t extraRead = op->offset - OffsetToChecksumBlockStart(op->offset);
    if (extraRead > 0)
        op->dataBuf->Consume(extraRead);
    if (op->dataBuf->BytesConsumable() > op->numBytesIO)
        op->dataBuf->Trim(op->numBytesIO);
}

uint32_t 
ChunkManager::GetChecksum(kfsChunkId_t chunkId, off_t offset)
{
    uint32_t checksumBlock = OffsetToChecksumBlockNum(offset);
    ChunkInfoHandle_t *cih;

    if (GetChunkInfoHandle(chunkId, &cih) < 0)
        return 0;

    // the checksums should be loaded...
    cih->chunkInfo.VerifyChecksumsLoaded();

    assert(checksumBlock <= MAX_CHUNK_CHECKSUM_BLOCKS);

    return cih->chunkInfo.chunkBlockChecksum[checksumBlock];
}

DiskConnection *
ChunkManager::SetupDiskConnection(kfsChunkId_t chunkId, KfsOp *op)
{
    ChunkInfoHandle_t *cih;
    DiskConnection *diskConnection;
    CMI tableEntry = mChunkTable.find(chunkId);

    if (tableEntry == mChunkTable.end()) {
        return NULL;
    }

    cih = tableEntry->second;
    if ((!cih->dataFH) || (cih->dataFH->mFd < 0)) {
        CleanupInactiveFds();
        if (OpenChunk(chunkId, O_RDWR) < 0) 
            return NULL;
    }
 
    cih->lastIOTime = time(0);
    diskConnection = new DiskConnection(cih->dataFH, op);

    return diskConnection;
}
    
void
ChunkManager::CancelChunkOp(KfsCallbackObj *cont, kfsChunkId_t chunkId)
{
    // Cancel the chunk operations scheduled by KfsCallbackObj on chunkId.
    // XXX: Fill it...
}

//
// dump out the contents of the chunkTable to disk
//
void
ChunkManager::Checkpoint()
{
    CheckpointOp *cop;
    // on the macs, i can't declare CMI iter;
    CMI iter = mChunkTable.begin();

    mLastCheckpointTime = time(NULL);

    if (!mIsChunkTableDirty)
        return;

    // KFS_LOG_VA_DEBUG("Checkpointing state");
    cop = new CheckpointOp(1);
    
#if 0
    // there are no more checkpoints on the chunkserver...this will all go
    // we are using this to rotate logs...

    ChunkInfoHandle_t *cih;

    for (iter = mChunkTable.begin(); iter != mChunkTable.end(); ++iter) {
        cih = iter->second;
        // If a chunk is being replicated, then it is not yet a part
        // of the namespace.  When replication is done, it becomes a
        // part of the namespace.  This model keeps recovery simple:
        // if we die in the midst of replicating a chunk, on restart,
        // we will the chunk as an orphan and throw it away.
        if (cih->isBeingReplicated)
            continue;
        cop->data << cih->chunkInfo.fileId << ' ';
        cop->data << cih->chunkInfo.chunkId << ' ';
        cop->data << cih->chunkInfo.chunkSize << ' ';
        cop->data << cih->chunkInfo.chunkVersion << ' ';

        cop->data << MAX_CHUNK_CHECKSUM_BLOCKS << ' ';
        for (uint32_t i = 0; i < MAX_CHUNK_CHECKSUM_BLOCKS; ++i) {
            cop->data << cih->chunkInfo.chunkBlockChecksum[i] << ' ';
        }
        cop->data << endl;
    }
#endif
    
    gLogger.Submit(cop);

    // Now, everything is clean...
    mIsChunkTableDirty = false;
}

//
// Get all the chunk directory entries from all the places we can
// store the chunks into a single array.
//
int
ChunkManager::GetChunkDirsEntries(struct dirent ***namelist)
{
    struct dirent **entries;
    vector<struct dirent **> dirEntries;
    vector<int> dirEntriesCount;
    int res, numChunkFiles = 0;
    uint32_t i;

    *namelist = NULL;
    for (i = 0; i < mChunkDirs.size(); i++) {
        res = scandir(mChunkDirs[i].dirname.c_str(), &entries, 0, alphasort);
        if (res < 0) {
            KFS_LOG_VA_INFO("Unable to open %s", mChunkDirs[i].dirname.c_str());
            for (i = 0; i < dirEntries.size(); i++) {
                entries = dirEntries[i];
                for (int j = 0; j < dirEntriesCount[i]; j++)
                    free(entries[j]);
                free(entries);
            }
            dirEntries.clear();
            return -1;
        }
        dirEntries.push_back(entries);
        dirEntriesCount.push_back(res);
        numChunkFiles += res;
    }
    
    // Get all the directory entries into one giganto array
    *namelist = (struct dirent **) malloc(sizeof(struct dirent **) * numChunkFiles);

    numChunkFiles = 0;
    for (i = 0; i < dirEntries.size(); i++) {
        int count = dirEntriesCount[i];
        entries = dirEntries[i];

        memcpy((*namelist) + numChunkFiles, entries, count * sizeof(struct dirent **));
        numChunkFiles += count;
    }
    return numChunkFiles;
}

void
ChunkManager::GetChunkPathEntries(vector<string> &pathnames)
{
    uint32_t i;
    struct dirent **entries = NULL;
    int res;

    for (i = 0; i < mChunkDirs.size(); i++) {
        res = scandir(mChunkDirs[i].dirname.c_str(), &entries, 0, alphasort);
        if (res < 0) {
            KFS_LOG_VA_INFO("Unable to open %s", mChunkDirs[i].dirname.c_str());
            continue;
        }
        for (int j = 0; j < res; j++) {
            string s = mChunkDirs[i].dirname + "/" + entries[j]->d_name;
            pathnames.push_back(s);
            free(entries[j]);
        }
        free(entries);
    }
}

void
ChunkManager::Restart()
{
    int version;

    version = gLogger.GetVersionFromCkpt();
    if (version == gLogger.GetLoggerVersionNum()) {
        Restore();
    } else {
        std::cout << "Unsupported version...copy out the data and copy it back in...." << std::endl;
        exit(-1);
    }

    // Write out a new checkpoint file with just version and set it at 2
    gLogger.Checkpoint(NULL);
}

void
ChunkManager::Restore()
{
    // sort all the chunk names alphabetically in each of the
    // directories
    vector<string> chunkPathnames;
    struct stat buf;
    int res;
    uint32_t i, numChunkFiles;

    GetChunkPathEntries(chunkPathnames);

    numChunkFiles = chunkPathnames.size();
    // each chunk file is of the form: <fileid>.<chunkid>.<chunkversion>  
    // parse the filename to extract out the chunk info
    for (i = 0; i < numChunkFiles; ++i) {
        string s = chunkPathnames[i];
        ChunkInfoHandle_t *cih;
        res = stat(s.c_str(), &buf);
        if ((res < 0) || (!S_ISREG(buf.st_mode)))
            continue;
        MakeChunkInfoFromPathname(s, buf.st_size, &cih);
        if (cih != NULL)
            AddMapping(cih);
        else {
            KFS_LOG_VA_INFO("Deleting possibly duplicate file %s", s.c_str());
            unlink(s.c_str());
        }
    }
}

void
ChunkManager::AddMapping(ChunkInfoHandle_t *cih)
{
    mNumChunks++;
    mChunkTable[cih->chunkInfo.chunkId] = cih;
    mUsedSpace += cih->chunkInfo.chunkSize;
    UpdateDirSpace(cih, cih->chunkInfo.chunkSize);
}

void
ChunkManager::ReplayAllocChunk(kfsFileId_t fileId, kfsChunkId_t chunkId,
                               int64_t chunkVersion)
{
    ChunkInfoHandle_t *cih;

    mIsChunkTableDirty = true;

    if (GetChunkInfoHandle(chunkId, &cih) == 0) {
        // If the entry exists, just update the version
        cih->chunkInfo.chunkVersion = chunkVersion;
        mChunkTable[chunkId] = cih;
        return;
    }
    mNumChunks++;
    // after replay is done, when we verify entries in the table, we
    // stat the file and fix up the sizes then.  so, no need to do
    // anything here.
    cih = new ChunkInfoHandle_t();
    cih->chunkInfo.fileId = fileId;
    cih->chunkInfo.chunkId = chunkId;
    cih->chunkInfo.chunkVersion = chunkVersion;
    mChunkTable[chunkId] = cih;
}

void
ChunkManager::ReplayChangeChunkVers(kfsFileId_t fileId, kfsChunkId_t chunkId,
                                    int64_t chunkVersion)
{
    ChunkInfoHandle_t *cih;

    if (GetChunkInfoHandle(chunkId, &cih) != 0) 
        return;

    KFS_LOG_VA_DEBUG("Chunk %ld already exists; changing version # to %ld",
                     chunkId, chunkVersion);
    
    // Update the version #
    cih->chunkInfo.chunkVersion = chunkVersion;
    mChunkTable[chunkId] = cih;
    mIsChunkTableDirty = true;
}

void
ChunkManager::ReplayDeleteChunk(kfsChunkId_t chunkId)
{
    ChunkInfoHandle_t *cih;
    CMI tableEntry = mChunkTable.find(chunkId);

    mIsChunkTableDirty = true;

    if (tableEntry != mChunkTable.end()) {
        cih = tableEntry->second;
        mUsedSpace -= cih->chunkInfo.chunkSize;
        mChunkTable.erase(chunkId);
        delete cih;
        
        mNumChunks--;
        assert(mNumChunks >= 0);
        if (mNumChunks < 0)
            mNumChunks = 0;

    }
}

void
ChunkManager::ReplayWriteDone(kfsChunkId_t chunkId, off_t chunkSize,
                              off_t offset, vector<uint32_t> checksums)
{
    ChunkInfoHandle_t *cih;
    int res;

    res = GetChunkInfoHandle(chunkId, &cih);
    if (res < 0)
        return;

    mIsChunkTableDirty = true;
    mUsedSpace -= cih->chunkInfo.chunkSize;    
    cih->chunkInfo.chunkSize = chunkSize;
    mUsedSpace += cih->chunkInfo.chunkSize;
    
    for (vector<uint32_t>::size_type i = 0; i < checksums.size(); i++) {
        off_t currOffset = offset + i * CHECKSUM_BLOCKSIZE;
        size_t checksumBlock = OffsetToChecksumBlockNum(currOffset);
        
        cih->chunkInfo.chunkBlockChecksum[checksumBlock] = checksums[i];
    }
}

void
ChunkManager::ReplayTruncateDone(kfsChunkId_t chunkId, off_t chunkSize)
{
    ChunkInfoHandle_t *cih;
    int res;
    off_t lastChecksumBlock;

    res = GetChunkInfoHandle(chunkId, &cih);
    if (res < 0)
        return;

    mIsChunkTableDirty = true;
    mUsedSpace -= cih->chunkInfo.chunkSize;    
    cih->chunkInfo.chunkSize = chunkSize;
    mUsedSpace += cih->chunkInfo.chunkSize;

    lastChecksumBlock = OffsetToChecksumBlockNum(chunkSize);

    cih->chunkInfo.chunkBlockChecksum[lastChecksumBlock] = 0;
}

void
ChunkManager::GetHostedChunks(vector<ChunkInfo_t> &result)
{
    ChunkInfoHandle_t *cih;

    // walk thru the table and pick up the chunk-ids
    for (CMI iter = mChunkTable.begin(); iter != mChunkTable.end(); ++iter) {
        cih = iter->second;
        result.push_back(cih->chunkInfo);
    }
}

int
ChunkManager::GetChunkInfoHandle(kfsChunkId_t chunkId, ChunkInfoHandle_t **cih)
{
    CMI iter = mChunkTable.find(chunkId);

    if (iter == mChunkTable.end()) {
        *cih = NULL;
        return -EBADF;
    }

    *cih = iter->second;
    return 0;
}

int
ChunkManager::AllocateWriteId(WriteIdAllocOp *wi)
{
    WriteOp *op;
    ChunkInfoHandle_t *cih;

    if (GetChunkInfoHandle(wi->chunkId, &cih) < 0)
        return -EBADF;

    if (wi->chunkVersion != cih->chunkInfo.chunkVersion) {
        KFS_LOG_VA_INFO("Version # mismatch(have=%d vs asked=%lu...failing a write",
                        cih->chunkInfo.chunkVersion, wi->chunkVersion);
        return -EINVAL;
    }

    mWriteId++;
    op = new WriteOp(wi->seq, wi->chunkId, wi->chunkVersion,
                     wi->offset, wi->numBytes, NULL, mWriteId);
    op->enqueueTime = time(NULL);
    wi->writeId = mWriteId;
    op->isWriteIdHolder = true;
    mPendingWrites.push_back(op);
    return 0;
}

int
ChunkManager::EnqueueWrite(WritePrepareOp *wp)
{
    WriteOp *op;
    ChunkInfoHandle_t *cih;

    if (GetChunkInfoHandle(wp->chunkId, &cih) < 0)
        return -EBADF;

    if (wp->chunkVersion != cih->chunkInfo.chunkVersion) {
        KFS_LOG_VA_INFO("Version # mismatch(have=%d vs asked=%lu...failing a write",
                         cih->chunkInfo.chunkVersion, wp->chunkVersion);
        return -EINVAL;
    }
    op = GetWriteOp(wp->writeId);
    if (op->dataBuf == NULL)
        op->dataBuf = wp->dataBuf;
    else
        op->dataBuf->Append(wp->dataBuf);
    wp->dataBuf = NULL;
    mPendingWrites.push_back(op);
    return 0;
}

// Helper functor that matches pending writes by chunkid's
class ChunkIdMatcher {
    kfsChunkId_t myid;
public:
    ChunkIdMatcher(kfsChunkId_t s) : myid(s) { }
    bool operator() (const WriteOp *r) {
        return (r->chunkId == myid);
    }
};

bool
ChunkManager::IsWritePending(kfsChunkId_t chunkId)
{
    list<WriteOp *>::iterator i;

    i = find_if(mPendingWrites.begin(), mPendingWrites.end(), 
                ChunkIdMatcher(chunkId));
    if (i == mPendingWrites.end())
        return false;
    return true;
}

int64_t
ChunkManager::GetChunkVersion(kfsChunkId_t c)
{
    ChunkInfoHandle_t *cih;

    if (GetChunkInfoHandle(c, &cih) < 0)
        return -1;

    return cih->chunkInfo.chunkVersion;
}

// Helper functor that matches write id's by sequence #'s
class WriteIdMatcher {
    int64_t myid;
public:
    WriteIdMatcher(int64_t s) : myid(s) { }
    bool operator() (const WriteOp *r) {
        return (r->writeId == myid);
    }
};

WriteOp *
ChunkManager::GetWriteOp(int64_t writeId)
{
    list<WriteOp *>::iterator i;
    WriteOp *op;

    i = find_if(mPendingWrites.begin(), mPendingWrites.end(), 
                WriteIdMatcher(writeId));
    if (i == mPendingWrites.end())
        return NULL;
    op = *i;
    mPendingWrites.erase(i);
    return op;
}

WriteOp *
ChunkManager::CloneWriteOp(int64_t writeId)
{
    list<WriteOp *>::iterator i;
    WriteOp *op, *other;

    i = find_if(mPendingWrites.begin(), mPendingWrites.end(), 
                WriteIdMatcher(writeId));
    if (i == mPendingWrites.end())
        return NULL;
    other = *i;
    if (other->status < 0)
        // if the write is "bad" already, don't add more data to it
        return NULL;

    // Since we are cloning, "touch" the time
    other->enqueueTime = time(NULL);
    // offset/size/buffer are to be filled in
    op = new WriteOp(other->seq, other->chunkId, other->chunkVersion, 
                     0, 0, NULL, other->writeId);
    return op;
}

void
ChunkManager::SetWriteStatus(int64_t writeId, int status)
{
    list<WriteOp *>::iterator i;
    WriteOp *op;

    i = find_if(mPendingWrites.begin(), mPendingWrites.end(), 
                WriteIdMatcher(writeId));
    if (i == mPendingWrites.end())
        return;
    op = *i;
    op->status = status;

    KFS_LOG_VA_INFO("Setting the status of writeid: %d to %d", writeId, status);
}

bool
ChunkManager::IsValidWriteId(int64_t writeId)
{
    list<WriteOp *>::iterator i;

    i = find_if(mPendingWrites.begin(), mPendingWrites.end(), 
                WriteIdMatcher(writeId));
    // valid if we don't hit the end of the list
    return (i != mPendingWrites.end());
}

void
ChunkManager::Timeout()
{
    time_t now = time(NULL);

#ifdef DEBUG
    verifyExecutingOnEventProcessor();
#endif

    if (now - mLastCheckpointTime > CKPT_TIME_INTERVAL) {
        Checkpoint();
        // if any writes have been around for "too" long, remove them
        // and reclaim memory
        ScavengePendingWrites();
        // cleanup inactive fd's and thereby free up fd's
        CleanupInactiveFds(true);
    }
}

void
ChunkManager::ScavengePendingWrites()
{
    list<WriteOp *>::iterator i;
    WriteOp *op;
    time_t now = time(NULL);
    ChunkInfoHandle_t *cih;

    i = mPendingWrites.begin();
    while (i != mPendingWrites.end()) {
        op = *i;
        // The list is sorted by enqueue time
        if (now - op->enqueueTime < MAX_PENDING_WRITE_LRU_SECS) {
            break;
        }
        // if it exceeds 5 mins, retire the op
        KFS_LOG_VA_DEBUG("Retiring write with id=%ld as it has been too long",
                         op->writeId);
        mPendingWrites.pop_front();

        if ((GetChunkInfoHandle(op->chunkId, &cih) == 0) &&
            (now - cih->lastIOTime >= INACTIVE_FDS_CLEANUP_INTERVAL_SECS)) {
            // close the chunk only if it is inactive
            CloseChunk(op->chunkId);
        }

        delete op;
        i = mPendingWrites.begin();
    }
}

int
ChunkManager::Sync(WriteOp *op)
{
    if (!op->diskConnection) {
        return -1;
    }
    return op->diskConnection->Sync(op->waitForSyncDone);
}

class InactiveFdCleaner {
    time_t now;
public:
    InactiveFdCleaner(time_t n) : now(n) { }
    void operator() (const std::tr1::unordered_map<kfsChunkId_t, ChunkInfoHandle_t *>::value_type v) {
        ChunkInfoHandle_t *cih  = v.second;
        
        if ((!cih->dataFH) || (cih->dataFH->mFd < 0) ||
            (gLeaseClerk.IsLeaseValid(cih->chunkInfo.chunkId)) ||
            (now - cih->lastIOTime < INACTIVE_FDS_CLEANUP_INTERVAL_SECS) ||
            (cih->isBeingReplicated))
            return;

        // we have a valid file-id and it has been over 5 mins since we last did I/O on it.
        KFS_LOG_VA_DEBUG("cleanup: closing fileid = %d, for chunk = %ld",
                         cih->dataFH->mFd,
                         cih->chunkInfo.chunkId);
        cih->Release();
    }
};

void
ChunkManager::CleanupInactiveFds(bool periodic)
{
    static time_t lastCleanupTime = time(0);
    time_t now = time(0);

    if (OPEN_FDS_LOW_WATERMARK == 0) {
        struct rlimit rlim;
        int res;

        res = getrlimit(RLIMIT_NOFILE, &rlim);
        if (res == 0) {
            OPEN_FDS_LOW_WATERMARK = rlim.rlim_cur / 2;
            // bump the soft limit to the hard limit
            rlim.rlim_cur = rlim.rlim_max;
            if (setrlimit(RLIMIT_NOFILE, &rlim) == 0) {
                KFS_LOG_VA_DEBUG("Setting # of open files to: %ld",
                                 rlim.rlim_cur);
                OPEN_FDS_LOW_WATERMARK = rlim.rlim_cur / 2;
            }
        }
    }

    // not enough time has elapsed
    if (periodic && (now - lastCleanupTime < INACTIVE_FDS_CLEANUP_INTERVAL_SECS))
        return;

    int totalOpenFds = globals().ctrOpenDiskFds.GetValue() +
        globals().ctrOpenNetFds.GetValue();

    // if we haven't cleaned up in 5 mins or if we too many fd's that
    // are open, clean up.
    if ((!periodic) && (totalOpenFds < OPEN_FDS_LOW_WATERMARK)) {
        return;
    }

    // either we are periodic cleaning or we have too many FDs open
    lastCleanupTime = time(0);

    for_each(mChunkTable.begin(), mChunkTable.end(), InactiveFdCleaner(now));
}

string
KFS::GetStaleChunkPath(const string &partition)
{
    return partition + "/lost+found/";
}

int64_t
ChunkManager::GetTotalSpace() 
{
#if defined(__APPLE__)
    for (uint32_t i = 0; i < mChunkDirs.size(); i++) {
        mChunkDirs[i].availableSpace = mTotalSpace;
    }
    return mTotalSpace;
#endif

    int64_t availableSpace = 0;
    set<unsigned long> seenDrives;


    for (uint32_t i = 0; i < mChunkDirs.size(); i++) {
        // report the space based on availability
#if defined(__APPLE__) || defined(__sun__) || (!defined(__i386__))
        struct statvfs result;

        if (statvfs(mChunkDirs[i].dirname.c_str(), &result) < 0) {
            int err = errno;
            KFS_LOG_VA_INFO("statvfs failed on %s with error: %d", mChunkDirs[i].dirname.c_str(), err);
            mChunkDirs[i].availableSpace = 0;
            if (err == EIO) {
                // We can't stat the directory.
                // Notify metaserver that all blocks on this
                // drive are lost
                NotifyMetaChunksLost(mChunkDirs[i].dirname);
            }
            continue;
        }
#else
        // we are on i386 on linux
        struct statvfs64 result;

        if (statvfs64(mChunkDirs[i].dirname.c_str(), &result) < 0) {
            int err = errno;
            KFS_LOG_VA_INFO("statvfs failed on %s with error: %d", mChunkDirs[i].dirname.c_str(), err);
            mChunkDirs[i].availableSpace = 0;
            if (err == EIO) {
                // We can't stat the directory.
                // Notify metaserver that all blocks on this
                // drive are lost
                NotifyMetaChunksLost(mChunkDirs[i].dirname);
            }
            continue;
        }
#endif

        if (seenDrives.find(result.f_fsid) != seenDrives.end()) {
            // if we have seen the drive where this directory is, then
            // we have already accounted for how much is free on the drive
            availableSpace += mChunkDirs[i].usedSpace;
            mChunkDirs[i].availableSpace = result.f_bavail * result.f_frsize + mChunkDirs[i].usedSpace;
        } else {
            // result.* is how much is available on disk; mUsedSpace is how
            // much we used up with chunks; so, the total storage available on
            // the drive is the sum of the two.  if we don't add mUsedSpace,
            // then all the chunks we write will get to use the space on disk and
            // won't get acounted for in terms of drive space.
            mChunkDirs[i].availableSpace = result.f_bavail * result.f_frsize + mChunkDirs[i].usedSpace;
            availableSpace += result.f_bavail * result.f_frsize + mChunkDirs[i].usedSpace;
            seenDrives.insert(result.f_fsid);
        }
        KFS_LOG_VA_DEBUG("Dir: %s has space %ld", mChunkDirs[i].dirname.c_str(), mChunkDirs[i].availableSpace);
    }
    // we got all the info...so report true value
    return min(availableSpace, mTotalSpace);
}

void
ChunkManagerTimeoutImpl::Timeout()
{
    SubmitOp(&mTimeoutOp);
}

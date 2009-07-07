//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: KfsWrite.cc 315 2009-04-22 18:58:19Z sjakub $
//
// Created 2006/10/02
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
// All the code to deal with writes from the client.
//----------------------------------------------------------------------------

#include "KfsClient.h"
#include "KfsClientInt.h"
#include "common/properties.h"
#include "common/log.h"
#include "meta/kfstypes.h"
#include "libkfsIO/Checksum.h"
#include "Utils.h"

#include <cerrno>
#include <iostream>
#include <string>

using std::string;
using std::ostringstream;
using std::istringstream;
using std::min;
using std::max;

using std::cout;
using std::endl;

using namespace KFS;

static bool
NeedToRetryAllocation(int status)
{
    return ((status == -EHOSTUNREACH) ||
	    (status == -ETIMEDOUT) ||
	    (status == -EBUSY) ||
	    (status == -EIO) ||
            (status == -KFS::EBADVERS) ||
	    (status == -KFS::EALLOCFAILED));
}

inline size_t GetChecksumBlockTailSize(off_t offset)
{
    size_t rem(offset % CHECKSUM_BLOCKSIZE);
    return (rem > 0 ? CHECKSUM_BLOCKSIZE - rem : 0);
}
    
ssize_t
KfsClientImpl::Write(int fd, const char *buf, size_t numBytes)
{
    MutexLock l(&mMutex);

    size_t nwrote = 0;
    ssize_t numIO = 0;
    int res;

    if (!valid_fd(fd) || mFileTable[fd] == NULL || mFileTable[fd]->openMode == O_RDONLY) {
        KFS_LOG_VA_INFO("Write to fd: %d failed---fd is likely closed", fd);
	return -EBADF;
    }
    FileAttr *fa = FdAttr(fd);
    if (fa->isDirectory)
	return -EISDIR;

    FilePosition *pos = FdPos(fd);
    pos->CancelPendingRead();
    //
    // Loop thru chunk after chunk until we write the desired #
    // of bytes.
    ChunkBuffer* const cb = FdBuffer(fd);
    while (nwrote < numBytes) {

	size_t nleft = numBytes - nwrote;

        // Don't need this: if we don't have the lease, don't
	// know where the chunk is, allocation will get that info.
	// LocateChunk(fd, pos->chunkNum);

	// need to retry here...
	if ((res = DoAllocation(fd)) < 0) {
	    // allocation failed...bail
	    break;
	}

	if (pos->preferredServer == NULL) {
	    numIO = OpenChunk(fd);
	    if (numIO < 0) {
		// KFS_LOG_VA_DEBUG("OpenChunk(%lld)", numIO);
		break;
	    }
	}

	if (nleft < cb->bufsz || cb->dirty) {
	    // either the write is small or there is some dirty
	    // data...so, aggregate as much as possible and then it'll
	    // get flushed
	    numIO = WriteToBuffer(fd, buf + nwrote, nleft);
	} else {
	    // write is big and there is nothing dirty...so,
	    // write-thru
	    numIO = WriteToServer(fd, pos->chunkOffset, buf + nwrote, nleft);
	}

	if (numIO < 0) {
            if (numIO == -KFS::ELEASEEXPIRED) {
                KFS_LOG_VA_INFO("Continuing to retry write for errorcode = %d", numIO);
                continue;
            }
            KFS_LOG_VA_INFO("Write failed %s @offset: %lld: asked: %d, did: %d, errorcode = %d",
                            mFileTable[fd]->pathname.c_str(), pos->fileOffset, numBytes, nwrote, numIO);

	    break;
	}

	nwrote += numIO;
	
	off_t newPos = Seek(fd, numIO, SEEK_CUR);

	if (newPos < 0) {
	    // KFS_LOG_VA_DEBUG("Seek(%lld)", newPos);
	    break;
	}
    }

    if (nwrote == 0 && numIO < 0)
	return numIO;

    if (nwrote != numBytes) {
	KFS_LOG_VA_DEBUG("----Write done: asked: %llu, got: %llu-----",
			  numBytes, nwrote);
    } else if (false && // turn off for now.
            cb->dirty &&
            cb->length >= max(
                CHECKSUM_BLOCKSIZE + GetChecksumBlockTailSize(cb->start),
                min(cb->bufsz >> 1, size_t(1) << 20))) {
        KFS_LOG_VA_DEBUG("starting sync chunk: %d offset: %d/%d size: %d",
            (int)pos->chunkNum, (int)cb->start,
            (int)pos->chunkOffset, (int)cb->length);
        mPendingOp.Start(fd, false);
    }
    return nwrote;
}

ssize_t
KfsClientImpl::WriteToBuffer(int fd, const char *buf, size_t numBytes)
{
    ssize_t numIO;
    size_t lastByte;
    FilePosition *pos = FdPos(fd);
    ChunkBuffer *cb = FdBuffer(fd);

    // if the buffer has dirty data and this write doesn't abut it,
    // or if buffer is full, flush the buffer before proceeding.
    // XXX: Reconsider buffering to do a read-modify-write of
    // large amounts of data.
    if (cb->dirty && cb->chunkno != pos->chunkNum) {
	int status = FlushBuffer(fd);
	if (status < 0)
	    return status;
    }

    cb->allocate();

    off_t start = pos->chunkOffset - cb->start;
    size_t previous = cb->length;
    if (cb->dirty && ((start != (off_t) previous) ||
	              (previous == cb->bufsz))) {
	int status = FlushBuffer(fd);
	if (status < 0)
	    return status;
    }

    if (!cb->dirty) {
	cb->chunkno = pos->chunkNum;
	cb->start = pos->chunkOffset;
	cb->length = 0;
	cb->dirty = true;
    }

    // ensure that write doesn't straddle chunk boundaries
    numBytes = min(numBytes, (size_t) (KFS::CHUNKSIZE - pos->chunkOffset));
    if (numBytes == 0)
	return 0;

    // max I/O we can do
    numIO = min(cb->bufsz - cb->length, numBytes);
    assert(numIO > 0);

    // KFS_LOG_VA_DEBUG("Buffer absorbs write...%d bytes", numIO);

    // chunkBuf[0] corresponds to some offset in the chunk,
    // which is defined by chunkBufStart.
    // chunkOffset corresponds to the position in the chunk
    // where the "filepointer" is currently at.
    // Figure out where the data we want copied into starts
    start = pos->chunkOffset - cb->start;
    assert(start >= 0 && start < (off_t) cb->bufsz);
    memcpy(&cb->buf[start], buf, numIO);

    lastByte = start + numIO;

    // update the size according to where the last byte just
    // got written.
    if (lastByte > cb->length)
	cb->length = lastByte;

    return numIO;
}

ssize_t
KfsClientImpl::FlushBuffer(int fd, bool flushOnlyIfHasFullChecksumBlock)
{
    ssize_t numIO = 0;
    size_t extra = 0;
    ChunkBuffer *cb = FdBuffer(fd);

    if (cb->dirty && (! flushOnlyIfHasFullChecksumBlock ||
            cb->length >= CHECKSUM_BLOCKSIZE +
                (extra = GetChecksumBlockTailSize(cb->start)))) {
        const size_t nWr = flushOnlyIfHasFullChecksumBlock ?
            extra + (cb->length - extra) / CHECKSUM_BLOCKSIZE * CHECKSUM_BLOCKSIZE :
            cb->length;
	numIO = WriteToServer(fd, cb->start, cb->buf, nWr);
	if (numIO >= 0) {
            cb->length -= nWr;
            cb->start += nWr;
            memmove(cb->buf, cb->buf + nWr, cb->length);
            cb->dirty = cb->length > 0;
        }
    }
    return numIO;
}

ssize_t
KfsClientImpl::WriteToServer(int fd, off_t offset, const char *buf, size_t numBytes)
{
    assert(KFS::CHUNKSIZE - offset >= 0);

    size_t numAvail = min(numBytes, (size_t) (KFS::CHUNKSIZE - offset));
    int res = 0;

    for (int retryCount = 0; retryCount < NUM_RETRIES_PER_OP; retryCount++) {
	// Same as read: align writes to checksum block boundaries
	if (offset + numAvail <= OffsetToChecksumBlockEnd(offset))
	    res = DoSmallWriteToServer(fd, offset, buf, numBytes);
	else {
            struct timeval startTime, endTime;
            double timeTaken;
            
            gettimeofday(&startTime, NULL);
            
	    res = DoLargeWriteToServer(fd, offset, buf, numBytes);

            gettimeofday(&endTime, NULL);
            
            timeTaken = (endTime.tv_sec - startTime.tv_sec) +
                (endTime.tv_usec - startTime.tv_usec) * 1e-6;

            if (timeTaken > 5.0) {
                ostringstream os;
                ChunkAttr *chunk = GetCurrChunk(fd);

                for (uint32_t i = 0; i < chunk->chunkServerLoc.size(); i++)
                    os << chunk->chunkServerLoc[i].ToString().c_str() << ' ';

                KFS_LOG_VA_INFO("Writes thru chain %s for chunk %lld are taking: %.3f secs", 
                                os.str().c_str(), GetCurrChunk(fd)->chunkId, timeTaken);
            }
            
            KFS_LOG_VA_DEBUG("Total Time to write data to server(s): %.4f secs", timeTaken);
        }

        if (res >= 0)
            break;

        // write failure...retry
        ostringstream os;
        ChunkAttr *chunk = GetCurrChunk(fd);
            
        for (uint32_t i = 0; i < chunk->chunkServerLoc.size(); i++)
            os << chunk->chunkServerLoc[i].ToString().c_str() << ' ';

        // whatever be the error, wait a bit and retry...
        KFS_LOG_VA_INFO("Daisy-chain: %s; Will retry allocation/write on chunk %lld due to error code: %d", 
                        os.str().c_str(), GetCurrChunk(fd)->chunkId, res);
        Sleep(LEASE_RETRY_DELAY_SECS);

        // save the value of res; in case we tried too many times
        // and are giving up, we need the error to propogate
        int r;
        if ((r = DoAllocation(fd, true)) < 0) {
            KFS_LOG_VA_INFO("Re-allocation on chunk %lld failed because of error code = %d", 
                            GetCurrChunk(fd)->chunkId, r);
            return r;
        }
    }

    if (res < 0) {
        // any other error
        string errstr = ErrorCodeToStr(res);
        ChunkAttr *chunk = GetCurrChunk(fd);

        KFS_LOG_VA_INFO("Retries failed: Write on chunk %lld failed because of error: (code = %d) %s", 
                        chunk->chunkId, res, errstr.c_str());
    }

    return res;
}

int
KfsClientImpl::DoAllocation(int fd, bool force)
{
    ChunkAttr *chunk = NULL;
    FileAttr *fa = FdAttr(fd);
    int res = 0;

    if (IsCurrChunkAttrKnown(fd))
	chunk = GetCurrChunk(fd);

    if (chunk && force)
	chunk->didAllocation = false;

    if ((chunk == NULL) || (chunk->chunkId == (kfsChunkId_t) -1) ||
	(!chunk->didAllocation)) {
	// if we couldn't locate the chunk, it must be a new one.
	// also, if it is an existing chunk, force an allocation
	// if needed (which'll cause version # bumps, lease
	// handouts etc).
	for (uint8_t retryCount = 0; retryCount < NUM_RETRIES_PER_OP; retryCount++) {
	    if (retryCount) {
		if (res == -EBUSY)
		    // the metaserver says it can't get us a lease for
		    // the chunk.  so, wait for a bit for the lease to
		    // expire and then retry
		    Sleep(LEASE_RETRY_DELAY_SECS);
		else
		    Sleep(RETRY_DELAY_SECS);
	    }
	    res = AllocChunk(fd);
	    if ((res >= 0) || (!NeedToRetryAllocation(res))) {
		break;
	    }
	    // allocation failed...retry
	}
	if (res < 0)
	    // allocation failed...bail
	    return res;
	chunk = GetCurrChunk(fd);
	assert(chunk != NULL);
	chunk->didAllocation = true;
	if (force) {
	    KFS_LOG_VA_INFO("Forced allocation: chunk=%lld, version=%lld",
                            chunk->chunkId, chunk->chunkVersion);
	}
        else {
            ++fa->chunkCount;
        }
    }
    return 0;

}

ssize_t
KfsClientImpl::DoSmallWriteToServer(int fd, off_t offset, const char *buf, size_t numBytes)
{
    return DoLargeWriteToServer(fd, offset, buf, numBytes);
}

ssize_t
KfsClientImpl::DoLargeWriteToServer(int fd, off_t offset, const char *buf, size_t numBytes)
{
    size_t numAvail, numWrote = 0;
    ssize_t numIO;
    FilePosition *pos = FdPos(fd);
    ChunkAttr *chunk = GetCurrChunk(fd);
    ServerLocation loc = chunk->chunkServerLoc[0];
    TcpSocket *masterSock = FdPos(fd)->GetChunkServerSocket(loc);
    vector<WritePrepareOp *> ops;
    vector<WriteInfo> writeId;

    assert(KFS::CHUNKSIZE - offset >= 0);

    numAvail = min(numBytes, (size_t) (KFS::CHUNKSIZE - offset));

    // cout << "Pushing to server: " << offset << ' ' << numBytes << endl;

    // get the write id
    numIO = AllocateWriteId(fd, offset, numBytes, writeId, masterSock);
    if (numIO < 0)
        return numIO;

    // Split the write into a bunch of smaller ops
    while (numWrote < numAvail) {
	WritePrepareOp *op = new WritePrepareOp(nextSeq(), chunk->chunkId,
	                                        chunk->chunkVersion, writeId);

	op->numBytes = min(MAX_BYTES_PER_WRITE_IO, numAvail - numWrote);

	if ((op->numBytes % CHECKSUM_BLOCKSIZE) != 0) {
	    // if the write isn't aligned to end on a checksum block
	    // boundary, then break the write into two parts:
	    //(1) start at offset and end on a checksum block boundary
	    //(2) is the rest, which is less than the size of checksum
	    //block
	    // This simplifies chunkserver code: either the writes are
	    // multiples of checksum blocks or a single write which is
	    // smaller than a checksum block.
	    if (op->numBytes > CHECKSUM_BLOCKSIZE)
		op->numBytes = (op->numBytes / CHECKSUM_BLOCKSIZE) * CHECKSUM_BLOCKSIZE;
	    // else case #2 from above comment and op->numBytes is setup right
	}

	assert(op->numBytes > 0);

	op->offset = offset + numWrote;

	// similar to read, breakup the write if it is straddling
	// checksum block boundaries.
	if (OffsetToChecksumBlockStart(op->offset) != op->offset) {
	    op->numBytes = min((size_t) (OffsetToChecksumBlockEnd(op->offset) - op->offset),
	                       op->numBytes);
	}

	op->AttachContentBuf(buf + numWrote, op->numBytes);
	op->contentLength = op->numBytes;
        op->checksum = ComputeBlockChecksum(op->contentBuf, op->contentLength);

        {
            ostringstream os;

            os << "@offset: " << op->offset << " nbytes: " << op->numBytes
               << " cksum: " << op->checksum;
            KFS_LOG_VA_DEBUG("%s", os.str().c_str());
        }

	numWrote += op->numBytes;
	ops.push_back(op);
    }

    // For pipelined data push to work, we break the write into a
    // sequence of smaller ops and push them to the master; the master
    // then forwards each op to one replica, who then forwards to
    // next.

    numIO = DoPipelinedWrite(fd, ops, masterSock);

    if (numIO < 0) {
        //
        // the write failed; caller will do the retry
        //
        KFS_LOG_VA_INFO("Write failed...chunk = %lld, version = %lld, offset = %lld, error = %d",
                        ops[0]->chunkId, ops[0]->chunkVersion, ops[0]->offset,
                        numIO);
    }

    // figure out how much was committed
    numIO = 0;
    for (vector<KfsOp *>::size_type i = 0; i < ops.size(); ++i) {
	WritePrepareOp *op = static_cast<WritePrepareOp *> (ops[i]);
	if (op->status < 0)
	    numIO = op->status;
	else if (numIO >= 0)
	    numIO += op->status;
	op->ReleaseContentBuf();
	delete op;
    }

    if (numIO >= 0 && (off_t)chunk->chunkSize < offset + numIO) {
	// grow the chunksize only if we wrote past the last byte in the chunk
	chunk->chunkSize = offset + numIO;

	// if we wrote past the last byte of the file, then grow the
	// file size.  Note that, chunks 0..chunkNum-1 are assumed to
	// be full.  So, take the size of the last chunk and to that
	// add the size of the "full" chunks to get the size
	FileAttr *fa = FdAttr(fd);
	off_t eow = chunk->chunkSize + (pos->chunkNum  * KFS::CHUNKSIZE);
	fa->fileSize = max(fa->fileSize, eow);
    }

    if (numIO != (ssize_t) numBytes) {
	KFS_LOG_VA_DEBUG("Wrote to server (fd = %d), %lld bytes, was asked %lld bytes",
	                 fd, numIO, numBytes);
    } else {
        KFS_LOG_VA_DEBUG("Wrote to server (fd = %d), %lld bytes",
                         fd, numIO);
    }

    return numIO;
}

int
KfsClientImpl::AllocateWriteId(int fd, off_t offset, size_t numBytes,
                               vector<WriteInfo> &writeId, TcpSocket *masterSock)
{
    ChunkAttr *chunk = GetCurrChunk(fd);
    WriteIdAllocOp op(nextSeq(), chunk->chunkId, chunk->chunkVersion, offset, numBytes);
    int res;

    op.chunkServerLoc = chunk->chunkServerLoc;
    res = DoOpSend(&op, masterSock);
    if ((res < 0) || (op.status < 0)) {
        if (op.status < 0)
            return op.status;
        return res;
    }
    res = DoOpResponse(&op, masterSock);
    if ((res < 0) || (op.status < 0)) {
        if (op.status < 0)
            return op.status;
        return res;
    }

    // get rid of any old stuff
    writeId.clear();

    writeId.reserve(op.chunkServerLoc.size());
    istringstream ist(op.writeIdStr);
    for (uint32_t i = 0; i < chunk->chunkServerLoc.size(); i++) {
        ServerLocation loc;
        int64_t id;

        ist >> loc.hostname;
        ist >> loc.port;
        ist >> id;
	writeId.push_back(WriteInfo(loc, id));
    }
    return 0;
}

int
KfsClientImpl::PushData(int fd, vector<WritePrepareOp *> &ops, 
                        uint32_t start, uint32_t count, TcpSocket *masterSock)
{
    uint32_t last = min((size_t) (start + count), ops.size());
    int res = 0;

    for (uint32_t i = start; i < last; i++) {        
        res = DoOpSend(ops[i], masterSock);
        if (res < 0)
            break;
    }
    return res;
}

int
KfsClientImpl::SendCommit(int fd, vector<WriteInfo> &writeId, TcpSocket *masterSock,
                          WriteSyncOp &sop)
{
    ChunkAttr *chunk = GetCurrChunk(fd);
    int res = 0;

    sop.Init(nextSeq(), chunk->chunkId, chunk->chunkVersion, writeId);

    res = DoOpSend(&sop, masterSock);
    if (res < 0)
        return sop.status;

    return 0;
    
}

int
KfsClientImpl::GetCommitReply(WriteSyncOp &sop, TcpSocket *masterSock)
{
    int res;

    res = DoOpResponse(&sop, masterSock);
    if (res < 0)
        return sop.status;
    return sop.status;

}

int
KfsClientImpl::DoPipelinedWrite(int fd, vector<WritePrepareOp *> &ops, TcpSocket *masterSock)
{
    int res;
    vector<WritePrepareOp *>::size_type next, minOps;
    WriteSyncOp syncOp[2];

    if (ops.size() == 0)
        return 0;

    // push the data to the server; to avoid bursting the server with
    // a full chunk, do it in a pipelined fashion:
    //  -- send 512K of data; do a flush; send another 512K; do another flush
    //  -- every time we get an ack back, we send another 512K
    //
  
    // we got 2 commits: current is the one we just sent; previous is
    // the one for which we are expecting a reply
    int prevCommit = 0;
    int currCommit = 1;
  
    minOps = min((size_t) (MIN_BYTES_PIPELINE_IO / MAX_BYTES_PER_WRITE_IO) / 2, ops.size());

    res = PushData(fd, ops, 0, minOps, masterSock);
    if (res < 0)
        goto error_out;

    res = SendCommit(fd, ops[0]->writeInfo, masterSock, syncOp[0]);

    if (res < 0)
        goto error_out;
  
    for (next = minOps; next < ops.size(); next += minOps) {
        res = PushData(fd, ops, next, minOps, masterSock);
        if (res < 0)
            goto error_out;

        res = SendCommit(fd, ops[next]->writeInfo, masterSock, syncOp[currCommit]);
        if (res < 0)
            goto error_out;

        res = GetCommitReply(syncOp[prevCommit], masterSock);
        prevCommit = currCommit;
        currCommit++;
        currCommit %= 2;
        if (res < 0)
            // the commit for previous failed; there is still the
            // business of getting the reply for the "current" one
            // that we sent out.
            break;
    }

    res = GetCommitReply(syncOp[prevCommit], masterSock);

  error_out:
    if (res < 0) {
        // res will be -1; we need to pick out the error from the op that failed
        for (uint32_t i = 0; i < ops.size(); i++) {
            if (ops[i]->status < 0) {
                res = ops[i]->status;
                break;
            }
        }
    }

    // set the status for each op: either all were successful or none was.
    for (uint32_t i = 0; i < ops.size(); i++) {
        if (res < 0) 
            ops[i]->status = res;
        else
            ops[i]->status = ops[i]->numBytes;
    }
    return res;
}

int
KfsClientImpl::IssueCommit(int fd, vector<WriteInfo> &writeId, TcpSocket *masterSock)
{
    ChunkAttr *chunk = GetCurrChunk(fd);
    WriteSyncOp sop(nextSeq(), chunk->chunkId, chunk->chunkVersion, writeId);
    int res;

    res = DoOpSend(&sop, masterSock);
    if (res < 0)
        return sop.status;

    res = DoOpResponse(&sop, masterSock);
    if (res < 0)
        return sop.status;
    return sop.status;
}




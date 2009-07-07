//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: IOBuffer.cc 326 2009-04-25 00:45:50Z sriramsrao $ 
//
// Created 2006/03/15
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

#include <cerrno>
#include <unistd.h>

#include <iostream>
#include <algorithm>

#include "IOBuffer.h"
#include "Globals.h"

using std::min;
using std::list;

using namespace KFS;
using namespace KFS::libkfsio;

// To conserve memory, by default, we allocate IOBufferData in 4K
// blocks.  However, applications are free to change this default unit
// to what they like.
uint32_t IOBUFSIZE = 4096;

// Call this function if you want to change the default allocation unit.
void libkfsio::SetIOBufferSize(uint32_t bufsz)
{
    IOBUFSIZE = bufsz;
}

IOBufferData::IOBufferData(uint32_t bufsz)
{
    // cout << "Allocating: " << this << endl;
    Init(bufsz);
}

IOBufferData::IOBufferData()
{
    // cout << "Allocating: " << this << endl;
    Init(IOBUFSIZE);
}

inline int IOBufferData::MaxAvailable(int numBytes) const
{
    return std::max(0, std::min(int(SpaceAvailable()), numBytes));
}

inline int IOBufferData::MaxConsumable(int numBytes) const
{
    return std::max(0, std::min(BytesConsumable(), numBytes));
}

// setup a new IOBufferData for read access by block sharing.  
IOBufferData::IOBufferData(IOBufferDataPtr &other, char *s, char *e) :
    mData(other->mData), mStart(s), mEnd(e), mProducer(e), mConsumer(s) {

}

void
IOBufferData::Init(uint32_t bufsz)
{
    // we'd like to align the memory to 16-byte boundaries; so
    // allocate an additional align bytes and expose only the
    // aligned part
    int align = 16;
    off_t offset;
    
    mData.reset(new char [bufsz + align]);
    offset = (off_t) (mData.get());
    // adjust start to be suitably aligned
    mStart = (char *) (align + ((offset - 1) & ~(align - 1)));
    mEnd = mStart + bufsz;
    mProducer = mConsumer = mStart;
}

IOBufferData::~IOBufferData()
{
    // cout << "Deleting: " << this << endl;

    mData.reset();
    mProducer = mConsumer = NULL;
}

int IOBufferData::ZeroFill(int numBytes)
{
    const int nbytes = MaxAvailable(numBytes);
    memset(mProducer, '\0', nbytes);
    mProducer += nbytes;
    return nbytes;
}

int IOBufferData::Fill(int numBytes)
{
    const int nbytes = MaxAvailable(numBytes);
    mProducer += nbytes;
    return nbytes;
}

int IOBufferData::Consume(int numBytes)
{
    const int nbytes = MaxConsumable(numBytes);
    mConsumer += nbytes; 
    assert(mConsumer <= mProducer);
    return nbytes;
}

int IOBufferData::Trim(int numBytes)
{
    const int nbytes = MaxConsumable(numBytes);
    mProducer = mConsumer + nbytes;
    return nbytes;
}

int IOBufferData::Read(int fd)
{
    int numBytes = mEnd - mProducer;
    int nread;

    assert(numBytes > 0);

    if (numBytes <= 0)
        return -1;

    nread = read(fd, mProducer, numBytes);

    if (nread > 0) {
        mProducer += nread;
        globals().ctrNetBytesRead.Update(nread);
    }
    
    return nread;
}

int IOBufferData::Write(int fd)
{
    int numBytes = mProducer - mConsumer;
    int nwrote;

    assert(numBytes > 0);

    if (numBytes <= 0) {
        return -1;
    }

    nwrote = write(fd, mConsumer, numBytes);

    if (nwrote > 0) {
        mConsumer += nwrote;
        globals().ctrNetBytesWritten.Update(nwrote);
    }

    return nwrote;
}

int IOBufferData::CopyIn(const char *buf, int numBytes)
{
    const int nbytes = MaxAvailable(numBytes);
    memmove(mProducer, buf, nbytes);
    mProducer += nbytes;
    return nbytes;
}

int IOBufferData::CopyIn(const IOBufferData *other, int numBytes)
{
    const int nbytes = MaxAvailable(
        std::min(numBytes, other->BytesConsumable()));
    memmove(mProducer, other->mConsumer, nbytes);
    mProducer += nbytes;
    return nbytes;
}

int IOBufferData::CopyOut(char *buf, int numBytes)
{
    const int nbytes = MaxConsumable(numBytes);
    memmove(buf, mConsumer, nbytes);
    return nbytes;
}

IOBuffer::IOBuffer()
{

}

IOBuffer::~IOBuffer()
{

}

void IOBuffer::Append(const IOBufferDataPtr &buf)
{
    mBuf.push_back(buf);
}

void IOBuffer::Append(IOBuffer *ioBuf)
{
    list<IOBufferDataPtr>::iterator iter;
    IOBufferDataPtr data;

    for (iter = ioBuf->mBuf.begin(); iter != ioBuf->mBuf.end(); iter++) {
        data = *iter;
        mBuf.push_back(data);
    }
    ioBuf->mBuf.clear();
}

int IOBuffer::Move(IOBuffer *other, int numBytes)
{
    list<IOBufferDataPtr>::iterator iter;
    IOBufferDataPtr data, dataCopy;
    int bytesMoved = 0;

    assert(other->BytesConsumable() >= numBytes);

    iter = other->mBuf.begin();
    while ((iter != other->mBuf.end()) &&
           (bytesMoved < numBytes)) {
        data = *iter;
        if (data->BytesConsumable() + bytesMoved < numBytes) {
            other->mBuf.pop_front();
            bytesMoved += data->BytesConsumable();
            mBuf.push_back(data);
        } else {
            // this is the last buffer being moved; only partial data
            // from the buffer needs to be moved.  do the move by
            // sharing the block (and therby avoid data copy)
            int bytesToMove = numBytes - bytesMoved;
            dataCopy.reset(new IOBufferData(data, data->Consumer(), 
                                            data->Consumer() + bytesToMove));
            mBuf.push_back(dataCopy);
            other->Consume(bytesToMove);
            bytesMoved += bytesToMove;
            assert(bytesMoved >= numBytes);
        }
        iter = other->mBuf.begin();
    }
    return bytesMoved;
}

void IOBuffer::Splice(IOBuffer *other, int offset, int numBytes)
{
    list<IOBufferDataPtr>::iterator iter, insertPt = mBuf.begin();
    IOBufferDataPtr data, dataCopy;
    int startPos = 0, extra;

    extra = offset - BytesConsumable();
    while (extra > 0) {
        int zeroed = min(IOBUFSIZE, (uint32_t) extra);
        data.reset(new IOBufferData());
        data->ZeroFill(zeroed);
        extra -= zeroed;
        mBuf.push_back(data);
    }
    assert(BytesConsumable() >= offset);

    assert(other->BytesConsumable() >= numBytes);

    // find the insertion point
    iter = mBuf.begin();
    while ((iter != mBuf.end()) &&
           (startPos < offset)) {
        data = *iter;
        if (data->BytesConsumable() + startPos > offset) {
            int bytesToCopy = offset - startPos;

            dataCopy.reset(new IOBufferData());

            dataCopy->CopyIn(data.get(), bytesToCopy);
            data->Consume(bytesToCopy);
            mBuf.insert(iter, dataCopy);
            startPos += dataCopy->BytesConsumable();
        } else {
            startPos += data->BytesConsumable();
            ++iter;
        }
        insertPt = iter;
    }

    // get rid of stuff between [offset...offset+numBytes]
    while ((iter != mBuf.end()) &&
           (startPos < offset + numBytes)) {
        data = *iter;
        extra = data->BytesConsumable();
        if (startPos + extra > offset + numBytes) {
            extra = offset + numBytes - startPos;
        }
        data->Consume(extra);
        startPos += extra;
        ++iter;
    }

    // now, put the thing at insertPt
    if (insertPt != mBuf.end())
        mBuf.splice(insertPt, other->mBuf);
    else {
        iter = other->mBuf.begin();
        while (iter != other->mBuf.end()) {
            data = *iter;
            mBuf.push_back(data);
            other->mBuf.pop_front();
            iter = other->mBuf.begin();            
        }
    }
}

void IOBuffer::ZeroFill(int numBytes)
{
    IOBufferDataPtr data;

    while (numBytes > 0) {
        int zeroed = min(IOBUFSIZE, (uint32_t) numBytes);
        data.reset(new IOBufferData());
        data->ZeroFill(zeroed);
        numBytes -= zeroed;
        mBuf.push_back(data);
    }

}

int IOBuffer::Read(int fd)
{
    IOBufferDataPtr data;
    int numRead = 0, res = -EAGAIN;

    if (mBuf.empty()) {
        data.reset(new IOBufferData());
        mBuf.push_back(data);
    }

    while (1) {
        data = mBuf.back();
        
        if (data->IsFull()) {
            data.reset(new IOBufferData());
            mBuf.push_back(data);
            continue;
        }
        res = data->Read(fd);
        if (res <= 0)
            break;
        numRead += res;
#if 0
        // XXX: experimental; we want to read minimal amoutn and see
        // if we can process it.
        if (numRead >= (int) (2 * IOBUFSIZE))
            break;
#endif
    }

    if ((numRead == 0) && (res < 0))
        // even when res = -1, we get an errno of 0
        return errno == 0 ? -EAGAIN : errno;

    return numRead;
}


int IOBuffer::Write(int fd)
{
    int res = -EAGAIN, numWrote = 0;
    IOBufferDataPtr data;
    bool didSend = false;

    while (!mBuf.empty()) {
        data = mBuf.front();
        if (data->IsEmpty()) {
            mBuf.pop_front();
	    continue;
        }
        assert(data->BytesConsumable() > 0);
        didSend = true;
        res = data->Write(fd);
        if (res <= 0)
            break;

        numWrote += res;
    }
    if (!didSend)
        return -EAGAIN;

    if ((numWrote == 0) && (res < 0)) {
        // even when res = -1, we get an errno of 0
        return errno == 0 ? -EAGAIN : errno;
    }

    return numWrote;
}

int
IOBuffer::BytesConsumable() const
{
    list<IOBufferDataPtr>::const_iterator iter;
    int numBytes = 0;

    for (iter = mBuf.begin(); iter != mBuf.end(); iter++) {
        const IOBufferDataPtr data = *iter;
        numBytes += data->BytesConsumable();
    }
    return numBytes;
}

void IOBuffer::Consume(int nbytes)
{
    list<IOBufferDataPtr>::iterator iter;
    IOBufferDataPtr data;
    int bytesConsumed;

    assert(nbytes >= 0);
    iter = mBuf.begin();
    while (iter != mBuf.end()) {
        data = *iter;
        bytesConsumed = data->Consume(nbytes);
        nbytes -= bytesConsumed;
        if (data->IsEmpty())
            mBuf.pop_front();
        if (nbytes <= 0)
            break;
        iter = mBuf.begin();
    }
    assert(nbytes == 0);
}

void IOBuffer::Trim(int nbytes)
{
    list<IOBufferDataPtr>::iterator iter;
    IOBufferDataPtr data;
    int bytesAvail, totBytes = 0;

    if (nbytes <= 0)
        return;

    iter = mBuf.begin();
    while (iter != mBuf.end()) {
        data = *iter;
        bytesAvail = data->BytesConsumable();
        if (bytesAvail + totBytes <= nbytes) {
            totBytes += bytesAvail;
            ++iter;
            continue;
        }
        if (totBytes == nbytes)
            break;

        data->Trim(nbytes - totBytes);
        ++iter;
        break;
    }
    
    while (iter != mBuf.end()) {
        data = *iter;
        data->Consume(data->BytesConsumable());
        ++iter;
    }
    assert(BytesConsumable() == nbytes);
}

int IOBuffer::CopyIn(const char *buf, int numBytes)
{
    IOBufferDataPtr data;
    int numCopied = 0, bytesCopied;

    if (mBuf.empty()) {
    	data.reset(new IOBufferData());
	mBuf.push_back(data);
    } else {
        data = mBuf.back();
    }

    while (numCopied < numBytes) {
        assert(data.get() != NULL);
        bytesCopied = data->CopyIn(buf + numCopied, 
                                   numBytes - numCopied);
        numCopied += bytesCopied;
        if (numCopied >= numBytes)
            break;
    	data.reset(new IOBufferData());
	mBuf.push_back(data);
    }

    return numCopied;
}

int IOBuffer::CopyOut(char *buf, int numBytes)
{
    list<IOBufferDataPtr>::iterator iter;
    IOBufferDataPtr data;
    char *curr = buf;
    int nread = 0, copied;

    buf[0] = '\0';
    for (iter = mBuf.begin(); iter != mBuf.end(); iter++) {
        data = *iter;
        copied = data->CopyOut(curr, numBytes - nread);
        assert(copied >= 0);
        curr += copied;
        nread += copied;
        assert(curr <= buf + numBytes);

        if (nread >= numBytes)
            break;
    }

    return nread;
}

//
// Clone the contents of an IOBuffer by block sharing
//
IOBuffer *IOBuffer::Clone()
{
    IOBuffer *clone = new IOBuffer();

    list<IOBufferDataPtr>::iterator iter;
    IOBufferDataPtr data1, data2;
    int numBytes;

    for (iter = mBuf.begin(); iter != mBuf.end(); iter++) {
        data1 = *iter;
        numBytes = data1->BytesConsumable();
        data2.reset(new IOBufferData(data1, data1->Consumer(), 
                                     data1->Consumer() + numBytes));
        clone->mBuf.push_back(data2);
    }

    return clone;
}

bool IOBuffer::IsEmpty() const
{
    for (list<IOBufferDataPtr>::const_iterator iter = mBuf.begin(); 
         iter != mBuf.end(); iter++) {
        const IOBufferDataPtr data = *iter;
        if (!data->IsEmpty())
            return false;
    }
    return true;
}

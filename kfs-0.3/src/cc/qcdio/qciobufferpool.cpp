//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: //depot/main/platform/kosmosfs/src/cc/qcdio/qciobufferpool.cpp#1 $
//
// Created 2008/11/01
// Author: Mike Ovsiannikov
//
// Copyright 2008,2009 Quantcast Corp.
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

#include "qciobufferpool.h"
#include "qcutils.h"
#include "qcdebug.h"
#include "qcstutils.h"
#include "qcdllist.h"

#include <sys/mman.h>
#include <errno.h>
#include <unistd.h>

class QCIoBufferPool::Partition
{
public:
    Partition()
        : mAllocPtr(0),
          mAllocSize(0),
          mStartPtr(0),
          mEndPtr(0),
          mFreeListPtr(0),
          mTotalCnt(0),
          mFreeCnt(0)
        { List::Init(*this); }
    ~Partition()
        { Partition::Destroy(); }
    int Create(
        int  inNumBuffers,
        int  inBufferSize,
        bool inLockMemoryFlag)
    {
        if (inBufferSize < (int)sizeof(Buffer)) {
            return EINVAL;
        }
        Destroy();
        if (inNumBuffers <= 0) {
            return 0;
        }
        size_t const kPageSize = sysconf(_SC_PAGESIZE);
        mAllocSize = size_t(inNumBuffers) * (inBufferSize + 1);
        mAllocSize = (mAllocSize + kPageSize - 1) / kPageSize * kPageSize;
        mAllocPtr = mmap(0, mAllocSize,
            PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANON, -1, 0);
        if (mAllocPtr == MAP_FAILED) {
            mAllocPtr = 0;
            return errno;
        }
        if (inLockMemoryFlag && mlock(mAllocPtr, mAllocSize) != 0) {
            Destroy();
            return errno;
        }
        mStartPtr += (((char*)mAllocPtr - (char*)0) + inBufferSize - 1) /
            inBufferSize * inBufferSize;
        mEndPtr      = mStartPtr;
        mTotalCnt    = inNumBuffers;
        mFreeListPtr = 0;
        mFreeCnt     = 0;
        for (; ;) {
            QCVERIFY(Put(mEndPtr));
            if (mFreeCnt >= mTotalCnt) {
                break;
            }
            mEndPtr += inBufferSize;
        }
        return 0;
    }
    void Destroy()
    {
        if (mAllocPtr && munmap(mAllocPtr, mAllocSize) != 0) {
            QCUtils::FatalError("munmap", errno);
        }
        mAllocPtr    = 0;
        mAllocSize   = 0;
        mStartPtr    = 0;
        mEndPtr      = 0;
        mFreeListPtr = 0;
        mTotalCnt    = 0;
        mFreeCnt     = 0;
    }
    char* Get()
    {
        if (! mFreeListPtr) {
            QCASSERT(mFreeCnt == 0);
            return 0;
        }
        QCASSERT(mFreeCnt >= 0);
        mFreeCnt--;
        return mFreeListPtr->Get(mFreeListPtr);
    }
    bool Put(
        char* inPtr)
    {
        if (inPtr < mStartPtr || inPtr > mEndPtr) {
            return false;
        }
        QCRTASSERT(mTotalCnt > mFreeCnt);
        mFreeListPtr = new(inPtr) Buffer(mFreeListPtr);
        mFreeCnt++;
        return true;
    }
    int GetFreeCount() const
        { return mFreeCnt; }
    int GetTotalCount() const
        { return mTotalCnt; }
    bool IsEmpty() const
        { return (mFreeCnt <= 0); }
    bool IsFull() const
        { return (mFreeCnt >= mTotalCnt); }

    typedef QCDLList<Partition, 0> List;
private:
    friend class QCDLListOp<Partition, 0>;
    friend class QCDLListOp<const Partition, 0>;

    class Buffer
    {
    public:
        Buffer(
            Buffer* inNextPtr)
            : mNextPtr(inNextPtr)
            {}
        char* Get(
            Buffer*& outListPtr)
        {
            outListPtr = mNextPtr;
            return reinterpret_cast<char*>(this);
        }
        void* operator new(size_t, void* inPtr)
            { return inPtr; }
    private:
        Buffer* mNextPtr;

        ~Buffer();
        Buffer(
            const Buffer& inBuffer);
        Buffer& operator=(
            const Buffer& inBuffer);
    };

    void*      mAllocPtr;
    size_t     mAllocSize;
    char*      mStartPtr;
    char*      mEndPtr;
    Buffer*    mFreeListPtr;
    int        mTotalCnt;
    int        mFreeCnt;
    Partition* mPrevPtr[1];
    Partition* mNextPtr[1];
};

typedef QCDLList<QCIoBufferPool::Client, 0> QCIoBufferPoolClientList;

QCIoBufferPool::Client::Client()
    : mPoolPtr(0)
{
    QCIoBufferPoolClientList::Init(*this);
}

    bool
QCIoBufferPool::Client::Unregister()
{
    return (mPoolPtr && mPoolPtr->UnRegister(*this));
}

QCIoBufferPool::QCIoBufferPool()
    : mMutex(),
      mBufferSize(0),
      mFreeCnt(0)
{
    QCIoBufferPoolClientList::Init(mClientListPtr);
    Partition::List::Init(mPartitionListPtr);
}

QCIoBufferPool::~QCIoBufferPool()
{
    QCStMutexLocker theLock(mMutex);
    QCIoBufferPool::Destroy();
    while (! QCIoBufferPoolClientList::IsEmpty(mClientListPtr)) {
        Client& theClient = *QCIoBufferPoolClientList::PopBack(mClientListPtr);
        QCASSERT(theClient.mPoolPtr == this);
        theClient.mPoolPtr = 0;
    }
}

    int
QCIoBufferPool::Create(
    int          inPartitionCount,
    int          inPartitionBufferCount,
    int          inBufferSize,
    bool         inLockMemoryFlag)
{
    QCStMutexLocker theLock(mMutex);
    Destroy();
    mBufferSize = inBufferSize;
    int theErr = 0;
    for (int i = 0; i < inPartitionCount; i++) {
        Partition& thePart = *(new Partition());
        Partition::List::PushBack(mPartitionListPtr, thePart);
        theErr = thePart.Create(
            inPartitionBufferCount, inBufferSize, inLockMemoryFlag);
        if (theErr) {
            Destroy();
            break;
        }
        mFreeCnt += thePart.GetFreeCount();
    }
    return theErr;
}

    void
QCIoBufferPool::Destroy()
{
    QCStMutexLocker theLock(mMutex);
    while (! Partition::List::IsEmpty(mPartitionListPtr)) {
        delete Partition::List::PopBack(mPartitionListPtr);
    }
    mBufferSize = 0;
    mFreeCnt    = 0;
}

    char*
QCIoBufferPool::Get(
    QCIoBufferPool::RefillReqId inRefillReqId /* = kRefillReqIdUndefined */)
{
    QCStMutexLocker theLock(mMutex);
    if (mFreeCnt <= 0 && ! TryToRefill(inRefillReqId, 1)) {
        return 0;
    }
    QCASSERT(mFreeCnt >= 1);
    // Always start from the first partition, to try to keep next
    // partitions full, and be able to reclaim these if needed.
    Partition::List::Iterator theItr(mPartitionListPtr);
    Partition* thePtr;
    while ((thePtr = theItr.Next()) && thePtr->IsEmpty())
        {}
    char* const theBufPtr = thePtr ? thePtr->Get() : 0;
    QCASSERT(theBufPtr && mFreeCnt > 0);
    mFreeCnt--;
    return theBufPtr;
}

    bool
QCIoBufferPool::Get(
    QCIoBufferPool::OutputIterator& inIt,
    int                             inBufCnt,
    QCIoBufferPool::RefillReqId     inRefillReqId /* = kRefillReqIdUndefined */)
{
    if (inBufCnt <= 0) {
        return true;
    }
    QCStMutexLocker theLock(mMutex);
    if (mFreeCnt < inBufCnt && ! TryToRefill(inRefillReqId, inBufCnt)) {
        return false;
    }
    QCASSERT(mFreeCnt >= inBufCnt);
    Partition::List::Iterator theItr(mPartitionListPtr);
    for (int i = 0; i < inBufCnt; ) {
        Partition* thePPtr;
        while ((thePPtr = theItr.Next()) && thePPtr->IsEmpty())
            {}
        QCASSERT(thePPtr);
        for (char* theBPtr; i < inBufCnt && (theBPtr = thePPtr->Get()); i++) {
            mFreeCnt--;
            inIt.Put(theBPtr);
        }
    }
    return true;
}

    void
QCIoBufferPool::Put(
    char* inBufPtr)
{
    if (! inBufPtr) {
        return;
    }
    QCStMutexLocker theLock(mMutex);
    PutSelf(inBufPtr);
}

    void
QCIoBufferPool::Put(
    QCIoBufferPool::InputIterator& inIt,
    int                            inBufCnt)
{
    if (inBufCnt < 0) {
        return;
    }
    QCStMutexLocker theLock(mMutex);
    for (int i = 0; i < inBufCnt; i++) {
        char* const theBufPtr = inIt.Get();
        if (! theBufPtr) {
            break;
        }
        PutSelf(theBufPtr);
    }
}

    bool
QCIoBufferPool::Register(
    QCIoBufferPool::Client& inClient)
{
    QCStMutexLocker theLock(mMutex);
    if (inClient.mPoolPtr) {
        return (inClient.mPoolPtr == this);
    }
    QCIoBufferPoolClientList::PushBack(mClientListPtr, inClient);
    inClient.mPoolPtr = this;
    return true;
}

    bool
QCIoBufferPool::UnRegister(
    QCIoBufferPool::Client& inClient)
{
    QCStMutexLocker theLock(mMutex);
    if (inClient.mPoolPtr != this) {
        return false;
    }
    QCIoBufferPoolClientList::Remove(mClientListPtr, inClient);
    inClient.mPoolPtr = 0;
    return true;
}

    void
QCIoBufferPool::PutSelf(
    char* inBufPtr)
{
    QCASSERT(mMutex.IsOwned());
    if (! inBufPtr) {
        return;
    }
    QCASSERT(((char*)mBufferSize - (char*)0) % mBufferSize == 0);
    Partition::List::Iterator theItr(mPartitionListPtr);
    Partition* thePtr;
    while ((thePtr = theItr.Next()) && ! thePtr->Put(inBufPtr))
        {}
    QCRTASSERT(thePtr);
    mFreeCnt++;
}

    bool
QCIoBufferPool::TryToRefill(
    QCIoBufferPool::RefillReqId inReqId,
    int                         inBufCnt)
{
    QCASSERT(mMutex.IsOwned());
    if (inReqId == kRefillReqIdUndefined) {
        return false;
    }
    QCIoBufferPoolClientList::Iterator theItr(mClientListPtr);    
    Client* thePtr;
    while ((thePtr = theItr.Next()) && mFreeCnt < inBufCnt) {
        QCASSERT(thePtr->mPoolPtr == this);
        {
            // QCStMutexUnlocker theUnlock(mMutex);
            thePtr->Release(inReqId, inBufCnt - mFreeCnt);
        }
    }
    return (mFreeCnt >= inBufCnt);
}

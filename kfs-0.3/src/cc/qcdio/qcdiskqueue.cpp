//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: //depot/main/platform/kosmosfs/src/cc/qcdio/qcdiskqueue.cpp#1 $
//
// Created 2008/11/11
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

#include "qcdiskqueue.h"
#include "qcthread.h"
#include "qcmutex.h"
#include "qcutils.h"
#include "qcstutils.h"
#include "qcdebug.h"

#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/uio.h>

class QCDiskQueue::Queue
{
public:
    Queue()
        : mMutex(),
          mWorkCond(),
          mFreeReqCond(),
          mBufferPoolPtr(0),
          mThreadsPtr(0),
          mBuffersPtr(0),
          mRequestsPtr(0),
          mFdPtr(0),
          mFilePendingReqCountPtr(0),
          mIoVecPtr(0),
          mLastBlockIdxPtr(0),
          mPendingReadBlockCount(0),
          mPendingWriteBlockCount(0),
          mPendingCount(0),
          mFreeCount(0),
          mThreadCount(0),
          mRequestBufferCount(0),
          mCompletionRunningCount(0),
          mFileCount(0),
          mFdCount(0),
          mBlockSize(0),
          mIoVecPerThreadCount(0),
          mFreeFdHead(kFreeFdEnd),
          mRunFlag(false)
        {}
    virtual ~Queue()
        { Queue::Stop(); }
    int Start(
        int             inThreadCount,
        int             inMaxQueueDepth,
        int             inMaxBuffersPerRequestCount,
        int             inFileCount,
        const char**    inFileNamesPtr,
        QCIoBufferPool& inBufferPool);
    void Stop()
    {
        QCStMutexLocker theLock(mMutex);
        StopSelf();
    }
    void Run(
        int inThreadIndex);
    EnqueueStatus Enqueue(
        ReqType        inReqType,
        FileIdx        inFileIdx,
        BlockIdx       inStartBlockIdx,
        InputIterator* inBufferIteratorPtr,
        int            inBufferCount,
        IoCompletion*  inIoCompletionPtr,
        Time           inTimeWaitNanoSec);
    bool Cancel(
        RequestId inRequestId);
    IoCompletion* CancelOrSetCompletionIfInFlight(
        RequestId     inRequestId,
        IoCompletion* inCompletionIfInFlightPtr);
    void GetPendingCount(
        int&     inRequestCount,
        int64_t& inReadBlockCount,
        int64_t& inWriteBlockCount)
    {
        QCStMutexLocker theLock(mMutex);
        inRequestCount    = mPendingCount;
        inReadBlockCount  = mPendingReadBlockCount;
        inWriteBlockCount = mPendingWriteBlockCount;
    }
    OpenFileStatus OpenFile(
        const char* inFileNamePtr,
        int64_t     inMaxFileSize,
        bool        inReadOnlyFlag);
    CloseFileStatus CloseFile(
        FileIdx inFileIdx,
        int64_t  inFileSize);
    int GetBlockSize() const
        { return mBlockSize; }
    EnqueueStatus Sync(
        FileIdx       inFileIdx,
        IoCompletion* inIoCompletionPtr,
        Time          inTimeWaitNanoSec);

private:
    typedef unsigned int RequestIdx;
    enum
    {
           kBlockBitCount     = 48,
           kFileIndexBitCount = 16
    };
    enum {
        kOpenFlags = O_RDWR
#ifdef O_DIRECT
        | O_DIRECT
#endif
#ifdef O_NOATIME
        | O_NOATIME
#endif
    };
    class Request
    {
    private:
        Request()
            : mPrevIdx(0),
              mNextIdx(0),
              mReqType(kReqTypeNone),
              mBufferCount(0),
              mFileIdx(0),
              mBlockIdx(0),
              mIoCompletionPtr(0)
            {}
        ~Request()
            {}
        RequestIdx    mPrevIdx;
        RequestIdx    mNextIdx;
        ReqType       mReqType;
        int           mBufferCount;
        uint64_t      mFileIdx:16;
        uint64_t      mBlockIdx:48;
        IoCompletion* mIoCompletionPtr;
    friend class Queue;
    };
    class BuffersIterator : public InputIterator
    {
    public:
        BuffersIterator(
            char**       inFirstBufPtr,
            unsigned int theBufCount)
         : mCurPtr(inFirstBufPtr),
           mEndPtr(inFirstBufPtr + theBufCount)
        {}
        virtual char* Get()
            { return (mCurPtr < mEndPtr ? *mCurPtr++ : 0); }
    private:
        char**       mCurPtr;
        char** const mEndPtr;

    private:
        BuffersIterator(
            const BuffersIterator& inItr);
        BuffersIterator& operator=(
            const BuffersIterator& inItr);
    };
    class BuffersGetIterator : public OutputIterator
    {
    public:
        BuffersGetIterator(
            char**       inFirstBufPtr,
            unsigned int theBufCount)
         : mCurPtr(inFirstBufPtr),
           mEndPtr(inFirstBufPtr + theBufCount)
        {}
        virtual void Put(
            char* inBufferPtr)
        {
            QCRTASSERT(mCurPtr < mEndPtr);
            *mCurPtr++ = inBufferPtr;
        }
    private:
        char**       mCurPtr;
        char** const mEndPtr;

    private:
        BuffersGetIterator(
            const BuffersGetIterator& inItr);
        BuffersGetIterator& operator=(
            const BuffersGetIterator& inItr);
    };
    class IoThread : public QCThread
    {
    public:
        IoThread()
            : mThreadIndex(0),
              mQueuePtr(0)
            {}
        virtual ~IoThread()
            {}
        virtual void Run()
        {
            QCASSERT(mQueuePtr);
            mQueuePtr->Run(mThreadIndex);
        }
        int Start(
            Queue&      inQueue,
            int         inThreadIndex,
            int         inStackSize,
            const char* inNamePtr)
        {
            mThreadIndex = inThreadIndex;
            mQueuePtr    = &inQueue;
            return TryToStart(this, inStackSize, inNamePtr);
        }
    private:
        int    mThreadIndex;
        Queue* mQueuePtr;
    };

    QCMutex         mMutex;
    QCCondVar       mWorkCond;
    QCCondVar       mFreeReqCond;
    QCIoBufferPool* mBufferPoolPtr;
    IoThread*       mThreadsPtr;
    char**          mBuffersPtr;
    Request*        mRequestsPtr;
    int*            mFdPtr;
    unsigned int*   mFilePendingReqCountPtr;
    struct iovec*   mIoVecPtr;
    int64_t*        mLastBlockIdxPtr;
    int64_t         mPendingReadBlockCount;
    int64_t         mPendingWriteBlockCount;
    int             mPendingCount;
    int             mFreeCount;
    int             mThreadCount;
    int             mRequestBufferCount;
    int             mCompletionRunningCount;
    int             mFileCount;
    int             mFdCount;
    int             mBlockSize;
    int             mIoVecPerThreadCount;
    int             mFreeFdHead;
    bool            mRunFlag;

    enum
    {
        kFreeQueueIdx = 0,
        kIoQueueIdx   = 1,
        kRequestQueueCount
    };
    enum
    {
        kFreeFdOffset = 2,
        kFreeFdEnd    = -1
    };
    char** GetBuffersPtr(
        Request& inReq)
    {
        return (mBuffersPtr +
            ((&inReq - mRequestsPtr) - kRequestQueueCount) * mRequestBufferCount
        );
    }
    void Init(
        Request& inReq)
    {
        const RequestIdx theIdx(&inReq - mRequestsPtr);
        inReq.mPrevIdx = theIdx;
        inReq.mNextIdx = theIdx;
    }
    bool IsInList(
        Request& inReq)
    {
        const RequestIdx theIdx(&inReq - mRequestsPtr);
        return (inReq.mPrevIdx != theIdx || inReq.mNextIdx != theIdx);
    }
    void Insert(
        Request& inBefore,
        Request& inReq)
    {
        const RequestIdx theIdx(&inReq - mRequestsPtr);
        mRequestsPtr[inBefore.mPrevIdx].mNextIdx = theIdx;
        inReq.mPrevIdx = inBefore.mPrevIdx;
        inReq.mNextIdx = (RequestIdx)(&inBefore - mRequestsPtr);
        inBefore.mPrevIdx = theIdx;
    }
    void Remove(
        Request& inReq)
    {
        mRequestsPtr[inReq.mPrevIdx].mNextIdx = inReq.mNextIdx;
        mRequestsPtr[inReq.mNextIdx].mPrevIdx = inReq.mPrevIdx;
        Init(inReq);
    }
    Request* PopFront(
        RequestIdx inIdx)
    {
        const RequestIdx theIdx = mRequestsPtr[inIdx].mNextIdx;
        if (theIdx == inIdx) {
            return 0;
        }
        Request& theReq = mRequestsPtr[theIdx];
        Remove(theReq);
        return &theReq;
    }
    void Put(
        Request* inReqPtr)
    {
        if (inReqPtr) {
            Put(*inReqPtr);
        }
    }
    void Put(
        Request& inReq)
    {
        inReq.mReqType = kReqTypeNone;
        Insert(mRequestsPtr[kFreeQueueIdx], inReq);
        if (mFreeCount++ <= 0) {
            mFreeReqCond.Notify();
        }
    }
    Request* Get()
    {
        Request* const theRetPtr = PopFront(kFreeQueueIdx);
        if (theRetPtr) {
            mFreeCount--;
        }
        return theRetPtr;
    }
    void Enqueue(
        Request& inReq)
    {
        Insert(mRequestsPtr[kIoQueueIdx], inReq);
        mPendingCount++;
        mFilePendingReqCountPtr[inReq.mFileIdx]++;
        if (inReq.mReqType == kReqTypeRead) {
            mPendingReadBlockCount += inReq.mBufferCount;
        } else if (inReq.mReqType == kReqTypeWrite) {
            mPendingWriteBlockCount += inReq.mBufferCount;
        } else {
            QCRTASSERT(! "Bad request type");
        }
    }
    Request* Dequeue()
        { return PopFront(kIoQueueIdx); }
    RequestId GetRequestId(
        const Request& inReq) const
        { return (RequestId)(&inReq - mRequestsPtr); }
    bool Cancel(
        Request& inReq)
    {
        if (inReq.mReqType == kReqTypeNone) {
            return false; // Not in flight, or in the queue.
        }
        Remove(inReq);
        RequestComplete(inReq, kErrorCancel, 0, 0);
        return true;
    }
    void Process(
        Request&      inReq,
        int*          inFdPtr,
        struct iovec* inIoVecPtr);
    void RequestComplete(
        Request& inReq,
        Error    inError,
        int      inSysError,
        int64_t  inIoByteCount,
        bool     inFreeBuffersIfNoIoCompletion = false)
    {
        QCASSERT(mMutex.IsOwned());
        QCRTASSERT(
            mPendingCount > 0 &&
            // inReq.mFileIdx >= 0 && always true: unsigned
            int(inReq.mFileIdx) < mFileCount &&
            mFilePendingReqCountPtr[inReq.mFileIdx] > 0
        );
        mPendingCount--;
        mFilePendingReqCountPtr[inReq.mFileIdx]--;
        if (inReq.mReqType == kReqTypeRead) {
            mPendingReadBlockCount -= inReq.mBufferCount;
        } else if (inReq.mReqType == kReqTypeWrite) {
            mPendingWriteBlockCount -= inReq.mBufferCount;
        }
        BuffersIterator theItr(GetBuffersPtr(inReq), inReq.mBufferCount);
        inReq.mReqType = kReqTypeNone;
        if (! inReq.mIoCompletionPtr) {
            if (inFreeBuffersIfNoIoCompletion) {
                QCStMutexUnlocker theUnlock(mMutex);
                mBufferPoolPtr->Put(theItr, inReq.mBufferCount);
            }
            return;
        }
        mCompletionRunningCount++;
        {
            QCStMutexUnlocker theUnlock(mMutex);
            if (! inReq.mIoCompletionPtr->Done(
                    GetRequestId(inReq),
                    inReq.mFileIdx,
                    inReq.mBlockIdx,
                    theItr,
                    inReq.mBufferCount,
                    inError,
                    inSysError,
                    inIoByteCount)) {
                // Free buffers.
                BuffersIterator theItr(
                    GetBuffersPtr(inReq), inReq.mBufferCount);
                mBufferPoolPtr->Put(theItr, inReq.mBufferCount);
            }
        }
        mCompletionRunningCount--;
        Put(inReq);
    }
    void StopSelf();

private:
    Queue(
        const Queue& inQueue);
    Queue& operator=(
        const Queue& inQueue);
};

    void
QCDiskQueue::Queue::StopSelf()
{
    QCASSERT(mMutex.IsOwned());
    mRunFlag = false;
    mWorkCond.NotifyAll();
    for (int i = 0; i < mThreadCount; i++) {
        QCThread& theThread = mThreadsPtr[i];
        QCStMutexUnlocker theUnlock(mMutex);
        theThread.Join();
    }
    QCASSERT(mCompletionRunningCount == 0);
    if (mRequestsPtr) {
        Request* theReqPtr;
        while ((theReqPtr = Dequeue())) {
            Cancel(*theReqPtr);
        }
        QCASSERT(mPendingCount == 0);
    }
    while (mFdCount > 0) {
        if (mFdPtr[--mFdCount] >= 0) {
            close(mFdPtr[mFdCount]);
        }
    }
    delete [] mFdPtr;
    mFdPtr = 0;
    delete [] mFilePendingReqCountPtr;
    mFilePendingReqCountPtr = 0;
    delete [] mLastBlockIdxPtr;
    mLastBlockIdxPtr = 0;
    delete [] mThreadsPtr;
    mThreadsPtr = 0;
    delete [] mBuffersPtr;
    mBuffersPtr = 0;
    mRequestBufferCount = 0;
    delete [] mRequestsPtr;
    mRequestsPtr = 0;
    mFreeCount = 0;
    mPendingCount = 0;
    mBufferPoolPtr = 0;
    delete [] mIoVecPtr;
    mIoVecPtr = 0;
    mIoVecPerThreadCount = 0;
    mThreadCount = 0;
    mFreeFdHead = kFreeFdEnd;
    mFileCount = 0;
}

    int
QCDiskQueue::Queue::Start(
    int             inThreadCount,
    int             inMaxQueueDepth,
    int             inMaxBuffersPerRequestCount,
    int             inFileCount,
    const char**    inFileNamesPtr,
    QCIoBufferPool& inBufferPool)
{
    QCStMutexLocker theLock(mMutex);
    StopSelf();
    if (inFileCount <= 0 || inThreadCount <= 0 ||
            inThreadCount <= 0 || inMaxQueueDepth <= 0 ||
            inMaxBuffersPerRequestCount <= 0) {
        return 0;
    }
    if (inFileCount >= (1 << kFileIndexBitCount)) {
        return EINVAL;
    }
    mBufferPoolPtr = &inBufferPool;
#ifdef IOV_MAX
    const int kMaxIoVecCount = IOV_MAX;
#else
    const int kMaxIoVecCount = 1 << 10;
#endif
    mIoVecPerThreadCount = inMaxBuffersPerRequestCount;
    if (mIoVecPerThreadCount > kMaxIoVecCount) {
        mIoVecPerThreadCount = kMaxIoVecCount;
    }
    mFileCount = inFileCount;
    mIoVecPtr = new struct iovec[mIoVecPerThreadCount * inThreadCount];
    mBlockSize = inBufferPool.GetBufferSize();
    const int theFdCount = inThreadCount * mFileCount;
    mFdPtr = new int[theFdCount];
    mFilePendingReqCountPtr = new unsigned int[mFileCount];
    mLastBlockIdxPtr = new int64_t[mFileCount];
    mFreeFdHead = kFreeFdEnd;
    for (mFdCount = 0; mFdCount < theFdCount; ) {
        int theError = 0;
        for (int i = 0; i < mFileCount; i++) {
            int& theFd = mFdPtr[mFdCount];
            theFd = inFileNamesPtr ? open(inFileNamesPtr[i], kOpenFlags) : -1;
            if (theFd < 0 && inFileNamesPtr) {
                theError = errno;
                break;
            }
            if (theFd >= 0 && fcntl(theFd, FD_CLOEXEC, 1)) {
                theError = errno;
                break;
            }
            if (++mFdCount > mFileCount) {
                continue;
            }
            const off_t theSize = theFd >= 0 ? lseek(theFd, 0, SEEK_END) : 0;
            if (theSize < 0) {
                theError = errno;
                break;
            }
            mFilePendingReqCountPtr[i] = 0;
            // Allow last partial block.
            mLastBlockIdxPtr[i] =
                (int64_t(theSize) + mBlockSize - 1) / mBlockSize;
            if (mLastBlockIdxPtr[i] >= (int64_t(1) << kBlockBitCount)) {
                theError = EOVERFLOW;
                break;
            }
            if (theFd < 0) {
                theFd = mFreeFdHead;
                mFreeFdHead = -(i + kFreeFdOffset);
            }
        }
        if (theError) {
            StopSelf();
            return theError;
        }
    }
    mBuffersPtr = new char*[inMaxQueueDepth * inMaxBuffersPerRequestCount];
    mRequestBufferCount = inMaxBuffersPerRequestCount;
    const int theReqCnt = kRequestQueueCount + inMaxQueueDepth;
    mRequestsPtr = new Request[theReqCnt];
    Init(mRequestsPtr[kFreeQueueIdx]);
    Init(mRequestsPtr[kIoQueueIdx]);
    for (int i = kRequestQueueCount; i < theReqCnt; i++) {
        Put(mRequestsPtr[i]);
    }
    mThreadsPtr = new IoThread[inThreadCount];
    mRunFlag    = true;
    const int         kStackSize = 32 << 10;
    const char* const kNamePtr   = "IO";
    for (mThreadCount = 0; mThreadCount < inThreadCount; mThreadCount++) {
        const int theRet = mThreadsPtr[mThreadCount].Start(
            *this, mThreadCount, kStackSize, kNamePtr);
        if (theRet != 0) {
            StopSelf();
            return theRet;
        }
    }
    return 0;
}

    QCDiskQueue::EnqueueStatus
QCDiskQueue::Queue::Enqueue(
    QCDiskQueue::ReqType        inReqType,
    QCDiskQueue::FileIdx        inFileIdx,
    QCDiskQueue::BlockIdx       inBlockIdx,
    QCDiskQueue::InputIterator* inBufferIteratorPtr,
    int                         inBufferCount,
    QCDiskQueue::IoCompletion*  inIoCompletionPtr,
    QCDiskQueue::Time           inTimeWaitNanoSec)
{
    if ((inReqType != kReqTypeRead && inReqType != kReqTypeWrite) ||
            inBufferCount <= 0 || inBufferCount > mRequestBufferCount ||
            (! inBufferIteratorPtr && inReqType == kReqTypeWrite)) {
        return EnqueueStatus(kRequestIdNone, kErrorParameter);
    }
    QCStMutexLocker theLock(mMutex);
    if (! mRunFlag) {
        return EnqueueStatus(kRequestIdNone, kErrorQueueStopped);
    }
    if (inFileIdx < 0 || inFileIdx >= mFileCount || mFdPtr[inFileIdx] < 0) {
        return EnqueueStatus(kRequestIdNone, kErrorFileIdxOutOfRange);
    }
    if (inBlockIdx < 0 ||
            inBlockIdx + (inBufferIteratorPtr ? 0 : inBufferCount) >
            mLastBlockIdxPtr[inFileIdx]) {
        return EnqueueStatus(kRequestIdNone, kErrorBlockIdxOutOfRange);
    }
    Request* theReqPtr = Get();
    if (! theReqPtr) {
        if (inTimeWaitNanoSec < 0) {
            mFreeReqCond.Wait(mMutex);
        } else if (inTimeWaitNanoSec == 0 ||
                ! mFreeReqCond.Wait(mMutex, inTimeWaitNanoSec)) {
            return EnqueueStatus(kRequestIdNone, kErrorOutOfRequests);
        }
        theReqPtr = Get();
    }
    QCASSERT(theReqPtr);
    Request& theReq = *theReqPtr;
    theReq.mReqType         = inReqType;
    theReq.mBufferCount     = 0;
    theReq.mFileIdx         = inFileIdx;
    theReq.mBlockIdx        = inBlockIdx;
    theReq.mIoCompletionPtr = inIoCompletionPtr;
    char** const theBufsPtr = GetBuffersPtr(theReq);
    if (inBufferIteratorPtr) {
        for (int i = 0; i < inBufferCount; i++) {
            char* const thePtr = inBufferIteratorPtr->Get();
            if (! thePtr) {
                break;
            }
            theBufsPtr[theReq.mBufferCount++] = thePtr;
        }
    } else if (inReqType == kReqTypeRead) {
        // Defer buffer allocation.
        theBufsPtr[0] = 0;
        theReq.mBufferCount = inBufferCount;
    }
    if (theReq.mBlockIdx + theReq.mBufferCount >
            uint64_t(mLastBlockIdxPtr[theReq.mFileIdx])) {
        Put(theReq);
        return EnqueueStatus(kRequestIdNone, kErrorBlockIdxOutOfRange);
    }
    if (theReq.mBufferCount <= 0) {
        Put(theReq);
        return EnqueueStatus(kRequestIdNone, kErrorBlockCountOutOfRange);
    }
    Enqueue(theReq);
    mWorkCond.Notify();
    return GetRequestId(theReq);
}

    bool
QCDiskQueue::Queue::Cancel(
    QCDiskQueue::RequestId inRequestId)
{
    QCStMutexLocker theLock(mMutex);
    if (mPendingCount <= 0 ||
            inRequestId < kRequestQueueCount ||
            inRequestId >= kRequestQueueCount + mPendingCount + mFreeCount ||
            ! IsInList(mRequestsPtr[inRequestId]) // in flight
            ) {
        return false;
    }
    return Cancel(mRequestsPtr[inRequestId]);
}


    QCDiskQueue::IoCompletion*
QCDiskQueue::Queue::CancelOrSetCompletionIfInFlight(
    QCDiskQueue::RequestId     inRequestId,
    QCDiskQueue::IoCompletion* inCompletionIfInFlightPtr)
{
    QCStMutexLocker theLock(mMutex);
    if (mPendingCount <= 0 ||
            inRequestId < kRequestQueueCount ||
            inRequestId >= kRequestQueueCount + mPendingCount + mFreeCount) {
        return 0;
    }
    Request& theReq = mRequestsPtr[inRequestId];
    if (IsInList(theReq)) {
        IoCompletion* const theIoCompletionPtr = theReq.mIoCompletionPtr;
        return (Cancel(theReq) ? theIoCompletionPtr : 0);
    }
    if (theReq.mReqType == kReqTypeNone) {
        // Completion is already running.
        return theReq.mIoCompletionPtr;
    }
    // In flight, changed io completion.
    theReq.mIoCompletionPtr = inCompletionIfInFlightPtr;
    return inCompletionIfInFlightPtr;
}

    /* virtual */ void
QCDiskQueue::Queue::Run(
    int inThreadIndex)
{
    QCStMutexLocker theLock(mMutex);
    QCASSERT(inThreadIndex >= 0 && inThreadIndex < mThreadCount);
    int* const          theFdPtr    = mFdPtr +
        mFdCount / mThreadCount * inThreadIndex;
    struct iovec* const theIoVecPtr = mIoVecPtr +
        mIoVecPerThreadCount * inThreadIndex;
    while (mRunFlag) {
        Request* theReqPtr;
        while (! (theReqPtr = Dequeue()) && mRunFlag) {
            mWorkCond.Wait(mMutex);
        }
        if (mRunFlag) {
            QCASSERT(theReqPtr);
            Process(*theReqPtr, theFdPtr, theIoVecPtr);
        } else if (theReqPtr) {
            Cancel(*theReqPtr);
        }
    }
}

    void
QCDiskQueue::Queue::Process(
    Request&      inReq,
    int*          inFdPtr,
    struct iovec* inIoVecPtr)
{
    QCASSERT(mMutex.IsOwned());
    QCASSERT(mIoVecPerThreadCount > 0 && mBufferPoolPtr);
    QCRTASSERT(mLastBlockIdxPtr[inReq.mFileIdx] >= 0 &&
        inReq.mBlockIdx + inReq.mBufferCount <=
        uint64_t(mLastBlockIdxPtr[inReq.mFileIdx]));

    const int    theFd       = inFdPtr[inReq.mFileIdx];
    char** const theBufPtr   = GetBuffersPtr(inReq);
    const off_t  theOffset   = (off_t)inReq.mBlockIdx * mBlockSize;
    const bool   theReadFlag = inReq.mReqType == kReqTypeRead;
    QCASSERT((theReadFlag || inReq.mReqType == kReqTypeWrite) && theFd >= 0);

    QCStMutexUnlocker theUnlock(mMutex);
    Error      theError      = kErrorNone;
    int        theSysError   = 0;
    const bool theGetBufFlag = ! theBufPtr[0];
    if (theGetBufFlag) {
        QCASSERT(theReadFlag);
        BuffersGetIterator theIt(theBufPtr, inReq.mBufferCount);
        // Allocate buffers for read request.
        if (! mBufferPoolPtr->Get(theIt, inReq.mBufferCount,
                QCIoBufferPool::kRefillReqIdRead)) {
            theError = kErrorOutOfBuffers;
        }
    }
    if (theError == kErrorNone &&
            lseek(theFd, theOffset, SEEK_SET) != theOffset) {
        theError    = kErrorSeek;
        theSysError = errno;
    }
    int     theBufCnt    = inReq.mBufferCount;
    char**  theCurBufPtr = theBufPtr;
    int64_t theIoByteCnt = 0;
    while (theBufCnt > 0 && theError == kErrorNone) {
        const int theIoVecCnt = mIoVecPerThreadCount < theBufCnt ?
            mIoVecPerThreadCount : theBufCnt;
        ssize_t theIoBytes = 0;
        for (int i = 0; i < theIoVecCnt; i++) {
            QCASSERT(*theCurBufPtr);
            inIoVecPtr[i].iov_base = *theCurBufPtr++;
            inIoVecPtr[i].iov_len  = mBlockSize;
            theIoBytes += mBlockSize;
        }
        if (theReadFlag) {
            const ssize_t theNRd = readv(theFd, inIoVecPtr, theIoVecCnt);
            if (theNRd < 0) {
                theError = kErrorRead;
                theSysError = theNRd < 0 ? errno : 0;
                break;
            }
            theIoByteCnt += theNRd;
            if (theNRd < theIoBytes) {
                if (theGetBufFlag) {
                    // Short read -- release extra buffers.
                    const int theUnusedCnt =
                        theIoVecCnt - (theNRd + mBlockSize - 1) / mBlockSize;
                    BuffersIterator theIt(
                        theCurBufPtr - theUnusedCnt, theUnusedCnt);
                    mBufferPoolPtr->Put(theIt, theUnusedCnt);
                    inReq.mBufferCount -= theUnusedCnt;
                }
                break;
            }
        } else {
            const ssize_t theNWr = writev(theFd, inIoVecPtr, theIoVecCnt);
            if (theNWr > 0) {
                theIoByteCnt += theNWr;
            }
            if (theNWr != theIoBytes) {
                theError = kErrorWrite;
                theSysError = errno;
                break;
            }
        }
        theBufCnt -= theIoVecCnt;
    }
    if (theGetBufFlag && theError != kErrorNone && theBufPtr[0]) {
        BuffersIterator theIt(theBufPtr, inReq.mBufferCount);
        mBufferPoolPtr->Put(theIt, inReq.mBufferCount);
        theBufPtr[0] = 0;
    }
    theUnlock.Lock();
    RequestComplete(inReq, theError, theSysError, theIoByteCnt, theGetBufFlag);
}

    QCDiskQueue::OpenFileStatus
QCDiskQueue::Queue::OpenFile(
    const char* inFileNamePtr,
    int64_t     inMaxFileSize,
    bool        inReadOnlyFlag)
{
    QCStMutexLocker theLock(mMutex);
    if (mFreeFdHead == kFreeFdEnd) {
        return OpenFileStatus(-1, kErrorFileIdxOutOfRange, 0);
    }
    const int theIdx = -mFreeFdHead - kFreeFdOffset;
    QCRTASSERT(
        theIdx >= 0 && theIdx < mFileCount && mFdPtr[theIdx] <= kFreeFdEnd);
    mFreeFdHead = mFdPtr[theIdx];
    int theSysErr = 0;
    for (int i = theIdx; i < mFdCount; i += mFileCount) {
        const int theFd = open(inFileNamePtr, kOpenFlags);
        if (theFd < 0 || fcntl(theFd, FD_CLOEXEC, 1)) {
            theSysErr = errno ? errno : -1;
            break;
        }
        mFdPtr[i] = theFd;
        if (i >= mFileCount) {
            continue;
        }
        const off_t theSize = inMaxFileSize < 0 ?
            lseek(theFd, 0, SEEK_END) : inMaxFileSize;
        if (theSize < 0) {
            theSysErr = errno;
            break;
        }
        mFilePendingReqCountPtr[i] = 0;
        mLastBlockIdxPtr[i] = (int64_t(theSize) + mBlockSize - 1) / mBlockSize;
    }
    if (! theSysErr) {
        return OpenFileStatus(theIdx, kErrorNone, theSysErr);
    }
    for (int i = theIdx; i < mFdCount; i += mFileCount) {
        close(mFdPtr[i]);
        mFdPtr[i] = -1;
    }
    mFdPtr[theIdx] = mFreeFdHead;
    mFreeFdHead = -(theIdx + kFreeFdOffset);
    return OpenFileStatus(-1, kErrorOpen, theSysErr);
}

    QCDiskQueue::CloseFileStatus
QCDiskQueue::Queue::CloseFile(
    QCDiskQueue::FileIdx inFileIdx,
    int64_t              inFileSize)
{
    QCStMutexLocker theLock(mMutex);
    if (inFileIdx < 0 || inFileIdx >= mFileCount || mFdPtr[inFileIdx] < 0) {
        return CloseFileStatus(kErrorParameter, 0);
    }
    if (mFilePendingReqCountPtr[inFileIdx] > 0) {
        return CloseFileStatus(kErrorHasPendingRequests, 0);
    }
    int theSysErr = 0;
    const int theFd = mFdPtr[inFileIdx];
    for (int i = inFileIdx + mFileCount; i < mFdCount; i += mFileCount) {
        if (close(mFdPtr[i])) {
            theSysErr = errno ? errno : -1;
        }
        mFdPtr[i] = -1;
    }
    mFdPtr[inFileIdx] = mFreeFdHead;
    mFreeFdHead = -(inFileIdx + kFreeFdOffset);
    theLock.Unlock();
    if (theFd >= 0) {
        if (inFileSize >= 0 && ftruncate(theFd, (off_t)inFileSize)) {
            theSysErr = errno ? errno : -1;
        }
        if (close(theFd)) {
            theSysErr = errno ? errno : -1;
        }
    }
    return CloseFileStatus(theSysErr ? kErrorClose : kErrorNone, theSysErr);
}

    QCDiskQueue::EnqueueStatus
QCDiskQueue::Queue::Sync(
    QCDiskQueue::FileIdx       inFileIdx,
    QCDiskQueue::IoCompletion* inIoCompletionPtr,
    QCDiskQueue::Time          inTimeWaitNanoSec)
{
    QCStMutexLocker theLock(mMutex);
    if (! mRunFlag) {
        return EnqueueStatus(kRequestIdNone, kErrorQueueStopped);
    }
    if (inFileIdx < 0 || inFileIdx >= mFileCount || mFdPtr[inFileIdx] < 0) {
        return EnqueueStatus(kRequestIdNone, kErrorFileIdxOutOfRange);
    }
    Request* theReqPtr = Get();
    if (! theReqPtr) {
        if (inTimeWaitNanoSec < 0) {
            mFreeReqCond.Wait(mMutex);
        } else if (inTimeWaitNanoSec == 0 ||
                ! mFreeReqCond.Wait(mMutex, inTimeWaitNanoSec)) {
            return EnqueueStatus(kRequestIdNone, kErrorOutOfRequests);
        }
        theReqPtr = Get();
    }
    QCASSERT(theReqPtr);
    // FIXME: implement real io barrier, for now just queue empty read request.
    Request& theReq = *theReqPtr;
    theReq.mReqType         = kReqTypeRead;
    theReq.mBufferCount     = 0;
    theReq.mFileIdx         = inFileIdx;
    theReq.mBlockIdx        = 0;
    theReq.mIoCompletionPtr = inIoCompletionPtr;
    char** const theBufsPtr = GetBuffersPtr(theReq);
    theBufsPtr[0] = 0;
    return EnqueueStatus(kErrorNone);
}

class QCDiskQueue::RequestWaiter : public QCDiskQueue::IoCompletion
{
public:
    typedef QCDiskQueue::CompletionStatus CompletionStatus;

    RequestWaiter(
        OutputIterator* inOutIteratorPtr)
        : mMutex(),
          mDoneCond(),
          mOutIteratorPtr(inOutIteratorPtr),
          mCompletionStatus(kErrorEnqueue),
          mDoneFlag(false)
        {}
    virtual ~RequestWaiter()
    {
        if (! mDoneFlag) {
            RequestWaiter::Wait();
        }
    }
    virtual bool Done(
        RequestId      inRequestId,
        FileIdx        inFileIdx,
        BlockIdx       inStartBlockIdx,
        InputIterator& inBufferItr,
        int            inBufferCount,
        Error          inCompletionCode,
        int            inSysErrorCode,
        int64_t        inIoByteCount)
    {
        QCStMutexLocker theLock(mMutex);
        mDoneFlag = true;
        mCompletionStatus =
            CompletionStatus(inCompletionCode, inSysErrorCode, inIoByteCount);
        CopyBufs(&inBufferItr, inBufferCount);
        mDoneCond.Notify();
        return true;
    }
    CompletionStatus Wait(
        EnqueueStatus  inStatus,
        InputIterator* inBufferItrPtr,
        int            inBufferCount)
    {
        if (inStatus.IsError()) {
            mDoneFlag = true;
            mCompletionStatus = CompletionStatus(inStatus.GetError());
            CopyBufs(inBufferItrPtr, inBufferCount);
            return mCompletionStatus;
        }
        return Wait();
    }
private:
    QCMutex               mMutex;
    QCCondVar             mDoneCond;
    OutputIterator* const mOutIteratorPtr;
    CompletionStatus      mCompletionStatus;
    bool                  mDoneFlag;

    CompletionStatus Wait()
    {
        QCStMutexLocker theLock(mMutex);
        while (! mDoneFlag) {
            mDoneCond.Wait(mMutex);
        }
        return mCompletionStatus;
    }
    void CopyBufs(
        InputIterator* inBufferItrPtr,
        int            inBufferCount)
    {
        if (! mOutIteratorPtr || ! inBufferItrPtr) {
            return;
        }
        for (int i = 0; i < inBufferCount; i++) {
            char* const theBufPtr = inBufferItrPtr->Get();
            if (! theBufPtr) {
                break;
            }
            mOutIteratorPtr->Put(theBufPtr);
        }
    }
};

    /* static */ const char*
QCDiskQueue::ToString(
    QCDiskQueue::Error inErrorCode)
{
    switch (inErrorCode)
    {
        case kErrorNone:                 return "none";
        case kErrorRead:                 return "read";
        case kErrorWrite:                return "write";
        case kErrorCancel:               return "io cancelled";
        case kErrorSeek:                 return "seek";
        case kErrorEnqueue:              return "enqueue";
        case kErrorOutOfBuffers:         return "out of io buffers";
        case kErrorParameter:            return "invalid parameter";
        case kErrorQueueStopped:         return "queue stopped";
        case kErrorFileIdxOutOfRange:    return "file index out of range";
        case kErrorBlockIdxOutOfRange:   return "block index out of range";
        case kErrorBlockCountOutOfRange: return "block count out of range";
        case kErrorOutOfRequests:        return "out of requests";
        case kErrorHasPendingRequests:   return "has pending requests";
        default:                         return "invalid error code";
    }
}

QCDiskQueue::QCDiskQueue()
    : mQueuePtr(0)
{
}

QCDiskQueue::~QCDiskQueue()
{
    QCDiskQueue::Stop();
}

    int
QCDiskQueue::Start(
    int             inThreadCount,
    int             inMaxQueueDepth,
    int             inMaxBuffersPerRequestCount,
    int             inFileCount,
    const char**    inFileNamesPtr,
    QCIoBufferPool& inBufferPool)
{
    Stop();
    mQueuePtr = new Queue();
    const int theRet = mQueuePtr->Start(inThreadCount, inMaxQueueDepth,
        inMaxBuffersPerRequestCount, inFileCount, inFileNamesPtr, inBufferPool);
    if (theRet != 0) {
        Stop();
    }
    return theRet;
}

    void
QCDiskQueue::Stop()
{
    delete mQueuePtr;
    mQueuePtr = 0;
}

    QCDiskQueue::EnqueueStatus
QCDiskQueue::Enqueue(
    QCDiskQueue::ReqType        inReqType,
    QCDiskQueue::FileIdx        inFileIdx,
    QCDiskQueue::BlockIdx       inStartBlockIdx,
    QCDiskQueue::InputIterator* inBufferIteratorPtr,
    int                         inBufferCount,
    QCDiskQueue::IoCompletion*  inIoCompletionPtr,
    QCDiskQueue::Time           inTimeWaitNanoSec)
{
    if (! mQueuePtr) {
        return EnqueueStatus(kRequestIdNone, kErrorParameter);
    }
    return mQueuePtr->Enqueue(
        inReqType,
        inFileIdx,
        inStartBlockIdx,
        inBufferIteratorPtr,
        inBufferCount,
        inIoCompletionPtr,
        inTimeWaitNanoSec);
}

    bool
QCDiskQueue::Cancel(
    QCDiskQueue::RequestId inRequestId)
{
    return (mQueuePtr && mQueuePtr->Cancel(inRequestId));
}

    QCDiskQueue::IoCompletion*
QCDiskQueue::CancelOrSetCompletionIfInFlight(
    QCDiskQueue::RequestId     inRequestId,
    QCDiskQueue::IoCompletion* inCompletionIfInFlightPtr)
{
    return (mQueuePtr ? mQueuePtr->CancelOrSetCompletionIfInFlight(
        inRequestId, inCompletionIfInFlightPtr) : 0);
}

    void
QCDiskQueue::GetPendingCount(
    int&     inRequestCount,
    int64_t& inReadBlockCount,
    int64_t& inWriteBlockCount)
{
    if (mQueuePtr) {
        mQueuePtr->GetPendingCount(
            inRequestCount, inReadBlockCount, inWriteBlockCount);
    } else {
        inRequestCount    = 0;
        inReadBlockCount  = 0;
        inWriteBlockCount = 0;
    }
}

    QCDiskQueue::CompletionStatus
QCDiskQueue::SyncIo(
    QCDiskQueue::ReqType         inReqType,
    QCDiskQueue::FileIdx         inFileIdx,
    QCDiskQueue::BlockIdx        inStartBlockIdx,
    QCDiskQueue::InputIterator*  inBufferIteratorPtr,
    int                          inBufferCount,
    QCDiskQueue::OutputIterator* inOutBufferIteratroPtr)
{
    if (inBufferCount <= 0) {
        return CompletionStatus();
    }
    if (! inBufferIteratorPtr && ! inOutBufferIteratroPtr) {
        return CompletionStatus(kErrorEnqueue);
    }
    RequestWaiter theWaiter(inOutBufferIteratroPtr);
    return theWaiter.Wait(
        Enqueue(inReqType,
                inFileIdx,
                inStartBlockIdx,
                inBufferIteratorPtr,
                inBufferCount,
                &theWaiter),
        inBufferIteratorPtr,
        inBufferCount
    );
}

    QCDiskQueue::OpenFileStatus
QCDiskQueue::OpenFile(
    const char* inFileNamePtr,
    int64_t     inMaxFileSize  /* = -1 */,
    bool        inReadOnlyFlag /* = false */)
{
    return ((mQueuePtr && inFileNamePtr) ?
        mQueuePtr->OpenFile(inFileNamePtr, inMaxFileSize, inReadOnlyFlag) :
        OpenFileStatus(-1, kErrorParameter, 0)
    );
}

    QCDiskQueue::CloseFileStatus
QCDiskQueue::CloseFile(
    QCDiskQueue::FileIdx inFileIdx,
    int64_t              inFileSize /* = -1 */)
{
    return (mQueuePtr ?
        mQueuePtr->CloseFile(inFileIdx, inFileSize) :
        CloseFileStatus(kErrorParameter, 0)
    );
}

    QCDiskQueue::EnqueueStatus
QCDiskQueue::Sync(
    QCDiskQueue::FileIdx       inFileIdx,
    QCDiskQueue::IoCompletion* inIoCompletionPtr,
    QCDiskQueue::Time          inTimeWaitNanoSec /* = -1 */)
{
    return (mQueuePtr ?
        mQueuePtr->Sync(inFileIdx, inIoCompletionPtr, inTimeWaitNanoSec) :
        EnqueueStatus(kRequestIdNone, kErrorParameter)
    );
}

    int
QCDiskQueue::GetBlockSize() const
{
    return (mQueuePtr ? mQueuePtr->GetBlockSize() : 0);
}

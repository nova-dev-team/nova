//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: NetManager.cc 336 2009-04-28 18:41:46Z sriramsrao $
//
// Created 2006/03/14
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

#include <sys/poll.h>
#include <cerrno>
#include <boost/scoped_array.hpp>

#include "NetManager.h"
#include "TcpSocket.h"
#include "Globals.h"

#include "common/log.h"
#include "kfsutils.h"

using std::mem_fun;
using std::list;

using namespace KFS;
using namespace KFS::libkfsio;

NetManager::NetManager(int timeoutMs)
    : mRemove(),
      mTimerWheelBucketItr(mRemove.end()),
      mCurConnection(0),
      mCurTimerWheelSlot(0),
      mConnectionsCount(0),
      mDiskOverloaded(false),
      mNetworkOverloaded(false),
      mIsOverloaded(false),
      mRunFlag(true),
      mTimerRunningFlag(false),
      mTimeoutMs(timeoutMs),
      mNow(time(0)),
      mMaxOutgoingBacklog(0),
      mNumBytesToSend(0),
      mPoll(*(new FdPoll()))
{}

NetManager::~NetManager()
{
    NetManager::CleanUp();
    delete &mPoll;
}

void
NetManager::AddConnection(NetConnectionPtr &conn)
{
    NetConnection::NetManagerEntry* const entry =
        conn->GetNetMangerEntry(*this);
    if (! entry) {
        return;
    }
    if (! entry->mAdded) {
        entry->mTimerWheelSlot = kTimerWheelSize;
        entry->mListIt = mTimerWheel[kTimerWheelSize].insert(
            mTimerWheel[kTimerWheelSize].end(), conn);
        mConnectionsCount++;
        assert(mConnectionsCount > 0);
        entry->mAdded = true;
    }
    conn->Update();
}

void
NetManager::RegisterTimeoutHandler(ITimeout *handler)
{
    list<ITimeout *>::iterator iter;
    for (iter = mTimeoutHandlers.begin(); iter != mTimeoutHandlers.end(); 
         ++iter) {
        if (*iter == handler) {
            return;
        }
    }
    mTimeoutHandlers.push_back(handler);
}

void
NetManager::UnRegisterTimeoutHandler(ITimeout *handler)
{
    if (handler == NULL)
        return;

    list<ITimeout *>::iterator iter;
    for (iter = mTimeoutHandlers.begin(); iter != mTimeoutHandlers.end(); 
            ++iter) {
        if (*iter == handler) {
            // Not not remove list element: this can be called when iterating
            // trough the list.
            *iter = 0;
            return;
        }
    }
}

inline void
NetManager::UpdateTimer(NetConnection::NetManagerEntry& entry, int timeOut)
{
    assert(entry.mAdded);

    int timerWheelSlot;
    if (timeOut < 0) {
        timerWheelSlot = kTimerWheelSize;
    } else if ((timerWheelSlot = mCurTimerWheelSlot +
            // When the timer is running the effective wheel size "grows" by 1:
            // leave (move) entries with timeouts >= kTimerWheelSize in (to) the
            // current slot.
            std::min((kTimerWheelSize - mTimerRunningFlag ? 0 : 1), timeOut)) >=
            kTimerWheelSize) {
        timerWheelSlot -= kTimerWheelSize;
    }
    // This method can be invoked from timeout handler.
    // Make sure  that the entry doesn't get moved to the end of the current
    // list, which can be traversed by the timer.
    if (timerWheelSlot != entry.mTimerWheelSlot) {
        if (mTimerWheelBucketItr == entry.mListIt) {
            ++mTimerWheelBucketItr;
        }
        mTimerWheel[timerWheelSlot].splice(
            mTimerWheel[timerWheelSlot].end(),
            mTimerWheel[entry.mTimerWheelSlot], entry.mListIt);
        entry.mTimerWheelSlot = timerWheelSlot;
    }
}

inline static int CheckFatalSysError(int err, const char* msg)
{
    if (err) {
        std::string const msg = KFSUtils::SysError(err);
        KFS_LOG_VA_FATAL("%s: %d %s", err, msg.c_str());
        abort();
    }
    return err;
}

void
NetManager::Update(NetConnection::NetManagerEntry& entry, int fd, bool resetTimer)
{
    if (! entry.mAdded) {
        return;
    }
    assert(*entry.mListIt);
    NetConnection& conn = **entry.mListIt;
    assert(fd >= 0 || ! conn.IsGood());
    // Always check if connection has to be removed: this method always
    // called before socket fd gets closed.
    if (! conn.IsGood() || fd < 0) {
        if (entry.mFd >= 0) {
            CheckFatalSysError(
                mPoll.Remove(entry.mFd),
                "failed to removed fd from poll set"
            );
            entry.mFd = -1;
        }
        assert(mConnectionsCount > 0 &&
            entry.mWriteByteCount >= 0 &&
            entry.mWriteByteCount <= mNumBytesToSend);
        entry.mAdded = false;
        mConnectionsCount--;
        mNumBytesToSend -= entry.mWriteByteCount;
        if (mTimerWheelBucketItr == entry.mListIt) {
            ++mTimerWheelBucketItr;
        }
        mRemove.splice(mRemove.end(),
            mTimerWheel[entry.mTimerWheelSlot], entry.mListIt);
        return;
    }
    if (&conn == mCurConnection) {
        // Defer all updates for the currently dispatched connection until the
        // end of the event dispatch loop.
        return;
    }
    // Update timer.
    if (resetTimer) {
        const int timeOut = conn.GetInactivityTimeout();
        if (timeOut >= 0) {
            entry.mExpirationTime = mNow + timeOut;
        }
        UpdateTimer(entry, timeOut);
    }
    // Update pending send.
    assert(entry.mWriteByteCount >= 0 &&
        entry.mWriteByteCount <= mNumBytesToSend);
    mNumBytesToSend -= entry.mWriteByteCount;
    entry.mWriteByteCount = std::max(0, conn.GetNumBytesToWrite());
    mNumBytesToSend += entry.mWriteByteCount;
    // Update poll set.
    const bool in  = conn.IsReadReady() &&
        (! mIsOverloaded || entry.mEnableReadIfOverloaded);
    const bool out = conn.IsWriteReady() || entry.mConnectPending;
    if (in != entry.mIn || out != entry.mOut) {
        assert(fd >= 0);
        const int op =
            (in ? FdPoll::kOpTypeIn : 0) + (out ? FdPoll::kOpTypeOut : 0);
        if (fd != entry.mFd && entry.mFd >= 0) {
            CheckFatalSysError(
                mPoll.Remove(entry.mFd),
                "failed to removed fd from poll set"
            );
            entry.mFd = -1;
        }
        if (entry.mFd < 0) {
            if (op && CheckFatalSysError(
                    mPoll.Add(fd, op, &conn),
                    "failed to add fd to poll set") == 0) {
                entry.mFd = fd;
            }
        } else {
            CheckFatalSysError(
                mPoll.Set(fd, op, &conn),
                "failed to change pool flags"
            );
        }
        entry.mIn  = in  && entry.mFd >= 0;
        entry.mOut = out && entry.mFd >= 0;
    }
}

void
NetManager::MainLoop()
{
    mNow = time(0);
    time_t lastTimerTime = mNow;
    CheckFatalSysError(
        mPoll.Add(globals().netKicker.GetFd(), FdPoll::kOpTypeIn),
        "failed to add net kicker's fd to poll set"
    );
    const int timerOverrunWarningTime(mTimeoutMs / (1000/2));
    while (mRunFlag) {
        const bool wasOverloaded = mIsOverloaded;
        CheckIfOverloaded();
        if (mIsOverloaded != wasOverloaded) {
            KFS_LOG_VA_INFO(
                "%s (%0.f bytes to send; %d disk IO's) ",
                mIsOverloaded ?
                    "System is now in overloaded state " :
                    "Clearing system overload state",
                double(mNumBytesToSend),
                globals().diskManager.NumDiskIOOutstanding());
            for (int i = 0; i <= kTimerWheelSize; i++) {
                for (List::iterator c = mTimerWheel[i].begin();
                        c != mTimerWheel[i].end(); ) {
                    assert(*c);
                    NetConnection& conn = **c;
                    ++c;
                    conn.Update(false);
                }
            }
        }
        const int ret = mPoll.Poll(mConnectionsCount + 1, mTimeoutMs);
        if (ret < 0 && ret != -EINTR && ret != -EAGAIN) {
            std::string const msg = KFSUtils::SysError(-ret);
            KFS_LOG_VA_ERROR("poll errror %d %s",  -ret, msg.c_str());
        }
        globals().netKicker.Drain();
        const int64_t nowMs = ITimeout::NowMs();
        mNow = time_t(nowMs / 1000);
        globals().diskManager.ReapCompletedIOs();
        // Unregister will set pointer to 0, but will never remove the list
        // node, so that the iterator always remains valid.
        for (std::list<ITimeout *>::iterator it = mTimeoutHandlers.begin();
                it != mTimeoutHandlers.end(); ) {
            if (*it) {
                (*it)->TimerExpired(nowMs);
            }
            if (*it) {
                ++it;
            } else {
                it = mTimeoutHandlers.erase(it);
            }
        }
        /// Process poll events.
        int   op;
        void* ptr;
        while (mPoll.Next(op, ptr)) {
            if (op == 0 || ! ptr) {
                continue;
            }
            NetConnection& conn = *reinterpret_cast<NetConnection*>(ptr);
            if (! conn.GetNetMangerEntry(*this)->mAdded) {
                // Skip stale event, the conection should be in mRemove list.
                continue;
            }
            // Defer update for this connection.
            mCurConnection = &conn;
            if ((op & FdPoll::kOpTypeIn) != 0 && conn.IsGood()) {
                conn.HandleReadEvent();
            }
            if ((op & FdPoll::kOpTypeOut) != 0 && conn.IsGood()) {
                conn.HandleWriteEvent();
            }
            if ((op & (FdPoll::kOpTypeError | FdPoll::kOpTypeHup)) != 0 &&
                    conn.IsGood()) {
                conn.HandleErrorEvent();
            }
            // Try to write, if the last write was sucessfull.
            conn.StartFlush();
            // Update the connection.
            mCurConnection = 0;
            conn.Update();
        }
        mRemove.clear();
        mNow = time(0);
        int slotCnt = std::min(int(kTimerWheelSize), int(mNow - lastTimerTime));
        if (slotCnt > timerOverrunWarningTime) {
            KFS_LOG_VA_DEBUG("Timer overrun %d seconds detected", slotCnt - 1);
        }
        mTimerRunningFlag = true;
        while (slotCnt-- > 0) {
            List& bucket = mTimerWheel[mCurTimerWheelSlot];
            mTimerWheelBucketItr = bucket.begin();
            while (mTimerWheelBucketItr != bucket.end()) {
                assert(*mTimerWheelBucketItr);
                NetConnection& conn = **mTimerWheelBucketItr;
                assert(conn.IsGood());
                ++mTimerWheelBucketItr;
                NetConnection::NetManagerEntry& entry =
                    *conn.GetNetMangerEntry(*this);
                const int timeOut = conn.GetInactivityTimeout();
                if (timeOut < 0) {
                    // No timeout, move it to the corresponding list.
                    UpdateTimer(entry, timeOut);
                } else if (entry.mExpirationTime <= mNow) {
                    conn.HandleTimeoutEvent();
                } else {
                    // Not expired yet, move to the new slot, taking into the
                    // account possible timer overrun.
                    UpdateTimer(entry,
                        slotCnt + int(entry.mExpirationTime - mNow));
                }
            }
            if (++mCurTimerWheelSlot >= kTimerWheelSize) {
                mCurTimerWheelSlot = 0;
            }
            mRemove.clear();
        }
        mTimerRunningFlag = false;
        lastTimerTime = mNow;
        mTimerWheelBucketItr = mRemove.end();
    }
    CheckFatalSysError(
        mPoll.Remove(globals().netKicker.GetFd()),
        "failed to removed net kicker's fd from poll set"
    );
    CleanUp();
}

void
NetManager::CheckIfOverloaded()
{
    if (mMaxOutgoingBacklog > 0) {
        if (!mNetworkOverloaded) {
            mNetworkOverloaded = (mNumBytesToSend > mMaxOutgoingBacklog);
        } else if (mNumBytesToSend <= mMaxOutgoingBacklog / 2) {
            // network was overloaded and that has now cleared
            mNetworkOverloaded = false;
        }
    }
    mIsOverloaded = mDiskOverloaded || mNetworkOverloaded;
}

void
NetManager::ChangeDiskOverloadState(bool v)
{
    if (mDiskOverloaded == v)
        return;
    mDiskOverloaded = v;
}

void
NetManager::CleanUp()
{
    mTimeoutHandlers.clear();
    for (int i = 0; i <= kTimerWheelSize; i++) {
        for (mTimerWheelBucketItr = mTimerWheel[i].begin();
                mTimerWheelBucketItr != mTimerWheel[i].end(); ) {
            NetConnection* const conn = mTimerWheelBucketItr->get();
            ++mTimerWheelBucketItr;
            if (conn) {
                if (conn->IsGood()) {
                    conn->HandleErrorEvent();
                }
                conn->Close();
            }
        }
        assert(mTimerWheel[i].empty());
        mRemove.clear();
    }
    mTimerWheelBucketItr = mRemove.end();
}

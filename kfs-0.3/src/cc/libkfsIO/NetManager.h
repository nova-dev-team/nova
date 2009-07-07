//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: NetManager.h 326 2009-04-25 00:45:50Z sriramsrao $
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

#ifndef _LIBIO_NETMANAGER_H
#define _LIBIO_NETMANAGER_H

#include <sys/time.h>

extern "C" {
#include <pthread.h>
}

#include <list>

#include "ITimeout.h"
#include "NetConnection.h"
#include "fdpoll.h"

namespace KFS
{

///
/// \file NetManager.h
/// The net manager provides facilities for multiplexing I/O on network
/// connections.  It keeps a list of connections on which it has to
/// call poll.  Whenever an "event" occurs on a connection (viz.,
/// read/write/error), it calls back the connection to handle the
/// event.  
/// 
/// The net manager also provides support for timeout notification.
/// Whenever a call to poll returns, that is an occurence of a
/// timeout.  Interested handlers can register with the net manager to
/// be notified of timeout.  In the current implementation, the
/// timeout interval is mPollTimeout.
//

class NetManager {
public:
    NetManager(int timeoutMs = 10000);
    ~NetManager();
    /// Add a connection to the net manager's list of connections that
    /// are used for building poll vector.
    /// @param[in] conn The connection that should be added.
    void AddConnection(NetConnectionPtr &conn);
    void RegisterTimeoutHandler(ITimeout *handler);
    void UnRegisterTimeoutHandler(ITimeout *handler);

    /// This API can be used to limit the backlog of outgoing data.
    /// Whenever the backlog exceeds the threshold, poll vector bits
    /// are turned off for incoming traffic.
    void SetBacklogLimit(int64_t v) {
        mMaxOutgoingBacklog = v;
    }
    void ChangeDiskOverloadState(bool v);

    ///
    /// This function never returns.  It builds a poll vector, calls
    /// poll(), and then evaluates the result of poll():  for
    /// connections on which data is I/O is possible---either for
    /// reading or writing are called back.  In the callback, the
    /// connections should take appropriate action.  
    ///
    /// NOTE: When a connection is closed (such as, via a call to
    /// NetConnection::Close()), then it automatically falls out of
    /// the net manager's list of connections that are polled.
    ///  
    void MainLoop();

    void Shutdown() { mRunFlag = false; }
    /// Methods used by NetConnection only.
    void Update(NetConnection::NetManagerEntry& entry, int fd, bool resetTimer);
private:
    typedef NetConnection::NetManagerEntry::List List;
    enum { kTimerWheelSize = (1 << 8) };

    /// Timer wheel.
    List                mTimerWheel[kTimerWheelSize + 1];
    List                mRemove;
    List::iterator      mTimerWheelBucketItr;
    NetConnection*      mCurConnection;
    int                 mCurTimerWheelSlot;
    int                 mConnectionsCount;
    /// when the system is overloaded--either because of disk or we
    /// have too much network I/O backlogged---we avoid polling fd's for
    /// read.  this causes back-pressure and forces the clients to
    /// slow down
    bool		mDiskOverloaded;
    bool		mNetworkOverloaded;
    bool                mIsOverloaded;
    bool                mRunFlag;
    bool                mTimerRunningFlag;
    /// timeout interval specified in the call to poll().
    const int           mTimeoutMs;
    time_t              mNow;
    int64_t		mMaxOutgoingBacklog;
    int64_t             mNumBytesToSend;
    FdPoll&	        mPoll;

    /// Handlers that are notified whenever a call to poll()
    /// returns.  To the handlers, the notification is a timeout signal.
    std::list<ITimeout *>	mTimeoutHandlers;

    void CheckIfOverloaded();
    void CleanUp();
    inline void UpdateTimer(NetConnection::NetManagerEntry& entry, int timeOut);
};

}

#endif // _LIBIO_NETMANAGER_H

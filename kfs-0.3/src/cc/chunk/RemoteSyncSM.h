//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: RemoteSyncSM.h 210 2008-11-05 02:51:49Z sriramsrao $ 
//
// Created 2006/09/27
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

#ifndef CHUNKSERVER_REMOTESYNCSM_H
#define CHUNKSERVER_REMOTESYNCSM_H

#include <list>

#include "libkfsIO/KfsCallbackObj.h"
#include "libkfsIO/DiskConnection.h"
#include "libkfsIO/NetConnection.h"
#include "KfsOps.h"
#include "Chunk.h"
#include "libkfsIO/ITimeout.h"
#include "meta/queue.h"

#include <sys/types.h>
#include <time.h>

#include <boost/shared_ptr.hpp>
#include <boost/enable_shared_from_this.hpp>

namespace KFS
{

class RemoteSyncSMTimeoutImpl;

// State machine that asks a remote server to commit a write
class RemoteSyncSM : public KfsCallbackObj,
                     public boost::enable_shared_from_this<RemoteSyncSM>
{
public:

    RemoteSyncSM(const ServerLocation &location) :
        mLocation(location), mSeqnum(1), mTimer(NULL)
    { 
        mLastResponseRecd = mLastRequestSent = time(0);
    };

    ~RemoteSyncSM();

    bool Connect();

    kfsSeq_t NextSeqnum() {
        return mSeqnum++;
    }
    void Enqueue(KfsOp *op);

    void Finish();

    // void Dispatch();

    int HandleEvent(int code, void *data);

    ServerLocation GetLocation() const {
        return mLocation;
    }

private:

    // if we don't get a reply back for an RPC in 5 mins, we assume
    // that the peer is non-responsive on the socket we are using/peer
    // could be dead and socket isn't telling us.  
    //
    static const int32_t INACTIVE_SERVER_TIMEOUT = 300;

    NetConnectionPtr mNetConnection;

    ServerLocation mLocation;

    /// Assign a sequence # for each op we send to the remote server
    kfsSeq_t mSeqnum;

    // if over N minutes have passed between the last request - last
    // response, that likely means that the chunkserver at the other
    // end isn't responding on this connection.  We use these values
    // to timeout connections.
    time_t mLastRequestSent;
    time_t mLastResponseRecd;
    
    /// A timer to periodically dispatch pending
    /// messages to the remote server
    RemoteSyncSMTimeoutImpl *mTimer;

    /// Queue of outstanding ops to be dispatched: this is the queue
    /// shared between event processor and the network threads.  When
    /// the network dispatcher runs, it pulls messages from this queue
    /// and stashes them away in the dispatched ops list.
    MetaQueue<KfsOp> mPendingOps;

    /// Queue of outstanding ops sent to remote server.
    std::list<KfsOp *> mDispatchedOps;

    /// We (may) have got a response from the peer.  If we are doing
    /// re-replication, then we need to wait until we got all the data
    /// for the op; in such cases, we need to know if we got the full
    /// response. 
    /// @retval 0 if we got the response; -1 if we need to wait
    int HandleResponse(IOBuffer *iobuf, int cmdLen);
    void FailAllOps();
};

/// A Timeout interface object for dispatching messages.
class RemoteSyncSMTimeoutImpl : public ITimeout {
public:
    RemoteSyncSMTimeoutImpl(RemoteSyncSM *mgr) {
        mRemoteSyncSM = mgr; 
    };
    /// On each timeout, check that the connection with the server is
    /// good.  Also, dispatch any pending messages.
    void Timeout() {
        // mRemoteSyncSM->Dispatch();
    };
private:
    /// Owning remote-sync SM.
    RemoteSyncSM        *mRemoteSyncSM;
};


typedef boost::shared_ptr<RemoteSyncSM> RemoteSyncSMPtr;

    RemoteSyncSMPtr FindServer(std::list<RemoteSyncSMPtr> &remoteSyncers, const ServerLocation &location, 
                               bool connect);
    
    void RemoveServer(std::list<RemoteSyncSMPtr> &remoteSyncers, RemoteSyncSM *target);

    void ReleaseAllServers(std::list<RemoteSyncSMPtr> &remoteSyncers);

}

#endif // CHUNKSERVER_REMOTESYNCSM_H

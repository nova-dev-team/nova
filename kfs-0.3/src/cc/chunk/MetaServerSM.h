//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: MetaServerSM.h 271 2009-02-11 06:53:09Z sriramsrao $ 
//
// Created 2006/06/07
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
// \file MetaServerSM.h
// \brief State machine that interfaces with the meta server and
// handles the RPCs sent by the meta server.
//
//----------------------------------------------------------------------------

#ifndef CHUNKSERVER_METASERVERSM_H
#define CHUNKSERVER_METASERVERSM_H

#include "libkfsIO/KfsCallbackObj.h"
#include "libkfsIO/ITimeout.h"
#include "libkfsIO/NetConnection.h"
#include "KfsOps.h"

#include "meta/queue.h"

#include <list>
#include <string>

namespace KFS
{

class MetaServerSMTimeoutImpl;

class MetaServerSM : public KfsCallbackObj {
public:
    MetaServerSM();
    ~MetaServerSM(); 

    /// In each hello to the metaserver, we send an MD5 sum of the
    /// binaries.  This should be "acceptable" to the metaserver and
    /// only then is the chunkserver allowed in.  This provides a
    /// simple mechanism to identify nodes that didn't receive a
    /// binary update or are running versions that the metaserver
    /// doesn't know about and shouldn't be inlcuded in the system.
    void SetMetaInfo(const ServerLocation &metaLoc, const char *clusterKey, int rackId,
                     const std::string &md5sum);

    /// Init function for configuring the metaserver SM.
    /// @param[in] chunkServerPort  Port at which chunk-server is
    /// listening for connections from KFS clients.
    /// @param[in] chunkServerHostname  Hostname at which chunk-server
    /// is running - to be used instead of gethostname()
    /// listening for connections from KFS clients.
    /// @retval 0 if we could connect/send HELLO; -1 otherwise
    void Init(int chunkServerPort, const std::string & chunkServerHostname);

    /// Send HELLO message.  This sends an op down to the event
    /// processor to get all the info.
    int SendHello();

    /// Generic event handler to handle RPC requests sent by the meta server.
    int HandleRequest(int code, void *data);

    bool HandleMsg(IOBuffer *iobuf, int msgLen);

    void EnqueueOp(KfsOp *op);

    /// If the connection to the server breaks, periodically, retry to
    /// connect; also dispatch ops.
    void Timeout();

    /// Return the server name/port information
    ServerLocation GetLocation() const {
        return mLocation;
    }

    kfsSeq_t nextSeq() {
        return mCmdSeq++;
    }

private:
    kfsSeq_t mCmdSeq;
    /// where is the server located?
    ServerLocation mLocation;

    /// An id that specifies the rack on which the server is located;
    /// this is used to do rack-aware replication
    int mRackId;

    /// "shared secret" key for this cluster.  This is used to prevent
    /// config mishaps: When we connect to a metaserver, we have to
    /// agree on the cluster key.
    std::string mClusterKey;

    /// An MD5 sum computed over the binaries that we send to the metaserver.
    std::string mMD5Sum;

    /// the port that the metaserver tells the clients to connect to us at.
    int mChunkServerPort;
    
    /// the hostname to use for discovering our IP address
    /// (instead of using gethostname)
    std::string mChunkServerHostname;

    /// Track if we have sent a "HELLO" to metaserver
    bool mSentHello;
    
    /// a handle to the hello op.  The model: the network processor
    /// queues the hello op to the event processor; the event
    /// processor pulls the result and enqueues the op back to us; the
    /// network processor dispatches the op and gets rid of it.
    HelloMetaOp *mHelloOp;

    /// list of ops that need to be dispatched: this is the queue that
    /// is shared between the event processor and the network
    /// dispatcher.  When the network dispatcher runs, it pulls ops
    /// from this queue and stashes them away in the dispatched list.
    MetaQueue<KfsOp> mPendingOps;

    /// same deal: queue that is shared between event processor and
    /// the network dispatcher.  when the ops are dispatched, they are
    /// deleted (i.e., fire'n'forget).
    MetaQueue<KfsOp> mPendingResponses;

    /// ops that we have sent to metaserver and are waiting for reply.
    std::list<KfsOp *> mDispatchedOps;

    /// Our connection to the meta server.
    NetConnectionPtr mNetConnection;

    /// A timer to periodically check that the connection to the
    /// server is good; if the connection broke, reconnect and do the
    /// handshake again.  Also, we use the timeout to dispatch pending
    /// messages to the server.
    MetaServerSMTimeoutImpl *mTimer;

    /// Connect to the meta server
    /// @retval 0 if connect was successful; -1 otherwise
    int Connect();

    /// Given a (possibly) complete op in a buffer, run it.
    bool HandleCmd(IOBuffer *iobuf, int cmdLen);
    /// Handle a reply to an RPC we previously sent.
    void HandleReply(IOBuffer *iobuf, int msgLen);

    /// Op has finished execution.  Send a response to the meta
    /// server.
    void EnqueueResponse(KfsOp *op);

    /// This is special: we dispatch mHelloOp and get rid of it.
    void DispatchHello();

    /// Submit all the enqueued ops
    void DispatchOps();
    /// Send out all the responses that are ready
    void DispatchResponse();

    /// We reconnected to the metaserver; so, resend all the pending ops.
    void ResubmitOps();
};

/// A Timeout interface object for checking connection status with the server
class MetaServerSMTimeoutImpl : public ITimeout {
public:
    MetaServerSMTimeoutImpl(MetaServerSM *mgr) {
        mMetaServerSM = mgr; 
    };
    /// On each timeout, check that the connection with the server is
    /// good.  Also, dispatch any pending messages.
    void Timeout() {
        mMetaServerSM->Timeout();
    };
private:
    /// Owning metaserver SM.
    MetaServerSM        *mMetaServerSM;
};

extern MetaServerSM gMetaServerSM;

}

#endif // CHUNKSERVER_METASERVERSM_H

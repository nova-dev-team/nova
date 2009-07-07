//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: ClientSM.h 210 2008-11-05 02:51:49Z sriramsrao $ 
//
// Created 2006/03/22
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

#ifndef _CLIENTSM_H
#define _CLIENTSM_H

namespace KFS
{
class ClientSM; // forward declaration to get things to build...
}

#include <deque>
#include <list>

#include "libkfsIO/KfsCallbackObj.h"
#include "libkfsIO/DiskConnection.h"
#include "libkfsIO/NetConnection.h"
#include "Chunk.h"
#include "RemoteSyncSM.h"
#include "KfsOps.h"

namespace KFS
{

    // There is a dependency in waiting for a write-op to finish
    // before we can execute a write-sync op. Use this struct to track
    // such dependencies.
    struct OpPair {
        // once op is finished, we can then execute dependent op.
        KfsOp *op;
        KfsOp *dependentOp;
    };

class ClientSM : public KfsCallbackObj {
public:

    ClientSM(NetConnectionPtr &conn);

    ~ClientSM(); 

    //
    // Sequence:
    //  Client connects.
    //   - A new client sm is born
    //   - reads a request out of the connection
    //   - client says READ chunkid
    //   - request handler calls the disk manager to get the size
    //   -- the request handler then runs in a loop:
    //       -- in READ START: schedule a read for 4k; transition to READ DONE
    //       -- in READ DONE: data that was read arrives; 
    //            schedule that data to be sent out and transition back to READ START
    //       
    int HandleRequest(int code, void *data);

    // This is a terminal state handler.  In this state, we wait for
    // all outstanding ops to finish and then destroy this.
    int HandleTerminate(int code, void *data);

    // For daisy-chain writes, retrieve the server object for the
    // chunkserver running at the specified location.
    //
    RemoteSyncSMPtr FindServer(const ServerLocation &loc, bool connect = true);

private:
    NetConnectionPtr	mNetConnection;
    /// Queue of outstanding ops from the client.  We reply to ops in FIFO
    std::deque<KfsOp *>	mOps;

    /// Queue of pending ops: ops that depend on other ops to finish before we can execute them.
    std::list<OpPair> mPendingOps;

    /// for writes, we daisy-chain the chunkservers in the forwarding path.  this list
    /// maintains the set of servers to which we have a connection.
    std::list<RemoteSyncSMPtr> mRemoteSyncers;

    /// Given a (possibly) complete op in a buffer, run it.
    /// @retval True if the command was handled (i.e., we have all the
    /// data and we could execute it); false otherwise.
    bool		HandleClientCmd(IOBuffer *iobuf, int cmdLen);

    /// Op has finished execution.  Send a response to the client.
    void		SendResponse(KfsOp *op);

    /// Submit ops that have been held waiting for doneOp to finish.
    void		OpFinished(KfsOp *doneOp);
};

}

#endif // _CLIENTSM_H

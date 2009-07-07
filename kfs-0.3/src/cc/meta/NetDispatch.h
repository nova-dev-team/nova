//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: NetDispatch.h 71 2008-07-07 15:49:14Z sriramsrao $ 
//
// Created 2006/06/01
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
// \file NetDispatch.h
// \brief Meta-server network dispatcher
// 
//----------------------------------------------------------------------------

#ifndef META_NETDISPATCH_H
#define META_NETDISPATCH_H

#include "libkfsIO/NetManager.h"
#include "libkfsIO/Acceptor.h"
#include "ChunkServerFactory.h"
#include "ClientManager.h"
#include "thread.h"

namespace KFS
{
    class NetDispatchTimeoutImpl;
    
    class NetDispatch {
    public:
        NetDispatch();
        ~NetDispatch();
        void Start(int clientAcceptPort, int chunkServerAcceptPort);
	//!< Call this method to prevent spinning: the main thread
	//!< calls this method and pauses.
	void WaitToFinish() {
		mWorker.join();
	}
        //!< Dispatch the results of RPC requests that have finished execution.
        //!< Also, dispatch layout related RPCs to chunk servers.
        void Dispatch();
        ChunkServerFactory *GetChunkServerFactory() {
            return mChunkServerFactory;
        }

    private:
        //!< Timer that periodically checks to see if
        //!< requests have completed execution/layout RPCs need to be
        //!< dispatched.
        NetDispatchTimeoutImpl *mNetDispatchTimeoutImpl;
        ClientManager *mClientManager; //!< tracks the connected clients
        ChunkServerFactory *mChunkServerFactory; //!< creates chunk servers when they connect
        MetaThread mWorker; //!< runs the poll loop in the net manager
    };

    class NetDispatchTimeoutImpl : public ITimeout {
    public:
        NetDispatchTimeoutImpl(NetDispatch *dis) {
            mNetDispatch = dis;
            // poll the logger/layout-mgr for RPCs every 100ms
            // SetTimeoutInterval(100);
        };
        ~NetDispatchTimeoutImpl() {
            mNetDispatch = NULL;
	};
        // On a timeout call the network dispatcher to see if any
        // RPC requests/replies are ready to be sent out.
        void Timeout() {
            mNetDispatch->Dispatch();
        };
    private:
        NetDispatch *mNetDispatch; //!< pointer to the owner (dispatch)
    };

    extern NetDispatch gNetDispatch;


}

#endif // META_NETDISPATCH_H

//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: NetDispatch.cc 92 2008-07-21 21:20:48Z sriramsrao $ 
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
// \file NetDispatch.cc
//
// \brief Meta-server network dispatcher.
//
//----------------------------------------------------------------------------

#include "NetDispatch.h"
#include "logger.h"
#include "LayoutManager.h"
#include "libkfsIO/Globals.h"

using namespace KFS;
using namespace KFS::libkfsio;

NetDispatch KFS::gNetDispatch;

NetDispatch::NetDispatch()
{
        mClientManager = new ClientManager();
        mChunkServerFactory = new ChunkServerFactory();
        mNetDispatchTimeoutImpl = new NetDispatchTimeoutImpl(this);
}

NetDispatch::~NetDispatch()
{
        globals().netManager.UnRegisterTimeoutHandler(mNetDispatchTimeoutImpl);

        delete mNetDispatchTimeoutImpl;
        delete mClientManager;
        delete mChunkServerFactory;
}

//
// Since we can't take a pointer to a member function easily, this is
// a workaround.  When the net-dispatch thread starts, it calls this
// function which in turn calls the real thing.
//
void *
net_dispatch_main(void *dummy)
{
        (void) dummy; // shut up g++
        globals().netManager.MainLoop();
        return NULL;
}

//
// Open up the server for connections.
//
void
NetDispatch::Start(int clientAcceptPort, int chunkServerAcceptPort)
{
        // Start the acceptors so that it sets up a connection with the net
        // manager for listening. 
        mClientManager->StartAcceptor(clientAcceptPort);
        mChunkServerFactory->StartAcceptor(chunkServerAcceptPort);
        // Setup the handler for polling the logger
        globals().netManager.RegisterTimeoutHandler(mNetDispatchTimeoutImpl);
        // Start polling....
        mWorker.start(net_dispatch_main, NULL);
}

///
/// Poll the logger to see if any op's have finished execution.  For
/// such ops, send a response back to the client.  Also, if there any
/// layout related RPCs, dispatch them now.
///
void
NetDispatch::Dispatch()
{
        MetaRequest *r;

        while ((r = oplog.next_result_nowait()) != NULL) {
                // The Client will send out a response and destroy r.
		if (r->clnt)
			r->clnt->HandleEvent(EVENT_CMD_DONE, (void *) r);
                else if (r->op == META_ALLOCATE) {
			// For truncations, we may need to internally
			// generate an allocation request.  In such a
			// case, run the allocation thru the entire gamut
			// (dispatch the request to chunkserver and log)
			// and then resume processing of the truncation.
			MetaAllocate *alloc = static_cast<MetaAllocate *> (r);
			assert(alloc->req != NULL);

			r = alloc->req;
			assert(r->op == META_TRUNCATE);
			r->suspended = false;
			submit_request(r);
			delete alloc;
                }
		else if (r->op == META_CHANGE_CHUNKVERSIONINC) {
			MetaChangeChunkVersionInc *ccvi = 
				static_cast<MetaChangeChunkVersionInc *> (r);
			if (ccvi->req != NULL) {
				// req->op was waiting for the chunkVersionInc change to
				// make it to disk.  Now that is done, we can push out req->op.
				r = ccvi->req;
				r->suspended = false;
				assert((r->op == META_ALLOCATE) && (r->clnt != NULL));
				r->clnt->HandleEvent(EVENT_CMD_DONE, (void *) r);
			}
			delete ccvi;
		}
		else if ((r->op == META_CHUNK_REPLICATE) || 
			(r->op == META_CHUNK_SIZE)) {
			// For replicating a chunk/computing a chunk's size, we sent a request to
			// a chunkserver; it did the work and sent back a reply.
			// We have processed the reply and that message is now here.
			// Nothing more to do.  So, get rid of it
			delete r;
		}
		else {
			// somehow, occasionally we are getting checkpoint requests here...
			KFS_LOG_VA_DEBUG("Getting an op (%d) with no client",
					r->op);
		}
        }

        gLayoutManager.Dispatch();
}


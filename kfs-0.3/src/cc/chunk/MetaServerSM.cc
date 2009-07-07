//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: MetaServerSM.cc 271 2009-02-11 06:53:09Z sriramsrao $ 
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
// \file MetaServerSM.cc
// \brief Handle interactions with the meta server.
//
//----------------------------------------------------------------------------

#include <unistd.h>
#include "common/log.h"
#include "MetaServerSM.h"
#include "ChunkManager.h"
#include "ChunkServer.h"
#include "Utils.h"

#include "libkfsIO/NetManager.h"
#include "libkfsIO/Globals.h"

#include <arpa/inet.h>
#include <netdb.h>

#include <algorithm>
#include <sstream>
using std::ostringstream;
using std::istringstream;
using std::find_if;
using std::list;

using namespace KFS;
using namespace KFS::libkfsio;

#include <boost/scoped_array.hpp>
using boost::scoped_array;

MetaServerSM KFS::gMetaServerSM;

MetaServerSM::MetaServerSM() :
    mCmdSeq(1), mRackId(-1), mSentHello(false), mHelloOp(NULL), mTimer(NULL)
{
    SET_HANDLER(this, &MetaServerSM::HandleRequest);
}

MetaServerSM::~MetaServerSM()
{
    if (mTimer)
        globals().netManager.UnRegisterTimeoutHandler(mTimer);
    delete mTimer;
    delete mHelloOp;
}

void 
MetaServerSM::SetMetaInfo(const ServerLocation &metaLoc, const char *clusterKey, 
                          int rackId, const std::string &md5sum)
{
    mLocation = metaLoc;
    mClusterKey = clusterKey;
    mRackId = rackId;
    mMD5Sum = md5sum;
}

void
MetaServerSM::Init(int chunkServerPort, const std::string & chunkServerHostname)
{
    if (mTimer == NULL) {
        mTimer = new MetaServerSMTimeoutImpl(this);
        globals().netManager.RegisterTimeoutHandler(mTimer);
    }
    mChunkServerPort = chunkServerPort;
    mChunkServerHostname = chunkServerHostname;
}

void
MetaServerSM::Timeout()
{
    if (!mNetConnection) {
        KFS_LOG_WARN("Connection to meta broke. Reconnecting...");
        if (Connect() < 0) {
            return;
        }
        SendHello();
        ResubmitOps();
    }
    DispatchOps();
    DispatchResponse();
}

int
MetaServerSM::Connect()
{
    TcpSocket *sock;

    if (mTimer == NULL) {
        mTimer = new MetaServerSMTimeoutImpl(this);
        globals().netManager.RegisterTimeoutHandler(mTimer);
    }

    KFS_LOG_VA_DEBUG("Trying to connect to: %s:%d",
                     mLocation.hostname.c_str(), mLocation.port);

    sock = new TcpSocket();
    if (sock->Connect(mLocation) < 0) {
        // KFS_LOG_DEBUG("Reconnect failed...");
        delete sock;
        return -1;
    }
    KFS_LOG_VA_INFO("Connect to metaserver (%s) succeeded...",
                    mLocation.ToString().c_str());

    mNetConnection.reset(new NetConnection(sock, this));
    // when the system is overloaded, we still want to add this
    // connection to the poll vector for reads; this ensures that we
    // get the heartbeats and other RPCs from the metaserver
    mNetConnection->EnableReadIfOverloaded();

    // Add this to the poll vector
    globals().netManager.AddConnection(mNetConnection);

    // time to resend all the ops queued?

    return 0;
}

int
MetaServerSM::SendHello()
{
    if (mHelloOp != NULL)
        return 0;

#ifdef DEBUG
    verifyExecutingOnNetProcessor();
#endif

    if (!mNetConnection) {
        if (Connect() < 0) {
            KFS_LOG_DEBUG("Unable to connect to meta server");
            return -1;
        }
    }
    
    struct hostent *hent = 0;
    
    if (mChunkServerHostname.size() < 1) {
        char hostname[256];
        gethostname(hostname, 256);

        // switch to IP address so we can avoid repeated DNS lookups
        hent = gethostbyname(hostname);
    }
    else {
        // switch to IP address so we can avoid repeated DNS lookups
        hent = gethostbyname(mChunkServerHostname.c_str());
    }
    
    in_addr ipaddr;

    if (hent == NULL) {
        die("Unable to resolve hostname");
    }
    memcpy(&ipaddr, hent->h_addr, hent->h_length);

    ServerLocation loc(inet_ntoa(ipaddr), mChunkServerPort);
    mHelloOp = new HelloMetaOp(nextSeq(), loc, mClusterKey, mMD5Sum, mRackId);
    mHelloOp->clnt = this;
    // send the op and wait for it comeback
    KFS::SubmitOp(mHelloOp);
    return 0;
}

void
MetaServerSM::DispatchHello()
{
    ostringstream os;

#ifdef DEBUG
    verifyExecutingOnNetProcessor();
#endif

    if (!mNetConnection) {
        if (Connect() < 0) {
            // don't have a connection...so, need to start the process again...
            delete mHelloOp;
            mHelloOp = NULL;
            return;
        }
    }
    mHelloOp->Request(os);
    mNetConnection->Write(os.str().c_str(), os.str().length());

    mSentHello = true;

    KFS_LOG_VA_INFO("Sent hello to meta server: %s", mHelloOp->Show().c_str());

    delete mHelloOp;
    mHelloOp = NULL;
}

#if 0
int
MetaServerSM::SendHello()
{
    ostringstream os;
    char hostname[256];

#ifdef DEBUG
    verifyExecutingOnNetProcessor();
#endif

    mChunkServerPort = chunkServerPort;

    if (!mNetConnection) {
        if (Connect() < 0) {
            KFS_LOG_DEBUG("Unable to connect to meta server");
            return -1;
        }
    }
    gethostname(hostname, 256);

    ServerLocation loc(hostname, chunkServerPort);
    HelloMetaOp op(nextSeq(), loc, mClusterKey);

    op.totalSpace = gChunkManager.GetTotalSpace();
    op.usedSpace = gChunkManager.GetUsedSpace();
    // XXX: For thread safety, force the request thru the event
    // processor to get this info.
    gChunkManager.GetHostedChunks(op.chunks);    

    op.Request(os);
    mNetConnection->Write(os.str().c_str(), os.str().length());

    mSentHello = true;

    KFS_LOG_VA_INFO("Sent hello to meta server: %s", op.Show().c_str());

    return 0;
}
#endif

///
/// Generic event handler.  Decode the event that occurred and
/// appropriately extract out the data and deal with the event.
/// @param[in] code: The type of event that occurred
/// @param[in] data: Data being passed in relative to the event that
/// occurred.
/// @retval 0 to indicate successful event handling; -1 otherwise.
///
int
MetaServerSM::HandleRequest(int code, void *data)
{
    IOBuffer *iobuf;
    KfsOp *op;
    int cmdLen;

#ifdef DEBUG
    verifyExecutingOnNetProcessor();
#endif

    switch (code) {
    case EVENT_NET_READ:
	// We read something from the network.  Run the RPC that
	// came in.
	iobuf = (IOBuffer *) data;
	while (IsMsgAvail(iobuf, &cmdLen)) {
            // if we don't have all the data for the command, bail
	    if (!HandleMsg(iobuf, cmdLen))
                break;
	}
	break;

    case EVENT_NET_WROTE:
	// Something went out on the network.  For now, we don't
	// track it. Later, we may use it for tracking throttling
	// and such.
	break;

    case EVENT_CMD_DONE:
	// An op finished execution.  Send a response back
	op = (KfsOp *) data;
        if (op->op == CMD_META_HELLO) {
            DispatchHello();
            break;
        }
            
        // the op will be deleted after we send the response.
	EnqueueResponse(op);
	break;

    case EVENT_NET_ERROR:
	// KFS_LOG_VA_DEBUG("Closing connection");

	if (mNetConnection)
	    mNetConnection->Close();

	mSentHello = false;
	// Give up the underlying pointer
	mNetConnection.reset();
	break;

    default:
	assert(!"Unknown event");
	break;
    }
    return 0;
}

bool
MetaServerSM::HandleMsg(IOBuffer *iobuf, int msgLen)
{
    char buf[5];

    iobuf->CopyOut(buf, 3);
    buf[4] = '\0';
    
    if (strncmp(buf, "OK", 2) == 0) {
        // This is a response to some op we sent earlier
        HandleReply(iobuf, msgLen);
        return true;
    } else {
        // is an RPC from the server
        return HandleCmd(iobuf, msgLen);
    }
}

void
MetaServerSM::HandleReply(IOBuffer *iobuf, int msgLen)
{
    scoped_array<char> buf;
    const char separator = ':';
    kfsSeq_t seq;
    int status;
    list<KfsOp *>::iterator iter;

    buf.reset(new char[msgLen + 1]);
    iobuf->CopyOut(buf.get(), msgLen);
    buf[msgLen] = '\0';
    
    iobuf->Consume(msgLen);
    istringstream ist(buf.get());
    Properties prop;

    prop.loadProperties(ist, separator, false);
    seq = prop.getValue("Cseq", (kfsSeq_t) -1);
    status = prop.getValue("Status", -1);
    if (status == -EBADCLUSTERKEY) {
        KFS_LOG_VA_FATAL("Aborting...due to cluster key mismatch; our key: %s",
                         mClusterKey.c_str());
        die("bad cluster key");
    }
    iter = find_if(mDispatchedOps.begin(), mDispatchedOps.end(), 
                   OpMatcher(seq));
    if (iter == mDispatchedOps.end()) 
        return;

    KfsOp *op = *iter;
    op->status = status;
    mDispatchedOps.erase(iter);

    // The op will be gotten rid of by this call.
    // op->HandleEvent(EVENT_CMD_DONE, op);
    KFS::SubmitOpResponse(op);
}

///
/// We have a command in a buffer.  It is possible that we don't have
/// everything we need to execute it (for example, for a stale chunks
/// RPC, we may not have received all the chunkids).  So, parse
/// out the command and if we have everything execute it.
/// 

bool
MetaServerSM::HandleCmd(IOBuffer *iobuf, int cmdLen)
{
    scoped_array<char> buf;
    StaleChunksOp *sc;
    istringstream ist;
    kfsChunkId_t c;
    int i, nAvail;
    KfsOp *op;

    buf.reset(new char[cmdLen + 1]);
    iobuf->CopyOut(buf.get(), cmdLen);
    buf[cmdLen] = '\0';
    
    if (ParseCommand(buf.get(), cmdLen, &op) != 0) {
        iobuf->Consume(cmdLen);

        KFS_LOG_VA_DEBUG("Aye?: %s", buf.get());
        // got a bogus command
        return false;
    }

    if (op->op == CMD_STALE_CHUNKS) {
        sc = static_cast<StaleChunksOp *> (op);
        // if we don't have all the data wait...
        nAvail = iobuf->BytesConsumable() - cmdLen;        
        if (nAvail < sc->contentLength) {
            delete op;
            return false;
        }
        iobuf->Consume(cmdLen);
        buf.reset(new char [sc->contentLength + 1]);
        buf[sc->contentLength] = '\0';
        iobuf->CopyOut(buf.get(), sc->contentLength);
        iobuf->Consume(sc->contentLength);

        ist.str(buf.get());
        for(i = 0; i < sc->numStaleChunks; ++i) {
            ist >> c;
            sc->staleChunkIds.push_back(c);
        }

    } else {
        iobuf->Consume(cmdLen);
    }

    op->clnt = this;
    // op->Execute();
    KFS::SubmitOp(op);
    return true;
}



void
MetaServerSM::EnqueueOp(KfsOp *op)
{
    op->seq = nextSeq();

    mPendingOps.enqueue(op);

    globals().netKicker.Kick();
}

///
/// Queue the response to the meta server request.  The response is
/// generated by MetaRequest as per the protocol.
/// @param[in] op The request for which we finished execution.
///

void
MetaServerSM::EnqueueResponse(KfsOp *op)
{
    mPendingResponses.enqueue(op);
    globals().netKicker.Kick();
}

void
MetaServerSM::DispatchOps()
{
    KfsOp *op;

#ifdef DEBUG
    verifyExecutingOnNetProcessor();
#endif

    while ((op = mPendingOps.dequeue_nowait()) != NULL) {
        ostringstream os;

        assert(op->op != CMD_META_HELLO);

        mDispatchedOps.push_back(op);

        // XXX: If the server connection is dead, hold on
        if ((!mNetConnection) || (!mSentHello)) {
            KFS_LOG_INFO("Metaserver connection is down...will dispatch later");
            return;
        }
        op->Request(os);
        mNetConnection->Write(os.str().c_str(), os.str().length());
    }
}

void
MetaServerSM::DispatchResponse()
{
    KfsOp *op;

#ifdef DEBUG
    verifyExecutingOnNetProcessor();
#endif

    while ((op = mPendingResponses.dequeue_nowait()) != NULL) {
        ostringstream os;

        // fire'n'forget..
        op->Response(os);
        mNetConnection->Write(os.str().c_str(), os.str().length());
        delete op;
    }
}

class OpDispatcher {
    NetConnectionPtr conn;
public:
    OpDispatcher(NetConnectionPtr &c) : conn(c) { }
    void operator() (KfsOp *op) {
        ostringstream os;

        op->Request(os);
        conn->Write(os.str().c_str(), os.str().length());
    }
};

// After re-establishing connection to the server, resubmit the ops.
void
MetaServerSM::ResubmitOps()
{
    for_each(mDispatchedOps.begin(), mDispatchedOps.end(),
             OpDispatcher(mNetConnection));
}

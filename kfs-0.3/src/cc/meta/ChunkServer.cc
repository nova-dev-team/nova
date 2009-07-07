//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: ChunkServer.cc 315 2009-04-22 18:58:19Z sjakub $ 
//
// Created 2006/06/06
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

#include "ChunkServer.h"
#include "LayoutManager.h"
#include "NetDispatch.h"
#include "util.h"
#include "libkfsIO/Globals.h"

using namespace KFS;
using namespace libkfsio;

#include <cassert>
#include <string>
#include <sstream>
using std::string;
using std::istringstream;

#include <boost/scoped_array.hpp>
using boost::scoped_array;

#include "common/log.h"

//
// if a chunkserver is not responsive for over 10 mins, mark it down
//
const int32_t INACTIVE_SERVER_TIMEOUT = 600;

#include "ChunkServerHeartbeater.h"

KFS::ChunkServerHeartbeater gChunkServerHeartbeater;

void KFS::ChunkServerHeartbeaterInit()
{
	globals().netManager.RegisterTimeoutHandler(&gChunkServerHeartbeater);
}

ChunkServer::ChunkServer() :
	mSeqNo(1),
	mHelloDone(false), mDown(false), mHeartbeatSent(false),
	mHeartbeatSkipped(false), mIsRetiring(false), mRackId(-1), 
	mNumCorruptChunks(0), mTotalSpace(0), mUsedSpace(0), mAllocSpace(0), 
	mNumChunks(0), mNumChunkWrites(0), 
	mNumChunkWriteReplications(0), mNumChunkReadReplications(0)
{
	// this is used in emulation mode...

}

ChunkServer::ChunkServer(NetConnectionPtr &conn) :
	mSeqNo(1), mNetConnection(conn), 
	mHelloDone(false), mDown(false), mHeartbeatSent(false),
	mHeartbeatSkipped(false), mIsRetiring(false), mRackId(-1), 
	mNumCorruptChunks(0), mTotalSpace(0), mUsedSpace(0), mAllocSpace(0), 
	mNumChunks(0), mNumChunkWrites(0), 
	mNumChunkWriteReplications(0), mNumChunkReadReplications(0)
{
        // Receive HELLO message
        SET_HANDLER(this, &ChunkServer::HandleHello);
}

ChunkServer::~ChunkServer()
{
	// KFS_LOG_VA_DEBUG("Deleting %p", this);

        if (mNetConnection)
                mNetConnection->Close();
}

///
/// Handle a HELLO message from the chunk server.
/// @param[in] code: The type of event that occurred
/// @param[in] data: Data being passed in relative to the event that
/// occurred.
/// @retval 0 to indicate successful event handling; -1 otherwise.
///
int
ChunkServer::HandleHello(int code, void *data)
{
	IOBuffer *iobuf;
	int msgLen, retval;

	switch (code) {
	case EVENT_NET_READ:
		// We read something from the network.  It
		// better be a HELLO message.
		iobuf = (IOBuffer *) data;
		if (IsMsgAvail(iobuf, &msgLen)) {
			retval = HandleMsg(iobuf, msgLen);
			if (retval < 0) {
				 // Couldn't process hello
				 // message...bye-bye
				mDown = true;
				gNetDispatch.GetChunkServerFactory()->RemoveServer(this);
				return -1;
			}
			if (retval > 0) {
				// not all data is available...so, hold on
				return 0;

			}
			mHelloDone = true;
			mLastHeard = time(NULL);
			// Hello message successfully
			// processed.  Setup to handle RPCs
			SET_HANDLER(this, &ChunkServer::HandleRequest);
		}
		break;

	 case EVENT_NET_WROTE:
		// Something went out on the network.  
		break;

	 case EVENT_NET_ERROR:
		// KFS_LOG_VA_DEBUG("Closing connection");
		mDown = true;
		gNetDispatch.GetChunkServerFactory()->RemoveServer(this);
		break;

	 default:
		assert(!"Unknown event");
		return -1;
	}
	return 0;
}


///
/// Generic event handler.  Decode the event that occurred and
/// appropriately extract out the data and deal with the event.
/// @param[in] code: The type of event that occurred
/// @param[in] data: Data being passed in relative to the event that
/// occurred.
/// @retval 0 to indicate successful event handling; -1 otherwise.
///
int
ChunkServer::HandleRequest(int code, void *data)
{
	IOBuffer *iobuf;
	int msgLen;
	MetaRequest *op;

	switch (code) {
	case EVENT_NET_READ:
		// We read something from the network.  It is
		// either an RPC (such as hello) or a reply to
		// an RPC we sent earlier.
		iobuf = (IOBuffer *) data;
		while (IsMsgAvail(iobuf, &msgLen)) {
			HandleMsg(iobuf, msgLen);
		}
		break;

	case EVENT_CMD_DONE:
		op = (MetaRequest *) data;
		if (!mDown) {
			SendResponse(op);
		}	
		// nothing left to be done...get rid of it
		delete op;
		break;

	case EVENT_NET_WROTE:
		// Something went out on the network.  
		break;

	case EVENT_NET_ERROR:
		KFS_LOG_VA_INFO("Chunk server %s is down...", GetServerName());

		FailDispatchedOps();
		
		// Take out the server from write-allocation
		mTotalSpace = mAllocSpace = mUsedSpace = 0;
		
		if (mNetConnection) {
			mNetConnection->Close();
			mNetConnection.reset();
		}

		mDown = true;
		// force the server down thru the main loop to avoid races
		op = new MetaBye(0, shared_from_this());
		op->clnt = this;

		gNetDispatch.GetChunkServerFactory()->RemoveServer(this);
		submit_request(op);
		
		break;

	default:
		assert(!"Unknown event");
		return -1;
	}
	return 0;
}

///
/// We have a message from the chunk server.  The message we got is one
/// of:
///  -  a HELLO message from the server 
///  -  it is a response to some command we previously sent
///  -  is an RPC from the chunkserver
/// 
/// Of these, for the first and third case,create an op and
/// send that down the pike; in the second case, retrieve the op from
/// the pending list, attach the response, and push that down the pike.
///
/// @param[in] iobuf: Buffer containing the command
/// @param[in] msgLen: Length of the command in the buffer
/// @retval 0 if we handled the message properly; -1 on error; 
///   1 if there is more data needed for this message and we haven't
///   yet received the data.
int
ChunkServer::HandleMsg(IOBuffer *iobuf, int msgLen)
{
	char buf[5];

	if (!mHelloDone) {
		return HandleHelloMsg(iobuf, msgLen);
	}
        
	iobuf->CopyOut(buf, 3);
	buf[4] = '\0';
	if (strncmp(buf, "OK", 2) == 0) {
		return HandleReply(iobuf, msgLen);
	}
	return HandleCmd(iobuf, msgLen);
}

/// Case #1: Handle Hello message from a chunkserver that
/// just connected to us.
int
ChunkServer::HandleHelloMsg(IOBuffer *iobuf, int msgLen)
{
	scoped_array<char> buf, contentBuf;
        MetaRequest *op;
        MetaHello *helloOp;
        int i, nAvail;
	istringstream ist;

        buf.reset(new char[msgLen + 1]);
        iobuf->CopyOut(buf.get(), msgLen);
        buf[msgLen] = '\0';

        assert(!mHelloDone);
    
        // We should only get a HELLO message here; anything
        // else is bad.
        if (ParseCommand(buf.get(), msgLen, &op) < 0) {
            KFS_LOG_VA_DEBUG("Aye?: %s", buf.get());
            iobuf->Consume(msgLen);
            // we couldn't parse out hello
            return -1;
        }

        // we really ought to get only hello here
        if (op->op != META_HELLO) {
            KFS_LOG_VA_DEBUG("Only  need hello...but: %s", buf.get());
            iobuf->Consume(msgLen);
            delete op;
            // got a bogus command
            return -1;

        }

        helloOp = static_cast<MetaHello *> (op);

        KFS_LOG_VA_INFO("New server: \n%s", buf.get());
        op->clnt = this;
        helloOp->server = shared_from_this();
        // make sure we have the chunk ids...
        if (helloOp->contentLength > 0) {
            nAvail = iobuf->BytesConsumable() - msgLen;
            if (nAvail < helloOp->contentLength) {
                // need to wait for data...
                delete op;
                return 1;
            }
            // we have everything
            iobuf->Consume(msgLen);

            contentBuf.reset(new char[helloOp->contentLength + 1]);
            contentBuf[helloOp->contentLength] = '\0';
                        
            // get the chunkids
            iobuf->CopyOut(contentBuf.get(), helloOp->contentLength);
            iobuf->Consume(helloOp->contentLength);

            ist.str(contentBuf.get());
            for (i = 0; i < helloOp->numChunks; ++i) {
                ChunkInfo c;

                ist >> c.fileId;
                ist >> c.chunkId;
                ist >> c.chunkVersion;
                helloOp->chunks.push_back(c);
                // KFS_LOG_VA_DEBUG("Server has chunk: %lld", chunkId);
            }
        } else {
            // Message is ready to be pushed down.  So remove it.
            iobuf->Consume(msgLen);
        }
        // send it on its merry way
        submit_request(op);
        return 0;
}

///
/// Case #2: Handle an RPC from a chunkserver.
///
int
ChunkServer::HandleCmd(IOBuffer *iobuf, int msgLen)
{
	scoped_array<char> buf;
        MetaRequest *op;

        buf.reset(new char[msgLen + 1]);
        iobuf->CopyOut(buf.get(), msgLen);
        buf[msgLen] = '\0';

        assert(mHelloDone);
    
        // Message is ready to be pushed down.  So remove it.
        iobuf->Consume(msgLen);

        if (ParseCommand(buf.get(), msgLen, &op) != 0) {
            return -1;
        }
	if (op->op == META_CHUNK_CORRUPT) {
		MetaChunkCorrupt *ccop = static_cast<MetaChunkCorrupt *>(op);

		ccop->server = shared_from_this();
	}
        op->clnt = this;
        submit_request(op);

	/*
        if (iobuf->BytesConsumable() > 0) {
                KFS_LOG_VA_DEBUG("More command data likely available: ");
        }
	*/
        return 0;
}

///
/// Case #3: Handle a reply from a chunkserver to an RPC we
/// previously sent.
///
int
ChunkServer::HandleReply(IOBuffer *iobuf, int msgLen)
{
	scoped_array<char> buf, contentBuf;
        MetaRequest *op;
        int status;
        seq_t cseq;
	istringstream ist;
        Properties prop;
        MetaChunkRequest *submittedOp;

        buf.reset(new char[msgLen + 1]);
        iobuf->CopyOut(buf.get(), msgLen);
        buf[msgLen] = '\0';

        assert(mHelloDone);
    
        // Message is ready to be pushed down.  So remove it.
        iobuf->Consume(msgLen);

        // We got a response for a command we previously
        // sent.  So, match the response to its request and
        // resume request processing.
        ParseResponse(buf.get(), msgLen, prop);
        cseq = prop.getValue("Cseq", (seq_t) -1);
        status = prop.getValue("Status", -1);

        op = FindMatchingRequest(cseq);
        if (op == NULL) {
            // Uh-oh...this can happen if the server restarts between sending
	    // the message and getting reply back
            // assert(!"Unable to find command for a response");
	    KFS_LOG_VA_WARN("Unable to find command for response (cseq = %d)", cseq);
            return -1;
        }
        // KFS_LOG_VA_DEBUG("Got response for cseq=%d", cseq);

        submittedOp = static_cast <MetaChunkRequest *> (op);
        submittedOp->status = status;
        if (submittedOp->op == META_CHUNK_HEARTBEAT) {
            mTotalSpace = prop.getValue("Total-space", (long long) 0);
            mUsedSpace = prop.getValue("Used-space", (long long) 0);
            mNumChunks = prop.getValue("Num-chunks", 0);
	    mAllocSpace = mUsedSpace + mNumChunkWrites * CHUNKSIZE;
	    mHeartbeatSent = false;
	    mLastHeard = time(NULL);
	} else if (submittedOp->op == META_CHUNK_REPLICATE) {
	    MetaChunkReplicate *mcr = static_cast <MetaChunkReplicate *> (op);
	    mcr->fid = prop.getValue("File-handle", (long long) 0);
	    mcr->chunkVersion = prop.getValue("Chunk-version", (long long) 0);
	} else if (submittedOp->op == META_CHUNK_SIZE) {
	    MetaChunkSize *mcs = static_cast <MetaChunkSize *> (op);
	    mcs->chunkSize = prop.getValue("Size", (off_t) -1);
	}
        ResumeOp(op);
    
        return 0;
}

void
ChunkServer::ResumeOp(MetaRequest *op)
{
        MetaChunkRequest *submittedOp;
        MetaAllocate *allocateOp;
        MetaRequest *req;

        // get the original request and get rid of the
        // intermediate one we generated for the RPC.
        submittedOp = static_cast <MetaChunkRequest *> (op);
        req = submittedOp->req;

        // op types:
        // - allocate ops have an "original" request; this
        //    needs to be reactivated, so that a response can
        //    be sent to the client.
        // -  delete ops are a fire-n-forget. there is no
        //    other processing left to be done on them.
        // -  heartbeat: update the space usage statistics and nuke
        // it, which is already done (in the caller of this method)
        // -  stale-chunk notify: we tell the chunk server and that is it.
        //
        if (submittedOp->op == META_CHUNK_ALLOCATE) {
                assert(req && (req->op == META_ALLOCATE));

		// if there is a non-zero status, don't throw it away
		if (req->status == 0)
                	req->status = submittedOp->status;

		delete submittedOp;                

                allocateOp = static_cast<MetaAllocate *> (req);
                allocateOp->numServerReplies++;
                // wait until we get replies from all servers
                if (allocateOp->numServerReplies == allocateOp->servers.size()) {
                    allocateOp->layoutDone = true;
                    // The op is no longer suspended.
                    req->suspended = false;
                    // send it on its merry way
                    submit_request(req);
                }
        }
        else if ((submittedOp->op == META_CHUNK_DELETE) ||
		 (submittedOp->op == META_CHUNK_TRUNCATE) ||
		 (submittedOp->op == META_CHUNK_HEARTBEAT) ||
		 (submittedOp->op == META_CHUNK_STALENOTIFY) ||
		 (submittedOp->op == META_CHUNK_VERSCHANGE) ||
		 (submittedOp->op == META_CHUNK_RETIRE)) {
                assert(req == NULL);
		delete submittedOp;                
        }
        else if (submittedOp->op == META_CHUNK_REPLICATE) {
		// This op is internally generated.  We need to notify
		// the layout manager of this op's completion.  So, send
		// it there.
		MetaChunkReplicate *mcr = static_cast<MetaChunkReplicate *>(submittedOp);
		KFS_LOG_VA_DEBUG("Meta chunk replicate for chunk %lld finished with version %lld, status: %d",
				mcr->chunkId, mcr->chunkVersion, submittedOp->status);
		submit_request(submittedOp);
		// the op will get nuked after it is processed
	}
	else if (submittedOp->op == META_CHUNK_SIZE) {
		// chunkserver has responded with the chunk's size.  So, update
		// the meta-tree
		submit_request(submittedOp);
	}
        else {
                assert(!"Unknown op!");
        }

}

///
/// The response sent by a chunkserver is of the form:
/// OK \r\n
/// Cseq: <seq #>\r\n
/// Status: <status> \r\n
/// {<other header/value pair>\r\n}*\r\n
///
/// @param[in] buf Buffer containing the response
/// @param[in] bufLen length of buf
/// @param[out] prop  Properties object with the response header/values
/// 
void
ChunkServer::ParseResponse(char *buf, int bufLen,
                           Properties &prop)
{
        istringstream ist(buf);
        const char separator = ':';
        string respOk;

        // KFS_LOG_VA_DEBUG("Got chunk-server-response: %s", buf);

        ist >> respOk;
        // Response better start with OK
        if (respOk.compare("OK") != 0) {

                KFS_LOG_VA_DEBUG("Didn't get an OK: instead, %s",
                                 respOk.c_str());

                return;
        }
        prop.loadProperties(ist, separator, false);
}

// Helper functor that matches ops by sequence #'s
class OpMatch {
	seq_t myseq;
public:
	OpMatch(seq_t s) : myseq(s) { }
	bool operator() (const MetaRequest *r) {
		return (r->opSeqno == myseq);
	}
};

///
/// Request/responses are matched based on sequence #'s.
///
MetaRequest *
ChunkServer::FindMatchingRequest(seq_t cseq)
{
        list<MetaRequest *>::iterator iter;
	MetaRequest *op;

	iter = find_if(mDispatchedReqs.begin(), mDispatchedReqs.end(), OpMatch(cseq));
	if (iter == mDispatchedReqs.end())
		return NULL;
	
	op = *iter;
	mDispatchedReqs.erase(iter);
	return op;
}

///
/// Queue an RPC request
///
void
ChunkServer::Enqueue(MetaRequest *r) 
{
        mPendingReqs.enqueue(r);
	globals().netKicker.Kick();
}

int
ChunkServer::AllocateChunk(MetaAllocate *r, int64_t leaseId)
{
        MetaChunkAllocate *ca;

        mAllocSpace += CHUNKSIZE;

	UpdateNumChunkWrites(1);

        ca = new MetaChunkAllocate(NextSeq(), r, this, leaseId);

        // save a pointer to the request so that we can match up the
        // response whenever we get it.
	Enqueue(ca);

        return 0;
}

int
ChunkServer::DeleteChunk(chunkId_t chunkId)
{
	MetaChunkDelete *r;

        mAllocSpace -= CHUNKSIZE;

	if (IsRetiring()) {
		EvacuateChunkDone(chunkId);
	}

	r = new MetaChunkDelete(NextSeq(), this, chunkId);

	// save a pointer to the request so that we can match up the
	// response whenever we get it.
	Enqueue(r);

	return 0;
}

int
ChunkServer::TruncateChunk(chunkId_t chunkId, off_t s)
{
	MetaChunkTruncate *r;

	mAllocSpace -= (CHUNKSIZE - s);

	// OFF_TYPE_CAST: off_t casted to size_t.
	// Should be fine though since 's' contains single chunk size.
	r = new MetaChunkTruncate(NextSeq(), this, chunkId, s);

	// save a pointer to the request so that we can match up the
	// response whenever we get it.
	Enqueue(r);

	return 0;
}

int
ChunkServer::GetChunkSize(fid_t fid, chunkId_t chunkId, const string &pathname)
{
	MetaChunkSize *r;

	r = new MetaChunkSize(NextSeq(), this, fid, chunkId, pathname);

	// save a pointer to the request so that we can match up the
	// response whenever we get it.
	Enqueue(r);

	return 0;
}

int
ChunkServer::ReplicateChunk(fid_t fid, chunkId_t chunkId, seq_t chunkVersion,
				const ServerLocation &loc)
{
	MetaChunkReplicate *r;

	r = new MetaChunkReplicate(NextSeq(), this, fid, chunkId, chunkVersion, loc);
	r->server = shared_from_this();
	mNumChunkWriteReplications++;
	// save a pointer to the request so that we can match up the
	// response whenever we get it.
	Enqueue(r);

	return 0;
}

void
ChunkServer::Heartbeat()
{
	if (!mHelloDone) {
		return;
	}

	if (mHeartbeatSent) {
		string loc = mLocation.ToString();
		time_t now = time(0);

		if (now - mLastHeard > INACTIVE_SERVER_TIMEOUT) {
			KFS_LOG_VA_INFO("Server %s has been non-responsive for too long; taking it down", loc.c_str());
			// We are executing in the context of the network thread
			// So, take the server down as though the net connection
			// broke.
			HandleRequest(EVENT_NET_ERROR, NULL);
			return;
		}

		// If a request is outstanding, don't send one more
		mHeartbeatSkipped = true;
		KFS_LOG_VA_INFO("Skipping send of heartbeat to %s", loc.c_str());
		return;
	}

	mHeartbeatSent = true;
	mHeartbeatSkipped = false;

        MetaChunkHeartbeat *r;

        r = new MetaChunkHeartbeat(NextSeq(), this);

        // save a pointer to the request so that we can match up the
        // response whenever we get it.
	Enqueue(r);
}

void
ChunkServer::NotifyStaleChunks(const vector<chunkId_t> &staleChunkIds)
{
	MetaChunkStaleNotify *r;

	mAllocSpace -= (CHUNKSIZE * staleChunkIds.size());
	r = new MetaChunkStaleNotify(NextSeq(), this);

	r->staleChunkIds = staleChunkIds;

	// save a pointer to the request so that we can match up the
	// response whenever we get it.
	Enqueue(r);

}

void
ChunkServer::NotifyStaleChunk(chunkId_t staleChunkId)
{
	MetaChunkStaleNotify *r;

	mAllocSpace -= CHUNKSIZE;
	r = new MetaChunkStaleNotify(NextSeq(), this);

	r->staleChunkIds.push_back(staleChunkId);

	// save a pointer to the request so that we can match up the
	// response whenever we get it.
	Enqueue(r);
}

void
ChunkServer::NotifyChunkVersChange(fid_t fid, chunkId_t chunkId, seq_t chunkVers)
{
        MetaChunkVersChange *r;

	r = new MetaChunkVersChange(NextSeq(), this, fid, chunkId, chunkVers);

	// save a pointer to the request so that we can match up the
	// response whenever we get it.
	Enqueue(r);
}

void
ChunkServer::SetRetiring()
{
	mIsRetiring = true;
	mRetireStartTime = time(NULL);
	KFS_LOG_VA_INFO("Initiation of retire for chunks on %s : %d blocks to do",
			ServerID().c_str(), mNumChunks);
}

void
ChunkServer::EvacuateChunkDone(chunkId_t chunkId)
{
	if (!mIsRetiring)
		return;
	mEvacuatingChunks.erase(chunkId);
	if (mEvacuatingChunks.empty()) {
		KFS_LOG_VA_INFO("Evacuation of chunks on %s is done; retiring",
				ServerID().c_str());
		Retire();
	}
}

void
ChunkServer::Retire()
{
	MetaChunkRetire *r;

	r = new MetaChunkRetire(NextSeq(), this);
	Enqueue(r);
}

//
// Helper functor that dispatches an RPC request to the server.
//
class OpDispatcher {
	ChunkServer *server;
	NetConnectionPtr conn;
public:
	OpDispatcher(ChunkServer *s, NetConnectionPtr &c) :
		server(s), conn(c) { }
	void operator()(MetaRequest *r) {

        	ostringstream os;
        	MetaChunkRequest *cr = static_cast <MetaChunkRequest *> (r);

        	if (!conn) {
                	// Server is dead...so, drop the op
                	r->status = -EIO;
                	server->ResumeOp(r);
			return;
        	}
        	assert(cr != NULL);

        	// Get the request into string format
        	cr->request(os);

        	// Send it on its merry way
        	conn->Write(os.str().c_str(), os.str().length());

		if (cr->op == META_CHUNK_REPLICATE) {
			MetaChunkReplicate *mcr = static_cast <MetaChunkReplicate *> (cr);
			KFS_LOG_VA_INFO("Dispatched re-replication request: %s",
					mcr->Show().c_str());
		}
		// Notify the server the op is dispatched
		server->Dispatched(r);
	}
};

void
ChunkServer::Dispatch()
{
	OpDispatcher dispatcher(this, mNetConnection);
	list<MetaRequest *> reqs;
	MetaRequest *r;

	while((r = mPendingReqs.dequeue_nowait())) {
		reqs.push_back(r);
	}
	for_each(reqs.begin(), reqs.end(), dispatcher);

	reqs.clear();
}

// Helper functor that fails an op with an error code.
class OpFailer {
	ChunkServer *server;
	int errCode;
public:
	OpFailer(ChunkServer *s, int c) : server(s), errCode(c) { };
	void operator() (MetaRequest *op) {
                op->status = errCode;
                server->ResumeOp(op);
	}
};

void
ChunkServer::FailDispatchedOps()
{
	for_each(mDispatchedReqs.begin(), mDispatchedReqs.end(), 	
			OpFailer(this, -EIO));

	mDispatchedReqs.clear();
}

void
ChunkServer::FailPendingOps()
{
	list<MetaRequest *> reqs;
	MetaRequest *r;

	while((r = mPendingReqs.dequeue_nowait())) {
		reqs.push_back(r);
	}
	for_each(reqs.begin(), reqs.end(), OpFailer(this, -EIO));
	reqs.clear();
}

inline float convertToMB(off_t bytes)
{
	return bytes / (1024.0 * 1024.0);
}

inline float convertToGB(off_t bytes)
{
	return bytes / (1024.0 * 1024.0 * 1024.0);
}

void
ChunkServer::GetRetiringStatus(string &result)
{
	if (!mIsRetiring)
		return;

	ostringstream ost;
	char timebuf[64];
	ctime_r(&mRetireStartTime, timebuf);
	if (timebuf[24] == '\n')
		timebuf[24] = '\0';

	ost << "s=" << mLocation.hostname << ", p=" << mLocation.port 
		<< ", started=" << timebuf 
		<< ", numLeft=" << mEvacuatingChunks.size() << ", numDone=" 
		<< mNumChunks - mEvacuatingChunks.size() << '\t';

	result += ost.str();
}

void
ChunkServer::Ping(string &result)
{
	ostringstream ost;
	time_t now = time(NULL);
	bool isOverloaded = false;
	uint64_t freeSpace = mTotalSpace - mUsedSpace;

	// for nodes taken out of write allocation, send the info back; this allows
	// the UI to color these nodes differently
	if (GetSpaceUtilization() > MAX_SERVER_SPACE_UTIL_THRESHOLD)
		isOverloaded = true;

	if (mTotalSpace < (1L << 30)) {
		ost << "s=" << mLocation.hostname << ", p=" << mLocation.port 
			<< ", rack=" << mRackId 
			<< ", used=" << convertToMB(mUsedSpace)
	    		<< "(MB), free=" << convertToMB(freeSpace) 
			<< "(MB), util=" << GetSpaceUtilization() * 100.0 
			<< "%, nblocks=" << mNumChunks 
			<< ", lastheard=" << now - mLastHeard << " (sec)"
			<< ", ncorrupt=" << mNumCorruptChunks
			<< ", nchunksToMove=" << mChunksToMove.size();
		if (isOverloaded)
			ost << ", overloaded=1";
		ost << "\t";
	} else {
		ost << "s=" << mLocation.hostname << ", p=" << mLocation.port 
			<< ", rack=" << mRackId 
			<< ", used=" << convertToGB(mUsedSpace)
	    		<< "(GB), free=" << convertToGB(freeSpace) 
			<< "(GB), util=" << GetSpaceUtilization() * 100.0 
			<< "%, nblocks=" << mNumChunks 
			<< ", lastheard=" << now - mLastHeard << " (sec)"
			<< ", ncorrupt=" << mNumCorruptChunks
			<< ", nchunksToMove=" << mChunksToMove.size();
		if (isOverloaded)
			ost << ", overloaded=1";
		ost << "\t";
	}
	result += ost.str();
}

void
ChunkServer::SendResponse(MetaRequest *op)
{
        ostringstream os;

        op->response(os);

        if (os.str().length() > 0)
            mNetConnection->Write(os.str().c_str(), os.str().length());
}

//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: LeaseClerk.cc 235 2009-01-07 23:34:55Z sriramsrao $ 
//
// Created 2006/10/09
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
// \brief Code for dealing with lease renewals.
//
//----------------------------------------------------------------------------

#include "LeaseClerk.h"
#include "libkfsIO/Globals.h"

#include "ChunkServer.h"
#include "ChunkManager.h"
#include "MetaServerSM.h"

using namespace KFS;
using namespace KFS::libkfsio;

LeaseClerk KFS::gLeaseClerk;

/// 0 is a special chunkid that is not used in the system.  so,
/// use that as a key to signify cleanup.
static const kfsChunkId_t chunkIdForCleanup = 0;

LeaseClerk::LeaseClerk()
{
    mLastLeaseCheckTime = time(0);
    SET_HANDLER(this, &LeaseClerk::HandleEvent);
    // RegisterLease(chunkIdForCleanup, 0);
}

void
LeaseClerk::RegisterLease(kfsChunkId_t chunkId, int64_t leaseId)
{
    time_t now = time(0);
    LeaseInfo_t lease;
    // Get rid of the old lease if we had one
    LeaseMapIter iter = mLeases.find(chunkId);

    if (iter != mLeases.end()) {
        lease = iter->second;
        // lease.timer->Cancel();
        mLeases.erase(iter);
    }

    lease.leaseId = leaseId;
    lease.expires = now + LEASE_INTERVAL_SECS;
    lease.lastWriteTime = now;
    lease.leaseRenewSent = false;
    /*
    lease.timer.reset(new Event(this, (void *) chunkId,
                                LEASE_RENEW_INTERVAL_MSECS, false));
    */
    mLeases[chunkId] = lease;
    // globals().eventManager.Schedule(lease.timer, LEASE_RENEW_INTERVAL_MSECS);
    // Dont' print msgs for lease cleanup events.
    if (chunkId != 0)
        KFS_LOG_VA_DEBUG("Registered lease: chunk=%ld, lease=%ld",
                         chunkId, leaseId);
}

void
LeaseClerk::UnRegisterLease(kfsChunkId_t chunkId)
{
    LeaseMapIter iter = mLeases.find(chunkId);
    if (iter != mLeases.end()) {
        mLeases.erase(iter);
    }
    KFS_LOG_VA_DEBUG("Lease for chunk = %ld unregistered",
                     chunkId);

}

void
LeaseClerk::DoingWrite(kfsChunkId_t chunkId)
{
    LeaseMapIter iter = mLeases.find(chunkId);

    if (iter == mLeases.end())
        return;

    LeaseInfo_t lease = iter->second;
    lease.lastWriteTime = time(0);
    
    mLeases[chunkId] = lease;
}

bool
LeaseClerk::IsLeaseValid(kfsChunkId_t chunkId)
{
    LeaseMapIter iter = mLeases.find(chunkId);

    if (iter == mLeases.end())
        return false;

    time_t now = time(NULL);
    LeaseInfo_t lease = iter->second;
    
    // now <= lease.expires ==> lease hasn't expired and is therefore
    // valid.
    return now <= lease.expires;
}

void
LeaseClerk::LeaseRenewed(kfsChunkId_t chunkId)
{
    LeaseMapIter iter = mLeases.find(chunkId);

    if (iter == mLeases.end())
        return;

    time_t now = time(NULL);
    LeaseInfo_t lease = iter->second;

    KFS_LOG_VA_INFO("lease renewed for chunk = %ld, lease = %ld", chunkId, lease.leaseId);

    lease.expires = now + LEASE_INTERVAL_SECS;

    lease.leaseRenewSent = false;
    mLeases[chunkId] = lease;
    //globals().eventManager.Schedule(lease.timer, LEASE_RENEW_INTERVAL_MSECS);
}

int
LeaseClerk::HandleEvent(int code, void *data)
{
    LeaseMapIter iter = mLeases.begin();
    LeaseInfo_t lease;
    LeaseRenewOp *op;
    kfsChunkId_t chunkId;
    time_t now = time(0);

#ifdef DEBUG
    verifyExecutingOnEventProcessor();
#endif

    switch(code) {
    case EVENT_CMD_DONE:
	// we got a reply for a lease renewal
	op = (LeaseRenewOp *) data;
	if (op->status == 0)
	    LeaseRenewed(op->chunkId);
	else
	    UnRegisterLease(op->chunkId);
	delete op;
	break;

    case EVENT_TIMEOUT:
	// time to renew some lease
	chunkId = (int64_t) data;

	iter = mLeases.find(chunkId);
	if (iter == mLeases.end())
	    return 0;

	lease = iter->second;

	if (chunkId == 0) {
	    CleanupExpiredLeases();
            LeaseRenewed(chunkIdForCleanup);
	    return 0;
	}
	// Renew the lease if a write is pending or a write
	// occured when we had a valid lease.
	if ((gChunkManager.IsWritePending(chunkId)) ||
	    (now - lease.lastWriteTime <= LEASE_INTERVAL_SECS)) {
	    // The seq # is something that the metaserverSM will fill
	    LeaseRenewOp *op = new LeaseRenewOp(-1, chunkId, lease.leaseId,
			"WRITE_LEASE");

	    KFS_LOG_VA_DEBUG("renewing lease for: chunk=%ld, lease=%ld",
                             chunkId, lease.leaseId);

	    op->clnt = this;
	    gMetaServerSM.EnqueueOp(op);
	} else {
	    KFS_LOG_VA_DEBUG("not renewing lease for: chunk=%ld, lease=%ld",
                             chunkId, lease.leaseId);
	    // else...need to cleanup expired leases
	}
	break;
        default:
            assert(!"Unknown event");
            break;
    }
    return 0;
}

void
LeaseClerk::CleanupExpiredLeases()
{
    time_t now = time(0);

    // Unfortunately, can't do: mLeases.erase(remove_if()).  This is
    // because remove_if() will reorder things and you can't do on a map.
    for (LeaseMapIter curr = mLeases.begin(); curr != mLeases.end(); ) {
        // messages could be in-flight...so wait for a full
        // lease-interval before discarding dead leases
        if (now - curr->second.expires > LEASE_INTERVAL_SECS) {
            LeaseMapIter toErase = curr;
            ++curr;
            mLeases.erase(toErase);
        } else
            ++curr;
    }
}

class LeaseRenewer {
    LeaseClerk *lc;
    time_t now;
public:
    LeaseRenewer(LeaseClerk *l, time_t n) : lc(l), now(n) { }
    void operator()(std::tr1::unordered_map <kfsChunkId_t, LeaseInfo_t>::value_type &v) {
        kfsChunkId_t chunkId = v.first;
        LeaseInfo_t lease = v.second;
        
        if ((lease.expires - now > LeaseClerk::LEASE_EXPIRE_WINDOW_SECS) || 
            (lease.leaseRenewSent)) {
            // if the lease is valid for a while or a lease renew is in flight, move on
            return;
        }
	// Renew the lease if a write is pending or a write
	// occured when we had a valid lease.
	if ((gChunkManager.IsWritePending(chunkId)) ||
	    (now - lease.lastWriteTime <= LEASE_INTERVAL_SECS)) {
	    // The seq # is something that the metaserverSM will fill
	    LeaseRenewOp *op = new LeaseRenewOp(-1, chunkId, lease.leaseId,
			"WRITE_LEASE");

	    KFS_LOG_VA_INFO("sending lease renew for: chunk=%ld, lease=%ld, lease valid=%d secs",
                            chunkId, lease.leaseId, lease.expires - now);

	    op->clnt = lc;
            v.second.leaseRenewSent = true;
	    gMetaServerSM.EnqueueOp(op);
        }
    }
};

void
LeaseClerk::Timeout()
{
    time_t now = time(0);
    if (now - mLastLeaseCheckTime < 1) 
        return;
    mLastLeaseCheckTime = now;
    // once per second, check the state of the leases
    CleanupExpiredLeases();
    for_each(mLeases.begin(), mLeases.end(), LeaseRenewer(this, now));
}

void
LeaseClerk::RelinquishLease(kfsChunkId_t chunkId)
{
    if (!IsLeaseValid(chunkId))
        return;

    // is a valid lease; so, notify metaserver
    time_t now = time(0);
    LeaseMapIter iter = mLeases.find(chunkId);
    LeaseInfo_t lease = iter->second;
    LeaseRelinquishOp *op = new LeaseRelinquishOp(-1, chunkId, lease.leaseId, "WRITE_LEASE");

    KFS_LOG_VA_INFO("sending lease relinquish for: chunk=%ld, lease=%ld, lease valid=%d secs",
                    chunkId, lease.leaseId, lease.expires - now);
    
    op->clnt = this;
    gMetaServerSM.EnqueueOp(op);

    mLeases.erase(iter);
    
}

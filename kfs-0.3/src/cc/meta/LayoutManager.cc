//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: LayoutManager.cc 285 2009-02-25 22:05:06Z sriramsrao $ 
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
// \file LayoutManager.cc
// \brief Handlers for chunk layout.
//
//----------------------------------------------------------------------------

#include <algorithm>
#include <functional>
#include <sstream>

#include "LayoutManager.h"
#include "kfstree.h"
#include "libkfsIO/Globals.h"

using std::for_each;
using std::find;
using std::ptr_fun;
using std::mem_fun;
using std::mem_fun_ref;
using std::bind2nd;
using std::sort;
using std::random_shuffle;
using std::remove_if;
using std::set;
using std::vector;
using std::map;
using std::min;
using std::endl;
using std::istringstream;

using namespace KFS;
using namespace KFS::libkfsio;

LayoutManager KFS::gLayoutManager;
/// Max # of concurrent read/write replications per node
///  -- write: is the # of chunks that the node can pull in from outside
///  -- read: is the # of chunks that the node is allowed to send out
///
const int MAX_CONCURRENT_WRITE_REPLICATIONS_PER_NODE = 5;
const int MAX_CONCURRENT_READ_REPLICATIONS_PER_NODE = 10;

///
/// When placing chunks, we see the space available on the node as well as
/// we take our estimate of the # of writes on
/// the node as a hint for choosing servers; if a server is "loaded" we should
/// avoid sending traffic to it.  This value defines a watermark after which load
/// begins to be an issue.
///
const uint32_t CONCURRENT_WRITES_PER_NODE_WATERMARK = 10;

/// Helper functor that can be used to find a chunkid from a vector
/// of meta chunk info's.

class ChunkIdMatcher {
	chunkId_t myid;
public:
	ChunkIdMatcher(chunkId_t c) : myid(c) { }
	bool operator() (MetaChunkInfo *c) {
		return c->chunkId == myid;
	}
};


/// The rebalancing thresholds should be set in the emulator to get desired
/// behavior.

LayoutManager::LayoutManager() :
	mLeaseId(1), mNumOngoingReplications(0),
	mIsRebalancingEnabled(false), 
	mMaxRebalanceSpaceUtilThreshold(0.0),
	mMinRebalanceSpaceUtilThreshold(0.0),
	mIsExecutingRebalancePlan(false),
	mLastChunkRebalanced(1), mLastChunkReplicated(1),
	mRecoveryStartTime(0), mMinChunkserversToExitRecovery(1)
{
	pthread_mutex_init(&mChunkServersMutex, NULL);

	mReplicationTodoStats = new Counter("Num Replications Todo");
	mOngoingReplicationStats = new Counter("Num Ongoing Replications");
	mTotalReplicationStats = new Counter("Total Num Replications");
	mFailedReplicationStats = new Counter("Num Failed Replications");
	mStaleChunkCount = new Counter("Num Stale Chunks");
	// how much to be done before we are done
	globals().counterManager.AddCounter(mReplicationTodoStats);
	// how much are we doing right now
	globals().counterManager.AddCounter(mOngoingReplicationStats);
	globals().counterManager.AddCounter(mTotalReplicationStats);
	globals().counterManager.AddCounter(mFailedReplicationStats);
	globals().counterManager.AddCounter(mStaleChunkCount);
}

class MatchingServer {
	ServerLocation loc;
public:
	MatchingServer(const ServerLocation &l) : loc(l) { }
	bool operator() (ChunkServerPtr &s) {
		return s->MatchingServer(loc);
	}
};

//
// Try to match servers by hostname: for write allocation, we'd like to place
// one copy of the block on the same host on which the client is running.
//
class MatchServerByHost {
	string host;
public:
	MatchServerByHost(const string &s) : host(s) { }
	bool operator() (ChunkServerPtr &s) {
		ServerLocation l = s->GetServerLocation();

		return l.hostname == host;
	}
};


/// Add the newly joined server to the list of servers we have.  Also,
/// update our state to include the chunks hosted on this server.
void
LayoutManager::AddNewServer(MetaHello *r)
{
        ChunkServerPtr s;
        vector <chunkId_t> staleChunkIds;
        vector <ChunkInfo>::size_type i;
	vector <ChunkServerPtr>::iterator j;
	uint64_t allocSpace = r->chunks.size() * CHUNKSIZE;

	if (r->server->IsDown())
		return;

        s = r->server;
        s->SetServerLocation(r->location);
        s->SetSpace(r->totalSpace, r->usedSpace, allocSpace);
	s->SetRack(r->rackId);

        // If a previously dead server reconnects, reuse the server's
        // position in the list of chunk servers.  This is because in
        // the chunk->server mapping table, we use the chunkserver's
        // position in the list of connected servers to find it.
        //
	j = find_if(mChunkServers.begin(), mChunkServers.end(), MatchingServer(r->location));
	if (j != mChunkServers.end()) {
		KFS_LOG_VA_DEBUG("Duplicate server: %s, %d",
				 r->location.hostname.c_str(), r->location.port);
		return;
        }

	for (i = 0; i < r->chunks.size(); ++i) {
		vector<MetaChunkInfo *> v;
		vector<MetaChunkInfo *>::iterator chunk;
		int res = -1;

		metatree.getalloc(r->chunks[i].fileId, v);

		chunk = find_if(v.begin(), v.end(), ChunkIdMatcher(r->chunks[i].chunkId));
		if (chunk != v.end()) {
			MetaChunkInfo *mci = *chunk;
			if (mci->chunkVersion <= r->chunks[i].chunkVersion) {
				// This chunk is non-stale.  Verify that there are
				// sufficient copies; if there are too many, nuke some.
				ChangeChunkReplication(r->chunks[i].chunkId);

				res = UpdateChunkToServerMapping(r->chunks[i].chunkId,
								s.get());
				assert(res >= 0);

				// get the chunksize for the last chunk of fid
				// stored on this server
				MetaFattr *fa = metatree.getFattr(r->chunks[i].fileId);
				if ((fa->filesize < 0) || (fa->filesize < (off_t) (fa->chunkcount * CHUNKSIZE))) {
					MetaChunkInfo *lastChunk = v.back();
					if (lastChunk->chunkId == r->chunks[i].chunkId)
						s->GetChunkSize(r->chunks[i].fileId,
								r->chunks[i].chunkId,
								"");
				}

				if (mci->chunkVersion < r->chunks[i].chunkVersion) {
					// version #'s differ.  have the chunkserver reset
					// to what the metaserver has.
					// XXX: This is all due to the issue with not logging
					// the version # that the metaserver is issuing.  What is going
					// on here is that,
					//  -- client made a request
					//  -- metaserver bumped the version; notified the chunkservers
					//  -- the chunkservers write out the version bump on disk
					//  -- the metaserver gets ack; writes out the version bump on disk
					//  -- and then notifies the client
					// Now, if the metaserver crashes before it writes out the
					// version bump, it is possible that some chunkservers did the
					// bump, but not the metaserver.  So, fix up.  To avoid other whacky
					// scenarios, we increment the chunk version # by the incarnation stuff
					// to avoid reissuing the same version # multiple times.
					s->NotifyChunkVersChange(r->chunks[i].fileId,
							r->chunks[i].chunkId,
							mci->chunkVersion);

				}
			}
			else {
                        	KFS_LOG_VA_INFO("Old version for chunk id = %lld => stale",
                                         r->chunks[i].chunkId);
			}
		}

                if (res < 0) {
                        /// stale chunk
                        KFS_LOG_VA_INFO("Non-existent chunk id = %lld => stale",
                                         r->chunks[i].chunkId);
                        staleChunkIds.push_back(r->chunks[i].chunkId);
			mStaleChunkCount->Update(1);
                }
	}

        if (staleChunkIds.size() > 0) {
                s->NotifyStaleChunks(staleChunkIds);
        }

	// prevent the network thread from wandering this list while we change it.
	pthread_mutex_lock(&mChunkServersMutex);

	mChunkServers.push_back(s);

	pthread_mutex_unlock(&mChunkServersMutex);

	vector<RackInfo>::iterator rackIter;

	rackIter = find_if(mRacks.begin(), mRacks.end(), RackMatcher(r->rackId));
	if (rackIter != mRacks.end()) {
		rackIter->addServer(s);
	} else {
		RackInfo ri(r->rackId);
		ri.addServer(s);
		mRacks.push_back(ri);
	}

	// Update the list since a new server is in
	CheckHibernatingServersStatus();
}

class MapPurger {
	CSMap &cmap;
	CRCandidateSet &crset;
	const ChunkServer *target;
public:
	MapPurger(CSMap &m, CRCandidateSet &c, const ChunkServer *t):
		cmap(m), crset(c), target(t) { }
	void operator () (const map<chunkId_t, ChunkPlacementInfo >::value_type p) {
		ChunkPlacementInfo c = p.second;
        	vector <ChunkServerPtr>::iterator i;

		//
		// only chunks hosted on the target need to be checked for
		// replication level
		//
		i = find_if(c.chunkServers.begin(), c.chunkServers.end(),
			ChunkServerMatcher(target));

		if (i == c.chunkServers.end())
			return;

		c.chunkServers.erase(remove_if(c.chunkServers.begin(), c.chunkServers.end(),
					ChunkServerMatcher(target)),
					c.chunkServers.end());
		cmap[p.first] = c;
		// we need to check the replication level of this chunk
		crset.insert(p.first);
	}
};

class MapRetirer {
	CSMap &cmap;
	CRCandidateSet &crset;
	ChunkServer *retiringServer;
public:
	MapRetirer(CSMap &m, CRCandidateSet &c, ChunkServer *t):
		cmap(m), crset(c), retiringServer(t) { }
	void operator () (const map<chunkId_t, ChunkPlacementInfo >::value_type p) {
		ChunkPlacementInfo c = p.second;
        	vector <ChunkServerPtr>::iterator i;

		i = find_if(c.chunkServers.begin(), c.chunkServers.end(),
			ChunkServerMatcher(retiringServer));

		if (i == c.chunkServers.end())
			return;

		// we need to check the replication level of this chunk
		crset.insert(p.first);
		retiringServer->EvacuateChunk(p.first);
	}
};

class MapDumper {
	ofstream &ofs;
public:
	MapDumper(ofstream &o) : ofs(o) { }
	void operator () (const map<chunkId_t, ChunkPlacementInfo >::value_type p) {
		chunkId_t cid = p.first;
		ChunkPlacementInfo c = p.second;

		ofs << cid << ' ' << c.fid << ' ' << c.chunkServers.size() << ' ';
		for (uint32_t i = 0; i < c.chunkServers.size(); i++) {
			ofs << c.chunkServers[i]->ServerID() << ' '
				<< c.chunkServers[i]->GetRack() << ' ';
		}
		ofs << endl;
	}
};

class MapDumperStream {
	ostringstream &ofs;
public:
	MapDumperStream(ostringstream &o) : ofs(o) { }
	void operator () (const map<chunkId_t, ChunkPlacementInfo >::value_type p) {
		chunkId_t cid = p.first;
		ChunkPlacementInfo c = p.second;

		ofs << cid << ' ' << c.fid << ' ' << c.chunkServers.size() << ' ';
		for (uint32_t i = 0; i < c.chunkServers.size(); i++) {
			ofs << c.chunkServers[i]->ServerID() << ' '
				<< c.chunkServers[i]->GetRack() << ' ';
		}
		ofs << endl;
	}
};

class PrintChunkServerInfo {
	ofstream &ofs;
public:
	PrintChunkServerInfo(ofstream &o) : ofs(o) { }
	void operator() (ChunkServerPtr &c) {
		ofs << c->ServerID() << ' ' << c->GetRack() << ' '
			<< c->GetTotalSpace() << ' ' << c->GetUsedSpace() << endl;
	}
};

//
// Dump out the chunk block map to a file.  The output can be used in emulation
// modes where we setup the block map and experiment.
//
void
LayoutManager::DumpChunkToServerMap()
{
	ofstream ofs;

	//
	// to make offline rebalancing/re-replication easier, dump out where the
	// servers are and how much space each has.
	//
	ofs.open("network.def");
	for_each(mChunkServers.begin(), mChunkServers.end(),
		PrintChunkServerInfo(ofs));
	ofs.flush();
	ofs.close();


	ofs.open("chunkmap.txt");

	for_each(mChunkToServerMap.begin(), mChunkToServerMap.end(),
		MapDumper(ofs));
	ofs.flush();
	ofs.close();
}

void
LayoutManager::DumpChunkReplicationCandidates(ostringstream &os)
{
	chunkId_t chunkId;
	for (CRCandidateSetIter citer = mChunkReplicationCandidates.begin();
		citer != mChunkReplicationCandidates.end(); ++citer) {
		chunkId = *citer;
		os << chunkId << ' ';
	}
}

// Dump chunk block map to response stream
void
LayoutManager::DumpChunkToServerMap(ostringstream &os)
{
	for_each(mChunkToServerMap.begin(), mChunkToServerMap.end(),
			 MapDumperStream(os));
}

void
LayoutManager::ServerDown(ChunkServer *server)
{
        vector <ChunkServerPtr>::iterator i =
		find_if(mChunkServers.begin(), mChunkServers.end(),
			ChunkServerMatcher(server));

	if (i == mChunkServers.end())
		return;

	vector<RackInfo>::iterator rackIter;

	rackIter = find_if(mRacks.begin(), mRacks.end(), RackMatcher(server->GetRack()));
	if (rackIter != mRacks.end()) {
		rackIter->removeServer(server);
		if (rackIter->getServers().size() == 0) {
			// the entire rack of servers is gone
			// so, take the rack out
			KFS_LOG_VA_INFO("All servers in rack %d are down; taking out the rack", 
					server->GetRack());
			mRacks.erase(rackIter);
		}
	}

	/// Fail all the ops that were sent/waiting for response from
	/// this server.
	server->FailPendingOps();

	// check if this server was sent to hibernation
	bool isHibernating = false;
	for (uint32_t j = 0; j < mHibernatingServers.size(); j++) {
		if (mHibernatingServers[j].location == server->GetServerLocation()) {
			// record all the blocks that need to be checked for
			// re-replication later
			MapPurger purge(mChunkToServerMap, mHibernatingServers[j].blocks, server);

			for_each(mChunkToServerMap.begin(), mChunkToServerMap.end(), purge);
			isHibernating = true;
			break;
		}
	}

	if (!isHibernating) {
		MapPurger purge(mChunkToServerMap, mChunkReplicationCandidates, server);
		for_each(mChunkToServerMap.begin(), mChunkToServerMap.end(), purge);
	}

	// for reporting purposes, record when it went down
	time_t now = time(NULL);
	string downSince = timeToStr(now);
	ServerLocation loc = server->GetServerLocation();

	const char *reason;
	if (isHibernating)
		reason = "Hibernated";
	else if (server->IsRetiring())
		reason = "Retired";
	else
		reason= "Unreachable";

	mDownServers << "s=" << loc.hostname << ", p=" << loc.port << ", down="
			<< downSince << ", reason=" << reason << "\t";

	mChunkServers.erase(i);
}

int
LayoutManager::RetireServer(const ServerLocation &loc, int downtime)
{
	ChunkServerPtr retiringServer;
	vector <ChunkServerPtr>::iterator i;

	i = find_if(mChunkServers.begin(), mChunkServers.end(), MatchingServer(loc));
	if (i == mChunkServers.end())
		return -1;

	retiringServer = *i;

	retiringServer->SetRetiring();
	if (downtime > 0) {
		HibernatingServerInfo_t hsi;

		hsi.location = retiringServer->GetServerLocation();
		hsi.sleepEndTime = time(0) + downtime;
		mHibernatingServers.push_back(hsi);

		retiringServer->Retire();

		return 0;
	}

	MapRetirer retirer(mChunkToServerMap, mChunkReplicationCandidates, retiringServer.get());
	for_each(mChunkToServerMap.begin(), mChunkToServerMap.end(), retirer);

	return 0;
}

/*
 * Chunk-placement algorithm is rack-aware. At a high-level, the algorithm tries
 * to keep at most 1 copy of a chunk per rack:
 *  - Sort the racks based on space
 *  - From each rack, find one or more candidate servers
 * This approach will work when we are placing a chunk for the first time.
 * Whenever we need to copy/migrate a chunk, the intent is to keep the chunk
 * migration traffic within the same rack.  Specifically:
 * 1. Need to re-replicate a chunk because a node is dead
 *      -- here, we exclude the 2 racks on which the chunk is already placed and
 *      try to find a new spot.  By the time we get to finding a spot, we have
 *      removed the info about where the chunk was.  So, put it on a unique rack
 * 2. Need to re-replicate a chunk because a node is retiring
 *	-- here, since we know which node is retiring and the rack it is on, we
 *	can keep the new spot to be on the same rack
 * 3. Need to re-replicate a chunk because we are re-balancing amongst nodes
 *	-- we move data between nodes in the same rack
 *	-- if we a new rack got added, we move data between racks; here we need
 *	   to be a bit careful: in one iteration, we move data from one rack to
 *	   a newly added rack; in the next iteration, we could move another copy
 *	   of the chunk to the same rack...fill this in.
 * If we can't place the 3 copies on 3 different racks, we put the moved data
 * whereever we can find a spot.  (At a later time, we'll need the fix code: if
 * a new rack becomes available, we move the 3rd copy to the new rack and get
 * the copies on different racks).
 *
 */


/*
 * Return an ordered list of candidate racks
 */
void
LayoutManager::FindCandidateRacks(vector<int> &result)
{
	set<int> dummy;

	FindCandidateRacks(result, dummy);
}

void
LayoutManager::FindCandidateRacks(vector<int> &result, const set<int> &excludes)
{
	set<int>::const_iterator iter;
	int32_t numRacksToChoose = mRacks.size() - excludes.size();
	int32_t count = 0;
	int rackId, nodeId;
	set<int> chosenRacks;

	result.clear();

	if (numRacksToChoose == 0)
		return;
	
	for (uint32_t i = 0; i < mRacks.size(); i++) {
		// paranoia: each candidate rack better have at least one node
		if (!excludes.empty()) {
			iter = excludes.find(mRacks[i].id());
			if (iter != excludes.end())
				continue;
		}
		if (mRacks[i].getServers().size() == 0) {
			// there are no nodes in this rack
			numRacksToChoose--;
		}
	}

	if (numRacksToChoose == 0)
		return;

	count = 0;
	// choose a rack proportional to the # of nodes that rack
	while (count < numRacksToChoose) {
		nodeId = rand() % mChunkServers.size(); 
		rackId = mChunkServers[nodeId]->GetRack();
		if (!excludes.empty()) {
			iter = excludes.find(rackId);
			if (iter != excludes.end())
				// rack is in the exclude list
				continue;
		}
		iter = chosenRacks.find(rackId);
		if (iter != chosenRacks.end()) {
			// we have chosen this rack already
			continue;
		}
		chosenRacks.insert(rackId);
		result.push_back(rackId);
		count++;
	}
}

struct ServerSpace {
	uint32_t serverIdx;
	uint32_t loadEstimate;
	uint64_t availSpace;
	uint64_t usedSpace;

	// sort in decreasing order: Prefer the server with more free
	// space, or in the case of a tie, the one with less used space.
	// also, prefer servers that are lightly loaded

	bool operator < (const ServerSpace &other) const {

		if ((loadEstimate > CONCURRENT_WRITES_PER_NODE_WATERMARK) &&
			(loadEstimate != other.loadEstimate)) {
			// prefer server that is "lightly" loaded
			return loadEstimate < other.loadEstimate;
		}

		if (availSpace != other.availSpace)
			return availSpace > other.availSpace;
		else
			return usedSpace < other.usedSpace;
	}
};

struct ServerSpaceUtil {
	uint32_t serverIdx;
	float utilization;

	// sort in increasing order of space utilization
	bool operator < (const ServerSpaceUtil &other) const {
		return utilization < other.utilization;
	}
};

void
LayoutManager::FindCandidateServers(vector<ChunkServerPtr> &result,
				const vector<ChunkServerPtr> &excludes,
				int rackId)
{
	if (mChunkServers.size() < 1)
		return;

	if (rackId > 0) {
		vector<RackInfo>::iterator rackIter;

		rackIter = find_if(mRacks.begin(), mRacks.end(), RackMatcher(rackId));
		if (rackIter != mRacks.end()) {
			FindCandidateServers(result, rackIter->getServers(), excludes, rackId);
			return;
		}
	}
	FindCandidateServers(result, mChunkServers, excludes, rackId);
}

static bool
IsCandidateServer(ChunkServerPtr &c)
{
	if ((c->GetAvailSpace() < ((uint64_t) CHUNKSIZE)) || (!c->IsResponsiveServer())
		|| (c->IsRetiring())) {
		// one of: no space, non-responsive, retiring...we leave
		// the server alone
		return false;
	}
	return true;
}

void
LayoutManager::FindCandidateServers(vector<ChunkServerPtr> &result,
				const vector<ChunkServerPtr> &sources,
				const vector<ChunkServerPtr> &excludes,
				int rackId)
{
	if (sources.size() < 1)
		return;

	vector<ChunkServerPtr> candidates;
	vector<ChunkServerPtr>::size_type i;
	vector<ChunkServerPtr>::const_iterator iter;

	for (i = 0; i < sources.size(); i++) {
		ChunkServerPtr c = sources[i];

		if ((rackId >= 0) && (c->GetRack() != rackId))
			continue;

		if (!IsCandidateServer(c))
			continue;
		if (excludes.size() > 0) {
			iter = find(excludes.begin(), excludes.end(), c);
			if (iter != excludes.end()) {
				continue;
			}
		}
		// XXX: temporary measure: take only under-utilized servers
		// we need to move a model where we give preference to
		// under-utilized servers
		if (c->GetSpaceUtilization() > MAX_SERVER_SPACE_UTIL_THRESHOLD)
			continue;
		candidates.push_back(c);
	}
	if (candidates.size() == 0)
		return;
	random_shuffle(candidates.begin(), candidates.end());
	for (i = 0; i < candidates.size(); i++) {
		result.push_back(candidates[i]);
	}
}

#if 0
void
LayoutManager::FindCandidateServers(vector<ChunkServerPtr> &result,
				const vector<ChunkServerPtr> &sources,
				const vector<ChunkServerPtr> &excludes,
				int rackId)
{
	if (sources.size() < 1)
		return;

	vector<ServerSpace> ss;
	vector<ChunkServerPtr>::size_type i, j;
	vector<ChunkServerPtr>::const_iterator iter;

	ss.resize(sources.size());

	for (i = 0, j = 0; i < sources.size(); i++) {
		ChunkServerPtr c = sources[i];

		if ((rackId >= 0) && (c->GetRack() != rackId))
			continue;
		if ((c->GetAvailSpace() < ((uint64_t) CHUNKSIZE)) || (!c->IsResponsiveServer())
			|| (c->IsRetiring())) {
			// one of: no space, non-responsive, retiring...we leave
			// the server alone
			continue;
		}
		if (excludes.size() > 0) {
			iter = find(excludes.begin(), excludes.end(), c);
			if (iter != excludes.end()) {
				continue;
			}
		}
		ss[j].serverIdx = i;
		ss[j].availSpace = c->GetAvailSpace();
		ss[j].usedSpace = c->GetUsedSpace();
		ss[j].loadEstimate = c->GetNumChunkWrites();
		j++;
	}

	if (j == 0)
		return;

	ss.resize(j);

	sort(ss.begin(), ss.end());

	result.reserve(ss.size());
	for (i = 0; i < ss.size(); ++i) {
		result.push_back(sources[ss[i].serverIdx]);
	}
}
#endif

void
LayoutManager::SortServersByUtilization(vector<ChunkServerPtr> &servers)
{
	vector<ServerSpaceUtil> ss;
	vector<ChunkServerPtr> temp;

	ss.resize(servers.size());
	temp.resize(servers.size());

	for (vector<ChunkServerPtr>::size_type i = 0; i < servers.size(); i++) {
		ss[i].serverIdx = i;
		ss[i].utilization = servers[i]->GetSpaceUtilization();
		temp[i] = servers[i];
	}

	sort(ss.begin(), ss.end());
	for (vector<ChunkServerPtr>::size_type i = 0; i < servers.size(); i++) {
		servers[i] = temp[ss[i].serverIdx];
	}
}


///
/// The algorithm for picking a set of servers to hold a chunk is: (1) pick
/// the server with the most amount of free space, and (2) to break
/// ties, pick the one with the least amount of used space.  This
/// policy has the effect of doing round-robin allocations.  The
/// allocated space is something that we track.  Note: We rely on the
/// chunk servers to tell us how much space is used up on the server.
/// Since servers can respond at different rates, doing allocations
/// based on allocated space ensures equitable distribution;
/// otherwise, if we were to do allocations based on the amount of
/// used space, then a slow responding server will get pummelled with
/// lots of chunks (i.e., used space will be updated on the meta
/// server at a slow rate, causing the meta server to think that the
/// chunk server has lot of space available).
///
int
LayoutManager::AllocateChunk(MetaAllocate *r)
{
	vector<ChunkServerPtr>::size_type i;
	vector<int> racks;

	if (r->numReplicas == 0) {
		// huh? allocate a chunk with 0 replicas???
		return -EINVAL;
	}

	FindCandidateRacks(racks);
	if (racks.size() == 0)
		return -ENOSPC;

	r->servers.reserve(r->numReplicas);

	uint32_t numServersPerRack = r->numReplicas / racks.size();
	if (r->numReplicas % racks.size())
		numServersPerRack++;

	// take the server local to the machine on which the client is on
	// make that the master; this avoids a network transfer
	ChunkServerPtr localserver;
	vector <ChunkServerPtr>::iterator j;

	j = find_if(mChunkServers.begin(), mChunkServers.end(),
			MatchServerByHost(r->clientHost));
	if ((j !=  mChunkServers.end())  && (IsCandidateServer(*j)))
		localserver = *j;

	if (localserver)
		r->servers.push_back(localserver);

	for (uint32_t idx = 0; idx < racks.size(); idx++) {
		vector<ChunkServerPtr> candidates, dummy;

		if (r->servers.size() >= (uint32_t) r->numReplicas)
			break;
		FindCandidateServers(candidates, dummy, racks[idx]);
		if (candidates.size() == 0)
			continue;
		// take as many as we can from this rack
		uint32_t n = 0;
		if (localserver && (racks[idx] == localserver->GetRack()))
			n = 1;
		for (uint32_t i = 0; i < candidates.size() && n < numServersPerRack; i++) {
			if (r->servers.size() >= (uint32_t) r->numReplicas)
				break;
			if (candidates[i] != localserver) {
				r->servers.push_back(candidates[i]);
				n++;
			}
		}
	}

	if (r->servers.size() == 0)
		return -ENOSPC;

	LeaseInfo l(WRITE_LEASE, mLeaseId, r->servers[0], r->pathname);
	mLeaseId++;

	r->master = r->servers[0];
	r->servers[0]->AllocateChunk(r, l.leaseId);

	for (i = 1; i < r->servers.size(); i++) {
		r->servers[i]->AllocateChunk(r, -1);
	}

	ChunkPlacementInfo v;

	v.fid = r->fid;
	v.chunkServers = r->servers;
	v.chunkLeases.push_back(l);

	mChunkToServerMap[r->chunkId] = v;

	if (r->servers.size() < (uint32_t) r->numReplicas)
		ChangeChunkReplication(r->chunkId);

	return 0;
}

int
LayoutManager::GetChunkWriteLease(MetaAllocate *r, bool &isNewLease)
{
	ChunkPlacementInfo v;
	vector<ChunkServerPtr>::size_type i;
	vector<LeaseInfo>::iterator l;

	// XXX: This is a little too conservative.  We should
	// check if any server has told us about a lease for this
	// file; if no one we know about has a lease, then deny
	// issuing the lease during recovery---because there could
	// be some server who has a lease and hasn't told us yet.
	if (InRecovery()) {
		KFS_LOG_INFO("GetChunkWriteLease: InRecovery() => EBUSY");
		return -EBUSY;
	}

	// if no allocation has been done, can't grab any lease
        CSMapIter iter = mChunkToServerMap.find(r->chunkId);
        if (iter == mChunkToServerMap.end())
                return -EINVAL;

	v = iter->second;
	if (v.chunkServers.size() == 0)
		// all the associated servers are dead...so, fail
		// the allocation request.
		return -KFS::EDATAUNAVAIL;

	if (v.ongoingReplications > 0) {
		// don't issue a write lease to a chunk that is being
		// re-replicated; this prevents replicas from diverging
		KFS_LOG_VA_INFO("Write lease: %lld is being re-replicated => EBUSY", r->chunkId);
		return -EBUSY;
	}

	l = find_if(v.chunkLeases.begin(), v.chunkLeases.end(),
			ptr_fun(LeaseInfo::IsValidWriteLease));
	if (l != v.chunkLeases.end()) {
		LeaseInfo lease = *l;
		string s = timeToStr(lease.expires);
#ifdef DEBUG
		time_t now = time(0);
		assert(now <= lease.expires);
		KFS_LOG_DEBUG("write lease exists...no version bump");
#endif
		// valid write lease; so, tell the client where to go
		KFS_LOG_VA_INFO("Valid write lease exists for %lld (expires = %s)", r->chunkId, s.c_str());
		isNewLease = false;
		r->servers = v.chunkServers;
		r->master = lease.chunkServer;
		return 0;
	}
	// there is no valid write lease; to issue a new write lease, we
	// need to do a version # bump.  do that only if we haven't yet
	// handed out valid read leases
	l = find_if(v.chunkLeases.begin(), v.chunkLeases.end(),
			ptr_fun(LeaseInfo::IsValidLease));
	if (l != v.chunkLeases.end()) {
		KFS_LOG_DEBUG("GetChunkWriteLease: read lease => EBUSY");
		return -EBUSY;
	}
	// no one has a valid lease
	LeaseCleanup(r->chunkId, v);

	// Need space on the servers..otherwise, fail it
	r->servers = v.chunkServers;
	for (i = 0; i < r->servers.size(); i++) {
		if (r->servers[i]->GetAvailSpace() < CHUNKSIZE)
			return -ENOSPC;
	}

	isNewLease = true;

	LeaseInfo lease(WRITE_LEASE, mLeaseId, r->servers[0], r->pathname);
	mLeaseId++;

	v.chunkLeases.push_back(lease);
	mChunkToServerMap[r->chunkId] = v;

	// when issuing a new lease, bump the version # by the increment
	r->chunkVersion += chunkVersionInc;
	r->master = r->servers[0];
	r->master->AllocateChunk(r, lease.leaseId);

	for (i = 1; i < r->servers.size(); i++) {
		r->servers[i]->AllocateChunk(r, -1);
	}

	KFS_LOG_VA_INFO("New write lease issued for %lld; version = %lld", r->chunkId, r->chunkVersion);

	return 0;
}

/*
 * \brief Process a reqeuest for a READ lease.
*/
int
LayoutManager::GetChunkReadLease(MetaLeaseAcquire *req)
{
	ChunkPlacementInfo v;

	if (InRecovery()) {
		KFS_LOG_INFO("GetChunkReadLease: inRecovery() => EBUSY");
		return -EBUSY;
	}

        CSMapIter iter = mChunkToServerMap.find(req->chunkId);
        if (iter == mChunkToServerMap.end())
                return -EINVAL;

	// issue a read lease
	LeaseInfo lease(READ_LEASE, mLeaseId);
	mLeaseId++;

	v = iter->second;
	v.chunkLeases.push_back(lease);
	mChunkToServerMap[req->chunkId] = v;
	req->leaseId = lease.leaseId;

	return 0;
}

class ValidLeaseIssued {
	CSMap &chunkToServerMap;
public:
	ValidLeaseIssued(CSMap &m) : chunkToServerMap(m) { }
	bool operator() (MetaChunkInfo *c) {
		ChunkPlacementInfo v;
		vector<LeaseInfo>::iterator l;

		CSMapIter iter = chunkToServerMap.find(c->chunkId);
		if (iter == chunkToServerMap.end())
			return false;
		v = iter->second;
		l = find_if(v.chunkLeases.begin(), v.chunkLeases.end(),
				ptr_fun(LeaseInfo::IsValidLease));
		return (l != v.chunkLeases.end());
	}
};

bool
LayoutManager::IsValidLeaseIssued(const vector <MetaChunkInfo *> &c)
{
	vector <MetaChunkInfo *>::const_iterator i;

	i = find_if(c.begin(), c.end(), ValidLeaseIssued(mChunkToServerMap));
	if (i == c.end())
		return false;
	KFS_LOG_VA_DEBUG("Valid lease issued on chunk: %lld",
			(*i)->chunkId);
	return true;
}

class LeaseIdMatcher {
	int64_t myid;
public:
	LeaseIdMatcher(int64_t id) : myid(id) { }
	bool operator() (const LeaseInfo &l) {
		return l.leaseId == myid;
	}
};

int
LayoutManager::LeaseRenew(MetaLeaseRenew *req)
{
	ChunkPlacementInfo v;
	vector<LeaseInfo>::iterator l;

        CSMapIter iter = mChunkToServerMap.find(req->chunkId);
        if (iter == mChunkToServerMap.end()) {
		if (InRecovery()) {
			// Allow lease renewals during recovery
			LeaseInfo lease(req->leaseType, req->leaseId);
			if (req->leaseId > mLeaseId)
				mLeaseId = req->leaseId + 1;
			v.chunkLeases.push_back(lease);
			mChunkToServerMap[req->chunkId] = v;
			return 0;
		}
                return -EINVAL;

	}
	v = iter->second;
	l = find_if(v.chunkLeases.begin(), v.chunkLeases.end(),
			LeaseIdMatcher(req->leaseId));
	if (l == v.chunkLeases.end())
		return -EINVAL;
	time_t now = time(0);
	if (now > l->expires) {
		// can't renew dead leases; get a new one
		v.chunkLeases.erase(l);
		return -ELEASEEXPIRED;
	}
	l->expires = now + LEASE_INTERVAL_SECS;
	mChunkToServerMap[req->chunkId] = v;
	return 0;
}

int
LayoutManager::LeaseRelinquish(MetaLeaseRelinquish *req)
{
	ChunkPlacementInfo v;
	vector<LeaseInfo>::iterator l;

        CSMapIter iter = mChunkToServerMap.find(req->chunkId);
	if (iter == mChunkToServerMap.end())
		return -ELEASEEXPIRED;
	
	v = iter->second;
	l = find_if(v.chunkLeases.begin(), v.chunkLeases.end(),
			LeaseIdMatcher(req->leaseId));
	if (l == v.chunkLeases.end())
		return -EINVAL;
	// the owner of the lease is giving up the lease; so, remove the lease
	v.chunkLeases.erase(l);
	mChunkToServerMap[req->chunkId] = v;
	return 0;
}

///
/// Handling a corrupted chunk involves removing the mapping
/// from chunk id->chunkserver that we know has it.
///
void
LayoutManager::ChunkCorrupt(MetaChunkCorrupt *r)
{
	ChunkPlacementInfo v;

	r->server->IncCorruptChunks();

        CSMapIter iter = mChunkToServerMap.find(r->chunkId);
	if (iter == mChunkToServerMap.end())
		return;

	v = iter->second;
	if(v.fid != r->fid) {
		KFS_LOG_VA_WARN("Server %s claims invalid chunk: <%lld, %lld> to be corrupt",
				r->server->ServerID().c_str(), r->fid, r->chunkId);
		return;
	}

	KFS_LOG_VA_INFO("Server %s claims file/chunk: <%lld, %lld> to be corrupt",
			r->server->ServerID().c_str(), r->fid, r->chunkId);
	v.chunkServers.erase(remove_if(v.chunkServers.begin(), v.chunkServers.end(),
			ChunkServerMatcher(r->server.get())), v.chunkServers.end());
	mChunkToServerMap[r->chunkId] = v;
	// check the replication state when the replicaiton checker gets to it
	ChangeChunkReplication(r->chunkId);

	// this chunk has to be replicated from elsewhere; since this is no
	// longer hosted on this server, take it out of its list of blocks
	r->server->MovingChunkDone(r->chunkId);
	if (r->server->IsRetiring()) {
		r->server->EvacuateChunkDone(r->chunkId);
	}
}

class ChunkDeletor {
    chunkId_t chunkId;
public:
    ChunkDeletor(chunkId_t c) : chunkId(c) { }
    void operator () (ChunkServerPtr &c) { c->DeleteChunk(chunkId); }
};

///
/// Deleting a chunk involves two things: (1) removing the
/// mapping from chunk id->chunk server that has it; (2) sending
/// an RPC to the associated chunk server to nuke out the chunk.
///
void
LayoutManager::DeleteChunk(chunkId_t chunkId)
{
	vector<ChunkServerPtr> c;

        // if we know anything about this chunk at all, then we
        // process the delete request.
	if (GetChunkToServerMapping(chunkId, c) != 0)
		return;

	// remove the mapping
	mChunkToServerMap.erase(chunkId);

	// submit an RPC request
	for_each(c.begin(), c.end(), ChunkDeletor(chunkId));
}


class Truncator {
    chunkId_t chunkId;
    off_t sz;
public:
    Truncator(chunkId_t c, off_t s) : chunkId(c), sz(s) { }
    void operator () (ChunkServerPtr &c) { c->TruncateChunk(chunkId, sz); }
};

///
/// To truncate a chunk, find the server that holds the chunk and
/// submit an RPC request to it.
///
void
LayoutManager::TruncateChunk(chunkId_t chunkId, off_t sz)
{
	vector<ChunkServerPtr> c;

        // if we know anything about this chunk at all, then we
        // process the truncate request.
	if (GetChunkToServerMapping(chunkId, c) != 0)
		return;

	// submit an RPC request
        Truncator doTruncate(chunkId, sz);
	for_each(c.begin(), c.end(), doTruncate);
}

void
LayoutManager::AddChunkToServerMapping(chunkId_t chunkId, fid_t fid,
					ChunkServer *c)
{
	ChunkPlacementInfo v;

        if (c == NULL) {
		// Store an empty mapping to signify the presence of this
		// particular chunkId.
		v.fid = fid;
		mChunkToServerMap[chunkId] = v;
		return;
        }

	assert(ValidServer(c));

	KFS_LOG_VA_DEBUG("Laying out chunk=%lld on server %s",
			 chunkId, c->GetServerName());

	if (UpdateChunkToServerMapping(chunkId, c) == 0)
            return;

	v.fid = fid;
        v.chunkServers.push_back(c->shared_from_this());
        mChunkToServerMap[chunkId] = v;
}

void
LayoutManager::RemoveChunkToServerMapping(chunkId_t chunkId)
{
        CSMapIter iter = mChunkToServerMap.find(chunkId);
        if (iter == mChunkToServerMap.end())
                return;

        mChunkToServerMap.erase(iter);
}

int
LayoutManager::UpdateChunkToServerMapping(chunkId_t chunkId, ChunkServer *c)
{
        // If the chunkid isn't present in the mapping table, it could be a
        // stale chunk
        CSMapIter iter = mChunkToServerMap.find(chunkId);
        if (iter == mChunkToServerMap.end())
                return -1;

	/*
	KFS_LOG_VA_DEBUG("chunk=%lld was laid out on server %s",
			 chunkId, c->GetServerName());
	*/
        iter->second.chunkServers.push_back(c->shared_from_this());

        return 0;
}

int
LayoutManager::GetChunkToServerMapping(chunkId_t chunkId, vector<ChunkServerPtr> &c)
{
        CSMapConstIter iter = mChunkToServerMap.find(chunkId);
        if ((iter == mChunkToServerMap.end()) ||
		(iter->second.chunkServers.size() == 0))
                return -1;

        c = iter->second.chunkServers;
        return 0;
}

/// Wrapper class due to silly template/smart-ptr madness
class Dispatcher {
public:
	Dispatcher() { }
	void operator() (ChunkServerPtr &c) {
		c->Dispatch();
	}
};

void
LayoutManager::Dispatch()
{
	// this method is called in the context of the network thread.
	// lock out the request processor to prevent changes to the list.

	pthread_mutex_lock(&mChunkServersMutex);

	for_each(mChunkServers.begin(), mChunkServers.end(), Dispatcher());

	pthread_mutex_unlock(&mChunkServersMutex);
}

class Heartbeater {
public:
	Heartbeater() { }
	void operator() (ChunkServerPtr &c) {
		c->Heartbeat();
	}
};

void
LayoutManager::HeartbeatChunkServers()
{
	// this method is called in the context of the network thread.
	// lock out the request processor to prevent changes to the list.

	pthread_mutex_lock(&mChunkServersMutex);

	for_each(mChunkServers.begin(), mChunkServers.end(), Heartbeater());

	pthread_mutex_unlock(&mChunkServersMutex);
}

bool
LayoutManager::ValidServer(ChunkServer *c)
{
	vector <ChunkServerPtr>::const_iterator i;

	i = find_if(mChunkServers.begin(), mChunkServers.end(),
		ChunkServerMatcher(c));
	return (i != mChunkServers.end());
}

class Pinger {
	string &result;
	// return the total/used for all the nodes in the cluster
	uint64_t &totalSpace;
	uint64_t &usedSpace;
public:
	Pinger(string &r, uint64_t &t, uint64_t &u) :
		result(r), totalSpace(t), usedSpace(u) { }
	void operator () (ChunkServerPtr &c)
	{
		c->Ping(result);
		totalSpace += c->GetTotalSpace();
		usedSpace += c->GetUsedSpace();
	}
};

class RetiringStatus {
	string &result;
public:
	RetiringStatus(string &r):result(r) { }
	void operator () (ChunkServerPtr &c) { c->GetRetiringStatus(result); }
};

void
LayoutManager::Ping(string &systemInfo, string &upServers, string &downServers, string &retiringServers)
{
	uint64_t totalSpace = 0, usedSpace = 0;
	Pinger doPing(upServers, totalSpace, usedSpace);
	for_each(mChunkServers.begin(), mChunkServers.end(), doPing);
	downServers = mDownServers.str();
	for_each(mChunkServers.begin(), mChunkServers.end(), RetiringStatus(retiringServers));

	ostringstream os;

	os << "Up since= " << timeToStr(mRecoveryStartTime) << '\t';
	os << "Total space= " << totalSpace << '\t';
	os << "Used space= " << usedSpace;
	systemInfo = os.str();
}

class UpServersList {
    ostringstream &os;
public:
	UpServersList(ostringstream &os): os(os) { }
    void operator () (ChunkServerPtr &c) {
        os << c->GetServerLocation().ToString() << endl;
    }
};

void
LayoutManager::UpServers(ostringstream &os)
{
    UpServersList upServers(os);
    for_each(mChunkServers.begin(), mChunkServers.end(), upServers);
}

/// functor to tell if a lease has expired
class LeaseExpired {
	time_t now;
public:
	LeaseExpired(time_t n): now(n) { }
	bool operator () (const LeaseInfo &l) { return now >= l.expires; }

};

class ChunkWriteDecrementor {
public:
	void operator() (ChunkServerPtr &c) { c->UpdateNumChunkWrites(-1); }
};

/// If the write lease on a chunk is expired, then decrement the # of writes
/// on the servers that are involved in the write.
class DecChunkWriteCount {
	fid_t f;
	chunkId_t c;
public:
	DecChunkWriteCount(fid_t fid, chunkId_t id) : f(fid), c(id) { }
	void operator() (const LeaseInfo &l) {
		if (l.leaseType != WRITE_LEASE)
			return;
		vector<ChunkServerPtr> servers;
		gLayoutManager.GetChunkToServerMapping(c, servers);
		for_each(servers.begin(), servers.end(), ChunkWriteDecrementor());
		// get the chunk's size from one of the servers
		if (servers.size() > 0)
			servers[0]->GetChunkSize(f, c, l.pathname);
	}

};

/// functor to that expires out leases
class LeaseExpirer {
	CSMap &cmap;
	time_t now;
public:
	LeaseExpirer(CSMap &m, time_t n): cmap(m), now(n) { }
	void operator () (const map<chunkId_t, ChunkPlacementInfo >::value_type p)
	{
		ChunkPlacementInfo c = p.second;
		chunkId_t chunkId = p.first;
		vector<LeaseInfo>::iterator i;

		i = remove_if(c.chunkLeases.begin(), c.chunkLeases.end(),
			LeaseExpired(now));

		for_each(i, c.chunkLeases.end(), DecChunkWriteCount(c.fid, chunkId));
		// trim the list
		c.chunkLeases.erase(i, c.chunkLeases.end());
		cmap[p.first] = c;
	}
};

void
LayoutManager::LeaseCleanup()
{
	time_t now = time(0);

	for_each(mChunkToServerMap.begin(), mChunkToServerMap.end(),
		LeaseExpirer(mChunkToServerMap, now));

}

// Cleanup the leases for a particular chunk
void
LayoutManager::LeaseCleanup(chunkId_t chunkId, ChunkPlacementInfo &v)
{
	for_each(v.chunkLeases.begin(), v.chunkLeases.end(),
		DecChunkWriteCount(v.fid, chunkId));
	v.chunkLeases.clear();
}

class RetiringServerPred {
public:
	RetiringServerPred() { }
	bool operator()(const ChunkServerPtr &c) {
		return c->IsRetiring();
	}
};

class ReplicationDoneNotifier {
	chunkId_t cid;
public:
	ReplicationDoneNotifier(chunkId_t c) : cid(c) { }
	void operator()(ChunkServerPtr &s) {
		s->EvacuateChunkDone(cid);
	}
};

class RackSetter {
	set<int> &racks;
	bool excludeRetiringServers;
public:
	RackSetter(set<int> &r, bool excludeRetiring = false) :
		racks(r), excludeRetiringServers(excludeRetiring) { }
	void operator()(const ChunkServerPtr &s) {
		if (excludeRetiringServers && s->IsRetiring())
			return;
		racks.insert(s->GetRack());
	}
};

int
LayoutManager::ReplicateChunk(chunkId_t chunkId, const ChunkPlacementInfo &clli,
				uint32_t extraReplicas)
{
	vector<int> racks;
	set<int> excludeRacks;
	// find a place
	vector<ChunkServerPtr> candidates;

	// two steps here: first, exclude the racks on which chunks are already
	// placed; if we can't find a unique rack, then put it wherever
	// for accounting purposes, ignore the rack(s) which contain a retiring
	// chunkserver; we'd like to move the block within that rack if
	// possible.

	for_each(clli.chunkServers.begin(), clli.chunkServers.end(),
		RackSetter(excludeRacks, true));

	FindCandidateRacks(racks, excludeRacks);
	if (racks.size() == 0) {
		// no new rack is available to put the chunk
		// take what we got
		FindCandidateRacks(racks);
		if (racks.size() == 0)
			// no rack is available
			return 0;
	}

	uint32_t numServersPerRack = extraReplicas / racks.size();
	if (extraReplicas % racks.size())
		numServersPerRack++;

	for (uint32_t idx = 0; idx < racks.size(); idx++) {
		if (candidates.size() >= extraReplicas)
			break;
		vector<ChunkServerPtr> servers;

		// find candidates other than those that are already hosting the
		// chunk
		FindCandidateServers(servers, clli.chunkServers, racks[idx]);

		// take as many as we can from this rack
		for (uint32_t i = 0; i < servers.size() && i < numServersPerRack; i++) {
			if (candidates.size() >= extraReplicas)
				break;
			candidates.push_back(servers[i]);
		}
	}

	if (candidates.size() == 0)
		return 0;

	return ReplicateChunk(chunkId, clli, extraReplicas, candidates);
}

int
LayoutManager::ReplicateChunk(chunkId_t chunkId, const ChunkPlacementInfo &clli,
				uint32_t extraReplicas, const
				vector<ChunkServerPtr> &candidates)
{
	ChunkServerPtr c, dataServer;
	vector<MetaChunkInfo *> v;
	vector<MetaChunkInfo *>::iterator chunk;
	fid_t fid = clli.fid;
	int numDone = 0;

	/*
	metatree.getalloc(fid, v);
	chunk = find_if(v.begin(), v.end(), ChunkIdMatcher(chunkId));
	if (chunk == v.end()) {
		panic("missing chunk", true);
	}

	MetaChunkInfo *mci = *chunk;
	*/

	for (uint32_t i = 0; i < candidates.size() && i < extraReplicas; i++) {
		vector<ChunkServerPtr>::const_iterator iter;

		c = candidates[i];
		// Don't send too many replications to a server
		if (c->GetNumChunkReplications() > MAX_CONCURRENT_WRITE_REPLICATIONS_PER_NODE)
			continue;
#ifdef DEBUG
		// verify that we got good candidates
		iter = find(clli.chunkServers.begin(), clli.chunkServers.end(), c);
		if (iter != clli.chunkServers.end()) {
			assert(!"Not possible...");
		}
#endif
		// prefer a server that is being retired to the other nodes as
		// the source of the chunk replication
		iter = find_if(clli.chunkServers.begin(), clli.chunkServers.end(),
				RetiringServerPred());

		const char *reason;

		if (iter != clli.chunkServers.end()) {
			reason = " evacuating chunk ";
			if (((*iter)->GetReplicationReadLoad() <
				MAX_CONCURRENT_READ_REPLICATIONS_PER_NODE) &&
				(*iter)->IsResponsiveServer())
				dataServer = *iter;
		} else {
			reason = " re-replication ";
		}

		// if we can't find a retiring server, pick a server that has read b/w available
		for (uint32_t j = 0; (!dataServer) &&
				(j < clli.chunkServers.size()); j++) {
			if ((clli.chunkServers[j]->GetReplicationReadLoad() >= 
				MAX_CONCURRENT_READ_REPLICATIONS_PER_NODE) ||
				(!(clli.chunkServers[j]->IsResponsiveServer())))
				continue;
			dataServer = clli.chunkServers[j];
		}
		if (dataServer) {
			ServerLocation srcLocation = dataServer->GetServerLocation();
			ServerLocation dstLocation = c->GetServerLocation();

			KFS_LOG_VA_INFO("Starting re-replication for chunk %lld (from: %s to %s) reason = %s",
					chunkId,
					srcLocation.ToString().c_str(),
					dstLocation.ToString().c_str(), reason);
			dataServer->UpdateReplicationReadLoad(1);
			/*
			c->ReplicateChunk(fid, chunkId, mci->chunkVersion,
				dataServer->GetServerLocation());
			*/
			// have the chunkserver get the version
			c->ReplicateChunk(fid, chunkId, -1,
				dataServer->GetServerLocation());
			numDone++;
		}
		dataServer.reset();
	}

	if (numDone > 0) {
		mTotalReplicationStats->Update(1);
		mOngoingReplicationStats->Update(numDone);
	}
	return numDone;
}

bool
LayoutManager::CanReplicateChunkNow(chunkId_t chunkId,
				ChunkPlacementInfo &c,
				int &extraReplicas)
{
	vector<LeaseInfo>::iterator l;

	// Don't replicate chunks for which a write lease
	// has been issued.
	l = find_if(c.chunkLeases.begin(), c.chunkLeases.end(),
		ptr_fun(LeaseInfo::IsValidWriteLease));

	if (l != c.chunkLeases.end())
		return false;

	extraReplicas = 0;
	// Can't re-replicate a chunk if we don't have a copy! so,
	// take out this chunk from the candidate set.
	if (c.chunkServers.size() == 0)
		return true;

	MetaFattr *fa = metatree.getFattr(c.fid);
	if (fa == NULL)
		// No file attr.  So, take out this chunk
		// from the candidate set.
		return true;

	// check if the chunk still exists
	vector<MetaChunkInfo *> v;
	vector<MetaChunkInfo *>::iterator chunk;

	metatree.getalloc(c.fid, v);
	chunk = find_if(v.begin(), v.end(), ChunkIdMatcher(chunkId));
	if (chunk == v.end()) {
		// This chunk doesn't exist in this file anymore.
		// So, take out this chunk from the candidate set.
		return true;
	}

	// if any of the chunkservers are retiring, we need to make copies
	// so, first determine how many copies we need because one of the
	// servers hosting the chunk is going down
	int numRetiringServers = count_if(c.chunkServers.begin(), c.chunkServers.end(),
					RetiringServerPred());
	// now, determine if we have sufficient copies
	if (numRetiringServers > 0) {
		if (c.chunkServers.size() - numRetiringServers < (uint32_t) fa->numReplicas) {
			// we need to make this many copies: # of servers that are
			// retiring plus the # this chunk is under-replicated
			extraReplicas = numRetiringServers +  (fa->numReplicas - c.chunkServers.size());
		} else {
			// we got sufficient copies even after accounting for
			// the retiring server.  so, take out this chunk from
			// replication candidates set.
			extraReplicas = 0;
		}
		return true;
	}

	// May need to re-replicate this chunk:
	//    - extraReplicas > 0 means make extra copies;
	//    - extraReplicas == 0, take out this chunkid from the candidate set
	//    - extraReplicas < 0, means we got too many copies; delete some
	extraReplicas = fa->numReplicas - c.chunkServers.size();

	if (extraReplicas < 0) {
		//
		// We need to delete additional copies; however, if
		// there is a valid (read) lease issued on the chunk,
		// then leave the chunk alone for now; we'll look at
		// deleting it when the lease has expired.  This is for
		// safety: if a client was reading from the copy of the
		// chunk that we are trying to delete, the client will
		// see the deletion and will have to failover; avoid
		// unnecessary failovers
		//
		l = find_if(c.chunkLeases.begin(), c.chunkLeases.end(),
				ptr_fun(LeaseInfo::IsValidLease));

		if (l != c.chunkLeases.end())
			return false;
	}

	return true;
}

class EvacuateChunkChecker {
	CRCandidateSet &candidates;
	CSMap &chunkToServerMap;
public:
	EvacuateChunkChecker(CRCandidateSet &c, CSMap &m) :
		candidates(c), chunkToServerMap(m) {}
	void operator()(ChunkServerPtr c) {
		if (!c->IsRetiring())
			return;
		CRCandidateSet leftover = c->GetEvacuatingChunks();
		for (CRCandidateSetIter citer = leftover.begin(); citer !=
			leftover.end(); ++citer) {
			chunkId_t chunkId = *citer;
			CSMapIter iter = chunkToServerMap.find(chunkId);

			if (iter == chunkToServerMap.end()) {
				c->EvacuateChunkDone(chunkId);
				KFS_LOG_VA_INFO("%s has bogus block %ld",
					c->GetServerLocation().ToString().c_str(), chunkId);
			} else {
				// XXX
				// if we don't think this chunk is on this
				// server, then we should update the view...

				candidates.insert(chunkId);
				KFS_LOG_VA_INFO("%s has block %ld that wasn't in replication candidates",
					c->GetServerLocation().ToString().c_str(), chunkId);
			}
		}
	}
};

void
LayoutManager::CheckHibernatingServersStatus()
{
	time_t now = time(0);

	vector <HibernatingServerInfo_t>::iterator iter = mHibernatingServers.begin();
	vector<ChunkServerPtr>::iterator i;

	while (iter != mHibernatingServers.end()) {
		i = find_if(mChunkServers.begin(), mChunkServers.end(),
			MatchingServer(iter->location));
		if ((i == mChunkServers.end()) && (now < iter->sleepEndTime)) {
			// within the time window where the server is sleeping
			// so, move on
			iter++;
			continue;
		}
		if (i != mChunkServers.end()) {
			KFS_LOG_VA_INFO("Hibernated server (%s) is back as promised...",
					iter->location.ToString().c_str());
		} else {
			// server hasn't come back as promised...so, check
			// re-replication for the blocks that were on that node
			KFS_LOG_VA_INFO("Hibernated server (%s) is not back as promised...",
				iter->location.ToString().c_str());
			mChunkReplicationCandidates.insert(iter->blocks.begin(), iter->blocks.end());
		}
		mHibernatingServers.erase(iter);
		iter = mHibernatingServers.begin();
	}
}

void
LayoutManager::ChunkReplicationChecker()
{
	if (InRecovery()) {
		return;
	}

	CheckHibernatingServersStatus();

	// There is a set of chunks that are affected: their server went down
	// or there is a change in their degree of replication.  in either
	// case, walk this set of chunkid's and work on their replication amount.

	chunkId_t chunkId;
	CRCandidateSet delset;
	int extraReplicas, numOngoing;
	uint32_t numIterations = 0;
	struct timeval start;

	gettimeofday(&start, NULL);

	for (CRCandidateSetIter citer = mChunkReplicationCandidates.begin();
		citer != mChunkReplicationCandidates.end(); ++citer) {
		chunkId = *citer;

		struct timeval now;
		gettimeofday(&now, NULL);

		if (ComputeTimeDiff(start, now) > 5.0)
			// if we have spent more than 5 seconds here, stop
			// serve other requests
			break;

        	CSMapIter iter = mChunkToServerMap.find(chunkId);
        	if (iter == mChunkToServerMap.end()) {
			delset.insert(chunkId);
			continue;
		}
		if (iter->second.ongoingReplications > 0)
			// this chunk is being re-replicated; we'll check later
			continue;

		if (!CanReplicateChunkNow(iter->first, iter->second, extraReplicas))
			continue;

		if (extraReplicas > 0) {
			numOngoing = ReplicateChunk(iter->first, iter->second, extraReplicas);
			iter->second.ongoingReplications += numOngoing;
			if (numOngoing > 0) {
				mNumOngoingReplications++;
				mLastChunkReplicated = chunkId;
				numIterations++;
			}
		} else if (extraReplicas == 0) {
			delset.insert(chunkId);
		} else {
			DeleteAddlChunkReplicas(iter->first, iter->second, -extraReplicas);
			delset.insert(chunkId);
		}
		if (numIterations > mChunkServers.size() *
			MAX_CONCURRENT_WRITE_REPLICATIONS_PER_NODE)
			// throttle...we are handing out
			break;
	}

	if (delset.size() > 0) {
		for (CRCandidateSetIter citer = delset.begin();
			citer != delset.end(); ++citer) {
			// Notify the retiring servers of any of their chunks have
			// been evacuated---such as, if there were too many copies of those
			// chunks, we are done evacuating them
			chunkId = *citer;
        		CSMapIter iter = mChunkToServerMap.find(chunkId);
			if (iter != mChunkToServerMap.end())
				for_each(iter->second.chunkServers.begin(), iter->second.chunkServers.end(),
					ReplicationDoneNotifier(chunkId));
			mChunkReplicationCandidates.erase(*citer);
		}
	}

	if (mChunkReplicationCandidates.size() == 0) {
		// if there are any retiring servers, we need to make sure that
		// the servers don't think there is a block to be replicated
		// if there is any such, let us get them into the set of
		// candidates...need to know why this happens
		for_each(mChunkServers.begin(), mChunkServers.end(),
			EvacuateChunkChecker(mChunkReplicationCandidates, mChunkToServerMap));
	}

	RebalanceServers();

	mReplicationTodoStats->Set(mChunkReplicationCandidates.size());
}

void
LayoutManager::FindReplicationWorkForServer(ChunkServerPtr &server, chunkId_t chunkReplicated)
{
	chunkId_t chunkId;
	int extraReplicas = 0, numOngoing;
	vector<ChunkServerPtr> c;

	if (server->IsRetiring())
		return;

	c.push_back(server);

	// try to start where we were done with this server
	CRCandidateSetIter citer = mChunkReplicationCandidates.find(chunkReplicated + 1);

	if (citer == mChunkReplicationCandidates.end()) {
		// try to start where we left off last time; if that chunk has
		// disappeared, find something "closeby"
		citer = mChunkReplicationCandidates.upper_bound(mLastChunkReplicated);
	}

	if (citer == mChunkReplicationCandidates.end()) {
		mLastChunkReplicated = 1;
		citer = mChunkReplicationCandidates.begin();
	}

	struct timeval start, now;

	gettimeofday(&start, NULL);

	for (; citer != mChunkReplicationCandidates.end(); ++citer) {
		gettimeofday(&now, NULL);

		if (ComputeTimeDiff(start, now) > 0.2)
			// if we have spent more than 200 m-seconds here, stop
			// serve other requests
			break;

		chunkId = *citer;

        	CSMapIter iter = mChunkToServerMap.find(chunkId);
        	if (iter == mChunkToServerMap.end()) {
			continue;
		}
		if (iter->second.ongoingReplications > 0)
			continue;

		// if the chunk is already hosted on this server, the chunk isn't a candidate for
		// work to be sent to this server.
		if (IsChunkHostedOnServer(iter->second.chunkServers, server) ||
			(!CanReplicateChunkNow(iter->first, iter->second, extraReplicas)))
			continue;

		if (extraReplicas > 0) {
			if (mRacks.size() > 1) {
				// when there is more than one rack, since we
				// are re-replicating a chunk, we don't want to put two
				// copies of a chunk on the same rack.  if one
				// of the nodes is retiring, we don't want to
				// count the node in this rack set---we want to
				// put a block on to the same rack.
				set<int> excludeRacks;
				for_each(iter->second.chunkServers.begin(), iter->second.chunkServers.end(),
					RackSetter(excludeRacks, true));
				if (excludeRacks.find(server->GetRack()) != excludeRacks.end())
					continue;
			}

			numOngoing = ReplicateChunk(iter->first, iter->second, 1, c);
			iter->second.ongoingReplications += numOngoing;
			if (numOngoing > 0) {
				mNumOngoingReplications++;
				mLastChunkReplicated = chunkId;
			}
		}

		if (server->GetNumChunkReplications() > MAX_CONCURRENT_WRITE_REPLICATIONS_PER_NODE)
			break;
	}
	// if there is any room left to do more work...
	ExecuteRebalancePlan(server);
}

void
LayoutManager::ChunkReplicationDone(MetaChunkReplicate *req)
{
	vector<ChunkServerPtr>::iterator source;

	mOngoingReplicationStats->Update(-1);

	// Book-keeping....
	CSMapIter iter = mChunkToServerMap.find(req->chunkId);

	if (iter != mChunkToServerMap.end()) {
		iter->second.ongoingReplications--;
		if (iter->second.ongoingReplications == 0)
			// if all the replications for this chunk are done,
			// then update the global counter.
			mNumOngoingReplications--;

		if (iter->second.ongoingReplications < 0)
			// sanity...
			iter->second.ongoingReplications = 0;
	}

	req->server->ReplicateChunkDone(req->chunkId);

	source = find_if(mChunkServers.begin(), mChunkServers.end(),
			MatchingServer(req->srcLocation));
	if (source !=  mChunkServers.end()) {
		(*source)->UpdateReplicationReadLoad(-1);
	}

	if (req->status != 0) {
		// Replication failed...we will try again later
		KFS_LOG_VA_INFO("%s: re-replication for chunk %lld failed, code = %d",
			req->server->GetServerLocation().ToString().c_str(),
			req->chunkId, req->status);
		mFailedReplicationStats->Update(1);
		return;
	}

	// replication succeeded: book-keeping

	// if any of the hosting servers were being "retired", notify them that
	// re-replication of any chunks hosted on them is finished
	if (iter != mChunkToServerMap.end()) {
		for_each(iter->second.chunkServers.begin(), iter->second.chunkServers.end(),
			ReplicationDoneNotifier(req->chunkId));
	}

	// validate that the server got the latest copy of the chunk
	vector<MetaChunkInfo *> v;
	vector<MetaChunkInfo *>::iterator chunk;

	metatree.getalloc(req->fid, v);
	chunk = find_if(v.begin(), v.end(), ChunkIdMatcher(req->chunkId));
	if (chunk == v.end()) {
		// Chunk disappeared -> stale; this chunk will get nuked
		KFS_LOG_VA_INFO("Re-replicate: chunk (%lld) disappeared => so, stale",
				req->chunkId);
		mFailedReplicationStats->Update(1);
		req->server->NotifyStaleChunk(req->chunkId);
		return;
	}
	MetaChunkInfo *mci = *chunk;
	if (mci->chunkVersion != req->chunkVersion) {
		// Version that we replicated has changed...so, stale
		KFS_LOG_VA_INFO("Re-replicate: chunk (%lld) version changed (was=%lld, now=%lld) => so, stale",
				req->chunkId, req->chunkVersion, mci->chunkVersion);
		mFailedReplicationStats->Update(1);
		req->server->NotifyStaleChunk(req->chunkId);
		return;
	}

	// Yaeee...all good...
	KFS_LOG_VA_DEBUG("%s reports that re-replication for chunk %lld is all done",
			req->server->GetServerLocation().ToString().c_str(), req->chunkId);
	UpdateChunkToServerMapping(req->chunkId, req->server.get());

	// since this server is now free, send more work its way...
	if (req->server->GetNumChunkReplications() <= 1) {
		FindReplicationWorkForServer(req->server, req->chunkId);
	}
}

//
// To delete additional copies of a chunk, find the servers that have the least
// amount of space and delete the chunk from there.  In addition, also pay
// attention to rack-awareness: if two copies are on the same rack, then we pick
// the server that is the most loaded and delete it there
//
void
LayoutManager::DeleteAddlChunkReplicas(chunkId_t chunkId, ChunkPlacementInfo &clli,
				uint32_t extraReplicas)
{
	vector<ChunkServerPtr> servers = clli.chunkServers, copiesToDiscard;
	uint32_t numReplicas = servers.size() - extraReplicas;
	set<int> chosenRacks;

	// if any of the copies are on nodes that are retiring, leave the copies
	// alone; we will reclaim space later if needed
        int numRetiringServers = count_if(servers.begin(), servers.end(),
					RetiringServerPred());
	if (numRetiringServers > 0)
		return;

	// We get servers sorted by increasing amount of space utilization; so the candidates
	// we want to delete are at the end
	SortServersByUtilization(servers);

	for_each(servers.begin(), servers.end(), RackSetter(chosenRacks));
	if (chosenRacks.size() == numReplicas) {
		// we need to keep as many copies as racks.  so, find the extra
		// copies on a given rack and delete them
		clli.chunkServers.clear();
		chosenRacks.clear();
		for (uint32_t i = 0; i < servers.size(); i++) {
			if (chosenRacks.find(servers[i]->GetRack()) ==
				chosenRacks.end()) {
				chosenRacks.insert(servers[i]->GetRack());
				clli.chunkServers.push_back(servers[i]);
			} else {
				// second copy on the same rack
				copiesToDiscard.push_back(servers[i]);
			}
		}
	} else {
		clli.chunkServers = servers;
		// Get rid of the extra stuff from the end
		clli.chunkServers.resize(numReplicas);

		// The first N are what we want to keep; the rest should go.
		copiesToDiscard.insert(copiesToDiscard.end(), servers.begin() + numReplicas, servers.end());
	}
	mChunkToServerMap[chunkId] = clli;

	ostringstream msg;
	msg << "Chunk " << chunkId << " lives on: \n";
	for (uint32_t i = 0; i < clli.chunkServers.size(); i++) {
		msg << clli.chunkServers[i]->GetServerLocation().ToString() << ' '
			<< clli.chunkServers[i]->GetRack() << "; ";
	}
	msg << "\n";
	msg << "Discarding chunk on: ";
	for (uint32_t i = 0; i < copiesToDiscard.size(); i++) {
		msg << copiesToDiscard[i]->GetServerLocation().ToString() << ' '
			<< copiesToDiscard[i]->GetRack() << " ";
	}
	msg << "\n";

	KFS_LOG_VA_INFO("%s", msg.str().c_str());

	for_each(copiesToDiscard.begin(), copiesToDiscard.end(), ChunkDeletor(chunkId));
}

void
LayoutManager::ChangeChunkReplication(chunkId_t chunkId)
{
	mChunkReplicationCandidates.insert(chunkId);
}

//
// Check if the server is part of the set of the servers hosting the chunk
//
bool
LayoutManager::IsChunkHostedOnServer(const vector<ChunkServerPtr> &hosters,
					const ChunkServerPtr &server)
{
	vector<ChunkServerPtr>::const_iterator iter;
	iter = find(hosters.begin(), hosters.end(), server);
	return iter != hosters.end();
}

class LoadedServerPred {
	double maxServerSpaceUtilThreshold;
public:
	LoadedServerPred(double m) : maxServerSpaceUtilThreshold(m) { }
	bool operator()(const ChunkServerPtr &s) const {
		return s->GetSpaceUtilization() > maxServerSpaceUtilThreshold;
	}
};

//
// We are trying to move a chunk between two servers on the same rack.  For a
// given chunk, we try to find as many "migration pairs" (source/destination
// nodes) within the respective racks.
//
void
LayoutManager::FindIntraRackRebalanceCandidates(vector<ChunkServerPtr> &candidates,
			const vector<ChunkServerPtr> &nonloadedServers,
			const ChunkPlacementInfo &clli)
{
	vector<ChunkServerPtr>::const_iterator iter;

	for (uint32_t i = 0; i < clli.chunkServers.size(); i++) {
		vector<ChunkServerPtr> servers;

		if (clli.chunkServers[i]->GetSpaceUtilization() <
			mMaxRebalanceSpaceUtilThreshold) {
			continue;
		}
		//we have a loaded server; find another non-loaded
		//server within the same rack (case 1 from above)
		FindCandidateServers(servers, nonloadedServers, clli.chunkServers,
					clli.chunkServers[i]->GetRack());
		if (servers.size() == 0) {
			// nothing available within the rack to do the move
			continue;
		}
		// make sure that we are not putting 2 copies of a chunk on the
		// same server
		for (uint32_t j = 0; j < servers.size(); j++) {
			iter = find(candidates.begin(), candidates.end(), servers[j]);
			if (iter == candidates.end()) {
				candidates.push_back(servers[j]);
				break;
			}
		}
	}
}


//
// For rebalancing, for a chunk, we could not find a candidate server on the same rack as a
// loaded server.  Hence, we are trying to move the chunk between two servers on two
// different racks.  So, find a migration pair: source/destination on two different racks.
//
void
LayoutManager::FindInterRackRebalanceCandidate(ChunkServerPtr &candidate,
			const vector<ChunkServerPtr> &nonloadedServers,
			const ChunkPlacementInfo &clli)
{
	vector<ChunkServerPtr> servers;

	FindCandidateServers(servers, nonloadedServers, clli.chunkServers);
	if (servers.size() == 0) {
		return;
	}
	// XXX: in emulation mode, we have 0 racks due to compile issues
	if (mRacks.size() <= 1)
		return;

	// if we had only one rack then the intra-rack move should have found a
	// candidate.
	assert(mRacks.size() > 1);
	if (mRacks.size() <= 1)
		return;

	// For the candidate we pick, we want to enforce the property that all
	// the copies of the chunks are on different racks.
	set<int> excludeRacks;
        for_each(clli.chunkServers.begin(), clli.chunkServers.end(),
	                RackSetter(excludeRacks));

	for (uint32_t i = 0; i < servers.size(); i++) {
		set<int>::iterator iter = excludeRacks.find(servers[i]->GetRack());
		if (iter == excludeRacks.end()) {
			candidate = servers[i];
			return;
		}
	}
}

//
// Periodically, if we find that some chunkservers have LOT (> 80% free) of space
// and if others are loaded (i.e., < 30% free space), move chunks around.  This
// helps with keeping better disk space utilization (and maybe load).
//
int
LayoutManager::RebalanceServers()
{
	if ((InRecovery()) || (mChunkServers.size() == 0)) {
		return 0;
	}

	// if we are doing rebalancing based on a plan, execute as
	// much of the plan as there is room.

	ExecuteRebalancePlan();

	for_each(mRacks.begin(), mRacks.end(), mem_fun_ref(&RackInfo::computeSpace));

	if (!mIsRebalancingEnabled)
		return 0;

	vector<ChunkServerPtr> servers = mChunkServers;
	vector<ChunkServerPtr> loadedServers, nonloadedServers;
	int extraReplicas, numBlocksMoved = 0, numSkipped = 0;

	for (uint32_t i = 0; i < servers.size(); i++) {
		if (servers[i]->IsRetiring())
			continue;
		if (servers[i]->GetSpaceUtilization() < mMinRebalanceSpaceUtilThreshold)
			nonloadedServers.push_back(servers[i]);
		else if (servers[i]->GetSpaceUtilization() > mMaxRebalanceSpaceUtilThreshold) 
			loadedServers.push_back(servers[i]);
	}

	if ((nonloadedServers.size() == 0) || (loadedServers.size() == 0))
		return 0;

	bool allbusy = false;
	// try to start where we left off last time; if that chunk has
	// disappeared, find something "closeby"
	CSMapIter iter = mChunkToServerMap.find(mLastChunkRebalanced);
	if (iter == mChunkToServerMap.end())
		iter = mChunkToServerMap.upper_bound(mLastChunkRebalanced);

	for (; iter != mChunkToServerMap.end(); iter++) {

		allbusy = true;
		for (uint32_t i = 0; i < nonloadedServers.size(); i++) {
			if (nonloadedServers[i]->GetNumChunkReplications() <
				MAX_CONCURRENT_WRITE_REPLICATIONS_PER_NODE) {
				allbusy = false;
				break;
			}
		}

		if (allbusy || (numBlocksMoved > 200) || (numSkipped > 500)) {
			allbusy = true;
			break;
		}

		chunkId_t chunkId = iter->first;
		ChunkPlacementInfo &clli = iter->second;
		vector<ChunkServerPtr> candidates;

		// chunk could be moved around if it is hosted on a loaded server
		vector<ChunkServerPtr>::const_iterator csp;
		csp = find_if(clli.chunkServers.begin(), clli.chunkServers.end(),
				LoadedServerPred(mMaxRebalanceSpaceUtilThreshold));
		if (csp == clli.chunkServers.end())
			continue;

		// we have seen this chunkId; next time, we'll start time from
		// around here
		mLastChunkRebalanced = chunkId;

		// If this chunk is already being replicated or it is busy, skip
		if ((clli.ongoingReplications > 0) ||
			(!CanReplicateChunkNow(chunkId, clli, extraReplicas)))
				continue;

		// if we got too many copies of this chunk, don't bother
		if (extraReplicas < 0)
			continue;


		// there are two ways nodes can be added:
		//  1. new nodes to an existing rack: in this case, we want to
		//  migrate chunk within a rack
		//  2. new rack of nodes gets added: in this case, we want to
		//  migrate chunks to the new rack as long as we don't get more
		//  than one copy onto the same rack

		FindIntraRackRebalanceCandidates(candidates, nonloadedServers, clli);
		if (candidates.size() == 0) {
			ChunkServerPtr cand;

			FindInterRackRebalanceCandidate(cand, nonloadedServers, clli);
			if (cand)
				candidates.push_back(cand);
		}

		if (candidates.size() == 0)
			// no candidates :-(
			continue;
		// get the chunk version
		vector<MetaChunkInfo *> v;
		vector<MetaChunkInfo *>::iterator chunk;

		metatree.getalloc(clli.fid, v);
		chunk = find_if(v.begin(), v.end(), ChunkIdMatcher(chunkId));
		if (chunk == v.end())
			continue;

		uint32_t numCopies = min(candidates.size(), clli.chunkServers.size());
		uint32_t numOngoing = 0;

		numOngoing = ReplicateChunkToServers(chunkId, clli, numCopies,
					candidates);
		if (numOngoing > 0) {
                        numBlocksMoved++;
		} else {
			numSkipped++;
		}
	}
	if (!allbusy)
		// reset
		mLastChunkRebalanced = 1;

        return numBlocksMoved;
}

int
LayoutManager::ReplicateChunkToServers(chunkId_t chunkId, ChunkPlacementInfo &clli,
					uint32_t numCopies,
					vector<ChunkServerPtr> &candidates)
{
	for (uint32_t i = 0; i < candidates.size(); i++) {
		assert(!IsChunkHostedOnServer(clli.chunkServers, candidates[i]));
	}

	int numOngoing = ReplicateChunk(chunkId, clli, numCopies, candidates);
	if (numOngoing > 0) {
		// add this chunk to the target set of chunkIds that we are tracking
		// for replication status change
		ChangeChunkReplication(chunkId);

		clli.ongoingReplications += numOngoing;
		mNumOngoingReplications++;
	}
	return numOngoing;
}

class RebalancePlanExecutor {
	LayoutManager *mgr;
public:
	RebalancePlanExecutor(LayoutManager *l) : mgr(l) { }
	void operator()(ChunkServerPtr &c) {
		if ((c->IsRetiring()) || (!c->IsResponsiveServer()))
			return;
		mgr->ExecuteRebalancePlan(c);
	}
};

int
LayoutManager::LoadRebalancePlan(const string &planFn)
{
	// load the plan from the specified file
	int fd = open(planFn.c_str(), O_RDONLY);

	if (fd < 0) {
		KFS_LOG_VA_INFO("Unable to open: %s", planFn.c_str());
		return -1;
	}

	RebalancePlanInfo_t rpi;
	int rval;

	while (1) {
		rval = read(fd, &rpi, sizeof(RebalancePlanInfo_t));
		if (rval != sizeof(RebalancePlanInfo_t))
			break;
		ServerLocation loc;
		istringstream ist(rpi.dst);
		vector <ChunkServerPtr>::iterator j;

		ist >> loc.hostname;
		ist >> loc.port;
		j = find_if(mChunkServers.begin(), mChunkServers.end(),
			MatchingServer(loc));
		if (j == mChunkServers.end())
			continue;
		ChunkServerPtr c = *j;
		c->AddToChunksToMove(rpi.chunkId);
	}
	close(fd);

	mIsExecutingRebalancePlan = true;
	KFS_LOG_VA_INFO("Setup for rebalance plan execution from %s is done", planFn.c_str());

	return 0;
}

void
LayoutManager::ExecuteRebalancePlan()
{
	if (!mIsExecutingRebalancePlan)
		return;

	for_each(mChunkServers.begin(), mChunkServers.end(),
		RebalancePlanExecutor(this));

	bool alldone = true;

	for (vector<ChunkServerPtr>::iterator iter = mChunkServers.begin();
		iter != mChunkServers.end(); iter++) {
		ChunkServerPtr c = *iter;
		set<chunkId_t> chunksToMove = c->GetChunksToMove();

		if (!chunksToMove.empty()) {
			alldone = false;
			break;
		}
	}

	if (alldone) {
		KFS_LOG_INFO("Execution of rebalance plan is complete...");
		mIsExecutingRebalancePlan = false;
	}
}

void
LayoutManager::ExecuteRebalancePlan(ChunkServerPtr &c)
{
	set<chunkId_t> chunksToMove = c->GetChunksToMove();
	vector<ChunkServerPtr> candidates;

	if (!mIsExecutingRebalancePlan)
		return;

	if (c->GetSpaceUtilization() > MAX_SERVER_SPACE_UTIL_THRESHOLD) {
		KFS_LOG_VA_INFO("Terminating rebalance plan execution for overloaded server %s", c->ServerID().c_str());
		c->ClearChunksToMove();
		return;
	}

	candidates.push_back(c);

	for (set<chunkId_t>::iterator citer = chunksToMove.begin();
		citer != chunksToMove.end(); citer++) {
		if (c->GetNumChunkReplications() >
			MAX_CONCURRENT_WRITE_REPLICATIONS_PER_NODE)
			return;

		chunkId_t cid = *citer;

		CSMapIter iter = mChunkToServerMap.find(cid);

		if (iter == mChunkToServerMap.end()) {
			// chunk got deleted from the time the plan was created
			// to now.
			c->MovingChunkDone(cid);
			continue;
		}

		int extraReplicas;

		if ((iter->second.ongoingReplications > 0) ||
			(!CanReplicateChunkNow(cid, iter->second,
				extraReplicas)))
			continue;
		if (IsChunkHostedOnServer(iter->second.chunkServers, c)) {
			// Paranoia...
			c->MovingChunkDone(cid);
			continue;
		}

		ReplicateChunkToServers(cid, iter->second, 1, candidates);
	}
}

class OpenFileChecker {
	set<fid_t> &readFd, &writeFd;
public:
	OpenFileChecker(set<fid_t> &r, set<fid_t> &w) :
		readFd(r), writeFd(w) { }
	void operator() (const map<chunkId_t, ChunkPlacementInfo >::value_type p) {
		ChunkPlacementInfo c = p.second;
		vector<LeaseInfo>::iterator l;

		l = find_if(c.chunkLeases.begin(), c.chunkLeases.end(),
				ptr_fun(LeaseInfo::IsValidWriteLease));
		if (l != c.chunkLeases.end()) {
			writeFd.insert(c.fid);
			return;
		}
		l = find_if(c.chunkLeases.begin(), c.chunkLeases.end(),
				ptr_fun(LeaseInfo::IsValidLease));
		if (l != c.chunkLeases.end()) {
			readFd.insert(c.fid);
			return;
		}
	}
};

void
LayoutManager::GetOpenFiles(string &openForRead, string &openForWrite)
{
	set<fid_t> readFd, writeFd;
	for_each(mChunkToServerMap.begin(), mChunkToServerMap.end(),
		OpenFileChecker(readFd, writeFd));
		// XXX: fill me in..map from fd->path name

}

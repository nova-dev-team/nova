//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: logcompactor_main.cc $
//
// Created 2008/06/18
//
// Author: Sriram Rao
//
// Copyright 2008 Quantcast Corp.
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
// \brief The metaserver writes out operational log records to a log
// file.  Every N minutes, the log file is rolled over (and a new one
// is used to write out data).  For fast recovery, it'd be desirable
// to compact the log files and produce a checkpoint file.  This tool
// provides such a capability: it takes a checkpoint file, applies the
// set of operations as defined in a sequence of one or more log files
// and produces a new checkpoint file.  When the metaserver rolls over the log
// files, it creates a symlink to point the "LAST" closed log file; when log
// compaction is done, we only compact upto the last closed log file.
// 
//----------------------------------------------------------------------------

#include "kfstree.h"
#include "logger.h"
#include "checkpoint.h"
#include "restore.h"
#include "replay.h"
#include "util.h"
#include "common/log.h"

#include <sys/stat.h>
#include <iostream>
#include <cassert>

using std::cout;
using std::endl;
using namespace KFS;

static int restoreCheckpoint();
static int replayLogs();

int main(int argc, char **argv)
{
    // use options: -l for logdir -c for checkpoint dir
    char optchar;
    bool help = false, computeDirSize = false;
    int16_t numReplicasPerFile = -1;
    string logdir, cpdir;
    int status;

    KFS::MsgLogger::Init(NULL);
    KFS::MsgLogger::SetLevel(log4cpp::Priority::INFO);

    while ((optchar = getopt(argc, argv, "hpl:c:r:")) != -1) {
        switch (optchar) {
            case 'l': 
                logdir = optarg;
                break;
            case 'c':
                cpdir = optarg;
                break;
            case 'h':
                help = true;
                break;
            case 'p':
                computeDirSize = true;
                break;
            case 'r':
                numReplicasPerFile = (int16_t) atoi(optarg);
                break;
            default:
                KFS_LOG_VA_ERROR("Unrecognized flag %c", optchar);
                help = true;
                break;
        }
    }

    if (help) {
        cout << "Usage: " << argv[0] << " [-l <logdir>] [-c <cpdir>] {-p} {-r <# of replicas>}"
             << endl;
        cout << "where -p means recompute size of each directory in the tree" << endl;
	cout << "where -r means change the replication for all files in the system to the specified value" << endl;
        exit(-1);
    }

    if (computeDirSize) {
        // since we are going to recompute the size of each dir.,
        // there is no need to update the size of a dir. when logs are replayed.
        //
        metatree.disableFidToPathname();
    }

    logger_setup_paths(logdir);
    checkpointer_setup_paths(cpdir);
    status = restoreCheckpoint();
    if (status != 0)
        panic("restore checkpoint failed!", false);
    status = replayLogs();
    if (status == 0) {
        if (computeDirSize) {
            metatree.recomputeDirSize();
        }
	if (numReplicasPerFile > 0) {
		metatree.changePathReplication(ROOTFID, numReplicasPerFile);
	}
        cp.do_CP();
    }
    exit(0);
}

static int restoreCheckpoint()
{
    int status = 0;

    if (file_exists(LASTCP)) {
        Restorer r;
        status = r.rebuild(LASTCP) ? 0 : -EIO;
    } else {
        status = metatree.new_tree();
    }
    return status;
}

static int replayLogs()
{
    int status, lastlog = -1, lognum;
    ino_t lastino;
    struct stat buf;

    // we want to replay log files that are "complete"---those that
    // won't ever be written to again.  so, starting with the log
    // associated with the CP, replay all the log files upto the
    // "last" log file.

    // get the inode # for the last file
    status = stat(LASTLOG.c_str(), &buf);
    if (status < 0)
        // no "last" log file; so nothing to do
        return status;

    // get the inode # for the log file that corresponds to last and
    // then replay those
    lastino = buf.st_ino;

    for (lognum = replayer.logno(); ;lognum++) {
        string logfn = oplog.logfile(lognum);

        status = stat(logfn.c_str(), &buf);
        if (status < 0)
            break;

        if (buf.st_ino == lastino) {
            lastlog = lognum;
            assert(buf.st_nlink == 2);
            break;
        }
    }

    
    if (lastlog == replayer.logno()) {
        cout << "No new logs since the last log; so, skipping checkpoint" << endl;
        return -2;
    }

    if (lastlog < 0)
        return -1;

    cout << "Replaying logs from log." << replayer.logno() << " ... log." << lastlog << endl;

    for (lognum = replayer.logno(); lognum <= lastlog;lognum++) {
        string logfn = oplog.logfile(lognum);

        replayer.openlog(logfn);

        status = replayer.playlog();
        if (status != 0)
            panic("log replay failed", false);
    }

    oplog.setLog(lognum);

    cout << "Replay of logs finished" << endl;

    return status;
}

//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: ChunkServer_main.cc 293 2009-03-19 22:21:13Z sriramsrao $ 
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

extern "C" {
#include <signal.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <openssl/hmac.h>
#include <openssl/md5.h>
#include <sys/time.h>
#include <sys/resource.h>
}

#include <string>
#include <vector>

#include "common/properties.h"
#include "libkfsIO/DiskManager.h"
#include "libkfsIO/NetManager.h"
#include "libkfsIO/Globals.h"

#include "ChunkServer.h"
#include "ChunkManager.h"

using namespace KFS;
using namespace KFS::libkfsio;
using std::string;
using std::vector;
using std::cout;
using std::endl;

string gLogDir;
vector<string> gChunkDirs;
string gMD5Sum;

ServerLocation gMetaServerLoc;
int64_t gTotalSpace;			// max. storage space to use
int gChunkServerClientPort;	// Port at which kfs clients connect to us
string gChunkServerHostname;	// Our hostname to use (instead of using gethostname() )

Properties gProp;
const char *gClusterKey;
int gChunkServerRackId;
int gChunkServerCleanupOnStart;

int ReadChunkServerProperties(char *fileName);
static void computeMD5(const char *pathname);

int
main(int argc, char **argv)
{
    if (argc < 2) {
        cout << "Usage: " << argv[0] << " <properties file> {<msg log file>}" << endl;
        exit(0);
    }

    if (argc > 2) {
        KFS::MsgLogger::Init(argv[2]);
    } else {
        KFS::MsgLogger::Init(NULL);
    }

    // set the coredump size to unlimited
    struct rlimit rlim;
    int err;
    rlim.rlim_cur = RLIM_INFINITY;
    rlim.rlim_max = RLIM_INFINITY;
    err = setrlimit(RLIMIT_CORE, &rlim);
    if (err) {
        KFS_LOG_VA_INFO("Unable to increase coredump file size: %d", err);
    }

    if (ReadChunkServerProperties(argv[1]) != 0) {
        cout << "Bad properties file: " << argv[1] << " aborting...\n";
        exit(-1);
    }

    // Initialize things...
    libkfsio::InitGlobals();

    KFS_LOG_INFO("Starting chunkserver...");
    
    // for writes, the client is sending WRITE_PREPARE with 64K bytes;
    // to enable the data to fit into a single buffer (and thereby get
    // the data to the underlying FS via a single aio_write()), allocate a bit
    // of extra space. 64K + 4k
    libkfsio::SetIOBufferSize(69632);

    globals().diskManager.InitForAIO();

    // would like to limit to 200MB outstanding
    // globals().netManager.SetBacklogLimit(200 * 1024 * 1024);

    // compute the MD5 of the binary
    computeMD5(argv[0]);

    KFS_LOG_VA_INFO("md5sum to send to metaserver: %s", gMD5Sum.c_str());

    gChunkServer.Init();
    gChunkManager.Init(gChunkDirs, gTotalSpace);
    gLogger.Init(gLogDir);
    gMetaServerSM.SetMetaInfo(gMetaServerLoc, gClusterKey, gChunkServerRackId, gMD5Sum);

    signal(SIGPIPE, SIG_IGN);

    // gChunkServerCleanupOnStart is a debugging option---it provides
    // "silent" cleanup
    if (gChunkServerCleanupOnStart == 0) {
        gChunkManager.Restart();
    }

    gChunkServer.MainLoop(gChunkServerClientPort, gChunkServerHostname);

    return 0;
}

static void
computeMD5(const char *pathname)
{
    MD5_CTX ctx;
    struct stat s;
    int fd;
    unsigned char md5sum[MD5_DIGEST_LENGTH];

    if (stat(pathname, &s) != 0)
	return;
    
    fd = open(pathname, O_RDONLY);
    MD5_Init(&ctx);
    unsigned char *buf = (unsigned char *) mmap(0, s.st_size, PROT_EXEC, MAP_SHARED, fd, 0);
    if (buf != NULL) {
        MD5(buf, s.st_size, md5sum);
        munmap(buf, s.st_size);
        char md5digest[2 * MD5_DIGEST_LENGTH + 1];
        md5digest[2 * MD5_DIGEST_LENGTH] = '\0';
        for (uint32_t i = 0; i < MD5_DIGEST_LENGTH; i++)
            sprintf(md5digest + i * 2, "%02x", md5sum[i]);
        gMD5Sum = md5digest;
        KFS_LOG_VA_DEBUG("md5sum calculated from binary: %s", gMD5Sum.c_str());
    }
    close(fd);
}

static bool
make_if_needed(const char *dirname, bool check)
{
    struct stat s;
    int res = stat(dirname, &s);

    if (check && (res < 0)) {
        // stat failed; maybe the drive is down.  Keep going
        cout << "Stat on dir: " << dirname  << " failed.  Moving on..." << endl;
        return true;
    }

    if ((res == 0) && S_ISDIR(s.st_mode))
	return true;

    return mkdir(dirname, 0755) == 0;
}

///
/// Read and validate the configuration settings for the chunk
/// server. The configuration file is assumed to contain lines of the
/// form: xxx.yyy.zzz = <value>
/// @result 0 on success; -1 on failure
/// @param[in] fileName File that contains configuration information
/// for the chunk server.
///
int
ReadChunkServerProperties(char *fileName)
{
    string::size_type curr = 0, next;
    string chunkDirPaths;
    string logLevel;
#ifdef NDEBUG
    const char *defLogLevel = "INFO";
#else
    const char *defLogLevel = "DEBUG";
#endif

    if (gProp.loadProperties(fileName, '=', true) != 0)
        return -1;

    gMetaServerLoc.hostname = gProp.getValue("chunkServer.metaServer.hostname", "");
    gMetaServerLoc.port = gProp.getValue("chunkServer.metaServer.port", -1);
    if (!gMetaServerLoc.IsValid()) {
        cout << "Aborting...bad meta-server host or port: ";
        cout << gMetaServerLoc.hostname << ':' << gMetaServerLoc.port << '\n';
        return -1;
    }

    gChunkServerClientPort = gProp.getValue("chunkServer.clientPort", -1);
    if (gChunkServerClientPort < 0) {
        cout << "Aborting...bad client port: " << gChunkServerClientPort << '\n';
        return -1;
    }
    cout << "Using chunk server client port: " << gChunkServerClientPort << '\n';

    gChunkServerHostname = gProp.getValue("chunkServer.hostname", "");
    if (gChunkServerHostname.size() > 0)
    {
      cout << "Using chunk server hostname: " << gChunkServerHostname << '\n';
    }
    
    // Paths are space separated directories for storing chunks
    chunkDirPaths = gProp.getValue("chunkServer.chunkDir", "chunks");

    while (curr < chunkDirPaths.size()) {
        string component;

        next = chunkDirPaths.find(' ', curr);
        if (next == string::npos)
            next = chunkDirPaths.size();

        component.assign(chunkDirPaths, curr, next - curr);

        curr = next + 1;

        if ((component == " ") || (component == "")) {
            continue;
        }

        if (!make_if_needed(component.c_str(), true)) {
            cout << "Aborting...failed to create " << component << '\n';
            return -1;
        }

        // also, make the directory for holding stale chunks in each "partition"
        string staleChunkDir = GetStaleChunkPath(component);
        // if the parent dir exists, make the stale chunks directory
        make_if_needed(staleChunkDir.c_str(), false);

        cout << "Using chunk dir = " << component << '\n';

        gChunkDirs.push_back(component);
    }

    gLogDir = gProp.getValue("chunkServer.logDir", "logs");
    if (!make_if_needed(gLogDir.c_str(), false)) {
	cout << "Aborting...failed to create " << gLogDir << '\n';
	return -1;
    }
    cout << "Using log dir = " << gLogDir << '\n';

    gTotalSpace = gProp.getValue("chunkServer.totalSpace", (long long) 0);
    cout << "Total space = " << gTotalSpace << '\n';

    gChunkServerCleanupOnStart = gProp.getValue("chunkServer.cleanupOnStart", 0);
    cout << "cleanup on start = " << gChunkServerCleanupOnStart << endl;

    gChunkServerRackId = gProp.getValue("chunkServer.rackId", (int) -1);
    cout << "Chunk server rack: " << gChunkServerRackId << endl;

    gClusterKey = gProp.getValue("chunkServer.clusterKey", "");
    cout << "using cluster key = " << gClusterKey << endl;
    
    if (gMD5Sum == "") {
        gMD5Sum = gProp.getValue("chunkServer.md5sum", "");
    }

    logLevel = gProp.getValue("chunkServer.loglevel", defLogLevel);
    if (logLevel == "INFO")
        KFS::MsgLogger::SetLevel(log4cpp::Priority::INFO);
    else
        KFS::MsgLogger::SetLevel(log4cpp::Priority::DEBUG);

    return 0;
}

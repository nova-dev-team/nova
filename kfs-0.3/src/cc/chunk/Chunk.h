//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: Chunk.h 259 2009-01-27 22:32:36Z sriramsrao $ 
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

#ifndef _CHUNKSERVER_CHUNK_H
#define _CHUNKSERVER_CHUNK_H

#include <stdint.h>
#include <unistd.h>
#include <string.h>
#include <boost/shared_ptr.hpp>

#include <vector>

#include "common/log.h"
#include "common/kfstypes.h"
#include "libkfsIO/FileHandle.h"
#include "libkfsIO/Checksum.h"
#include "Utils.h"

namespace KFS
{

///
/// \file Chunk.h
/// \brief Declarations related to a Chunk in KFS.
///


/// 
/// \brief ChunkInfo_t
/// For each chunk, the chunkserver maintains a meta-data file.  This
/// file defines the chunk attributes such as, the file it is
/// associated with, the chunk version #, and the checksums for each
/// block of the file.  For each chunk, this structure is read in at
/// startup time.
///

/// The max # of checksum blocks we have for a given chunk
const uint32_t MAX_CHUNK_CHECKSUM_BLOCKS = CHUNKSIZE /  CHECKSUM_BLOCKSIZE;

/// In the chunk header, we store upto 256 char of the file that
/// originally created the chunk.  
const size_t MAX_FILENAME_LEN = 256;

const int CHUNK_META_MAGIC = 0xCAFECAFE;
const int CHUNK_META_VERSION = 0x1;

// This structure is on-disk
struct DiskChunkInfo_t {
    DiskChunkInfo_t() : metaMagic (CHUNK_META_MAGIC), metaVersion(CHUNK_META_VERSION) { }
    DiskChunkInfo_t(kfsFileId_t f, kfsChunkId_t c, off_t s, kfsSeq_t v) :
        metaMagic (CHUNK_META_MAGIC), metaVersion(CHUNK_META_VERSION),
        fileId(f), chunkId(c), chunkVersion(v), chunkSize(s), numReads(0) { 
        memset(filename, 0, MAX_FILENAME_LEN);
    }
    void SetChecksums(const uint32_t *checksums) {
        memcpy(chunkBlockChecksum, checksums, MAX_CHUNK_CHECKSUM_BLOCKS * sizeof(uint32_t));
    }

    int Validate(kfsChunkId_t cid) const {
        if (metaMagic != CHUNK_META_MAGIC) {
            KFS_LOG_VA_INFO("Magic # mismatch (got: %x, expect: %x)", 
                            metaMagic, CHUNK_META_MAGIC);
            return -KFS::EBADCKSUM;
        }
        if (metaVersion != CHUNK_META_VERSION) {
            KFS_LOG_VA_INFO("Version # mismatch (got: %x, expect: %x)", 
                            metaVersion, CHUNK_META_VERSION);
            return -KFS::EBADCKSUM;
        }

        if (chunkId != cid) {
            KFS_LOG_VA_INFO("Chunkid mismatch (got: %ld, expect: %ld)", 
                            chunkId, cid);
            return -KFS::EBADCKSUM;
        }
        return 0;
    }

    int metaMagic;
    int metaVersion;

    kfsFileId_t fileId;
    kfsChunkId_t chunkId;
    kfsSeq_t chunkVersion;
    off_t  chunkSize; 
    uint32_t chunkBlockChecksum[MAX_CHUNK_CHECKSUM_BLOCKS];
    // some statistics about the chunk: 
    // -- version # has an estimate of the # of writes
    // -- track the # of reads
    // ...
    uint32_t numReads;
    char filename[MAX_FILENAME_LEN];
};

// This structure is in-core
struct ChunkInfo_t {

    ChunkInfo_t() : fileId(0), chunkId(0), chunkVersion(0), chunkSize(0), 
                    chunkBlockChecksum(NULL)
    {
        // memset(chunkBlockChecksum, 0, sizeof(chunkBlockChecksum));
    }
    ~ChunkInfo_t() {
        delete [] chunkBlockChecksum;
    }
    ChunkInfo_t(const ChunkInfo_t &other) :
        fileId(other.fileId), chunkId(other.chunkId), chunkVersion(other.chunkVersion),
        chunkSize(other.chunkSize), chunkBlockChecksum(NULL), dirname(other.dirname) {
    }
    ChunkInfo_t& operator= (const ChunkInfo_t &other) 
    {
        fileId = other.fileId;
        chunkId = other.chunkId;
        chunkVersion = other.chunkVersion;
        chunkSize = other.chunkSize;
        dirname = other.dirname;
        SetChecksums(other.chunkBlockChecksum);

        return *this;
    }

    void Init(kfsFileId_t f, kfsChunkId_t c, off_t v) {
        fileId = f;
        chunkId = c;
        chunkVersion = v;
        chunkBlockChecksum = new uint32_t[MAX_CHUNK_CHECKSUM_BLOCKS];
        memset(chunkBlockChecksum, 0, MAX_CHUNK_CHECKSUM_BLOCKS * sizeof(uint32_t));
    }

    void SetDirname(const std::string &path) {
        dirname = path;
    }
    
    std::string GetDirname() {
        return dirname;
    }

    bool AreChecksumsLoaded() {
        return chunkBlockChecksum != NULL;
    }

    void UnloadChecksums() {
        delete [] chunkBlockChecksum;
        chunkBlockChecksum = NULL;
        KFS_LOG_VA_DEBUG("Unloading in the chunk checksum for chunk %d", chunkId);
    }

    void SetChecksums(const uint32_t *checksums) {
        delete [] chunkBlockChecksum;
        if (checksums == NULL) {
            chunkBlockChecksum = NULL;
            return;
        }

        chunkBlockChecksum = new uint32_t[MAX_CHUNK_CHECKSUM_BLOCKS];
        memcpy(chunkBlockChecksum, checksums, MAX_CHUNK_CHECKSUM_BLOCKS * sizeof(uint32_t));
    }

    void LoadChecksums(int fd) {
        Deserialize(fd, true);
    }

    void VerifyChecksumsLoaded() {
        assert(chunkBlockChecksum != NULL);
        if (chunkBlockChecksum == NULL)
            die("Checksums are not loaded!");
    }

    // save the chunk meta-data to the buffer; 
    void Serialize(IOBuffer *dataBuf) {
        DiskChunkInfo_t dci(fileId, chunkId, chunkSize, chunkVersion);

        assert(chunkBlockChecksum != NULL);
        dci.SetChecksums(chunkBlockChecksum);

        dataBuf->CopyIn((char *) &dci, sizeof(DiskChunkInfo_t));
    }

    int Deserialize(int fd, bool validate) {
        DiskChunkInfo_t dci;
        int res;
        
        res = pread(fd, &dci, sizeof(DiskChunkInfo_t), 0);
        if (res != sizeof(DiskChunkInfo_t))
            return -EINVAL;
        return Deserialize(dci, validate);
    }

    int Deserialize(const DiskChunkInfo_t &dci, bool validate) {
        if (validate) {
            if (dci.metaMagic != CHUNK_META_MAGIC) {
                KFS_LOG_VA_INFO("Magic # mismatch (got: %x, expect: %x)", 
                                dci.metaMagic, CHUNK_META_MAGIC);
                return -EINVAL;
            }
            if (dci.metaVersion != CHUNK_META_VERSION) {
                KFS_LOG_VA_INFO("Version # mismatch (got: %x, expect: %x)", 
                                dci.metaVersion, CHUNK_META_VERSION);
                return -EINVAL;
            }
        }
        fileId = dci.fileId;
        chunkId = dci.chunkId;
        chunkSize = dci.chunkSize;
        chunkVersion = dci.chunkVersion;

        delete [] chunkBlockChecksum;
        chunkBlockChecksum = new uint32_t[MAX_CHUNK_CHECKSUM_BLOCKS];
        memcpy(chunkBlockChecksum, dci.chunkBlockChecksum,
               MAX_CHUNK_CHECKSUM_BLOCKS * sizeof(uint32_t));
        KFS_LOG_VA_DEBUG("Loading in the chunk checksum for chunk %d", chunkId);

        return 0;
    }

    kfsFileId_t fileId;
    kfsChunkId_t chunkId;
    kfsSeq_t chunkVersion;
    off_t  chunkSize; 
    // uint32_t chunkBlockChecksum[MAX_CHUNK_CHECKSUM_BLOCKS];
    // this is unpinned; whenever we open the chunk, this has to be
    // paged in...damn..would've been nice if this was at the end
    uint32_t *chunkBlockChecksum;
    // some statistics about the chunk: 
    // -- version # has an estimate of the # of writes
    // -- track the # of reads
    // ...
    uint32_t numReads;
    // Record the directory on which the chunk is stored.  For JBOD
    // settings, we will have one directory per drive; stash the
    // directory so that we can find the chunk file on disk.
    std::string dirname;
};

}

#endif // _CHUNKSERVER_CHUNK_H

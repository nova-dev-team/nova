//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: Checksum.cc 263 2009-02-09 21:41:53Z sriramsrao $ 
//
// Created 2006/09/12
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
// An adaptation of the 32-bit Adler checksum algorithm
//
//----------------------------------------------------------------------------

#include "Checksum.h"

#include <algorithm>
#include <vector>
#include <zlib.h>

using std::min;
using std::vector;
using std::list;

using namespace KFS;

uint32_t
KFS::OffsetToChecksumBlockNum(off_t offset)
{
    return offset / CHECKSUM_BLOCKSIZE;
}

uint32_t
KFS::OffsetToChecksumBlockStart(off_t offset)
{
    return (offset / CHECKSUM_BLOCKSIZE) *
        CHECKSUM_BLOCKSIZE;
}

uint32_t
KFS::OffsetToChecksumBlockEnd(off_t offset)
{
    return ((offset / CHECKSUM_BLOCKSIZE) + 1) *
        CHECKSUM_BLOCKSIZE;
}

uint32_t
KFS::ComputeBlockChecksum(const char *buf, size_t len)
{
    uint32_t res = adler32(0L, Z_NULL, 0);
    
    res = adler32(res, (const Bytef *) buf, len);
    return res;
}

uint32_t
KFS::ComputeBlockChecksum(IOBuffer *data, size_t len)
{
    uint32_t res = adler32(0L, Z_NULL, 0);

    for (list<IOBufferDataPtr>::iterator iter = data->mBuf.begin();
         len > 0 && (iter != data->mBuf.end()); ++iter) {
        IOBufferDataPtr blk = *iter;
        size_t tlen = min((size_t) blk->BytesConsumable(), len);

        if (tlen == 0)
            continue;

        res = adler32(res, (const Bytef *) blk->Consumer(), tlen);
        len -= tlen;
    }
    return res;
}

vector<uint32_t>
KFS::ComputeChecksums(IOBuffer *data, size_t len)
{
    vector<uint32_t> cksums;
    list<IOBufferDataPtr>::iterator iter = data->mBuf.begin();

    if (len < CHECKSUM_BLOCKSIZE) {
        uint32_t cks = ComputeBlockChecksum(data, len);
        cksums.push_back(cks);
        return cksums;
    }

    if (iter == data->mBuf.end())
        return cksums;

    IOBufferDataPtr blk = *iter;
    char *buf = blk->Consumer();

    /// Compute checksum block by block
    while ((len > 0) && (iter != data->mBuf.end())) {
        size_t currLen = 0;
        uint32_t res = adler32(0L, Z_NULL, 0);

        while (currLen < CHECKSUM_BLOCKSIZE) {
            unsigned navail = min((size_t) (blk->Producer() - buf), len);
            if (currLen + navail > CHECKSUM_BLOCKSIZE)
                navail = CHECKSUM_BLOCKSIZE - currLen;

            if (navail == 0) {
                iter++;
                if (iter == data->mBuf.end())
                    break;
                blk = *iter;
                buf = blk->Consumer();
                continue;
            }

            currLen += navail;
            len -= navail;
            res = adler32(res, (const Bytef *) buf, navail);
            buf += navail;
        }
        cksums.push_back(res);
    }
    return cksums;
}

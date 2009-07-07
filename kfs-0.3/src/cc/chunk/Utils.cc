//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: Utils.cc 103 2008-07-27 19:20:41Z sriramsrao $ 
//
// Created 2006/09/27
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

#include "Utils.h"
#include "common/log.h"

using std::vector;
using std::string;
using namespace KFS;

///
/// Return true if there is a sequence of "\r\n\r\n".
/// @param[in] iobuf: Buffer with data sent by the client
/// @param[out] msgLen: string length of the command in the buffer
/// @retval true if a command is present; false otherwise.
///
bool KFS::IsMsgAvail(IOBuffer *iobuf, int *msgLen)
{
    char buf[1024];
    int nAvail, len = 0, i;

    nAvail = iobuf->BytesConsumable();
    if (nAvail > 1024)
        nAvail = 1024;
    len = iobuf->CopyOut(buf, nAvail);

    // Find the first occurence of "\r\n\r\n"
    for (i = 3; i < len; ++i) {
        if ((buf[i - 3] == '\r') &&
            (buf[i - 2] == '\n') &&
            (buf[i - 1] == '\r') &&
            (buf[i] == '\n')) {
            // The command we got is from 0..i.  The strlen of the
            // command is i+1.
            *msgLen = i + 1;
            return true;
        }
    }
    return false;
}

void KFS::die(const string &msg)
{
    KFS_LOG_VA_FATAL("Panic'ing: %s", msg.c_str());
    abort();
}

void KFS::split(std::vector<std::string> &component, const string &path, char separator)
{
    string::size_type curr = 0, nextsep = 0;
    string v;

    while (nextsep != string::npos) {
        nextsep = path.find(separator, curr);
        v = path.substr(curr, nextsep - curr);
        curr = nextsep + 1;
        component.push_back(v);
    }
}

float KFS::ComputeTimeDiff(const struct timeval &startTime, const struct timeval &endTime)
{
    float timeSpent;

    timeSpent = (endTime.tv_sec * 1e6 + endTime.tv_usec) - 
        (startTime.tv_sec * 1e6 + startTime.tv_usec);
    return timeSpent / 1e6;
}

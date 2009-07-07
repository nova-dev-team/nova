//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: kfsutils.h 326 2009-04-25 00:45:50Z sriramsrao $
//
// Created 2008/11/01
// Author: Mike Ovsiannikov
//
// Copyright 2008,2009 Quantcast Corp.
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

#ifndef KFSUTILS_H
#define KFSUTILS_H

#include <stdint.h>
#include <string>

#define KFSRTASSERT(a) \
    if (!(a)) KFSUtils::AssertionFailure(#a, __FILE__, __LINE__)

struct KFSUtils
{
    static void FatalError(
        const char* inMsgPtr,
        int         inSysError);
    static std::string SysError(
        int         inSysError,
        const char* inMsgPtr = 0);
    static void AssertionFailure(
        const char* inMsgPtr,
        const char* inFileNamePtr,
        int         inLineNum);
    static int64_t ReserveFileSpace(
        int     inFd,
        int64_t inSize);
    static int AllocateFileSpace(
        const char* inFileNamePtr,
        int64_t     inSize,
        int64_t     inMinSize            = -1,
        int64_t*    inInitialFileSizePtr = 0);
    static int AllocateFileSpace(
        int      inFd,
        int64_t  inSize,
        int64_t  inMinSize            = -1,
        int64_t* inInitialFileSizePtr = 0);
};

#endif /* KFSUTILS_H */

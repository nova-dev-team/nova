//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id:$
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
// Implementation of the net kicker object.
//----------------------------------------------------------------------------

#include "NetKicker.h"
#include "Globals.h"

using namespace KFS;
using namespace KFS::libkfsio;

NetKicker::NetKicker()
{
    int res; 

    res = pipe(mPipeFds);
    if (res < 0) {
        perror("Pipe: ");
        return;
    }
    fcntl(mPipeFds[0], F_SETFL, O_NONBLOCK);
    fcntl(mPipeFds[1], F_SETFL, O_NONBLOCK);
}

void
NetKicker::Kick()
{
    char buf = 'k';
        
    write(mPipeFds[1], &buf, sizeof(char));
}

int 
NetKicker::Drain()
{
    int bufsz = 512, res;
    char buf[512];

    res = read(mPipeFds[0], buf, bufsz);
    return res;
}

int
NetKicker::GetFd() const
{
    return mPipeFds[0];
}

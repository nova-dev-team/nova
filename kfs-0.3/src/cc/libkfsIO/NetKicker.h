//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id:$
//
// Created 2008/05/04
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
// \brief In a multi-threaded setup, the net-manager runs on a
// separate thread.  It has a poll loop with a delay of N secs; during
// this time, any messages generated interally maybe held waiting for
// the net-manager thread to wake up.  This has the effect of
// introducing N sec delay for sending outbound messages.  To force
// the net-manager to wake up, create a "net kicker" object and write
// some data to it.  That is, create a pipe and make the pipe fd part
// of the net-manager's poll loop; whenever we write data to the pipe,
// the net-manager will wake up and service out-bound messages.
//
// ----------------------------------------------------------------------------

#ifndef LIBKFSIO_NETKICKER_H
#define LIBKFSIO_NETKICKER_H

#include <unistd.h>
#include "KfsCallbackObj.h"
#include "NetManager.h"

namespace KFS
{
    class NetKicker : public KfsCallbackObj {
    public:
        NetKicker();
        /// The "write" portion of the pipe writes one byte on the fd.
        void Kick();
        /// This is the callback from the net-manager to drain out the
        /// bytes written on the pipe
        int Drain();
        int GetFd() const;
    private:
        int mPipeFds[2];
    };

}

#endif // LIBKFSIO_NETKICKER_H

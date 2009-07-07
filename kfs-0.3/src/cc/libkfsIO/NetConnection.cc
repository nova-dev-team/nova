//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: NetConnection.cc 326 2009-04-25 00:45:50Z sriramsrao $
//
// Created 2006/03/14
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

#include "Globals.h"
#include "NetConnection.h"
#include "kfsutils.h"

using namespace KFS;
using namespace KFS::libkfsio;

void NetConnection::HandleReadEvent()
{
    if (! IsGood()) {
        KFS_LOG_DEBUG("Read event ignored: socket closed");
    } else if (mListenOnly) {
        TcpSocket* const sock = mSock->Accept();
#ifdef DEBUG
        if (sock == NULL) {
            KFS_LOG_VA_DEBUG("# of open-fd's: disk=%d, net=%d",
                             globals().ctrOpenDiskFds.GetValue(),
                             globals().ctrOpenNetFds.GetValue());
        }
#endif
        if (sock) {
            NetConnectionPtr conn(new NetConnection(sock, NULL));
            conn->mTryWrite = true; // Should be connected, and good to write.
            mCallbackObj->HandleEvent(EVENT_NEW_CONNECTION, (void *) &conn);
            conn->Update();
        }
    } else if (IsReadReady()) {
        const int nread = mInBuffer.Read(mSock->GetFd());
        if (nread == 0) {
            KFS_LOG_DEBUG("Read 0 bytes...connection dropped");
            mCallbackObj->HandleEvent(EVENT_NET_ERROR, NULL);
        } else if (nread > 0) {
            mCallbackObj->HandleEvent(EVENT_NET_READ, (void *)&mInBuffer);
        }
    }
    Update();
}

void NetConnection::HandleWriteEvent()
{
    if (IsWriteReady() && IsGood()) {
        const int nwrote = mOutBuffer.Write(mSock->GetFd());
        if (nwrote <= 0 && nwrote != -EAGAIN && nwrote != -EINTR) {
            std::string const msg = KFSUtils::SysError(-nwrote);
            KFS_LOG_VA_DEBUG("Wrote 0 bytes...connection dropped, error: %d",
                -nwrote, msg.c_str());
            mCallbackObj->HandleEvent(EVENT_NET_ERROR, NULL);
        } else if (nwrote > 0) {
            mCallbackObj->HandleEvent(EVENT_NET_WROTE, (void *)&mOutBuffer);
        }
        mTryWrite = mOutBuffer.IsEmpty();
    }
    mNetManagerEntry.SetConnectPending(false);
    Update();
}

void NetConnection::HandleErrorEvent()
{
    KFS_LOG_DEBUG("Got an error on socket.  Closing connection");
    Close();
    mCallbackObj->HandleEvent(EVENT_NET_ERROR, NULL);
    Update();
}

void NetConnection::HandleTimeoutEvent()
{
    const int timeOut = GetInactivityTimeout();
    if (timeOut < 0) {
        KFS_LOG_VA_DEBUG("Ignoring timeout event, time out value: %d", timeOut);
    } else {
        KFS_LOG_DEBUG("No activity on socket...returning error");
        mCallbackObj->HandleEvent(EVENT_INACTIVITY_TIMEOUT, NULL);
    }
    Update();
}

void NetConnection::Update(bool resetTimer)
{
    KFS::libkfsio::globals().netManager.Update(mNetManagerEntry,
        IsGood() ? mSock->GetFd() : -1, resetTimer);
}

NetConnection::NetManagerEntry* NetConnection::GetNetMangerEntry(NetManager& manager)
{
    assert(&manager == &KFS::libkfsio::globals().netManager);
    return &mNetManagerEntry;
}

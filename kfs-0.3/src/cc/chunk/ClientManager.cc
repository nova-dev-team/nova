//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: ClientManager.cc 206 2008-10-30 21:48:58Z sriramsrao $ 
//
// Created 2006/03/28
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

#include "ClientManager.h"

using std::list;
using namespace KFS;

ClientManager KFS::gClientManager;

void 
ClientManager::StartAcceptor(int port)
{
    mAcceptor = new Acceptor(port, this);
    if (!mAcceptor->IsAcceptorStarted()) {
        die("Unable to start acceptor!");
    }
}

void
ClientManager::Remove(ClientSM *clnt)
{
    list<ClientSM *>::iterator iter = find(mClients.begin(), mClients.end(), clnt);

    assert(iter != mClients.end());
    if (iter == mClients.end())
        return;
    mClients.erase(iter);
}

//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: ClientManager.h 71 2008-07-07 15:49:14Z sriramsrao $ 
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

#ifndef _CLIENTMANAGER_H
#define _CLIENTMANAGER_H

#include "libkfsIO/Acceptor.h"
#include "ClientSM.h"

namespace KFS
{

class ClientManager : public IAcceptorOwner {
public:
    ClientManager() {
        mAcceptor = NULL;
    };
    virtual ~ClientManager() {
        delete mAcceptor;
    };
    void StartAcceptor(int port);
    KfsCallbackObj *CreateKfsCallbackObj(NetConnectionPtr &conn) {
        ClientSM *clnt = new ClientSM(conn);
        mClients.push_back(clnt);
        return clnt;
    }
    void Remove(ClientSM *clnt);
private:
    std::list<ClientSM *> mClients;
    Acceptor	*mAcceptor;
};

extern ClientManager gClientManager;

}

#endif // _CLIENTMANAGER_H

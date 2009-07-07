//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: concurrency.h 71 2008-07-07 15:49:14Z sriramsrao $ 
//
// \brief Provide class definitions relating to concurrency.
// 
// Created 2007/08/21
// Author: Wang Lam (Kosmix Corp.) 
//
// Copyright 2007 Kosmix Corp.
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

#ifndef LIBKFSCLIENT_CONCURRENCY_H
#define LIBKFSCLIENT_CONCURRENCY_H

#include <cerrno>
#include <cassert>
extern "C" {
#include <pthread.h>
}

namespace KFS
{
    class MutexLock;
}

//
// The MutexLock class is a convenience class that facilitates
// locking a mutex on function entry and automatically unlocking
// it on function exit.
//
class KFS::MutexLock {
public:
    /**
     * Acquire the mutex
     */
    MutexLock( pthread_mutex_t *mutex ) : mMutex(mutex) 
    { pthread_mutex_lock(mMutex); }

    /**
     * Release the mutex automatically
     */
    ~MutexLock()
    { int rval = pthread_mutex_unlock(mMutex); assert(!rval); (void)rval; }

private:
    pthread_mutex_t *mMutex;
};

#endif // LIBKFSCLIENT_CONCURRENCY_H

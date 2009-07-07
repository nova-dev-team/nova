//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: //depot/main/platform/kosmosfs/src/cc/qcdio/qcstutils.h#1 $
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

#ifndef QCSTUTILS_H
#define QCSTUTILS_H

#include "qcmutex.h"

class QCStMutexLocker
{
public:
    QCStMutexLocker(
        QCMutex& inMutex)
        : mMutexPtr(&inMutex)
        { Lock(); }
    QCStMutexLocker(
        QCMutex* inMutexPtr = 0)
        : mMutexPtr(inMutexPtr)
        { Lock(); }
    ~QCStMutexLocker()
        { Unlock(); }
    void Lock()
    {
        if (mMutexPtr) {
            mMutexPtr->Lock();
        }
    }
    void Unlock()
    {
        if (mMutexPtr) {
            mMutexPtr->Unlock();
            mMutexPtr = 0;
        }
    }
    void Attach(
        QCMutex* inMutexPtr)
    {
        Unlock();
        mMutexPtr = inMutexPtr;
        Lock();
    }
    void Detach()
        { mMutexPtr = 0; }
private:
    QCMutex* mMutexPtr;

private:
    QCStMutexLocker(
        const QCStMutexLocker& inLocker);
    QCStMutexLocker& operator=(
        const QCStMutexLocker& inLocker);
};

class QCStMutexUnlocker
{
public:
    QCStMutexUnlocker(
        QCMutex& inMutex)
        : mMutexPtr(&inMutex)
        { Unlock(); }
    QCStMutexUnlocker(
        QCMutex* inMutexPtr = 0)
        : mMutexPtr(inMutexPtr)
        { Unlock(); }
    ~QCStMutexUnlocker()
        { Lock(); }
    void Lock()
    {
        if (mMutexPtr) {
            mMutexPtr->Lock();
            mMutexPtr = 0;
        }
    }
    void Unlock()
    {
        if (mMutexPtr) {
            mMutexPtr->Unlock();
        }
    }
    void Attach(
        QCMutex* inMutexPtr)
    {
        Lock();
        mMutexPtr = inMutexPtr;
        Unlock();
    }
    void Detach()
        { mMutexPtr = 0; }
private:
    QCMutex* mMutexPtr;

private:
    QCStMutexUnlocker(
        const QCStMutexUnlocker& inUnlocker);
    QCStMutexUnlocker& operator=(
        const QCStMutexUnlocker& inUnlocker);
};


#endif /* QCSTUTILS_H */

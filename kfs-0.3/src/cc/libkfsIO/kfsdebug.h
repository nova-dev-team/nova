//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: kfsdebug.h 326 2009-04-25 00:45:50Z sriramsrao $
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

#ifndef KFSDEBUG_H
#define KFSDEBUG_H

#include <assert.h>

#define KFSASSERT(a) assert(a)

#ifdef NDEBUG
#   define KFSVERIFY(a) a
#else
#   define KFSVERIFY(a) KFSASSERT(a)
#endif

#endif /* KFSDEBUG_H */

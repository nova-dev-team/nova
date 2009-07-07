//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: log.h 188 2008-10-15 19:54:42Z sriramsrao $ 
//
// Created 2007/10/17
//
// Copyright 2008 Quantcast Corp.
// Copyright 2007-2008 Kosmix Corp.
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
// \brief A logging facility that uses log4cpp.
//
//----------------------------------------------------------------------------

#ifndef COMMON_LOG_H
#define COMMON_LOG_H

#define LOG4CPP_FIX_ERROR_COLLISION 1
#include <log4cpp/Category.hh>
#include <log4cpp/Priority.hh>
#include <libgen.h>

namespace KFS 
{
    // Have a singleton logger for an application
    class MsgLogger {
    private:
        MsgLogger();
        MsgLogger(const MsgLogger &other);
        const MsgLogger & operator = (const MsgLogger &other);
    public:
        static log4cpp::Category *logger;

#ifdef NDEBUG
        static void Init(const char *filename) {
            Init(filename, log4cpp::Priority::INFO);
        }
#else
        static void Init(const char *filename) {
            Init(filename, log4cpp::Priority::DEBUG);
        }
#endif        
        static void Init(const char *filename, 
                         log4cpp::Priority::Value priority);

        static void SetLevel(log4cpp::Priority::Value priority);

    };

#ifndef THIS_FILE
#define THIS_FILE basename((char *) __FILE__)
#endif

#ifndef KFS_LOG_DEBUG
#define KFS_LOG_DEBUG(msg) MsgLogger::logger->debug("(%s:%d) " msg, THIS_FILE, __LINE__);
#define KFS_LOG_VA_DEBUG(msg, ...) \
	do { \
         MsgLogger::logger->debug("(%s:%d) " msg, THIS_FILE, __LINE__, __VA_ARGS__); \
        } while(0)
#endif

#ifndef KFS_LOG_INFO
#define KFS_LOG_INFO(msg) MsgLogger::logger->info("(%s:%d) " msg, THIS_FILE, __LINE__);
#define KFS_LOG_VA_INFO(msg, ...) \
	do { \
         MsgLogger::logger->info("(%s:%d) " msg, THIS_FILE, __LINE__, __VA_ARGS__); \
        } while(0)
#endif

#ifndef KFS_LOG_WARN
#define KFS_LOG_WARN(msg) MsgLogger::logger->warn("(%s:%d) " msg, THIS_FILE, __LINE__);
#define KFS_LOG_VA_WARN(msg, ...) \
	do { \
         MsgLogger::logger->warn("(%s:%d) " msg, THIS_FILE, __LINE__, __VA_ARGS__); \
        } while(0)
#endif

#ifndef KFS_LOG_ERROR
#define KFS_LOG_ERROR(msg) MsgLogger::logger->error("(%s:%d) " msg, THIS_FILE, __LINE__);
#define KFS_LOG_VA_ERROR(msg, ...) \
	do { \
         MsgLogger::logger->error("(%s:%d) " msg, THIS_FILE, __LINE__, __VA_ARGS__); \
        } while(0)
#endif

#ifndef KFS_LOG_FATAL
#define KFS_LOG_FATAL(msg) MsgLogger::logger->fatal("(%s:%d) " msg, THIS_FILE, __LINE__);
#define KFS_LOG_VA_FATAL(msg, ...) \
	do { \
         MsgLogger::logger->fatal("(%s:%d) " msg, THIS_FILE, __LINE__, __VA_ARGS__); \
        } while(0)
#endif

}

#endif // COMMON_LOG_H

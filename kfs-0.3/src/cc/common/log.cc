//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: log.cc 264 2009-02-10 00:50:10Z sriramsrao $ 
//
// Created 2005/03/01
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
//----------------------------------------------------------------------------

#include "log.h"
#include <stdlib.h>
#include <log4cpp/RollingFileAppender.hh>
#include <log4cpp/OstreamAppender.hh>
#include <log4cpp/PatternLayout.hh>
#include "DailyRollingFileAppender.h"

using namespace KFS;

log4cpp::Category* KFS::MsgLogger::logger = NULL;

void
MsgLogger::Init(const char *filename, log4cpp::Priority::Value priority)
{
    log4cpp::Appender* appender;
    log4cpp::PatternLayout* layout = new log4cpp::PatternLayout();
    layout->setConversionPattern("%d{%m-%d-%Y %H:%M:%S.%l} %p - %m %n");

    if (filename != NULL) {
        // set the max. log file size to be 100M before it rolls over
        // to the next; save the last N log files. 
        // appender = new log4cpp::RollingFileAppender("default", std::string(filename),
        // 100 * 1024 * 1024, 100);
        appender = new log4cpp::DailyRollingFileAppender("default", std::string(filename));
    }
    else
        appender = new log4cpp::OstreamAppender("default", &std::cerr);

    appender->setLayout(layout);

    logger = &(log4cpp::Category::getInstance(std::string("kfs")));
    logger->addAppender(appender);
    // logger->setAdditivity(false);
    logger->setPriority(priority);
}

void MsgLogger::SetLevel(log4cpp::Priority::Value priority) {
    logger->setPriority(priority);
}

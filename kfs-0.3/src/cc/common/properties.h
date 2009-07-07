//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: properties.h 71 2008-07-07 15:49:14Z sriramsrao $ 
//
// \brief Properties file similar to java.util.Properties
//
// Created 2004/05/05
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

#ifndef COMMON_PROPERTIES_H
#define COMMON_PROPERTIES_H
  
#include <istream>
#include <string>
#include <map>

namespace KFS
{

class Properties {

  private :
    //Map that holds the (key,value) pairs
    std::map<std::string, std::string> * propmap; 
    std::string removeLTSpaces(std::string);

  public  :
    // load the properties from a file
    int loadProperties(const char* fileName, char delimiter, bool verbose, bool multiline = false);
    // load the properties from an in-core buffer
    int loadProperties(std::istream &ist, char delimiter, bool verbose, bool multiline = false);
    std::string getValue(std::string key, std::string def) const;
    const char* getValue(std::string key, const char* def) const;
    int getValue(std::string key, int def);
    long getValue(std::string key, long def);
    long long getValue(std::string key, long long def);
    uint64_t getValue(std::string key, uint64_t def);
    double getValue(std::string key, double def);   
    void setValue(const std::string key, const std::string value);
    void getList(std::string &outBuf, std::string linePrefix) const;
    Properties();
    Properties(const Properties &p);
    ~Properties();

};

}

#endif // COMMON_PROPERTIES_H

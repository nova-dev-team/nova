//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: TcpSocket.cc 253 2009-01-27 05:16:40Z sriramsrao $ 
//
// Created 2006/03/10
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

#include "TcpSocket.h"
#include "common/log.h"

#include "Globals.h"

#include <cerrno>
#include <poll.h>
#include <netdb.h>
#include <arpa/inet.h>

using std::min;
using std::max;
using std::vector;
using std::string;

using namespace KFS;
using namespace KFS::libkfsio;

TcpSocket::~TcpSocket()
{
    Close();
}

int TcpSocket::Listen(int port)
{
    struct sockaddr_in	ourAddr;
    int reuseAddr = 1;

    mSockFd = socket(PF_INET, SOCK_STREAM, 0);
    if (mSockFd == -1) {
        perror("Socket: ");
        return -1;
    }

    memset(&ourAddr, 0, sizeof(struct sockaddr_in));
    ourAddr.sin_family = AF_INET;
    ourAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    ourAddr.sin_port = htons(port);

    /* 
     * A piece of magic here: Before we bind the fd to a port, setup
     * the fd to reuse the address. If we move this line to after the
     * bind call, then things don't work out.  That is, we bind the fd
     * to a port; we panic; on restart, bind will fail with the address
     * in use (i.e., need to wait 2MSL for TCP's time-wait).  By tagging
     * the fd to reuse an address, everything is happy.
     */
    if (setsockopt(mSockFd, SOL_SOCKET, SO_REUSEADDR, 
                   (char *) &reuseAddr, sizeof(reuseAddr)) < 0) {
        perror("Setsockopt: ");
    }

    if (bind(mSockFd, (struct sockaddr *) &ourAddr, sizeof(ourAddr)) < 0) {
        perror("Bind: ");
        close(mSockFd);
        mSockFd = -1;
        return -1;
    }
    
    if (listen(mSockFd, 5) < 0) {
        perror("listen: ");
    }

    globals().ctrOpenNetFds.Update(1);

    return 0;
    
}

TcpSocket* TcpSocket::Accept()
{
    int fd;
    struct sockaddr_in	cliAddr;    
    TcpSocket *accSock;
    socklen_t cliAddrLen = sizeof(cliAddr);

    if ((fd = accept(mSockFd, (struct sockaddr *) &cliAddr, &cliAddrLen)) < 0) {
        perror("Accept: ");
        return NULL;
    }
    accSock = new TcpSocket(fd);

    accSock->SetupSocket();

    globals().ctrOpenNetFds.Update(1);

    return accSock;
}

int TcpSocket::Connect(const struct sockaddr_in *remoteAddr, bool nonblockingConnect)
{
    int res = 0;

    Close();

    mSockFd = socket(PF_INET, SOCK_STREAM, 0);
    if (mSockFd == -1) {
        return -1;
    }

    if (nonblockingConnect) {
        // when we do a non-blocking connect, we mark the socket
        // non-blocking; then call connect and it wil return
        // EINPROGRESS; the fd is added to the select loop to check
        // for completion
        fcntl(mSockFd, F_SETFL, O_NONBLOCK);
    }

    res = connect(mSockFd, (struct sockaddr *) remoteAddr, sizeof(struct sockaddr_in));
    if ((res < 0) && (errno != EINPROGRESS)) {
        perror("Connect: ");
        close(mSockFd);
        mSockFd = -1;
        return -1;
    }

    if ((res < 0) && nonblockingConnect)
        res = -errno;

    SetupSocket();

    globals().ctrOpenNetFds.Update(1);

    return res;
}

int TcpSocket::Connect(const ServerLocation &location, bool nonblockingConnect)
{
    struct hostent *hostInfo;
    struct sockaddr_in remoteAddr;

    remoteAddr.sin_addr.s_addr = inet_addr(location.hostname.c_str());

    if (remoteAddr.sin_addr.s_addr == (in_addr_t) -1) {
        // do the conversion if we weren't handed an IP address

        hostInfo = gethostbyname(location.hostname.c_str());

        if (hostInfo == NULL) {
#if defined __APPLE__
            KFS_LOG_VA_DEBUG("herrno: %d, errstr = %s", h_errno, hstrerror(h_errno));
#endif
            perror("gethostbyname: ");
            return -1;
        }

        memcpy(&remoteAddr.sin_addr.s_addr, hostInfo->h_addr, sizeof(struct in_addr));
    }
    remoteAddr.sin_port = htons(location.port);
    remoteAddr.sin_family = AF_INET;

    return Connect(&remoteAddr, nonblockingConnect);
}

void TcpSocket::SetupSocket()
{
#if defined(__sun__)
    int bufSize = 512 * 1024;
#else
    int bufSize = 65536;
#endif
    int flag = 1;

    // get big send/recv buffers and setup the socket for non-blocking I/O
    if (setsockopt(mSockFd, SOL_SOCKET, SO_SNDBUF, (char *) &bufSize, sizeof(bufSize)) < 0) {
        perror("Setsockopt: ");
    }
    if (setsockopt(mSockFd, SOL_SOCKET, SO_RCVBUF, (char *) &bufSize, sizeof(bufSize)) < 0) {
        perror("Setsockopt: ");
    }
    // enable keep alive so we can socket errors due to detect network partitions
    if (setsockopt(mSockFd, SOL_SOCKET, SO_KEEPALIVE, (char *) &flag, sizeof(flag)) < 0) {
        perror("Disabling NAGLE: ");
    }

    fcntl(mSockFd, F_SETFL, O_NONBLOCK);
    // turn off NAGLE
    if (setsockopt(mSockFd, IPPROTO_TCP, TCP_NODELAY, (char *) &flag, sizeof(flag)) < 0) {
        perror("Disabling NAGLE: ");
    }

}

int TcpSocket::GetPeerName(struct sockaddr *peerAddr, int len)
{
    socklen_t peerLen = len;

    if (getpeername(mSockFd, peerAddr, &peerLen) < 0) {
        perror("getpeername: ");
        return -1;
    }
    return 0;
}

string TcpSocket::GetPeerName()
{
    struct sockaddr_in saddr;
    char ipname[INET_ADDRSTRLEN];

    if (GetPeerName((struct sockaddr*) &saddr, sizeof(struct sockaddr_in)) < 0)
        return "unknown src";
    if (inet_ntop(AF_INET, &(saddr.sin_addr), ipname, INET_ADDRSTRLEN) == NULL)
        return "unknown src";        
    return ipname;
}

int TcpSocket::Send(const char *buf, int bufLen)
{
    int nwrote;

    nwrote = send(mSockFd, buf, bufLen, 0);
    if (nwrote > 0) {
        globals().ctrNetBytesWritten.Update(nwrote);
    }
    return nwrote;
}

int TcpSocket::Recv(char *buf, int bufLen)
{
    int nread;

    nread = recv(mSockFd, buf, bufLen, 0);
    if (nread > 0) {
        globals().ctrNetBytesRead.Update(nread);
    }

    return nread;
}

int TcpSocket::Peek(char *buf, int bufLen)
{
    int nread;

    nread = recv(mSockFd, buf, bufLen, MSG_PEEK);
    return nread;
}

bool TcpSocket::IsGood()
{
    if (mSockFd < 0)
        return false;

    return true;
}


void TcpSocket::Close()
{
    if (mSockFd < 0) {
        return;
    }
    close(mSockFd);
    mSockFd = -1;
    globals().ctrOpenNetFds.Update(-1);
}

int TcpSocket::DoSynchSend(const char *buf, int bufLen)
{
    int numSent = 0;
    int res = 0, nfds;
    fd_set fds;
    struct timeval timeout;

    while (numSent < bufLen) {
        if (mSockFd < 0)
            break;
        if (res < 0) {
            FD_ZERO(&fds);
            FD_SET(mSockFd, &fds);
            timeout.tv_sec = 1;
            timeout.tv_usec = 0;
            nfds = select(mSockFd + 1, NULL, &fds, &fds, &timeout);
            if (nfds == 0)
                continue;
        }

        res = Send(buf + numSent, bufLen - numSent);
        if (res == 0)
            return 0;
        if ((res < 0) && 
            ((errno == EAGAIN) || (errno == EWOULDBLOCK) || (errno == EINTR)))
            continue;
        if (res < 0)
            break;
        numSent += res;
    }
    if (numSent > 0) {
        globals().ctrNetBytesWritten.Update(numSent);
    }
    return numSent;
}

// 
// Receive data within a certain amount of time.  If the server is too slow in responding, bail
//
int TcpSocket::DoSynchRecv(char *buf, int bufLen, struct timeval &timeout)
{
    int numRecd = 0;
    int res = 0, nfds;
    struct pollfd pfd;

    while (numRecd < bufLen) {
        if (mSockFd < 0)
            break;

        if (res < 0) {
            pfd.fd = mSockFd;
            pfd.events = POLLIN;
            pfd.revents = 0;
            nfds = poll(&pfd, 1, timeout.tv_sec * 1000);
            // get a 0 when timeout expires
            if (nfds == 0) {
                KFS_LOG_DEBUG("Timeout in synch recv");
                return numRecd > 0 ? numRecd : -ETIMEDOUT;
            }
        }

        res = Recv(buf + numRecd, bufLen - numRecd);
        if (res == 0)
            return 0;
        if ((res < 0) && 
            ((errno == EAGAIN) || (errno == EWOULDBLOCK) || (errno == EINTR)))
            continue;
        if (res < 0)
            break;
        numRecd += res;
    }
    if (numRecd > 0) {
        globals().ctrNetBytesRead.Update(numRecd);
    }

    return numRecd;
}


// 
// Receive data within a certain amount of time and discard them.  If
// the server is too slow in responding, bail
//
int TcpSocket::DoSynchDiscard(int nbytes, struct timeval &timeout)
{
    int numRecd = 0, ntodo, res;
    const int bufSize = 4096;
    char buf[bufSize];

    while (numRecd < nbytes) {
        ntodo = min(nbytes - numRecd, bufSize);
        res = DoSynchRecv(buf, ntodo, timeout);
        if (res == -ETIMEDOUT)
            return numRecd;
        assert(numRecd >= 0);
        if (numRecd < 0)
            break;
        numRecd += res;
    }
    return numRecd;
}

// 
// Peek data within a certain amount of time.  If the server is too slow in responding, bail
//
int TcpSocket::DoSynchPeek(char *buf, int bufLen, struct timeval &timeout)
{
    int numRecd = 0;
    int res, nfds;
    struct pollfd pfd;

    while (1) {
        pfd.fd = mSockFd;
        pfd.events = POLLIN;
        pfd.revents = 0;
        nfds = poll(&pfd, 1, timeout.tv_sec * 1000);
        // get a 0 when timeout expires
        if (nfds == 0) {
            return -ETIMEDOUT;
        }

        res = Peek(buf + numRecd, bufLen - numRecd);
        if (res == 0)
            return 0;
        if ((res < 0) && (errno == EAGAIN))
            continue;
        if (res < 0)
            break;
        numRecd += res;
        if (numRecd > 0)
            break;
    }
    return numRecd;
}

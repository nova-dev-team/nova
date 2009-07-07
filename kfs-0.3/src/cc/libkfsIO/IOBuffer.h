//---------------------------------------------------------- -*- Mode: C++ -*-
// $Id: IOBuffer.h 330 2009-04-27 17:52:44Z sjakub $ 
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

#ifndef _LIBIO_IOBUFFER_H
#define _LIBIO_IOBUFFER_H

extern "C" {
#include <stdint.h>
}

#include <cassert>
#include <list>
#include <exception>

#include <boost/shared_ptr.hpp>
#include <boost/shared_array.hpp>

namespace KFS
{

///
/// \file IOBuffer.h
/// \brief Buffers used for I/O in KFS.
///
/// An IOBuffer in KFS is a stream of IOBufferData.  Each IOBufferData
/// is an immutable buffer---that is, there is a single producer for
/// data in the buffer and that data maybe consumed by multiple
/// consumers.
///
/// In the current implementation, IOBufferData objects are single
/// producer, multiple consumers.
///

class IOBufferData;

typedef boost::shared_array<char> IOBufferBlockPtr;
///
/// \typedef IOBufferDataPtr
/// Since an IOBufferData can be shared, encapsulate it in a smart
/// pointer so that the cleanup occurs when all references are
/// released.
///
typedef boost::shared_ptr<IOBufferData> IOBufferDataPtr;

///
/// \class IOBufferData
/// \brief An IOBufferData contains a buffer and associated
/// producer/consumer points.

class IOBufferData {
public:
    IOBufferData();

    /// Create an IOBufferData blob with a backing buffer of the specified size.
    IOBufferData(uint32_t bufsz);

    /// Create an IOBufferData blob by sharing data block from other;
    /// set the producer/consumer based on the start/end positions
    /// that are passed in
    IOBufferData(IOBufferDataPtr &other, char *s, char *e);
    ~IOBufferData();

    ///
    /// Read data from file descriptor into the buffer.
    /// @param[in] fd file descriptor to be used for reading.
    /// @result Returns the # of bytes read
    ///
    int Read(int fd);

    ///
    /// Write data from the buffer to the file descriptor.
    /// @param[in] fd file descriptor to be used for writing.
    /// @result Returns the # of bytes written
    ///
    int Write(int fd);

    ///
    /// Copy data into the buffer.  For doing a copy, data is appended
    /// to the buffer starting at the offset corresponding to
    /// mProducer.  # of bytes copied is min (# of bytes, space
    /// avail), where space avail = mEnd - mProducer.
    ///
    /// NOTE: As a result of copy, the "producer" pointer is not
    /// advanced. 
    ///
    /// @param[out] buf A containing the data to be copied in.
    /// @param[in] numBytes # of bytes to be copied.
    /// @retval Returns the # of bytes copied.
    ///
    int CopyIn(const char *buf, int numBytes);
    int CopyIn(const IOBufferData *other, int numBytes);
    ///
    /// Copy data out the buffer.  For doing a copy, data is copied
    /// out of the buffer starting at the offset corresponding to
    /// mConsumer.  # of bytes copied is min (# of bytes, bytes
    /// avail), where bytes avail = mProducer - mConsumer.
    ///
    /// NOTE: As a result of copy, the "consumer" pointer is not
    /// advanced. 
    ///
    /// @param[out] buf A containing the data to be copied in.
    /// @param[in] numBytes # of bytes to be copied.
    /// @retval Returns the # of bytes copied.
    ///
    int CopyOut(char *buf, int numBytes);

    char *Start() { return mStart; }
    char *Producer() { return mProducer; }
    char *Consumer() { return mConsumer; }
    const char *Start() const { return mStart; }
    const char *Producer() const { return mProducer; }
    const char *Consumer() const { return mConsumer; }

    ///
    /// Some data has been filled in the buffer.  So, advance
    /// mProducer.
    /// @param[in] nbytes # of bytes of data filled
    /// @retval # of bytes filled in this buffer.
    ///
    int Fill(int nbytes); 
    int ZeroFill(int nbytes);

    ///
    /// Some data has been consumed from the buffer.  So, advance
    /// mConsumer.
    /// @param[in] nbytes # of bytes of data consumed
    /// @retval # of bytes consumed from this buffer.
    ///
    int Consume(int nbytes);

    ///
    /// Remove some data from the end of the buffer.  So, pull back
    /// mProducer
    /// @param[in] nbytes # of bytes of data to be trimmed
    /// @retval # of bytes in this buffer.
    ///
    int Trim(int nbytes);

    /// Returns the # of bytes available for consumption.
    int BytesConsumable() const { return mProducer - mConsumer; }

    /// Return the space available in the buffer
    size_t SpaceAvailable() const { return mEnd - mProducer; }
    int IsFull() const { return mProducer >= mEnd; }
    int IsEmpty() const { return mProducer <= mConsumer; }

private:
    /// Data buffer that is ref-counted for sharing.
    IOBufferBlockPtr	mData;
    /// Pointers that correspond to the start/end of the buffer
    char		*mStart, *mEnd;
    /// Pointers into mData that correspond to producer/consumer
    char		*mProducer, *mConsumer;

    /// Allocate memory and init the pointers.
    void		Init(uint32_t bufsz);

    inline int MaxAvailable(int numBytes) const;
    inline int MaxConsumable(int numBytes) const;
};


///
/// \struct IOBuffer
/// An IOBuffer consists of a list of IOBufferDataPtr.  It provides
/// API's for reading/writing data to/from the buffer.  Operations on
/// IOBuffer translates to operations on appropriate IOBufferDataPtr.
///
struct IOBuffer {
    IOBuffer();
    ~IOBuffer();

    IOBuffer *Clone();

    /// Append the IOBufferData block to the list stored in this buffer.
    /// Unlike methods with IOBuffer as argument, this method will not
    /// Consume() or change buf in any way, the underlying buffer will be
    /// shared.
    void Append(const IOBufferDataPtr &buf);

    /// Append the contents of ioBuf to this buffer.
    void Append(IOBuffer *ioBuf);

    int Read(int fd);
    int Write(int fd);

    /// Move data from one buffer to another.  This involves (mostly)
    /// shuffling pointers without incurring data copying.
    /// The requirement is that "other" better have as much bytes as
    /// we are trying to move.
    /// @param[in] other  Buffer from which data has to be moved
    /// @param[in] numBytes  # of bytes of data to be moved over
    ///
    int Move(IOBuffer *other, int numBytes);

    /// Splice data from other to "this".  The key here is that, data
    /// from other is inserted starting at the specified offset.  The
    /// requirements are that: (1) data in "this" should be at least
    /// of length offset bytes, and (2) other better have as much
    /// bytes as we are trying to move.
    /// @param[in] other  Buffer from which data has to be spliced
    /// @param[in] offset  The offset at which data has to be spliced in
    /// @param[in] numBytes  # of bytes of data to be moved over
    ///
    void Splice(IOBuffer *other, int offset, int numBytes);

    /// Zero fill the buffer for length numBytes.
    /// @param[in] numBytes  # of bytes to be zero-filled.
    void ZeroFill(int numBytes);

    ///
    /// Copy data into the buffer.  For doing a copy, data is appended
    /// to the last buffer in mBuf.  If the amount of data to be
    /// copied exceeds space in the last buffer, additional buffers
    /// are allocated and copy operation runs to finish.
    ///
    /// NOTE: As a result of copy, the "producer" portion of an
    /// IOBufferData is not advanced. 
    ///
    /// @param[in] buf A containing the data to be copied in.
    /// @param[in] numBytes # of bytes to be copied in.
    /// @retval Returns the # of bytes copied.
    ///
    int CopyIn(const char *buf, int numBytes);

    ///
    /// Copy data out of the buffer.  For doing a copy, data is copied
    /// from the first buffer in mBuf.  If the amount of data to be
    /// copied exceeds what is available in the first buffer, the list
    /// of buffers is walked to copy out data.
    ///
    /// NOTE: As a result of copy, the "consumer" portion of an
    /// IOBufferData is not advanced. 
    ///
    /// @param[out] buf A null-terminated buffer containing the data
    /// copied out.
    /// @param[in] bufLen Length of buf passed in.  At most bufLen
    /// bytes are copied out.
    /// @retval Returns the # of bytes copied.
    ///
    int CopyOut(char *buf, int bufLen);
    
    ///
    /// Consuming data in the IOBuffer translates to advancing the
    /// "consumer" point on underlying IOBufferDataPtr.  From the head
    /// of the list, the consumer point will be advanced on sufficient
    /// # of buffers.
    ///
    void Consume(int nbytes);

    /// Returns the # of bytes that are available for consumption.
    int BytesConsumable() const;

    /// Trim data from the end of the buffer to nbytes.  This is the
    /// converse of consume, where data is removed from the front of
    /// the buffer.
    void Trim(int nbytes);

    /// Returns true if buffer has no data.
    bool IsEmpty() const;

    /// List of IOBufferData blocks that comprise this buffer.
    std::list<IOBufferDataPtr> mBuf;
};


    namespace libkfsio
    {
        /// API to set the default allocation when allocating
        /// IOBufferData().  The default allocation unit is 4K unless
        /// changed by this API call.
        void SetIOBufferSize(uint32_t bufsz);
    }

}

#endif // _LIBIO_IOBUFFER_H

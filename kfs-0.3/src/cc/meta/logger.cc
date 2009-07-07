/*!
 * $Id: logger.cc 250 2009-01-25 07:17:49Z sriramsrao $ 
 *
 * Copyright 2008 Quantcast Corp.
 * Copyright 2006-2008 Kosmix Corp.
 *
 * This file is part of Kosmos File System (KFS).
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * \file logger.cc
 * \brief thread for logging metadata updates
 * \author Blake Lewis (Kosmix Corp.)
 */

#include <csignal>

#include "logger.h"
#include "queue.h"
#include "checkpoint.h"
#include "util.h"
#include "replay.h"
#include "common/log.h"
#include "libkfsIO/Globals.h"

using namespace KFS;

// default values
string KFS::LOGDIR("./kfslog");
string KFS::LASTLOG(LOGDIR + "/last");

Logger KFS::oplog(LOGDIR);


/*!
 * \brief log the request and flush the result to the fs buffer.
*/
int
Logger::log(MetaRequest *r)
{
	int res = r->log(file);
	if (res >= 0)
		flushResult(r);
	return res;
}

/*!
 * \brief flush log entries to disk
 *
 * Make sure that all of the log entries are on disk and
 * update the highest sequence number logged.
 */
void
Logger::flushLog()
{
	thread.lock();
	seq_t last = nextseq;
	thread.unlock();

	file.flush();
	if (file.fail())
		panic("Logger::flushLog", true);

	thread.lock();
	committed = last;
	thread.unlock();
}

/*!
 * \brief set the log filename/log # to seqno
 * \param[in] seqno	the next log sequence number (lognum)
 */
void
Logger::setLog(int seqno)
{
	assert(seqno >= 0);
	lognum = seqno;
	logname = logfile(lognum);
}

/*!
 * \brief open a new log file for writing
 * \param[in] seqno	the next log sequence number (lognum)
 * \return		0 if successful, negative on I/O error
 */
int
Logger::startLog(int seqno)
{
	assert(seqno >= 0);
	lognum = seqno;
	logname = logfile(lognum);
	if (file_exists(logname)) {
		// following log replay, until the next CP, we
		// should continue to append to the logfile that we replayed.
		// seqno will be set to the value we got from the chkpt file.
		// So, don't overwrite the log file.
		KFS_LOG_VA_DEBUG("Opening %s in append mode", logname.c_str());
		file.open(logname.c_str(), std::ios_base::app);
		return (file.fail()) ? -EIO : 0;
	}
	file.open(logname.c_str());
	file << "version/" << VERSION << '\n';

	// for debugging, record when the log was opened
	time_t t = time(NULL);

	file << "time/" << ctime(&t);
	return (file.fail()) ? -EIO : 0;
}

/*!
 * \brief close current log file and begin a new one
 */
int
Logger::finishLog()
{
	thread.lock();
	// for debugging, record when the log was closed
	time_t t = time(NULL);

	file << "time/" << ctime(&t);
	file.flush();
	file.close();
	link_latest(logname, LASTLOG);
	if (file.fail())
		warn("link_latest", true);
	incp = committed;
	int status = startLog(lognum + 1);
	cp.resetMutationCount();
	thread.unlock();
	// cp.start_CP();
	return status;
}

/*!
 * \brief make sure result is on disk
 * \param[in] r	the result of interest
 *
 * If this result has a higher sequence number than what is
 * currently known to be on disk, flush the log to disk.
 */
void
Logger::flushResult(MetaRequest *r)
{
	if (r->seqno > committed) {
		flushLog();
		assert(r->seqno <= committed);
	}
}

/*!
 * \brief return next available result
 * \return result that was at the head of the result queue
 *
 * Return the next available result.
 */
MetaRequest *
Logger::next_result()
{
	MetaRequest *r = logged.dequeue();
	return r;
}

/*!
 * \brief return next result, non-blocking
 *
 * Same as Logger::next_result() except that it returns NULL
 * if the result queue is empty.
 */
MetaRequest *
Logger::next_result_nowait()
{
	MetaRequest *r = logged.dequeue_nowait();
	return r;
}

/*!
 * \brief logger main loop
 *
 * Pull requests from the pending queue and call the appropriate
 * log routine to write them into the log file.  Note any mutations
 * in the checkpoint structure (so that it will realize that there
 * are changes to record), and finally, move the request onto the
 * result queue for return to the clients.  Before the result is released
 * to the network dispatcher, we flush the log.
 */
void *
logger_main(void *dummy)
{
	for (;;) {
		MetaRequest *r = oplog.get_pending();
		bool is_cp = (r->op == META_LOG_ROLLOVER);
		if (r->mutation && r->status == 0) {
			oplog.log(r);
			if (!is_cp) {
				cp.note_mutation();
			}
		}
		if (is_cp) {
			oplog.save_cp(r);
		} else
			oplog.add_logged(r);
		if (oplog.isPendingEmpty()) 
			// notify the net-manager that things are ready to go
			libkfsio::globals().netKicker.Kick();
	}
	return NULL;
}

void *
logtimer(void *dummy)
{
	int status, sig;
	sigset_t sset;
	sigemptyset(&sset);
	sigaddset(&sset, SIGALRM);

	alarm(LOG_ROLLOVER_MAXSEC);
	for (;;) {
		status = sigwait(&sset, &sig);
		if (status == EINTR)	// happens under gdb for some reason
			continue;
		assert(status == 0 && sig == SIGALRM);
		alarm(LOG_ROLLOVER_MAXSEC);
		MetaLogRollover logreq;
		// if the metatree hasn't been mutated, avoid a log file
		// rollover
		if (!cp.isCPNeeded())
			continue;
		submit_request(&logreq);
		(void) oplog.wait_for_cp();
	}

	return NULL;
}


void
KFS::logger_setup_paths(const string &logdir)
{
	if (logdir != "") {
		LOGDIR = logdir;
		LASTLOG = LOGDIR + "/last";
		oplog.setLogDir(LOGDIR);
	}
}

void
KFS::logger_init()
{
	if (oplog.startLog(replayer.logno()) < 0)
		panic("KFS::logger_init, startLog", true);
	oplog.start(logger_main);
	// use a timer to rotate logs
	oplog.start_timer(logtimer);
}

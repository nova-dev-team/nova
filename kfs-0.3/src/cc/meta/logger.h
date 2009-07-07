/*!
 * $Id: logger.h 71 2008-07-07 15:49:14Z sriramsrao $ 
 *
 * \file logger.h
 * \brief metadata logger
 * \author Blake Lewis (Kosmix Corp.)
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
 */
#if !defined(KFS_LOGGER_H)
#define KFS_LOGGER_H

#include <fstream>
#include <sstream>
#include <string>

#include "kfstypes.h"
#include "queue.h"
#include "thread.h"
#include "request.h"
#include "util.h"

using std::string;
using std::ofstream;

namespace KFS {

/*!
 * \brief Class for logging metadata updates
 *
 * This class consists of two threads: 
 *  - one thread that writes the updates to the logs and then dispatches the
 *  logged results to the network thread
 *  - another thread that runs a timer to cause periodic log rollover.  Whenever
 *  the log rollover occurs, after we close the log file, we create a link from
 *  "LAST" to the recently closed log file.  This is used by the log compactor
 *  to determine the set of files that can be compacted.
 */
class Logger {
	string logdir;		//!< directory where logs are kept
	int lognum;		//!< for generating log file names
	string logname;		//!< name of current log file
	ofstream file;		//!< the current log file
	seq_t nextseq;		//!< next request sequence no.
	seq_t committed;	//!< highest request known to be on disk
	seq_t incp;		//!< highest request in a checkpoint
	MetaQueue <MetaRequest> pending; //!< list of still-unlogged results
	MetaQueue <MetaRequest> logged;	//!< list of logged results
	MetaQueue <MetaRequest> cpdone; //!< completed CP (aka log rollover)
	MetaThread thread;	//!< thread synchronization
	MetaThread timer;	//!< timer to rollover log files
	string genfile(int n)	//!< generate a log file name
	{
		std::ostringstream f(std::ostringstream::out);
		f << n; 
		return logdir + "/log." + f.str();
	}
	void flushLog();
	void flushResult(MetaRequest *r);
public:
	static const int VERSION = 1;
	Logger(string d): logdir(d), lognum(-1), nextseq(0), committed(0) { }
	~Logger() { file.close(); }
	void setLogDir(const string &d)
	{
		logdir = d;
	}

	string logfile(int n)	//!< generate a log file name
	{
		return makename(logdir, "log", n);
	}
	/*!
	 * \brief check whether request is stored on disk
	 * \param[in] r the request of interest
	 * \return	whether it is on disk
	 */
	bool iscommitted(MetaRequest *r)
	{
		return r->seqno != 0 && r->seqno <= committed;
	}
	//!< log a request
	int log(MetaRequest *r);
	void add_pending(MetaRequest *r) { pending.enqueue(r); }
	/*!
	 * \brief get a pending request and assign it a sequence number
	 * \return the request
	 */
	MetaRequest *get_pending()
	{
		MetaRequest *r = pending.dequeue();
		r->seqno = ++nextseq;
		return r;
	}
	bool isPendingEmpty()
	{
		return pending.empty();
	}
	MetaRequest *next_result();
	MetaRequest *next_result_nowait();
	seq_t checkpointed() { return incp; }	//!< highest seqno in CP
	void add_logged(MetaRequest *r) { logged.enqueue(r); }
	void save_cp(MetaRequest *r) { cpdone.enqueue(r); }
	MetaRequest *wait_for_cp() { return cpdone.dequeue(); }
	void start(MetaThread::thread_start_t func)	//!< start thread
	{
		thread.start(func, NULL);
	}
	void setLog(int seqno);		//!< set the log filename based on seqno
	int startLog(int seqno);	//!< start a new log file
	int finishLog();		//!< tie off log file before CP
	const string name() const { return logname; }	//!< name of log file
	/*!
	 * \brief set initial sequence numbers at startup
	 * \param[in] last last sequence number from checkpoint or log
	 */
	void set_seqno(seq_t last)
	{
		incp = committed = nextseq = last;
	}
	/*!
	 * Use a timer to rollover the log files every N minutes
	 */
	void start_timer(MetaThread::thread_start_t func)
	{
		timer.start(func, NULL);
	}
};

extern string LOGDIR;
extern string LASTLOG;
const unsigned int LOG_ROLLOVER_MAXSEC = 600;	//!< max. seconds between CP's/log rollover
extern Logger oplog;
extern void logger_setup_paths(const string &logdir);
extern void logger_init();
extern MetaRequest *next_result();

}
#endif // !defined(KFS_LOGGER_H)

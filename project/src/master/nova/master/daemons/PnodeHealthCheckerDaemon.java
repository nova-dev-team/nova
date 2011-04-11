package nova.master.daemons;

import org.apache.log4j.Logger;

/**
 * Deamon to check each pnode's reachability.
 * 
 * @author santa
 * 
 */
public class PnodeHealthCheckerDaemon extends MasterDaemon {

	/**
	 * Log4j logger.
	 */
	Logger logger = Logger.getLogger(PnodeHealthCheckerDaemon.class);

	/**
	 * Flag value indicating that this thread should stop
	 */
	private boolean stopFlag = false;

	private Object stopFlagSem = new Object();

	/**
	 * Override Thread.run(), do real work here.
	 */
	@Override
	public void run() {
		logger.info("PnodeHealthCheckerDaemon start up");
		while (this.stopFlag == false) {
			logger.trace("PnodeHealthCheckerDaemon wake up");

			poll();

			try {
				synchronized (this.stopFlagSem) {
					// wait for 100 milliseconds, or be notified immediately
					// when notify() is called
					this.stopFlagSem.wait(100);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		logger.info("PnodeHealthCheckerDaemon stopped");
	}

	/**
	 * Do work, on round.
	 */
	private void poll() {
		if (this.stopFlag == false) {
			// TODO @santa work1
		}
	}

	@Override
	public void stopWork() {
		this.stopFlag = true;
		synchronized (this.stopFlagSem) {
			this.stopFlagSem.notifyAll();
		}
		logger.info("PnodeHealthCheckerDaemon stopping");
	}

}

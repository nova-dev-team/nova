package nova.common.util;

import java.nio.channels.ClosedChannelException;

import org.apache.log4j.Logger;

/**
 * Background working daemon threads.
 * 
 * @author santa
 * 
 */
public abstract class SimpleDaemon extends Thread {

    /**
     * Private counter for daemons.
     */
    private static int daemonCounter = 0;

    /**
     * Sleep interval between each working round.
     */
    private final long sleepMilli;

    /**
     * Whether current daemon should stop.
     */
    private boolean stopFlag = false;

    /**
     * Provide notification on change of stopFlag.
     */
    private Object stopFlagSem = new Object();

    /**
     * Log4j logger.
     */
    Logger logger = null;

    /**
     * Default constructor, set sleep interval to 100ms.
     */
    public SimpleDaemon() {
        this(100);
    }

    /**
     * Construct a SimpleDaemon
     * 
     * @param sleepMilli
     *            The sleep intervl between each working round.
     */
    public SimpleDaemon(long sleepMilli) {
        this.sleepMilli = sleepMilli;

        SimpleDaemon.daemonCounter++;
        this.setName("daemon-" + SimpleDaemon.daemonCounter);

        // init worker using "this.getClass()" instead of "SimpleDaemon.class",
        // so that we could know exactly what subclass is being logged.
        logger = Logger.getLogger(this.getClass());

    }

    /**
     * Ask the deamon to stop its current work. This is just merely a
     * notification, so you have to join the thread after calling this thread.
     */
    public void stopWork() {
        this.stopFlag = true;
        synchronized (this.stopFlagSem) {
            this.stopFlagSem.notifyAll();
        }
    }

    /**
     * Check if current daemon is being stopped.
     * 
     * @return If current daemon should stop.
     */
    public boolean isStopping() {
        return this.stopFlag;
    }

    /**
     * Do one working round. This call is repeated called after a certain
     * interval. This function MUST return in limited time! And your
     * implementation should check the result of isStopping(), and return as
     * soon as it gives a "true" result.
     * 
     * @throws ClosedChannelException
     */
    abstract protected void workOneRound();

    /**
     * Daemon life cycle, repeatedly calls workOneRound() until isStoppping()
     * says "true".
     */
    @Override
    public void run() {
        String klass = this.getClass().getName();
        logger.info(klass + " start up");
        while (this.stopFlag == false) {
            logger.trace(klass + " wake up");

            try {
                workOneRound();
            } catch (Throwable e) {
                // let exception handling routine do its work
                exceptionCaught(e);
            }

            try {
                synchronized (this.stopFlagSem) {
                    // wait for some milliseconds, or be notified immediately
                    // when notify() is called
                    this.stopFlagSem.wait(this.sleepMilli);
                }
            } catch (InterruptedException e) {
                // let exception handling routine do its work
                exceptionCaught(e);
            }
        }
        logger.info(klass + " stopped");
    }

    /**
     * Handle an exception. Override this if necessary.
     * 
     * @param e
     *            Exception thrown by workOneRound() function. It could also be
     *            InterruptedException, which is thrown by the sleep function
     *            between each workOneRound() call.
     */
    protected void exceptionCaught(Throwable e) {
        logger.error("Exception caught", e);
    }
}

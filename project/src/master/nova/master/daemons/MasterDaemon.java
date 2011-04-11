package nova.master.daemons;

/**
 * Background working daemon threads.
 * 
 * @author santa
 * 
 */
public abstract class MasterDaemon extends Thread {

	/**
	 * Ask the deamon to stop its current work. This is just merely a
	 * notification, so you have to join the thread after calling this thread.
	 */
	abstract public void stopWork();

}

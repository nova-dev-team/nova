package nova.worker;

import nova.common.service.SimpleServer;
import nova.master.api.MasterProxy;

/**
 * The worker module of Nova.
 * 
 * @author santa
 * 
 */
public class NovaWorker extends SimpleServer {

	/**
	 * Connection to nova master.
	 */
	MasterProxy master = null;

	/**
	 * Application entry of NovaWorker.
	 * 
	 * @param args
	 *            Environment variables.
	 */
	public static void main(String[] args) {
		System.out.println("This is a dummy worker!");
	}

}

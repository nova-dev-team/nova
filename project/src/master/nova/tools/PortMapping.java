package nova.tools;

import java.util.ArrayList;

public class PortMapping extends Thread {

	private PortMapping(int localPort, String remoteAddr) {

	}

	@Override
	public void run() {

	}

	public synchronized static void addMapping(int localPort, String remoteAddr) {

	}

	public synchronized static void removeMapping(int localPort) {

	}

	public synchronized static ArrayList<PortMapping> listMapping() {
		ArrayList<PortMapping> list = new ArrayList<PortMapping>();
		return list;
	}

}

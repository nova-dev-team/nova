package nova.test.worker;

import java.io.IOException;

import nova.worker.models.StreamGobbler;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TestExecCmd {
	@Test
	public void test() {
		Logger log = Logger.getLogger(TestExecCmd.class);

		Process p;
		String[] cmds = { "ipconfig", "dir" };
		// String[] cmds = {
		// "ifconfig br0 down",
		// "brctl delbr br0",
		// "brctl addbr br0",
		// "brctl setbridgeprio br0 0",
		// "brctl addif br0 eth0",
		// "ifconfig eth0 0.0.0.0",
		// "ifconfig br0 " + Conf.getString("worker.bind_host")
		// + " netmask " + Conf.getString("worker.bind_host"),
		// "brctl sethello br0 1", "brctl setmaxage br0 4",
		// "brctl setfd br0 4", "ifconfig br0 up",
		// "route add default gw " + Conf.getString("worker.gateway") };
		for (String cmd : cmds) {
			System.out.println(cmd);
		}

		try {
			for (String cmd : cmds) {
				p = Runtime.getRuntime().exec(cmd);
				StreamGobbler errorGobbler = new StreamGobbler(
						p.getErrorStream(), "ERROR");
				errorGobbler.start();
				StreamGobbler outGobbler = new StreamGobbler(
						p.getInputStream(), "STDOUT");
				outGobbler.start();
				try {
					if (p.waitFor() != 0) {
						log.error("pack iso returned abnormal value!");
					}
				} catch (InterruptedException e1) {
					log.error("pack iso process terminated", e1);
				}
			}
		} catch (IOException e1) {
			log.error("exec mkisofs cmd error!", e1);
		}
	}
}
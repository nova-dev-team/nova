package nova.master.daemons;

import java.io.IOException;
import java.util.List;

import nova.master.models.Vnode;
import nova.worker.models.StreamGobbler;

import org.apache.log4j.Logger;

public class VncForwardDeamon {

	/**
	 * log4j logger
	 */
	Logger log = Logger.getLogger(VncForwardDeamon.class);

	protected void WorkOneRound() {

		String pwd, ip, port;
		String cmd1, cmd2;
		Process p1, p2;

		List<Vnode> allVnodes = Vnode.all();
		for (Vnode vnode : allVnodes) {
			if (vnode.getPmachineId() != null) {
				pwd = String.valueOf(vnode.getId());
				ip = String.valueOf(vnode.getIp());
				port = String.valueOf(vnode.getPort());
				if (port != null) {
					cmd1 = "../tools/server_side/bin/vnc_proxy_ctl del -p "
							+ pwd + " -s tmp/sockets/vnc_proxy.sock";
					try {
						p1 = Runtime.getRuntime().exec(cmd1);
						StreamGobbler errorGobbler = new StreamGobbler(
								p1.getErrorStream(), "ERROR");
						errorGobbler.start();
						StreamGobbler outGobbler = new StreamGobbler(
								p1.getInputStream(), "STDOUT");
						outGobbler.start();
						try {
							if (p1.waitFor() != 0) {
								log.error("create cmd1 returned abnormal value!");
							}
						} catch (InterruptedException e) {
							log.error("create cmd1 terminated", e);
						}
					} catch (IOException e) {
						// e.printStackTrace();
						log.error("create cmd1 error!", e);
					}
					cmd2 = "../tools/server_side/bin/vnc_proxy_ctl add -p "
							+ pwd + " -d " + ip + ":" + port
							+ " -s tmp/sockets/vnc_proxy.sock";
					try {
						p2 = Runtime.getRuntime().exec(cmd2);
						StreamGobbler errorGobbler = new StreamGobbler(
								p2.getErrorStream(), "ERROR");
						errorGobbler.start();
						StreamGobbler outGobbler = new StreamGobbler(
								p2.getInputStream(), "STDOUT");
						outGobbler.start();
						try {
							if (p2.waitFor() != 0) {
								log.error("create cmd2 returned abnormal value!");
							}
						} catch (InterruptedException e) {
							log.error("create cmd2 terminated", e);
						}
					} catch (IOException e) {
						e.printStackTrace();
						log.error("create cmd2 error!", e);
					}
				}
			}
		}
	}
}

package nova.test.worker;

import java.net.InetSocketAddress;

import nova.worker.NovaWorker;

import org.junit.Test;

/**
 * test vdisk pool functions
 * 
 * @author shayf
 * 
 */
public class TestVdiskPool {
	@Test
	public void test() {
		String workerHost = "127.0.0.1";
		int workerPort = 9285;
		InetSocketAddress workerAddr = new InetSocketAddress(workerHost,
				workerPort);
		NovaWorker.getInstance().bind(workerAddr);

		// StartVnodeHandler svh = new StartVnodeHandler();
		// StartVnodeMessage msg = new StartVnodeMessage(null);
		// msg.setHyperVisor("kvm");
		// msg.setName("vm");
		// msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
		// msg.setMemSize("524288");
		// msg.setCpuCount("1");
		// msg.setArch("i686");
		// msg.setCdImage("/media/data/ubuntu-10.04-desktop-i386.iso");
		// msg.setEmulatorPath("/usr/bin/kvm");
		// msg.setRunAgent("false");
		//
		// ChannelHandlerContext ctx = null;
		// MessageEvent e = null;
		// SimpleAddress xreply = null;
		// svh.handleMessage(msg, ctx, e, xreply);
		//
		// msg.setHyperVisor("KVM");
		// msg.setName("vm2");
		// msg.setUuid("1f7c794b-2e17-45ef-3c55-ece004e76aef");
		// msg.setMemSize("524288");
		// msg.setCpuCount("1");
		// msg.setArch("i686");
		// msg.setCdImage("");
		// msg.setEmulatorPath("/usr/bin/kvm");
		// msg.setRunAgent("false");
		//
		// ChannelHandlerContext ctx2 = null;
		// MessageEvent e2 = null;
		// SimpleAddress xreply2 = null;
		// svh.handleMessage(msg, ctx2, e2, xreply2);

		try {
			while (true) {
				Thread.sleep(10000);
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		// NovaWorker.getInstance().shutdown();

		// File src = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
		// "linux.img"));
		// long len = src.length();
		// for (int i = 1; i <= VdiskPoolDaemon.getPOOL_SIZE(); i++) {
		// File tmp = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
		// "vdiskpool", "linux.img.pool." + Integer.toString(i)));
		// Assert.assertTrue(tmp.exists());
		// Assert.assertTrue(tmp.length() == len);
		// }

	}
}

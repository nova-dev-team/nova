package nova.test.worker;

import java.io.File;
import java.io.IOException;

import nova.common.util.Utils;
import nova.worker.NovaWorker;

/**
 * test vdisk pool functions
 * 
 * @author shayf
 * 
 */
public class RunVdiskPoolDaemon {

	public static void main(String[] args) {
		NovaWorker.getInstance().start();

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
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		File newImg = new File(
				Utils.pathJoin(Utils.NOVA_HOME, "run", "del.img"));
		if (!newImg.exists()) {
			try {
				newImg.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		File revokeFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
				"vdiskpool", "del.img.revoke"));
		if (!revokeFile.exists()) {
			try {
				revokeFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		NovaWorker.getInstance().shutdown();
	}
}

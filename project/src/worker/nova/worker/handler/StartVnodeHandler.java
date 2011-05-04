package nova.worker.handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

/**
 * Handler for "start new vnode" request.
 * 
 * @author santa
 * 
 */
public class StartVnodeHandler implements
		SimpleHandler<StartVnodeHandler.Message> {

	/**
	 * Message for "start new vnode" request.
	 * 
	 * @author santa
	 * 
	 */
	public static class Message {

		public Message(SimpleAddress vAddr) {
			this.vAddr = vAddr;
		}

		/**
		 * Basic information required to start a new vnode.
		 */
		public SimpleAddress vAddr;

	}

	/**
	 * Handle "start new vnode" request.
	 */
	@Override
	public void handleMessage(Message msg, ChannelHandlerContext ctx,
			MessageEvent e, SimpleAddress xreply) {

		// TODO @shayf Add real handler for creating a new vnode
		// connect the qemu system
		Connect conn = null;
		try {
			conn = new Connect("qemu:///system", false);
		} catch (LibvirtException e1) {
			System.out.println("exception caught:" + e1);
			System.out.println(e1.getError());
		}

		// find conf file, currently using test-domain-template.xml
		BufferedReader br;
		String tmp = null;
		try {
			String filePath = Utils.pathJoin(Utils.NOVA_HOME, "conf", "virt",
					"test-domain-template.xml");
			br = new BufferedReader(new FileReader(filePath));

			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			tmp = sb.toString();
			br.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// create domain and show some info
		try {
			Domain testDomain = conn.domainCreateLinux(tmp, 0);
			System.out
					.println("Domain:" + testDomain.getName() + " id "
							+ testDomain.getID() + " running "
							+ testDomain.getOSType());
		} catch (LibvirtException e1) {
			System.out.println("exception caught:" + e1);
			System.out.println(e1.getError());
		}

	}
}

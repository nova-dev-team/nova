package nova.test.worker;

import java.io.IOException;

import nova.common.service.SimpleAddress;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.worker.NovaWorker;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.handler.StartVnodeHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

/**
 * test for add vnode, run in linux, qemu required, remember to kill the
 * vmachine after running
 * 
 * @author shayf
 * 
 */
public class TestStartVnode {
	@Test
	public void test() {
		StartVnodeHandler svh = new StartVnodeHandler();
		StartVnodeMessage msg = new StartVnodeMessage(null);
		msg.setName("vm");
		msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
		msg.setMemSize("524288");
		msg.setCpuCount("1");
		msg.setArch("i686");
		msg.setCdImage("");
		msg.setEmulatorPath("/usr/bin/kvm");
		msg.setRunAgent("false");
		Conf conf = null;
		try {
			conf = Utils.loadConf();
			// conf.setDefaultValue("vm_network_interface", "");
			// conf.setDefaultValue("vm_network_bridge", "");
			// conf.setDefaultValue("fix_vnc_mouse_pointer", "true");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		NovaWorker.getInstance().setConf(conf);

		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		svh.handleMessage(msg, ctx, e, xreply);
	}
}

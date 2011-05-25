package nova.master.handler;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.DeleteVnodeMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;
import nova.worker.api.WorkerProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class DeleteVnodeHandler implements SimpleHandler<DeleteVnodeMessage> {

	/**
	 * Log4j logger;
	 */
	Logger log = Logger.getLogger(DeleteVnodeMessage.class);

	@Override
	public void handleMessage(DeleteVnodeMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub
		Vnode vnode = Vnode.findById(msg.id);
		if (vnode != null) {
			WorkerProxy wp = new WorkerProxy(new SimpleAddress("", 3000));
			wp.connect(new InetSocketAddress(Pnode.findById(msg.id).getIp(),
					Pnode.findById(msg.id).getPort()));
			wp.sendStopVnode("kvm", vnode.getUuid());
			log.info("Delete vnode: " + vnode.getName());
			Vnode.delete(vnode);
		} else {
			log.info("Vnode @ id: " + String.valueOf(msg.id) + "not exists.");
		}
	}
}

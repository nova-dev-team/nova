package nova.master.handler;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.master.api.messages.DeleteVclusterMessage;
import nova.master.models.Pnode;
import nova.master.models.Vcluster;
import nova.master.models.Vnode;
import nova.worker.api.WorkerProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class DeleteVclusterHandler implements
		SimpleHandler<DeleteVclusterMessage> {

	/**
	 * Log4j logger;
	 */
	Logger log = Logger.getLogger(DeleteVclusterHandler.class);

	@Override
	public void handleMessage(DeleteVclusterMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub
		Vcluster vcluster = Vcluster.findById(msg.id);
		Vnode vnode = new Vnode();
		if (vcluster != null) {
			for (int i = 0; i < vcluster.getClusterSize(); i++) {
				vnode = Vnode.findByIp(IntegerToIpv4(Ipv4ToInteger(vcluster
						.getFristIp()) + i));
				if (vnode != null) {
					WorkerProxy wp = new WorkerProxy(new SimpleAddress(
							Conf.getString("master.bind_host"),
							Conf.getInteger("master.bind_port")));
					wp.connect(new InetSocketAddress(Pnode.findById(msg.id)
							.getIp(), Pnode.findById(msg.id).getPort()));
					wp.sendStopVnode("kvm", vnode.getUuid());
					log.info("Delete vnode: " + vnode.getName());
					Vnode.delete(vnode);
				} else {
					log.info("Vnode @ id: " + String.valueOf(msg.id)
							+ "not exists.");
				}
				log.info("Delete vcluster: " + vcluster.getClusterName());
				Vcluster.delete(vcluster);
			}
		} else {
			log.info("Vcluster @ id: " + String.valueOf(msg.id) + "not exists.");
		}

	}

	public int Ipv4ToInteger(String ipv4) {
		String[] params = ipv4.split("\\.");
		return ((Integer.parseInt(params[0]) * 256 + Integer
				.parseInt(params[1])) * 256 + Integer.parseInt(params[2]))
				* 256 + Integer.parseInt(params[3]);
	}

	public String IntegerToIpv4(int intIp) {
		int[] params = new int[4];
		for (int i = 3; i > -1; i--) {
			params[i] = intIp % 256;
			intIp = intIp / 256;
			;
		}
		return String.valueOf(params[0]) + "." + String.valueOf(params[1])
				+ "." + String.valueOf(params[2]) + "."
				+ String.valueOf(params[3]);
	}

}

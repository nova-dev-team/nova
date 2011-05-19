package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.CreateVnodeMessage;
import nova.master.models.Pnode;
import nova.master.models.Vcluster;
import nova.master.models.Vnode;
import nova.worker.api.WorkerProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class CreateVnodeHandler implements SimpleHandler<CreateVnodeMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(CreateVnodeMessage.class);

	@Override
	public void handleMessage(CreateVnodeMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub
		Vcluster vcluster = new Vcluster();
		for (Vcluster vc : Vcluster.all()) {
			vcluster = vc;
		}
		SimpleAddress vAddr = new SimpleAddress(vcluster.getFristIp(), 4000);

		int pid = 1;

		Vnode vnode = new Vnode();
		vnode.setAddr(vAddr);
		vnode.setPmachineId(pid);
		vnode.setName(msg.vmName);
		vnode.setCpuCount(msg.cpuCount);
		vnode.setMemorySize(msg.memorySize);
		vnode.setSoftList(msg.applianceList);
		vnode.setCdrom(msg.vmImage);
		vnode.setStatus(Vnode.Status.PREPARING);
		vnode.save();
		log.info("Created new vnode: " + vnode.getIp());

		Pnode pnode = Pnode.findById(pid);
		WorkerProxy wp = new WorkerProxy(pnode.getAddr());
		wp.sendStartVnode("kvm", vAddr, "true", String.valueOf(msg.memorySize),
				String.valueOf(msg.cpuCount), msg.vmImage, "false");

	}
}

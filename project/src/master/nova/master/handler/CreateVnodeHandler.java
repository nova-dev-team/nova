package nova.master.handler;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
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
		System.out.println(pnode.getIp());
		WorkerProxy wp = new WorkerProxy(new SimpleAddress(
				Conf.getString("master.bind_host"),
				Conf.getInteger("master.bind_port")));

		// @ zhaoxun to do...
		wp.connect(new InetSocketAddress(Conf.getString("worker.bind_host"),
				Conf.getInteger("worker.bind_port")));
		/*
		 * System.out.println("kvm" + vAddr + "true" +
		 * String.valueOf(msg.memorySize) + String.valueOf(msg.cpuCount) +
		 * msg.vmImage + "false");
		 */
		wp.sendStartVnode("kvm", msg.vmName, vAddr, "false",
				String.valueOf(msg.memorySize), String.valueOf(msg.cpuCount),
				msg.vmImage, "false");

		/**
		 * ArrayList<Pair<String, String>> appList = new ArrayList<Pair<String,
		 * String>>(); Pair<String, String> pair = new Pair<String, String>();
		 * for (Appliance appliance : Appliance.all()) {
		 * pair.setFirst(appliance.getDisplayName());
		 * pair.setSecond(appliance.getDescription()); appList.add(pair); }
		 * 
		 * @SuppressWarnings("unchecked") Pair<String, String>[] apps = new
		 *                                Pair[appList.size()]; for (int i = 0;
		 *                                i < appList.size(); i++) { apps[i] =
		 *                                appList.get(i); }
		 * 
		 *                                AgentProxy ap = new AgentProxy(new
		 *                                SimpleAddress(
		 *                                Conf.getString("master.bind_host"),
		 *                                Conf.getInteger("master.bind_port")));
		 *                                ap.connect(new
		 *                                InetSocketAddress(vAddr.ip, Conf
		 *                                .getInteger("agent.bind_port")));
		 *                                ap.sendApplianceList(apps);
		 */
	}
}

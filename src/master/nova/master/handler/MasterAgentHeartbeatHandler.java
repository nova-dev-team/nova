package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.AgentHeartbeatMessage;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterAgentHeartbeatHandler implements
        SimpleHandler<AgentHeartbeatMessage> {

    /**
     * Log4j logger;
     */
    Logger log = Logger.getLogger(MasterAgentHeartbeatHandler.class);

    @Override
    public void handleMessage(AgentHeartbeatMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        System.out
                .println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        // TODO Auto-generated method stub
        if (xreply == null) {
            log.warn("Got an agent heartbeat message, but the reply address is null!");
        } else {
            log.info("Got agent heartbeat message from: " + xreply);
        }

        // TODO @zhaoxun possibly update vnode.agentStatus
        // String oldStatus = null;
        // String newStatus = null;
        Vnode vnode = Vnode.findByIp(xreply.ip);
        if (vnode != null) {
            // oldStatus = vnode.getAgentStatus();
            vnode.setAgentStatus("on");
            // newStatus = vnode.getAgentStatus();
            log.info("Update status of agent @ " + vnode.getAddr() + " to "
                    + vnode.getAgentStatus());
            vnode.save();
        } else {
            log.error("Vnode with host " + xreply.ip + " not found!");
        }

        // if ((!oldStatus.equals("on")) && newStatus.equals("on")) {
        // ArrayList<Pair<String, String>> appList = new ArrayList<Pair<String,
        // String>>();
        // Pair<String, String> pair = new Pair<String, String>();
        // for (Appliance appliance : Appliance.all()) {
        // pair.setFirst(appliance.getDisplayName());
        // pair.setSecond(appliance.getDescription());
        // appList.add(pair);
        // }
        // @SuppressWarnings("unchecked")
        // Pair<String, String>[] apps = new Pair[appList.size()];
        // for (int i = 0; i < appList.size(); i++) {
        // apps[i] = appList.get(i);
        // }
        //
        // AgentProxy ap = new AgentProxy(new SimpleAddress(
        // Conf.getString("master.bind_host"),
        // Conf.getInteger("master.bind_port")));
        // ap.connect(new InetSocketAddress(xreply.ip, Conf
        // .getInteger("agent.bind_port")));
        // ap.sendApplianceList(apps);
        //
        // }
    }

}

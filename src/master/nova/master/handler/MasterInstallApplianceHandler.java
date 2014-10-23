package nova.master.handler;

import java.net.InetSocketAddress;

import nova.agent.api.AgentProxy;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.master.api.messages.MasterInstallApplianceMessage;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterInstallApplianceHandler implements
        SimpleHandler<MasterInstallApplianceMessage> {

    /**
     * Log4j logger;
     */
    Logger log = Logger.getLogger(MasterInstallApplianceHandler.class);

    @Override
    public void handleMessage(MasterInstallApplianceMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub

        AgentProxy ap = new AgentProxy(new SimpleAddress(
                Conf.getString("master.bind_host"),
                Conf.getInteger("master.bind_port")));
        ap.connect(new InetSocketAddress(Vnode.findById(msg.aid).getIp(), Conf
                .getInteger("agent.bind_port")));
        ap.sendInstallAppliance(msg.appNames);

    }

}

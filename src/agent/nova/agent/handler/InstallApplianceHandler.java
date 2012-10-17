package nova.agent.handler;

import java.util.concurrent.ConcurrentHashMap;

import nova.agent.NovaAgent;
import nova.agent.api.messages.InstallApplianceMessage;
import nova.agent.appliance.Appliance;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Get list of softwares will be download and install
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class InstallApplianceHandler implements
        SimpleHandler<InstallApplianceMessage> {

    @Override
    public void handleMessage(InstallApplianceMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

        ConcurrentHashMap<String, Appliance> appliances = NovaAgent
                .getInstance().getAppliances();

        // if appliance is new for agent, then put it into the appliances list
        // and change the status of this appliance to DOWNLOAD_PENDING
        for (String appName : msg.getAppNames()) {
            if (appliances.containsKey(appName) == false) {
                Appliance app = new Appliance(appName);
                appliances.put(appName, app);
            }
            Appliance app = appliances.get(appName);
            if (app.getStatus().equals(Appliance.Status.NOT_INSTALLED)) {
                app.setStatus(Appliance.Status.DOWNLOAD_PENDING);
            }
        }
        // save new appliances status
        NovaAgent.getInstance().saveAppliances();

    }
}

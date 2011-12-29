package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.AppliancesFirstInstalledMessage;
import nova.test.functional.agent.experiment.TimeInfo;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class ApplicancesFirstInstalledHandler implements
        SimpleHandler<AppliancesFirstInstalledMessage> {
    Logger logger = Logger.getLogger(AppliancesFirstInstalledMessage.class);

    @Override
    public void handleMessage(AppliancesFirstInstalledMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

        // TODO delete experiment codes between //////////////////// and
        // //////////////
        // //////////////////////////////////////////////////////////////////////////
        TimeInfo.setDeployTime(msg.ipAddress, System.currentTimeMillis());
        TimeInfo.setTotalTime(msg.ipAddress, System.currentTimeMillis());
        TimeInfo.print();
        // ////////////////////////////////////////////////////////////////////////////
        System.err.println(msg.ipAddress + ": applicances first installed!");
    }
}

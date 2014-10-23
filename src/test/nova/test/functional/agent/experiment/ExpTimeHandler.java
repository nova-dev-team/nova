package nova.test.functional.agent.experiment;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Set experiment time consumption got from workers
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ExpTimeHandler implements SimpleHandler<ExpTimeMessage> {

    @Override
    public void handleMessage(ExpTimeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        TimeInfo.setTime(msg.ip, msg.timeType, msg.currentTime);
    }

}

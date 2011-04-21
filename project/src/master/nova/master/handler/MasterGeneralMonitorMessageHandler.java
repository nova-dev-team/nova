package nova.master.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.tools.perf.GeneralMonitorInfo;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterGeneralMonitorMessageHandler implements
		ISimpleHandler<GeneralMonitorMessage> {

	Logger logger = Logger.getLogger(GeneralMonitorInfo.class);

	@Override
	public void handleMessage(GeneralMonitorMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO @zhaoxun Auto-generated method stub

		logger.info("Got GeneralMonitorInfo from " + xreply);
	}

}

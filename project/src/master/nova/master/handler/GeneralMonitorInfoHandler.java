package nova.master.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.SimpleAddress;
import nova.common.tools.perf.GeneralMonitorInfo;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class GeneralMonitorInfoHandler implements
		ISimpleHandler<GeneralMonitorInfo> {

	Logger logger = Logger.getLogger(GeneralMonitorInfo.class);

	@Override
	public void handleMessage(GeneralMonitorInfo msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub

		logger.info("Got GeneralMonitorInfo from " + xreply);
	}

}

package nova.master.handler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

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
		// TODO @zhaoxun Auto-generated method stub

		logger.info("Got GeneralMonitorInfo from " + xreply);

		try {
			FileOutputStream out = new FileOutputStream(
					"GeneralMonitorInfo.txt");
			PrintStream p = new PrintStream(out);
			p.println(msg.toString());

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}

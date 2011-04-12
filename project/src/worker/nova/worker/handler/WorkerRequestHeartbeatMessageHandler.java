package nova.worker.handler;

import nova.common.service.ISimpleHandler;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.worker.NovaWorker;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class WorkerRequestHeartbeatMessageHandler implements
		ISimpleHandler<RequestHeartbeatMessage> {

	@Override
	public void handleMessage(RequestHeartbeatMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, String xfrom) {

		// TODO @shayf sendback heartbeat immediately

		if (NovaWorker.getInstance().getMaster() == null) {
			NovaWorker.getInstance().registerMaster(xfrom);
		}
	}

}

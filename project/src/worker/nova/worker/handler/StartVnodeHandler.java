package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Handler for "start new vnode" request.
 * 
 * @author santa
 * 
 */
public class StartVnodeHandler implements
		SimpleHandler<StartVnodeHandler.Message> {

	/**
	 * Message for "start new vnode" request.
	 * 
	 * @author santa
	 * 
	 */
	public static class Message {

		public Message(SimpleAddress vAddr) {
			this.vAddr = vAddr;
		}

		/**
		 * Basic information required to start a new vnode.
		 */
		public SimpleAddress vAddr;

	}

	/**
	 * Handle "start new vnode" request.
	 */
	@Override
	public void handleMessage(Message msg, ChannelHandlerContext ctx,
			MessageEvent e, SimpleAddress xreply) {

		// TODO @shayf Add real handler for creating a new vnode

	}

}

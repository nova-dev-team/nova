package nova.worker.handler;

import nova.common.service.ISimpleHandler;
import nova.master.models.VnodeBasic;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * Handler for "start new vnode" request.
 * 
 * @author santa
 * 
 */
public class StartVnodeHandler implements
		ISimpleHandler<StartVnodeHandler.Message> {

	/**
	 * Message for "start new vnode" request.
	 * 
	 * @author santa
	 * 
	 */
	public static class Message {

		public Message(VnodeBasic vnodeInfo) {
			this.vnodeInfo = vnodeInfo;
		}

		/**
		 * Basic information required to start a new vnode.
		 */
		public VnodeBasic vnodeInfo;

	}

	/**
	 * Handle "start new vnode" request.
	 */
	@Override
	public void handleMessage(Message msg, ChannelHandlerContext ctx,
			MessageEvent e, String xfrom) {
		// TODO @santa Add dummy example here

		// TODO @shayf Add real handler for creating a new vnode

	}

}

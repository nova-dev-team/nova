package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.MasterMigrateCompleteMessage;
import nova.master.models.Migration;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class MasterMigrateCompleteHandler implements
		SimpleHandler<MasterMigrateCompleteMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(MasterMigrateCompleteMessage.class);

	@Override
	public void handleMessage(MasterMigrateCompleteMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub

		Vnode vnode = Vnode.findByUuid(msg.migrateUuid);
		Migration migration = Migration.findVnodeId(vnode.getId());
		Migration.delete(migration);
		// ???
		vnode.setStatus(Vnode.Status.RUNNING);
		vnode.save();
	}

}

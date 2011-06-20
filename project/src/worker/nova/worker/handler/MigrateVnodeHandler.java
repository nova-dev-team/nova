package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.worker.api.messages.MigrateVnodeMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class MigrateVnodeHandler implements SimpleHandler<MigrateVnodeMessage> {

	/**
	 * band width during migration
	 */
	private long bandWidth;

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(MigrateVnodeHandler.class);

	@Override
	public void handleMessage(MigrateVnodeMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO @shayf finish migration
		Connect conn = null, dconn = null;
		try {
			conn = new Connect("qemu:///system", true);
			dconn = new Connect("qemu://otherPnode/system", true);
			Domain srcDomain = conn.domainLookupByID(Integer
					.parseInt(msg.vnodeId));
			long flag = 0;
			String uri = null;
			srcDomain.migrate(dconn, flag, srcDomain.getName(), uri, bandWidth);
		} catch (LibvirtException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}

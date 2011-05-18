package nova.master.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.CreateVclusterMessage;
import nova.master.models.Vcluster;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class CreateVclusterHandler implements
		SimpleHandler<CreateVclusterMessage> {
	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(CreateVclusterMessage.class);

	@Override
	public void handleMessage(CreateVclusterMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub
		// to modify

		List<Integer> usedIpSegments = new ArrayList<Integer>();
		int gatewayIpIval = Ipv4ToInteger("10.0.2.255");
		usedIpSegments.add(gatewayIpIval);
		usedIpSegments.add(gatewayIpIval);

		for (Vcluster vcluster : Vcluster.all()) {
			usedIpSegments.add(Ipv4ToInteger(vcluster.getFristIp()));
			usedIpSegments.add(Ipv4ToInteger(vcluster.getFristIp())
					+ vcluster.getClusterSize() - 1);
		}
		Collections.sort(usedIpSegments);

		int firstUsableIpIval = Ipv4ToInteger("10.0.2.0");
		int lastUsableIpIval = Ipv4ToInteger("10.0.2.254");

		int testIpIval = firstUsableIpIval;
		while (testIpIval + msg.vclusterSize - 1 < lastUsableIpIval) {
			int testLastIpIval = testIpIval + msg.vclusterSize - 1;
			boolean usable = true;
			for (int i = 0; i < usedIpSegments.size(); i = i + 2) {
				if (testLastIpIval < usedIpSegments.get(i)
						|| testIpIval > usedIpSegments.get(i + 1)) {
					usable = true;
				} else {
					usable = false;
					break;
				}
			}

			if (usable) {
				break;
			} else {
				for (int i = 0; i < usedIpSegments.size(); i = i + 2) {
					if (usedIpSegments.get(i + 1) + 1 > testIpIval) {
						testIpIval = usedIpSegments.get(i + 1) + 1;
						break;
					}
				}
			}
		}

		if (testIpIval + msg.vclusterSize - 1 >= lastUsableIpIval) {
			log.info("There is not enough Ip address available for VMs.");
		}

		Vcluster vcluster = new Vcluster();
		vcluster.setClusterName(msg.vclusterName);
		vcluster.setClusterSize(msg.vclusterSize);
		vcluster.setFristIp(IntegerToIpv4(testIpIval));
		vcluster.save();
	}

	public int Ipv4ToInteger(String ipv4) {
		String params[] = ipv4.split(".");
		return ((Integer.parseInt(params[0]) * 256 + Integer
				.parseInt(params[1])) * 256 + Integer.parseInt(params[2]))
				* 256 + Integer.parseInt(params[3]);
	}

	public String IntegerToIpv4(int intIp) {
		int params[] = new int[4];
		for (int i = 3; i > -1; i--) {
			params[i] = intIp % 256;
			intIp = intIp / 256;
			;
		}
		return String.valueOf(params[0]) + "." + String.valueOf(params[1])
				+ "." + String.valueOf(params[2]) + "."
				+ String.valueOf(params[3]);
	}

}

package nova.master.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.master.api.messages.CreateVclusterMessage;
import nova.master.models.Vcluster;
import nova.test.functional.agent.experiment.TimeInfo;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 * An class for store time consumption results in an creation process of virtual
 * cluster
 * 
 * @author gaotao@gmail.com
 * 
 */
public class CreateVclusterHandler implements
        SimpleHandler<CreateVclusterMessage> {
    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(CreateVclusterMessage.class);

    public int vclusterid = 0;

    @Override
    public void handleMessage(CreateVclusterMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        // to modify

        List<Long> usedIpSegments = new ArrayList<Long>();
        long gatewayIpIval = Utils.ipv4ToInteger(Conf
                .getString("vnode.gateway_ip"));
        usedIpSegments.add(gatewayIpIval);
        usedIpSegments.add(gatewayIpIval);

        for (Vcluster vcluster : Vcluster.all()) {
            usedIpSegments.add(Utils.ipv4ToInteger(vcluster.getFristIp()));
            usedIpSegments.add(Utils.ipv4ToInteger(vcluster.getFristIp())
                    + vcluster.getClusterSize() - 1);
        }
        Collections.sort(usedIpSegments);

        long firstUsableIpIval = Utils.ipv4ToInteger(Conf
                .getString("vnode.first_usable_ip"));
        long lastUsableIpIval = Utils.ipv4ToInteger(Conf
                .getString("vnode.last_usable_ip"));

        long testIpIval = firstUsableIpIval;

        while (testIpIval + msg.vclusterSize - 1 < lastUsableIpIval) {
            long testLastIpIval = testIpIval + msg.vclusterSize - 1;
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

        testIpIval = usedIpSegments.get(usedIpSegments.size() - 1) + 1;
        Vcluster vcluster = new Vcluster();
        vcluster.setClusterName(msg.vclusterName);
        vcluster.setClusterSize(msg.vclusterSize);
        vcluster.setFristIp(Utils.integerToIpv4(testIpIval));
        vcluster.setFristName(msg.firstname);
        vcluster.setOsUsername(msg.firstname);
        vcluster.setUserId(msg.userId);
        vcluster.save();
        // TODO delete these experiments codes when necessary
        // /////////////////////////////////////////////////////////
        // Initialize vClusterRange of TimeInfo
        TimeInfo.setVClusterInfo(vcluster.getClusterName(),
                vcluster.getFristIp(), vcluster.getClusterSize());
        // //////////////////////////////////////////////////////////
    }
}

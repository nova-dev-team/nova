package nova.master.handler;

import java.io.File;
import java.io.IOException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.service.message.PnodePerfMessage;
import nova.common.util.RRDTools;
import nova.master.models.Pnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;

/**
 * Save monitor information into RRD. Some thing todo by zhaoxun
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class MasterPnodePerfHandler implements SimpleHandler<PnodePerfMessage> {

    Logger logger = Logger.getLogger(MasterPnodePerfHandler.class);

    @Override
    public void handleMessage(PnodePerfMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        // TODO @zhaoxun get pair of uuid/rrdPath from database

        Pnode pnode = Pnode.findByIp(xreply.ip);
        if (pnode != null) {
            String rrdPath = "build/" + pnode.getId() + ".rrd";

            int timeInterval = 5;
            int rrdLength = 5000;

            File file = new File(rrdPath);
            if (file.exists() == false) {
                RRDTools.CreateMonitorInfoRRD(rrdPath, timeInterval, rrdLength);
                logger.info(xreply.ip + ": RRD file is created!");
            }
            try {
                RrdDb rrdDb = new RrdDb(rrdPath);
                RRDTools.addMonitorInfoInRRD(rrdDb,
                        msg.getGeneralMonitorInfo(), Util.getTime());
                rrdDb.close();
            } catch (IOException ex) {
                logger.error("Error updating RRD", ex);
            } catch (RrdException ex) {
                logger.error("Error updating RRD", ex);
            }

            logger.info("Got GeneralMonitorInfo from " + xreply);
        }
    }

}

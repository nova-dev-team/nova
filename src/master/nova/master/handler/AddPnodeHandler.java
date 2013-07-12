package nova.master.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.models.Pnode;
import nova.worker.models.StreamGobbler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class AddPnodeHandler implements SimpleHandler<AddPnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(AddPnodeMessage.class);

    @Override
    public void handleMessage(AddPnodeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        System.out.println("333");
        Pnode pnode = Pnode.findByIp(msg.pAddr.getIp());
        if (pnode == null) {
            // new pnode
            pnode = new Pnode();
            pnode.setAddr(msg.pAddr);
            pnode.setVmCapacity(msg.vmCapacity);
            pnode.setStatus(Pnode.Status.ADD_PENDING);

            pnode.setHostname(msg.pAddr.getInetSocketAddress().getHostName());
            /*
             * byte[] mac = null; try { mac = NetworkInterface.getByInetAddress(
             * msg.pAddr.getInetAddress()).getHardwareAddress(); } catch
             * (SocketException e1) { // TODO Auto-generated catch block
             * e1.printStackTrace(); } StringBuffer sb = new StringBuffer(); for
             * (int i = 0; i < mac.length; i++) { if (i != 0) sb.append("-");
             * String s = Integer.toHexString(mac[i] & 0xFF);
             * sb.append(s.length() == 1 ? 0 + s : s);
             * 
             * }
             * 
             * pnode.setMacAddress(sb.toString().toUpperCase());
             */
            getPnodeInfo(msg.pAddr, pnode);
            pnode.save();
            log.info("Added new pnode: " + pnode.getAddr());
        } else {
            // pnode exists
            if (pnode.getStatus().equals(Pnode.Status.RUNNING)) {
                // already working
                log.info("Pnode @" + pnode.getAddr() + " already RUNNING");
            } else {
                pnode.setStatus(Pnode.Status.ADD_PENDING);
                pnode.save();
                log.info("Pnode @" + pnode.getAddr()
                        + " already added, set to ADD_PENDING");
            }

        }
    }

    private void getPnodeInfo(SimpleAddress addr, final Pnode pnode) {
        String strcmd = "ping " + addr.ip + " -c 1";
        try {
            Process p = Runtime.getRuntime().exec(strcmd);
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
                    "ERROR");
            errorGobbler.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
                    "STDOUT");
            outGobbler.start();
            try {
                if (p.waitFor() != 0) {
                    log.error("ping " + addr.ip + " returned abnormal value!");
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                log.error("ping " + addr.ip + " process terminated!", e);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.error("ping " + addr.ip + " cmd error!", e);
        }
        strcmd = "arp " + addr.ip;
        try {

            Process p = Runtime.getRuntime().exec(strcmd);
            final InputStream is = p.getInputStream();

            Thread getmac = new Thread(new Runnable() {
                public void run() {
                    String line, result = "-1";
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    try {
                        while ((line = br.readLine()) != null) {

                            result = line;
                        }
                        if (result.compareToIgnoreCase("-1") != 0) {
                            String strmac = result.split("[\\t \\n]+")[2];
                            pnode.setMacAddress(strmac);
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            getmac.start();

            try {
                if (p.waitFor() != 0) {
                    log.error("get mac address:" + addr.ip
                            + " return abnormal value!");
                }
                try {
                    getmac.join();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                log.error("get mac address:" + addr.ip + " terminated!", e);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.error("get mac address:" + addr.ip + " cmd error!", e);

        }
    }
}

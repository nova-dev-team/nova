package nova.worker.handler;

import java.io.IOException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;
import nova.worker.api.messages.ObtainSshKeysMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import sun.net.ftp.FtpClient;

public class ObtainSshKeysHandler implements
        SimpleHandler<ObtainSshKeysMessage> {
    Logger logger = Logger.getLogger(ObtainSshKeysHandler.class);

    @Override
    public void handleMessage(ObtainSshKeysMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        String hostIp = Conf.getString("storage.ftp.bind_host");
        int ftpPort = Conf.getInteger("storage.ftp.bind_port");
        String userName = Conf.getString("storage.ftp.admin.username");
        String password = Conf.getString("storage.ftp.admin.password");
        String savePath = Utils.pathJoin(Utils.NOVA_HOME, "run", "softwares");
        try {
            FtpClient fc = FtpUtils
                    .connect(hostIp, ftpPort, userName, password);
            FtpUtils.downloadDir(fc,
                    Utils.pathJoin("ssh_keys", msg.vClusterName, msg.vmName),
                    Utils.pathJoin(savePath, "sshkeys", msg.vmName));
            System.out.println(Utils.pathJoin("ssh_keys", msg.vClusterName,
                    msg.vmName)
                    + "-> "
                    + Utils.pathJoin(savePath, "sshkeys", msg.vmName));
            FtpUtils.downloadFile(fc, "/ssh_keys/" + msg.vClusterName
                    + "/authorized_keys", Utils.pathJoin(savePath, "sshkeys",
                    msg.vmName, "authorized_keys"));
            System.out.println(msg.vClusterName + " -> " + msg.vmName);
            logger.info("Have downloaded ssh keys pairs from server!");
        } catch (IOException e1) {
            logger.error("Downloading pictures fail: ", e1);
        }
    }
}

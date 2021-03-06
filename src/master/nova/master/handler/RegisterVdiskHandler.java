package nova.master.handler;

import java.io.File;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.master.api.messages.RegisterVdiskMessage;
import nova.master.models.Vdisk;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class RegisterVdiskHandler implements
        SimpleHandler<RegisterVdiskMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(RegisterVdiskHandler.class);

    @Override
    public void handleMessage(RegisterVdiskMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        Vdisk vdisk = Vdisk.findByFileName(msg.fileName);
        if (vdisk == null) {
            // new vdisk
            vdisk = new Vdisk();
            vdisk.setDisplayName(msg.displayName);
            vdisk.setFileName(msg.fileName);
            vdisk.setDiskFormat(msg.imageType);
            vdisk.setOsFamily(msg.osFamily);
            vdisk.setOsName(msg.osName);
            vdisk.setDescription(msg.description);
            vdisk.save();
            log.info("Registered new vdisk: " + vdisk.getFileName());

            String strdstfolder = Utils.pathJoin(Utils.NOVA_HOME, "run", "run");
            String strdstimg = strdstfolder + msg.fileName;
            // todo:eagle copy disk
            String srcimg = Utils.pathJoin(Utils.NOVA_HOME, "data/ftp_home",
                    msg.fileName);
            File dstFile = new File(strdstimg);
            File srcFile = new File(srcimg);
            if (dstFile.isFile() && dstFile.exists() == false
                    && srcFile.exists()) {
                Utils.copyOneFile(srcimg, strdstimg);
            }
        } else {
            // the vdisk exists
            log.info("Vdisk @" + vdisk.getFileName() + " already registered");
        }

    }
}

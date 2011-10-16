package nova.test.functional.worker;

import java.io.File;
import java.io.IOException;

import nova.common.util.Utils;
import nova.worker.NovaWorker;

/**
 * test vdisk pool functions
 * 
 * @author shayf
 * 
 */
public class RunVdiskPoolDaemon {

    public static void main(String[] args) {
        NovaWorker.getInstance().start();

        // StartVnodeHandler svh = new StartVnodeHandler();
        // StartVnodeMessage msg = new StartVnodeMessage("kvm", null, "false",
        // null);
        // msg.setName("vm");
        // msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
        // msg.setHdaImage("small.img");
        // msg.setCdImage("");
        // msg.setRunAgent("false");
        //
        // ChannelHandlerContext ctx = null;
        // MessageEvent e = null;
        // SimpleAddress xreply = null;
        // svh.handleMessage(msg, ctx, e, xreply);
        //
        // StartVnodeMessage msg2 = new StartVnodeMessage("KVM", null, "false",
        // "524288", "1", "small.img", "false");
        // msg2.setName("vm2");
        // msg2.setUuid("1f7c794b-2e17-45ef-3c55-ece004e76aef");
        // msg2.setCdImage("");
        //
        // ChannelHandlerContext ctx2 = null;
        // MessageEvent e2 = null;
        // SimpleAddress xreply2 = null;
        // svh.handleMessage(msg2, ctx2, e2, xreply2);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        File newImg = new File(
                Utils.pathJoin(Utils.NOVA_HOME, "run", "del.img"));
        if (!newImg.exists()) {
            try {
                newImg.createNewFile();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        File revokeFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                "vdiskpool", "del.img.revoke"));
        if (!revokeFile.exists()) {
            try {
                revokeFile.createNewFile();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        NovaWorker.getInstance().shutdown();
    }
}

package nova.master.daemons;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import nova.common.util.RRDTools;
import nova.common.util.SimpleDaemon;
import nova.master.api.messages.CreateVnodeMessage;
import nova.master.api.messages.MasterMigrateVnodeMessage;
import nova.master.handler.ChooseBestPnodeHandler;
import nova.master.handler.CreateVnodeHandler;
import nova.master.handler.MasterMigrateVnodeHandler;
import nova.master.models.Pnode;
import nova.master.models.Vnode;

/**
 * Deamon to deal with load balance.
 * 
 * @author Herbert
 * 
 */

public class AutoManagerDaemon extends SimpleDaemon {

    /**
     * Log4j logger.
     */

    Logger logger = Logger.getLogger(AutoManagerDaemon.class);

    public AutoManagerDaemon() {
        super(2000);
    }

    /**
     * do work, one round
     */

    @Override
    protected void workOneRound() {

        double[][] vmonitor_data = null;
        int ismigrate = 0;
        for (Vnode vnd : Vnode.all()) {
            vmonitor_data = RRDTools.getVnodeMonitorInfo(vnd.getUuid());
            // System.out.println("Coming2" + vnd.getUuid());
            logger.info("Coming to " + vnd.getUuid());
            double vndcpu;
            if (vmonitor_data == null) {
                // System.out.println("No file");
                logger.info("No file! ");
                continue;
            }

            vndcpu = vmonitor_data[vmonitor_data.length - 1][0];
            if (vndcpu > 40) {
                ismigrate = 1;
                // System.out.println("Migrate, I am very happy!!!");
                logger.info("Migrate. I'm very happy! ");
            }
            // System.out.println("vndcpu info " + vndcpu);
            logger.info("vndcpu info " + vndcpu);
            if (vndcpu > 70) {
                // Select a pnode
                ChooseBestPnodeHandler handler = new ChooseBestPnodeHandler();
                handler.handleMessage(null, null, null, null);
                if (handler.pnodeid != -1) {
                    String pnode_id = String.valueOf(handler.pnodeid);
                    String uuname = UUID.randomUUID().toString();
                    new CreateVnodeHandler()
                            .handleMessage(
                                    new CreateVnodeMessage("ubuntu12.046.qcow2",
                                            uuname, Integer.parseInt("2"),
                                            Integer.parseInt("512000"), null,
                                            Integer.parseInt(pnode_id), 0, null,
                                            true, "kvm", 0, 0),
                                    null, null, null);
                    try {
                        sleep(120000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {

                    // String create_instance_error
                    // ="alert('No available Physical Machine Found!')";
                }

            }
        }
        if (ismigrate == 0) {
            // choose the right pmathine
            // migrate the vms

            ChooseBestPnodeHandler handler = new ChooseBestPnodeHandler();
            handler.handleMessage(null, null, null, null);
            long migrate_nodeid = handler.pnodeid;
            List<Pnode> allnodes = Pnode.all();
            Pnode migrate_node = null;
            for (Pnode node : allnodes) {
                if (node.getStatus() == Pnode.Status.RUNNING) {
                    if (node.getId() == migrate_nodeid) {
                        migrate_node = node;
                        break;
                    }
                }
            }
            // System.out.println("Migrate_nodeid= " + migrate_nodeid);
            logger.info("Migrate_nodeid= " + migrate_nodeid);
            while (migrate_node.getCurrentVMNum() > 0) {
                long vnode_id = -1;

                for (Pnode node : allnodes) {
                    if (node.getStatus() == Pnode.Status.RUNNING) {
                        if (node.getId() != migrate_nodeid) {
                            List<Vnode> vms = Vnode.all();

                            for (Vnode vnode : vms) {
                                if (vnode.getPmachineId() == migrate_nodeid) {
                                    vnode_id = vnode.getId();
                                    break;
                                }
                            }
                            // System.out.println("Vnode_id= " + vnode_id
                            // + " to id= " + node.getId());
                            logger.info("Vnode_id= " + vnode_id + " to id= "
                                    + node.getId());
                            try {
                                sleep(30000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            new MasterMigrateVnodeHandler().handleMessage(
                                    new MasterMigrateVnodeMessage(vnode_id,
                                            migrate_nodeid, node.getId()),
                                    null, null, null);
                            try {
                                sleep(12000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if (migrate_node.getCurrentVMNum() == 0)
                                break;

                        }
                    }
                }

            }
            try {
                sleep(120000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}

package nova.master.daemons;

import java.util.List;

import nova.common.util.SimpleDaemon;
import nova.master.models.Vcluster;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;

public class RemoveEmptyVClusterDaemon extends SimpleDaemon {

    Logger logger = Logger.getLogger(AutoManagerDaemon.class);

    public RemoveEmptyVClusterDaemon() {
        super(5000);
    }

    @Override
    protected void workOneRound() {
        // TODO Auto-generated method stub
        List<Vcluster> allvcls = Vcluster.all();
        for (Vcluster vcl : allvcls) {
            int size = 0;

            // modified by Herb i<vcl.getClusterSize();
            for (int i = 0; i < 8; i++) {
                String name = vcl.getOsUsername();
                logger.info("name:  " + name);
                if (i != 0) {
                    String name2 = name.substring(0, name.length() - 1);
                    StringBuffer buf = new StringBuffer();
                    buf.append("" + name2 + i);
                    name2 = buf.toString();
                    if (name.equals(name2))
                        continue;
                    else
                        name = name2;
                    // logger.info("name:  " + name);

                }
                Vnode vnode = Vnode.findByName(name);
                if (vnode != null) {
                    size++;
                }
            }
            if (size == 0) {
                Vcluster.delete(vcl);
            } else {
                vcl.setClusterSize(size);
                vcl.save();
            }
        }
    }
}

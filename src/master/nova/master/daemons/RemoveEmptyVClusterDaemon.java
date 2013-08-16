package nova.master.daemons;

import java.util.List;

import nova.common.util.SimpleDaemon;
import nova.common.util.Utils;
import nova.master.models.Vcluster;
import nova.master.models.Vnode;

public class RemoveEmptyVClusterDaemon extends SimpleDaemon {

    public RemoveEmptyVClusterDaemon() {
        super(5000);
    }

    @Override
    protected void workOneRound() {
        // TODO Auto-generated method stub
        List<Vcluster> allvcls = Vcluster.all();
        for (Vcluster vcl : allvcls) {
            int size = 0;
            for (int i = 0; i < vcl.getClusterSize(); i++) {
                Vnode vnode = Vnode.findByIp(Utils.integerToIpv4(Utils
                        .ipv4ToInteger(vcl.getFristIp()) + i));
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

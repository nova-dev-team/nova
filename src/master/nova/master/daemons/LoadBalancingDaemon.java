package nova.master.daemons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import nova.common.util.RRDTools;
import nova.common.util.SimpleDaemon;
import nova.master.models.Pnode;
import nova.master.models.Vnode;

/**
 * a simple vm scheduling algorithm. load balancing.
 * 
 * @author Tianyu Chen
 */
public class LoadBalancingDaemon extends SimpleDaemon {

    /**
     * Logger
     */
    Logger logger = Logger.getLogger(AutoManagerDaemon.class);

    /**
     * the spot value threshold. value that is larger will be considered a
     * "hot spot".
     */
    double spotThreshold;

    public LoadBalancingDaemon(double setSpotThreshold) {
        // wait 120s for next round
        super(120000);
        this.spotThreshold = setSpotThreshold;
    }

    /**
     * calculate spot value using performance data.
     * 
     * @param cpu
     * @param mem
     * @param netIn
     * @param netOut
     * @return
     */
    protected double calculateSpotValue(double cpu, double mem, double netIn,
            double netOut) {
        return (1.0 / (1.0 - cpu)) * (1.0 / (1.0 - mem))
                * ((1.0 / (1.0 - netIn)) + (1.0 / (1.0 - netOut)));
    }

    protected double calculateShiftValue(double cpu, double mem, double netIn,
            double netOut, double memSize) {
        // use the highest single value and memory size to determine
        double highest = Collections
                .max(Arrays.asList(cpu, mem, netIn, netOut));
        return highest / memSize;
    }

    protected Vnode selectVnodeFromHotspot(Pnode hotspot) {
        /**
         * TBD
         */
        return null;
    }

    /**
     * work one round.
     */
    @Override
    protected void workOneRound() {
        // 1. find the hotspot of physical machines
        // 1.1 find out the running physical nodes and their ids
        List<Pnode> pnodes = Pnode.all();
        // 1.2 collecting CPU / MEM / NETWORK IO performance data in percentile
        Pnode hotspot = null;
        double hotspotSpotValue = -1.0;
        for (Pnode pnd : pnodes) {
            logger.info(
                    String.format("Pnode %d says aye! ", (int) pnd.getId()));
            /**
             * 0.cpu 1.mhz 2.ncpu 3.free mem 4.used mem 5.total mem 6.ram size
             * 7.free disk 8.used disk 9.total disk 10.bandwidth 11.down 12.up
             */
            double perf[] = RRDTools.getLatestMonitorInfo((int) pnd.getId());
            double cpuLoad = perf[0] / 100.0, memLoad = perf[4] / perf[5],
                    netInLoad = perf[11] / perf[10],
                    netOutLoad = perf[12] / perf[10];
            double spotValue = calculateSpotValue(cpuLoad, memLoad, netInLoad,
                    netOutLoad);
            /**
             * if the "spot value" of this node is larger than the threshold, we
             * recognize it as a "hot spot".
             */
            if (spotValue > this.spotThreshold) {
                if (hotspot == null) {
                    hotspot = pnd;
                    hotspotSpotValue = spotValue;
                } else {
                    if (spotValue > hotspotSpotValue) {
                        hotspot = pnd;
                        hotspotSpotValue = spotValue;
                    }
                }
            }
        }

        // 2. choose the best vm on the physical node
        // 2.1 if there is no hot spot, return.
        if (hotspot == null) {
            logger.info("Well, no hotspot detected. ");
            return;
        }
        // 2.2 if there is a hot spot, select the most "influential" vnode from
        // it.
        int vmCnt = hotspot.getCurrentVMNum();
        logger.info(String.format(
                "Well, pnode %d is a hot spot. There are %d vnodes on it. ",
                (int) hotspot.getId(), vmCnt));
        List<Vnode> vnodes = hotspot.getVnodes();
        if (vnodes.isEmpty()) {
            // throw exception if the hot spot is not due to high payload on
            // virtual machines
            throw new NoVnodeOnHotspotException();
        }

        Vnode migrant;
        double maxShiftValue = -1.0;
        for (Vnode vnd : vnodes) {
            /**
             * 0.cpu 1.mhz 2.ncpu 3.free mem 4.used mem 5.total mem 6.ram size
             * 7.free disk 8.used disk 9.total disk 10.bandwidth 11.down 12.up
             */
            double perf[] = RRDTools.getLatestVnodeMonitorInfo(vnd.getUuid());
            double cpuLoad = perf[0] / 100.0, memLoad = perf[4] / perf[5],
                    netInLoad = perf[11] / perf[10],
                    netOutLoad = perf[12] / perf[10], memSize = perf[4];
            // !!! there may be some issues with the memory size !!!
            double shiftValue = calculateShiftValue(cpuLoad, memLoad, netInLoad,
                    netOutLoad, memSize);
            // choose the virtual machine with the largest shift value
            if (shiftValue > maxShiftValue) {
                migrant = vnd;
                maxShiftValue = shiftValue;
            }
        }

        // 3. find the destination

        // 4. migrate
    }
}

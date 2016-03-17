package nova.master.daemons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import nova.common.util.PerfData;
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
    protected double calculateSpotValue(PerfData perf) {
        return (1.0 / (1.0 - perf.getCpuLoad()))
                * (1.0 / (1.0 - perf.getMemLoad()))
                * ((1.0 / (1.0 - perf.getNetInLoad()))
                        + (1.0 / (1.0 - perf.getNetOutLoad())));
    }

    /**
     * calculate shift value for a virtual machine
     * 
     * @param cpu
     * @param mem
     * @param netIn
     * @param netOut
     * @param memSize
     * @return
     */
    protected double calculateShiftValue(PerfData perf) {
        // !!! may be some issues with the memory size
        // use the highest single value and memory size to determine
        double highest = Collections.max(Arrays.asList(perf.getCpuLoad(),
                perf.getMemLoad(), perf.getNetInLoad(), perf.getNetOutLoad()));
        return highest / perf.getMemSize();
    }

    /**
     * select hot spot physical machine
     * 
     * @return
     */
    protected Pnode selectPnodeHotspot() {
        // 1.1 find out the running physical nodes and their ids
        List<Pnode> pnodes = Pnode.all();
        // 1.2 collecting CPU / MEM / NETWORK IO performance data
        Pnode hotspot = null;
        double hotspotSpotValue = -1.0;
        for (Pnode pnd : pnodes) {
            logger.info(
                    String.format("Pnode %d says aye! ", (int) pnd.getId()));

            int pndId = (int) pnd.getId();
            PerfData perf = new PerfData(RRDTools.getLatestMonitorInfo(pndId));

            double spotValue = calculateSpotValue(perf);
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

        // return null if no hot spot is detected
        return hotspot;
    }

    /**
     * select the most "influential" virtual machine from the hot spot
     * 
     * @param hotspot
     * @return
     */
    protected Vnode selectVnodeFromHotspot(Pnode hotspot) {
        // debug info
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

        Vnode migrant = null;
        double maxShiftValue = -1.0;
        for (Vnode vnd : vnodes) {
            PerfData perf = new PerfData(
                    RRDTools.getLatestVnodeMonitorInfo(vnd.getUuid()));

            double shiftValue = calculateShiftValue(perf);
            // choose the virtual machine with the largest shift value
            if (shiftValue > maxShiftValue) {
                migrant = vnd;
                maxShiftValue = shiftValue;
            }
        }

        return migrant;
    }

    protected Pnode selectPnodeDestination(Vnode migrant) {
        /**
         * three qualifications of destination physical machine:
         * 
         * 1. destination cpu < 1 - CPU / N 2. destination free mem >= memory
         * required by migrant 3. destination free net i/o >= net i/o required
         * by migrant
         */

        List<Pnode> pnodes = Pnode.all();
        Pnode destination = null;
        double maxSpotValue = -1.0;
        double migrPerf[] = RRDTools
                .getLatestVnodeMonitorInfo(migrant.getUuid());

        for (Pnode pnd : pnodes) {
            double destPerf[] = RRDTools
                    .getLatestMonitorInfo((int) pnd.getId());
        }

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
        // 1. find the hot spot of physical machines
        Pnode hotspot = this.selectPnodeHotspot();

        // 2. choose the best vm on the physical node
        // 2.1 if there is no hot spot, return.
        if (hotspot == null) {
            logger.info("Well, no hotspot detected. ");
            return;
        }
        // 2.2 if there is a hot spot, select the most "influential" virtual
        // machine from it.
        Vnode migrant = this.selectVnodeFromHotspot(hotspot);
        if (migrant == null) {
            throw new NullPointerException();
        }

        // 3. find the destination

        // 4. migrate
    }
}

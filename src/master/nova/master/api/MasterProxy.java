package nova.master.api;

import java.net.InetSocketAddress;
import java.util.Collection;

import nova.agent.appliance.Appliance;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.message.AgentHeartbeatMessage;
import nova.common.service.message.AgentPerfMessage;
import nova.common.service.message.PnodeHeartbeatMessage;
import nova.common.service.message.PnodePerfMessage;
import nova.common.service.message.VnodeHeartbeatMessage;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.api.messages.ApplianceInfoMessage;
import nova.master.api.messages.AppliancesFirstInstalledMessage;
import nova.master.api.messages.DeletePnodeMessage;
import nova.master.api.messages.DeleteVclusterMessage;
import nova.master.api.messages.DeleteVnodeMessage;
import nova.master.api.messages.MasterInstallApplianceMessage;
import nova.master.api.messages.MasterMigrateCompleteMessage;
import nova.master.api.messages.MasterMigrateVnodeMessage;
import nova.master.api.messages.PnodeCreateVnodeMessage;
import nova.master.api.messages.PnodeStatusMessage;
import nova.master.api.messages.RegisterApplianceMessage;
import nova.master.api.messages.RegisterVdiskMessage;
import nova.master.api.messages.UnregisterApplianceMessage;
import nova.master.api.messages.UnregisterVdiskMessage;
import nova.master.api.messages.VnodeIPMessage;
import nova.master.api.messages.VnodeStatusMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;
import nova.test.functional.agent.experiment.ExpTimeMessage;

/**
 * Proxy for Master node.
 * 
 * @author santa
 * 
 */
public class MasterProxy extends SimpleProxy {

    public MasterProxy(InetSocketAddress bindAddr) {
        super(bindAddr);
    }

    public MasterProxy(SimpleAddress replyAddr) {
        super(replyAddr);
    }

    /**
     * Report a heartbeat to Master node.
     */
    public void sendPnodeHeartbeat() {
        super.sendRequest(new PnodeHeartbeatMessage());
    }

    public void sendVnodeHeartbeat() {
        super.sendRequest(new VnodeHeartbeatMessage());
    }

    public void sendAgentHeartbeat() {
        super.sendRequest(new AgentHeartbeatMessage());
    }

    /**
     * Send a monitor info to master node.
     */
    public void sendPnodeMonitorInfo() {
        super.sendRequest(new PnodePerfMessage());
    }

    public void sendVnodeMonitorInfo() {
        super.sendRequest(new AgentPerfMessage());
    }

    public void sendAddPnode(SimpleAddress pAddr) {
        super.sendRequest(new AddPnodeMessage(pAddr));
    }

    /*
     * public void sendCreateVnode(String vmImage, String vmName, int cpuCount,
     * int memorySize, String applianceList, int pnodeId, int ipOffset, String
     * hypervisor) { super.sendRequest(new CreateVnodeMessage(vmImage, vmName,
     * cpuCount, memorySize, applianceList, pnodeId, ipOffset, null, true,
     * hypervisor)); }
     * 
     * public void sendCreateVcluster(String vclusterName, int vclusterSize) {
     * super.sendRequest(new CreateVclusterMessage(vclusterName, vclusterSize));
     * }
     */

    public void SendRegisterVdisk(String displayName, String fileName,
            String imageType, String osFamily, String osName, String imgPath,
            String description) {
        super.sendRequest(new RegisterVdiskMessage(displayName, fileName,
                imageType, osFamily, osName, imgPath, description));
    }

    public void SendRegisterAppliance(String displayName, String fileName,
            String osFamily, String description) {
        super.sendRequest(new RegisterApplianceMessage(displayName, fileName,
                osFamily, description));
    }

    public void sendPnodeStatus(SimpleAddress pAddr, Pnode.Status status) {
        super.sendRequest(new PnodeStatusMessage(pAddr, status));
    }

    public void sendVnodeStatus(String vnodeIp, String uuid, Vnode.Status status) {
        super.sendRequest(new VnodeStatusMessage(vnodeIp, uuid, status));
    }

    // add by Herb for Real IP setting
    public void sendVnodeIP(String vnodeIp, String uuid) {
        super.sendRequest(new VnodeIPMessage(vnodeIp, uuid));
    }

    public void sendApplianceStatus(Collection<Appliance> appliances) {
        super.sendRequest(new ApplianceInfoMessage((Appliance[]) appliances
                .toArray()));
    }

    public void sendDeletePnode(long id) {
        super.sendRequest(new DeletePnodeMessage(id));
    }

    public void sendDeleteVnode(long id) {
        super.sendRequest(new DeleteVnodeMessage(id));
    }

    public void sendDeleteVcluster(long id) {
        super.sendRequest(new DeleteVclusterMessage(id));
    }

    public void sendUnregisterAppliance(long id) {
        super.sendRequest(new UnregisterApplianceMessage(id));
    }

    public void sendUnregisterVdisk(long id) {
        super.sendRequest(new UnregisterVdiskMessage(id));
    }

    public void sendInstallAppliance(long aid, String[] appNames) {
        super.sendRequest(new MasterInstallApplianceMessage(aid, appNames));
    }

    public void sendMigrateVnode(long vnodeId, long migrateFrom, long migrateTo) {
        super.sendRequest(new MasterMigrateVnodeMessage(vnodeId, migrateFrom,
                migrateTo));
    }

    public void sendMigrateComplete(String migrateUuid, String strPnodeIP,
            String strVNCPort, String hypervisor) {
        super.sendRequest(new MasterMigrateCompleteMessage(migrateUuid,
                strPnodeIP, strVNCPort, hypervisor));
    }

    public void sendAppliancesFirstInstalledMessage(SimpleAddress simpleAddress) {
        super.sendRequest(new AppliancesFirstInstalledMessage(simpleAddress));
    }

    public void sendPnodeCreateVnodeMessage(String PnodeIP, long VnodeId,
            int VnodePort, String vuuid, String hypervisor) {
        super.sendRequest(new PnodeCreateVnodeMessage(PnodeIP, VnodeId,
                VnodePort, vuuid, hypervisor));
    }

    // TODO delete experiment codes between //////////////////// and
    // //////////////
    // //////////////////////////////////////////////////////////////////////////
    public void sendExpTimeMessage(String ip, String timeType, long curTime) {
        super.sendRequest(new ExpTimeMessage(ip, timeType, curTime));
    }
    // /////////////////////////////////////////////////////////////////////////
}

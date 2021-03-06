package nova.worker.api.messages;

import java.util.HashMap;

import nova.common.service.SimpleAddress;

/**
 * Message for "start new vnode" request.
 * 
 * @author santa
 * 
 */
public class StartVnodeMessage {

    public StartVnodeMessage() {
        super();
    }

    public StartVnodeMessage(String hyperVisor, String name,
            SimpleAddress vAddr, boolean wakeupOnly, String memSize,
            String cpuCount, String hdaImage, boolean runAgent, String apps[],
            String ipAddr, String subnetMask, String gateWay, String vnodeID,
            int isvim, int network) {
        super();
        this.hyperVisor = hyperVisor;
        this.name = name;
        this.vAddr = vAddr;
        this.wakeupOnly = wakeupOnly;
        this.memSize = memSize;
        this.cpuCount = cpuCount;
        this.hdaImage = hdaImage;
        this.runAgent = runAgent;
        this.apps = apps;
        this.ipAddr = ipAddr;
        this.subnetMask = subnetMask;
        this.gateWay = gateWay;
        this.uuid = vnodeID;
        this.arch = "x86_64";
        this.isvim = isvim;
        this.network = network;
    }

    public StartVnodeMessage(String hyperVisor, boolean wakeupOnly,
            boolean runAgent, String uuid) {
        super();
        this.wakeupOnly = wakeupOnly;
        this.hyperVisor = hyperVisor;
        this.runAgent = runAgent;
        this.uuid = uuid;
    }

    /**
     * params for config
     */
    boolean wakeupOnly;
    String hyperVisor;
    String name;
    String uuid;// when creating vnode this means vnodeid, when resuming this
                // means uuid
    String memSize;
    String cpuCount;
    String arch;
    String bootDevice;
    String cdImage;
    String hdaImage;
    boolean runAgent;
    String emulatorPath;
    String sourceFile;
    String cdromPath;
    String interfaceType;
    String sourcebridge;
    String macAddress;
    String sourceNetwork;
    String inputType;
    String determinCdrom;
    String determinNetwork;
    String determinVnc;
    String bus;
    String apps[];
    String ipAddr;
    String subnetMask;
    String gateWay;
    int isvim;
    int network;

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getGateWay() {
        return gateWay;
    }

    public void setGateWay(String gateWay) {
        this.gateWay = gateWay;
    }

    public String[] getApps() {
        return apps;
    }

    public void setApps(String[] apps) {
        this.apps = apps;
    }

    public boolean getWakeupOnly() {
        return wakeupOnly;
    }

    public void setWakeupOnly(boolean wakeupOnly) {
        this.wakeupOnly = wakeupOnly;
    }

    public String getHyperVisor() {
        return hyperVisor;
    }

    public void setHyperVisor(String hyperVisor) {
        this.hyperVisor = hyperVisor;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getDeterminVnc() {
        return determinVnc;
    }

    public void setDeterminVnc(String determinVnc) {
        this.determinVnc = determinVnc;
    }

    public String getDeterminNetwork() {
        return determinNetwork;
    }

    public void setDeterminNetwork(String determinNetwork) {
        this.determinNetwork = determinNetwork;
    }

    public String getDeterminCdrom() {
        return determinCdrom;
    }

    public void setDeterminCdrom(String determinCdrom) {
        this.determinCdrom = determinCdrom;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getBus() {
        return bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getSourceNetwork() {
        return sourceNetwork;
    }

    public void setSourceNetwork(String sourceNetwork) {
        this.sourceNetwork = sourceNetwork;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getSourcebridge() {
        return sourcebridge;
    }

    public void setSourcebridge(String sourcebridge) {
        this.sourcebridge = sourcebridge;
    }

    public String getCdromPath() {
        return cdromPath;
    }

    public void setCdromPath(String cdromPath) {
        this.cdromPath = cdromPath;
    }

    public String getEmulatorPath() {
        return emulatorPath;
    }

    public void setEmulatorPath(String emulatorPath) {
        this.emulatorPath = emulatorPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMemSize() {
        return memSize;
    }

    public void setMemSize(String memSize) {
        this.memSize = memSize;
    }

    public String getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(String cpuCount) {
        this.cpuCount = cpuCount;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getBootDevice() {
        return bootDevice;
    }

    public void setBootDevice(String bootDevice) {
        this.bootDevice = bootDevice;
    }

    public String getCdImage() {
        return cdImage;
    }

    public void setCdImage(String cdImage) {
        this.cdImage = cdImage;
    }

    public String getHdaImage() {
        return hdaImage;
    }

    public void setHdaImage(String hdaImage) {
        this.hdaImage = hdaImage;
    }

    public boolean getRunAgent() {
        return runAgent;
    }

    public void setRunAgent(boolean runAgent) {
        this.runAgent = runAgent;
    }

    public int getIsvim() {
        return isvim;
    }

    public void setIsvim(int isvim) {
        this.isvim = isvim;
    }

    public int getNetwork() {
        return network;
    }

    public void setNetwork(int network) {
        this.network = network;
    }

    public HashMap<String, Object> getHashMap() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("determinCdrom", determinCdrom);
        values.put("determinNetwork", determinNetwork);
        values.put("determinVnc", determinVnc);
        values.put("name", name);
        values.put("uuid", uuid);
        values.put("memSize", memSize);
        values.put("cpuCount", cpuCount);
        values.put("arch", arch);
        values.put("bootDevice", bootDevice);
        values.put("cdImage", cdImage);
        values.put("hdaImage", hdaImage);
        values.put("runAgent", runAgent);
        values.put("emulatorPath", emulatorPath);
        values.put("cdromPath", cdromPath);
        values.put("interfaceType", interfaceType);
        values.put("sourcebridge", sourcebridge);
        values.put("macAddress", macAddress);
        values.put("sourceNetwork", sourceNetwork);
        values.put("inputType", inputType);
        values.put("sourceFile", sourceFile);
        values.put("isvim", isvim);
        values.put("network", network);
        return values;
    }

    /**
     * Basic information required to start a new vnode.
     */
    public SimpleAddress vAddr;

}

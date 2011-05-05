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

	/**
	 * params for config
	 */
	String hyperVisor;
	String name;
	String machine;
	String uuid;
	String memSize;
	String cpuCount;
	String arch;
	String bootDevice;
	String cdImage;
	String hdaImage;
	String runAgent;
	String emulatorPath;
	String cdromPath;
	String interfaceType;
	String sourcebridge;
	String macAddress;
	String sourceNetwork;
	String inputType;

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

	String bus;

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

	public String getHyperVisor() {
		return hyperVisor;
	}

	public void setHyperVisor(String hyperVisor) {
		this.hyperVisor = hyperVisor;
	}

	public String getMachine() {
		return machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
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

	public String getRunAgent() {
		return runAgent;
	}

	public void setRunAgent(String runAgent) {
		this.runAgent = runAgent;
	}

	public StartVnodeMessage(SimpleAddress vAddr) {
		this.vAddr = vAddr;
	}

	public HashMap<String, Object> getHashMap() {
		HashMap<String, Object> values = new HashMap<String, Object>();
		values.put("hyperVisor", hyperVisor);
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
		return values;

	}

	/**
	 * Basic information required to start a new vnode.
	 */
	public SimpleAddress vAddr;

}
package nova.common.tools.perf;

import com.google.gson.Gson;

/**
 * Disk information. Contains total, used and free size.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class DiskInfo {
	public long totalDiskSize = 0;
	public long usedDiskSize = 0;
	public long freeDiskSize = 0;

	public DiskInfo() {

	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	// private Sigar sigar = new Sigar();
	// private SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
	// private FileSystem fileSystem;
	// private FileSystemUsage usage;
	//
	// // FileSystem numbers
	// public FileSystem[] getFileSystems() throws SigarException {
	// return this.proxy.getFileSystemList();
	// }
	//
	// public void setFileSystem(FileSystem filesystem) throws SigarException {
	// this.fileSystem = filesystem;
	// this.usage = this.sigar.getFileSystemUsage(filesystem.getDirName());
	// }
	//
	// // Total disk size in KB
	// public long getTotalDisk() {
	// return usage.getTotal();
	// }
	//
	// // Used disk size in KB
	// public long getUsedDisk() {
	// return usage.getUsed();
	// }
	//
	// // Free disk size in KB
	// public long getFreeDisk() {
	// return usage.getFree();
	// }
	//
	// // File system name
	// public String getFileSystemName() {
	// return this.fileSystem.getDevName();
	// }
}

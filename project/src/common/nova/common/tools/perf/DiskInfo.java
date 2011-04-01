package nova.common.tools.perf;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;

public class DiskInfo {
	private Sigar sigar = new Sigar();
	private SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
	private FileSystem fileSystem;
	private FileSystemUsage usage;

	// FileSystem numbers
	public FileSystem[] getFileSystems() throws SigarException {
		return this.proxy.getFileSystemList();
	}

	public void setFileSystem(FileSystem filesystem) throws SigarException {
		this.fileSystem = filesystem;
		this.usage = this.sigar.getFileSystemUsage(filesystem.getDirName());
	}

	// Total disk size in KB
	public long getTotalDisk() {
		return usage.getTotal();
	}

	// Used disk size in KB
	public long getUsedDisk() {
		return usage.getUsed();
	}

	// Free disk size in KB
	public long getFreeDisk() {
		return usage.getFree();
	}

	// File system name
	public String getFileSystemName() {
		return this.fileSystem.getDevName();
	}
}

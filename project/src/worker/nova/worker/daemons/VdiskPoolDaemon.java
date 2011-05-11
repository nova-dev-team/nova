package nova.worker.daemons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nova.common.util.SimpleDaemon;
import nova.common.util.Utils;

import org.apache.log4j.Logger;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;

/**
 * Daemon thread that maintains vdisk pool
 * 
 * @author shayf
 * 
 */
public class VdiskPoolDaemon extends SimpleDaemon {

	public VdiskPoolDaemon() {
		super(2000);
		VdiskFiles = new VdiskFile[POOL_SIZE];
		for (int i = 0; i < POOL_SIZE; i++) {
			VdiskFiles[i] = new VdiskFile();
			VdiskFiles[i].setStatValue(i + 1); // also set len values here
			VdiskFiles[i].setLastVisitTime(System.currentTimeMillis());
			System.out.println(Integer.toString(i + 1) + "\t"
					+ VdiskFiles[i].getStat().toString() + "\tlen\t"
					+ VdiskFiles[i].getLen() + "\tvisittime\t"
					+ VdiskFiles[i].getLastVisitTime());
		}
		this.setLastCheckIsVmRunningTime(System.currentTimeMillis());
		this.setMbps(1);
		this.setVmRunning(true);
	}

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(VdiskPoolDaemon.class);

	static int POOL_SIZE = 5;

	private long lastCheckIsVmRunningTime;
	private int mbps;
	private boolean vmRunning;

	public boolean isVmRunning() throws LibvirtException {
		if (System.currentTimeMillis() - this.getLastCheckIsVmRunningTime() < 5000) {
			return this.vmRunning;
		} else {
			this.setLastCheckIsVmRunningTime(System.currentTimeMillis());
			Connect conn = null;
			conn = new Connect("qemu:///system", true);
			if (conn.numOfDomains() > 0) {
				System.out.println("numofdomains\t"
						+ Integer.toString(conn.numOfDomains()));
				conn.close();
				this.setVmRunning(true);
			} else {
				conn.close();
				this.setVmRunning(false);
			}
			return this.vmRunning;
		}
	}

	public void setVmRunning(boolean vmRunning) {
		this.vmRunning = vmRunning;
	}

	public int getMbps() {
		return mbps;
	}

	public void setMbps(int mbps) {
		this.mbps = mbps;
	}

	public long getLastCheckIsVmRunningTime() {
		return lastCheckIsVmRunningTime;
	}

	public void setLastCheckIsVmRunningTime(long lastCheckIsVmRunningTime) {
		this.lastCheckIsVmRunningTime = lastCheckIsVmRunningTime;
	}

	private static class VdiskFile {
		Status stat;
		long len;
		long lastVisitTime;

		public long getLen() {
			return len;
		}

		public void setLen(long len) {
			this.len = len;
		}

		public long getLastVisitTime() {
			return lastVisitTime;
		}

		public void setLastVisitTime(long lastVisitTime) {
			this.lastVisitTime = lastVisitTime;
		}

		public Status getStat() {
			return stat;
		}

		public void setStat(Status stat) {
			this.stat = stat;
		}

		public void setStatValue(int i) {
			File img = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
					"vdiskpool", "linux.img.pool." + Integer.toString(i)));
			File lockedImg = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
					"vdiskpool", "linux.img.pool." + Integer.toString(i)
							+ ".lock"));
			if (!img.exists() && !lockedImg.exists()) {
				this.stat = Status.NOT_EXIST;
				len = -1;
			} else if (img.exists() && !lockedImg.exists()) {
				this.stat = Status.AVAILABLE;
				len = img.length();
			} else if (!img.exists() && lockedImg.exists()) {
				this.stat = Status.LOCKED;
				len = lockedImg.length();
			} else { // both exist
				lockedImg.delete();
				this.stat = Status.AVAILABLE;
				len = img.length();
			}
		}

		enum Status {
			/**
			 * The vdiskfile is not existed.
			 */
			NOT_EXIST,

			/**
			 * The vdiskfile is locked for writing
			 */
			LOCKED,

			/**
			 * The vdiskfile is available
			 */
			AVAILABLE,
		}
	}

	VdiskFile[] VdiskFiles;

	public static int getPOOL_SIZE() {
		return POOL_SIZE;
	}

	public static void setPOOL_SIZE(int poolSize) {
		POOL_SIZE = poolSize;
	}

	private void autoSpeedCopy(File sourceFile, File tmpFile)
			throws IOException, LibvirtException {
		FileInputStream input = new FileInputStream(sourceFile);
		FileOutputStream output = new FileOutputStream(tmpFile);
		long bytesCopied = 0;
		byte[] b = new byte[1024 * 5];
		int cnt;
		// int loop = 0;
		long timeStamp = System.currentTimeMillis();
		double sleepUsec = 1.0;
		while (!this.isStopping()) {
			// loop++;
			// if (loop % 1024 == 0) {
			// if (loop > 1)
			// break;
			// }

			cnt = input.read(b);
			if (cnt <= 0)
				break;
			output.write(b, 0, cnt);
			if (this.getMbps() > 0) {
				System.out.println("low speed copy file" + tmpFile.getName());
				bytesCopied += cnt;
				if (bytesCopied > this.getMbps() * 1024 * 1024) {
					timeStamp -= System.currentTimeMillis();
					if (bytesCopied < 1024 * 1024 * this.getMbps() * timeStamp
							/ 1000) {
						sleepUsec /= 1.1;
					} else {
						sleepUsec *= 1.1;
					}
					if (sleepUsec > 1000 * 20) {
						sleepUsec = 1000 * 20;
					}
					try {
						Thread.sleep(Math.round(sleepUsec));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (!this.isVmRunning()) {
					this.setMbps(0);
				}
			} else {
				System.out.println("high speed copy file" + tmpFile.getName());
				if (this.isVmRunning()) {
					timeStamp = System.currentTimeMillis();
					sleepUsec = 1.0;
					this.setMbps(1);
					bytesCopied = 0;
				}
			}
		}
		output.flush();
		output.close();
		input.close();
	}

	@Override
	protected void workOneRound() {
		if (this.isStopping() == true) {
			return;
		}
		final String path = Utils.pathJoin(Utils.NOVA_HOME, "run", "vdiskpool");
		for (int i = POOL_SIZE; i > 0; i--) {
			if (VdiskFiles[i - 1].getStat().equals(VdiskFile.Status.NOT_EXIST)) {
				VdiskFiles[i - 1].setStat(VdiskFile.Status.LOCKED);
				try {
					System.out.println("file"
							+ Utils.pathJoin(path,
									"linux.img.pool." + Integer.toString(i))
							+ " " + VdiskFile.Status.NOT_EXIST.toString());
					System.out.println("going to copy file"
							+ Utils.pathJoin(path,
									"linux.img.pool." + Integer.toString(i)));
					String sourceUrl = Utils.pathJoin(Utils.NOVA_HOME, "run",
							"linux.img");
					String destUrl = Utils.pathJoin(path, "linux.img.pool."
							+ Integer.toString(i) + ".lock");
					File sourceFile = new File(sourceUrl);
					File tmpFile = new File(destUrl);
					File destFile = new File(Utils.pathJoin(path,
							"linux.img.pool." + Integer.toString(i)));
					if (sourceFile.isFile()) {
						this.autoSpeedCopy(sourceFile, tmpFile);
					}

					if (tmpFile.length() == sourceFile.length()) {
						tmpFile.renameTo(destFile);
						System.out.println("rename file" + tmpFile.getName()
								+ " to " + destFile.getName());
						VdiskFiles[i - 1].setStat(VdiskFile.Status.AVAILABLE);
						VdiskFiles[i - 1].setLen(destFile.length());
						VdiskFiles[i - 1].setLastVisitTime(System
								.currentTimeMillis());
					} else {
						VdiskFiles[i - 1].setLen(tmpFile.length());
						VdiskFiles[i - 1].setLastVisitTime(System
								.currentTimeMillis());
					}
					if (this.isStopping()) {
						break;
					}
				} catch (IOException e) {
					log.error("copy image fail", e);
				} catch (LibvirtException e) {
					log.error("libvirt connection fail", e);
				}

			} else if (VdiskFiles[i - 1].getStat().equals(
					VdiskFile.Status.LOCKED)) {
				System.out.println("time step:\t"
						+ Long.toString(System.currentTimeMillis()
								- VdiskFiles[i - 1].getLastVisitTime()));
				if (System.currentTimeMillis()
						- VdiskFiles[i - 1].getLastVisitTime() > 1000) {
					File lockedImg = new File(Utils.pathJoin(Utils.NOVA_HOME,
							"run", "vdiskpool",
							"linux.img.pool." + Integer.toString(i) + ".lock"));
					if (VdiskFiles[i - 1].getLen() == lockedImg.length()) {
						// copy failed
						lockedImg.delete();
					}
					VdiskFiles[i - 1].setStat(VdiskFile.Status.NOT_EXIST);
					VdiskFiles[i - 1].setLen(-1);
					VdiskFiles[i - 1].setLastVisitTime(System
							.currentTimeMillis());
				}
			}
		}
	}

}

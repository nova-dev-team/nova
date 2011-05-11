package nova.worker.daemons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nova.common.util.SimpleDaemon;
import nova.common.util.Utils;

import org.apache.log4j.Logger;

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
			VdiskFiles[i].setStatValue(i + 1);
			// VdiskFiles[i].setLastVisitTime(System.currentTimeMillis());
			System.out.println(Integer.toString(i + 1) + "\t"
					+ VdiskFiles[i].getStat().toString() + "\tlen\t"
					+ VdiskFiles[i].getLen() + "\tvisittime\t"
					+ VdiskFiles[i].getLastVisitTime());
		}
	}

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(VdiskPoolDaemon.class);

	static int POOL_SIZE = 5;

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
			lastVisitTime = System.currentTimeMillis();
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

	@Override
	protected void workOneRound() {
		final String path = Utils.pathJoin(Utils.NOVA_HOME, "run", "vdiskpool");
		for (int i = POOL_SIZE; i > 0; i--) {
			if (VdiskFiles[i - 1].getStat().equals(VdiskFile.Status.NOT_EXIST)) {
				VdiskFiles[i - 1].setStat(VdiskFile.Status.LOCKED);
				try {
					System.out.println("file"
							+ Utils.pathJoin(path,
									"linux.img.pool." + Integer.toString(i))
							+ VdiskFile.Status.NOT_EXIST.toString());
					System.out.println("copying file"
							+ Utils.pathJoin(path,
									"linux.img.pool." + Integer.toString(i)));
					String sourceUrl = Utils.pathJoin(Utils.NOVA_HOME, "run",
							"linux.img");
					String destUrl = Utils.pathJoin(path, "linux.img.pool."
							+ Integer.toString(i) + ".lock");
					File sourceFile = new File(sourceUrl);
					if (sourceFile.isFile()) {
						FileInputStream input = new FileInputStream(sourceFile);
						FileOutputStream output = new FileOutputStream(destUrl);
						byte[] b = new byte[1024 * 5];
						int len;
						while ((!this.isStopping())
								&& ((len = input.read(b)) != -1)) {
							output.write(b, 0, len);
						}
						output.flush();
						output.close();
						input.close();
					}
					File tmpFile = new File(destUrl);
					File destFile = new File(Utils.pathJoin(path,
							"linux.img.pool." + Integer.toString(i)));
					if (tmpFile.length() == sourceFile.length()) {
						tmpFile.renameTo(destFile);
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

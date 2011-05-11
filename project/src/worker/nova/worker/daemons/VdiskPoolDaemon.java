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
		super();
		VdiskFiles = new VdiskFile[POOL_SIZE];
		for (int i = 0; i < POOL_SIZE; i++) {
			VdiskFiles[i] = new VdiskFile();
			// System.out.println(VdiskFiles[i].getStat().toString());
			VdiskFiles[i].setStatValue(i + 1);
			System.out.println(VdiskFiles[i].getStat().toString());
		}
	}

	public VdiskPoolDaemon(long sleepMilli) {
		super(sleepMilli);
		VdiskFiles = new VdiskFile[POOL_SIZE];
		for (int i = 0; i < POOL_SIZE; i++) {
			VdiskFiles[i].setStatValue(i + 1);
		}
	}

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(VdiskPoolDaemon.class);

	static int POOL_SIZE = 5;

	private static class VdiskFile {
		Status stat;

		public Status getStat() {
			return stat;
		}

		public void setStat(Status stat) {
			this.stat = stat;
		}

		public void setStatValue(int i) {
			File image = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
					"vdiskpool", "linux.img.pool." + Integer.toString(i)));
			if (!image.exists()) {
				this.stat = Status.NOT_EXIST;
			} else {
				this.stat = Status.AVAILABLE;
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
					String destUrl = Utils.pathJoin(Utils.pathJoin(path,
							"linux.img.pool." + Integer.toString(i)));
					File sourceFile = new File(sourceUrl);
					if (sourceFile.isFile()) {
						FileInputStream input = new FileInputStream(sourceFile);
						FileOutputStream output = new FileOutputStream(destUrl);
						byte[] b = new byte[1024 * 5];
						int len;
						while ((len = input.read(b)) != -1) {
							output.write(b, 0, len);
						}
						output.flush();
						output.close();
						input.close();
					}
				} catch (IOException e) {
					log.error("copy image fail", e);
				}
				VdiskFiles[i - 1].setStat(VdiskFile.Status.AVAILABLE);
			} else if (VdiskFiles[i - 1].getStat().equals(
					VdiskFile.Status.LOCKED)) {
				// TODO @shayf test if copy fails

			}
		}
	}

}

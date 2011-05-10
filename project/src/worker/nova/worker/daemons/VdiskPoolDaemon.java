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

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(VdiskPoolDaemon.class);

	static int POOL_SIZE = 5;

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
			File image = new File(Utils.pathJoin(path, "linux.img.pool."
					+ Integer.toString(i)));
			if (!image.exists()) {
				try {
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
			}
		}
	}

}

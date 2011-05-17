package nova.agent.appliance;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import nova.agent.NovaAgent;
import nova.common.util.Conf;
import nova.common.util.Utils;

import org.apache.log4j.Logger;

import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

/**
 * Fetch appliances through ftp
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class FtpApplianceFetcher extends ApplianceFetcher {

	private String hostIp = null;
	private String userName = null;
	private String password = null;
	private String myPath = null;
	private String applianceName = null;

	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(FtpApplianceFetcher.class);

	public FtpApplianceFetcher() {
		this.hostIp = Conf.getString("agent.ftp.host");
		this.userName = Conf.getString("agent.ftp.user_name");
		this.password = Conf.getString("agent.ftp.password");
		this.myPath = Conf.getString("agent.software.save_path");
	}

	public void setApplianceName(Appliance app) {
		this.applianceName = app.getName();
	}

	@Override
	public void fetch(Appliance app) throws IOException {
		setApplianceName(app); // save the name of appliance being downloading
		FtpClient fc = connect();
		downloadDir(fc, "", app.getName());
		if (statusCancelled()) {
			NovaAgent.getInstance().getAppliances().get(app.getName())
					.setStatus(Appliance.Status.NOT_INSTALLED);
			deleteDir("", app.getName());
		}

		close(fc);

	}

	/**
	 * Connect to one ftp
	 * 
	 * @return FtpClient
	 */
	private FtpClient connect() throws IOException {
		FtpClient fc = null;
		final int ftpPort = Conf.getInteger("agent.ftp.port");
		fc = new FtpClient(hostIp, ftpPort);
		fc.openServer(hostIp, ftpPort);
		fc.login(this.userName, this.password);
		return fc;
	}

	/**
	 * Close one connect to ftp
	 * 
	 * @param fc
	 */
	private void close(FtpClient fc) {
		try {
			fc.closeServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete directory recursively
	 * 
	 * @param rootPath
	 *            whole path of one directory, default value is ""
	 * @param dirName
	 *            directory name
	 */
	public void deleteDir(String rootPath, String dirName) {
		String absolutePath = Utils.pathJoin(Utils.NOVA_HOME, this.myPath,
				rootPath, dirName);
		File file = new File(absolutePath);
		if (file.exists()) {
			File[] fileList = file.listFiles();
			String entry = null;
			for (File f : fileList) {
				entry = f.getName();
				if (f.isDirectory()) {
					deleteDir(Utils.pathJoin(rootPath, dirName), entry);
				} else {
					f.delete();
				}
			}
			file.delete();
		} else {
			logger.error("No such file: " + absolutePath);
		}
	}

	/**
	 * Judge if the downloading appliance is cancelled
	 * 
	 * @return {@link Boolean}
	 */
	private boolean statusCancelled() {
		return NovaAgent.getInstance().getAppliances().get(this.applianceName)
				.getStatus().equals(Appliance.Status.CANCELLED);
	}

	/**
	 * Download one file from ftp server
	 * 
	 * @param fClient
	 * @param rootPath
	 *            whole path of one directory in ftp
	 * @param fileName
	 *            the file that will be downloaded
	 * @return
	 * @throws IOException
	 *             Download one file exception
	 */
	private boolean downloadFile(FtpClient fClient, String rootPath,
			String fileName) throws IOException {

		TelnetInputStream is = null;
		FileOutputStream os = null;

		fClient.binary();
		is = fClient.get(fileName);
		java.io.File outfile = new java.io.File(Utils.pathJoin(Utils.NOVA_HOME,
				this.myPath, rootPath, fileName));
		os = new FileOutputStream(outfile);
		byte[] bytes = new byte[32 * 1024];
		int c = 0;

		while ((c = is.read(bytes)) != -1) {
			os.write(bytes, 0, c);

			if (statusCancelled()) {
				break;
			}
		}

		is.close();
		os.close();
		return true;
	}

	/**
	 * Download one directory from ftp
	 * 
	 * @param fc
	 * @param rootPath
	 *            whole path of one directory in ftp, default value is ""
	 * @param dirName
	 *            the directory that will be downloaded
	 */
	private boolean downloadDir(FtpClient fc, String rootPath, String dirName) {

		if (!statusCancelled()) {
			try {
				fc.cd(dirName);
				Utils.mkdirs(Utils.pathJoin(Utils.NOVA_HOME, this.myPath,
						rootPath, dirName));
				DataInputStream dis = new DataInputStream(fc.list());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						dis));

				String ftpEntry = null;

				while ((ftpEntry = br.readLine()) != null && !statusCancelled()) {
					int fnameStart = nthFieldStart(ftpEntry, 8);
					String entry = ftpEntry.substring(fnameStart);
					if ((nthField(ftpEntry, 0)).startsWith("d")) {
						// d is directory
						downloadDir(fc, Utils.pathJoin(rootPath, dirName),
								entry);
					} else {
						downloadFile(fc, Utils.pathJoin(rootPath, dirName),
								entry);

					}
				}
				dis.close();
				fc.cdUp();
			} catch (IOException e) {
				logger.error("Download error: ", e);
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if a char is whitespace.
	 * 
	 * @param ch
	 * @return
	 */
	private static boolean isWhiteSpace(byte ch) {
		return ch == ' ' || ch == '\t' || ch == '\n';
	}

	/**
	 * Get the n-th field in an ftp file entry.
	 * 
	 * @param ftpItem
	 * @param nth
	 * @return
	 */
	public static String nthField(String ftpItem, int nth) {
		int start = nthFieldStart(ftpItem, nth);
		int stop = nthFieldStop(ftpItem, nth);
		return ftpItem.substring(start, stop);
	}

	/**
	 * Get the start offset of the n-th field in an ftp entry.
	 * 
	 * @param ftpItem
	 * @param nth
	 * @return
	 */
	public static int nthFieldStart(String ftpItem, int nth) {
		int currentFieldId = -1;
		int idx = 0;
		boolean inWhiteSpace = true;
		byte[] ftpItemBytes = ftpItem.getBytes();
		for (;;) {
			byte ch = ftpItemBytes[idx];
			if (inWhiteSpace == true) {
				if (isWhiteSpace(ch)) {
					// do nothing, pass along
				} else {
					inWhiteSpace = false;
					currentFieldId++;
					if (currentFieldId >= nth)
						break;
				}
			} else {
				if (isWhiteSpace(ch)) {
					inWhiteSpace = true;
				} else {
					// do nothing, pass along
				}
			}
			idx++;
		}
		return idx;
	}

	/**
	 * Get the stop offset (surpass end) of the n-th field in an ftp entry.
	 * 
	 * @param ftpItem
	 * @param nth
	 * @return
	 */
	public static int nthFieldStop(String ftpItem, int nth) {
		byte[] ftpItemBytes = ftpItem.getBytes();
		int idx = nthFieldStart(ftpItem, nth);
		for (;;) {
			if (idx >= ftpItemBytes.length)
				break;
			byte ch = ftpItemBytes[idx];
			if (isWhiteSpace(ch)) {
				break;
			}
			idx++;
		}
		return idx;
	}
}

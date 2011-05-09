package nova.agent.appliance;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import nova.agent.NovaAgent;
import nova.common.util.Conf;
import nova.common.util.Utils;
import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

public class FtpApplianceFetcher extends ApplianceFetcher {

	private String hostIp = null;
	private String userName = null;
	private String password = null;
	private String myPath = null;

	public FtpApplianceFetcher() {
		this.hostIp = Conf.getString("agent.ftp.host");
		this.userName = Conf.getString("agent.ftp.user_name");
		this.password = Conf.getString("agent.ftp.password");
		this.myPath = Conf.getString("agent.software.save_path");
	}

	@Override
	public void fetch(Appliance app) throws IOException {
		downloadDirFromFtp(app.getName());
		// NovaAgent.getInstance().getAppliances().get(app.getName()).getStatus()
		// == Appliance.Status.CANCELLED;
	}

	private FtpClient connectToFtp() {
		FtpClient fc = null;
		try {
			fc = new FtpClient(this.hostIp);
			fc.openServer(hostIp, 8021);
			fc.login(this.userName, this.password);
		} catch (IOException e) {
			return null;
		}
		return fc;
	}

	private boolean downloadFromFtp(FtpClient fClient, String softName) {

		TelnetInputStream is = null;
		FileOutputStream os = null;
		try {
			fClient.binary();
			is = fClient.get(softName);
			java.io.File outfile = new java.io.File(Utils.pathJoin(this.myPath
					+ softName));
			os = new FileOutputStream(outfile);
			byte[] bytes = new byte[1024];
			int c = 0;

			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);

				if (NovaAgent.getInstance().getAppliances().get(softName)
						.getStatus() == Appliance.Status.CANCELLED) {
					return false;
				}

			}

			is.close();
			os.close();
			fClient.closeServer();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private void downloadDirFromFtp(String dirName) {
		FtpClient fc = connectToFtp();
		try {
			fc.cd(dirName);
			DataInputStream dis = new DataInputStream(fc.list());

			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String ftpEntry = null;
			while ((ftpEntry = br.readLine()) != null) {
				// System.out.println(nthField(ftpEntry, 0));
				int fnameStart = nthFieldStart(ftpEntry, 8);
				String fname = ftpEntry.substring(fnameStart);
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
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

package nova.agent;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import nova.common.interfaces.Cancelable;
import nova.common.interfaces.Progressable;
import nova.common.util.Utils;
import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

/**
 * Download one selected software from ftp
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class DownloadProgress implements Cancelable, Progressable {

	private String hostIp = GlobalPara.hostIp;
	private String userName = GlobalPara.userName;
	private String password = GlobalPara.password;
	private String myPath = GlobalPara.myPath; // Download to where

	private FtpClient fc = connectToFtp();

	public FtpClient getFtpClient() {
		return this.fc;
	}

	public void closeFtpClient() {
		try {
			this.fc.closeServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			this.fc.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DownloadProgress() {
		this.hostIp = GlobalPara.hostIp;
		this.userName = GlobalPara.userName;
		this.password = GlobalPara.password;
		this.myPath = GlobalPara.myPath;
	}

	@Override
	public int getProgress() {
		// TODO @gaotao Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDone() {
		// TODO @gaotao Auto-generated method stub
		return false;
	}

	@Override
	public void cancel() {
		// TODO @gaotao Auto-generated method stub

	}

	@Override
	public boolean isCanceled() {
		// TODO @gaotao Auto-generated method stub
		return false;
	}

	/**
	 * Connect to ftp
	 * 
	 * @return {@link FtpClient}
	 */
	private FtpClient connectToFtp() {
		FtpClient fc = null;
		try {
			fc = new FtpClient(this.hostIp);
			fc.login(this.userName, this.password);
		} catch (IOException e) {
			return null;
		}
		return fc;
	}

	/**
	 * 
	 * @param fClient
	 *            {@link FtpClient}
	 * @param softName
	 *            {@link String}
	 * @return {@link Boolean}
	 */
	public boolean downloadFromFtp(FtpClient fClient, String softName) {
		GlobalPara.downProcess.setBorderPainted(true);
		GlobalPara.downProcess.setBackground(Color.pink);
		GlobalPara.downProcess.setStringPainted(true);
		GlobalPara.statusInfo.setText("Downloading " + softName + "...");
		GlobalPara.statusInfo.setBackground(Color.BLACK);
		GlobalPara.downProcess.setVisible(true);
		GlobalPara.currentBytes = 0;
		GlobalPara.totalBytes = 0;

		GlobalPara.downProcess.setValue(0);
		GlobalPara.downProcess.setMaximum(100);
		TelnetInputStream is = null;
		FileOutputStream os = null;
		try {
			fClient.binary();
			int tb = getFileSize(fClient, softName);
			GlobalPara.totalBytes = tb;
			is = fClient.get(softName);
			java.io.File outfile = new java.io.File(Utils.pathJoin(this.myPath,
					softName));
			os = new FileOutputStream(outfile);
			byte[] bytes = new byte[1024];
			int c = 0;
			while ((c = is.read(bytes)) != -1) {

				os.write(bytes, 0, c);
				GlobalPara.currentBytes = GlobalPara.currentBytes + c;
				GlobalPara.downProcess
						.setValue((int) (((double) GlobalPara.currentBytes / (double) GlobalPara.totalBytes) * 100));
			}

			is.close();
			os.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Get the length of one file
	 * 
	 * @param client
	 *            {@link FtpClient}
	 * @param fileName
	 *            {@link String}
	 * @return {@link Integer}
	 * @throws IOException
	 */
	public int getFileSize(FtpClient client, String fileName)
			throws IOException {
		TelnetInputStream lst = client.list();
		String str = "";
		fileName = fileName.toLowerCase();
		while (true) {
			int c = lst.read();
			char ch = (char) c;
			if (c < 0 || ch == '\n') {
				str = str.toLowerCase();
				if (str.indexOf(fileName) >= 0) {
					StringTokenizer tk = new StringTokenizer(str);
					int index = 0;
					while (tk.hasMoreTokens()) {
						String token = tk.nextToken();
						if (index == 4)
							try {
								return Integer.parseInt(token);
							} catch (NumberFormatException ex) {
								return -1;
							}
						index++;
					}
				}
				str = "";
			}
			if (c <= 0)
				break;
			str += ch;
		}
		return -1;
	}

	public void downLoad(String softName) {
		downloadFromFtp(fc, softName);
		GlobalPara.statusInfo.setText("Installing " + softName + "...");
		// GlobalPara.downloadedBuffer.write(this.softName);
	}
}

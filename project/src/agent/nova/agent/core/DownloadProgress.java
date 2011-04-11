package nova.agent.core;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import nova.agent.common.util.GlobalPara;
import nova.common.interfaces.Cancelable;
import nova.common.interfaces.Progressable;
import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

/**
 * Download software from ftp
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class DownloadProgress implements Cancelable, Progressable, Runnable {

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

	private String hostIp;
	private String userName;
	private String password;
	private String softName;
	private String myPath;

	public DownloadProgress(String ftpAdress, String userName, String password,
			String softNm) {
		this.hostIp = ftpAdress;
		this.userName = userName;
		this.password = password;
		this.softName = softNm;
		this.myPath = GlobalPara.myPath;
	}

	/**
	 * Connect to ftp
	 * 
	 * @return {@link FtpClient}
	 */
	public FtpClient connectToFtp() {
		FtpClient fc = null;
		try {
			fc = new FtpClient(this.hostIp);
			fc.login(this.userName, this.password);
		} catch (IOException e) {
			return null;
		}
		return fc;
	}

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
			java.io.File outfile = new java.io.File(this.myPath + softName);
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
			fClient.closeServer();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Get the length of one file
	 * 
	 * @param FtpClient
	 * @param fileName
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

	@Override
	public void run() {
		FtpClient fc = connectToFtp();
		downloadFromFtp(fc, this.softName);
		GlobalPara.statusInfo.setText("Installing " + this.softName + "...");
	}

}

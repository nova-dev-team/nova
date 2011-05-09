package nova.agent.appliance;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nova.agent.NovaAgent;
import nova.common.util.Conf;
import nova.common.util.Utils;
import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

public class FtpApplianceFetcher extends ApplianceFetcher {
	private String hostIp = null;
	private String userName = null;
	private String password = null;
	private String softName = null;
	private String myPath = null;

	public FtpApplianceFetcher() {
		this.hostIp = Conf.getString("agent.ftp.host");
		this.userName = Conf.getString("agent.ftp.user_name");
		this.password = Conf.getString("agent.ftp.password");
		this.myPath = Conf.getString("agent.software.save_path");
	}

	public void setSoftName(String softName) {
		this.softName = softName;
	}

	@Override
	public void fetch(Appliance app) throws IOException {
		setSoftName(app.getName());
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

				if (NovaAgent.getInstance().getAppliances().get(this.softName)
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

	@SuppressWarnings("deprecation")
	private void downloadDirFromFtp(String dirName) {
		FtpClient fc = connectToFtp();
		try {
			fc.cd(dirName);
			DataInputStream dis = new DataInputStream(fc.list());
			String filename = "";

			while ((filename = dis.readLine()) != null) {
				// if (filename.startsWith("drwxr")) {
				System.out.println(filename);
				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

package nova.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import nova.agent.daemons.PackageDownloadDaemon;
import nova.agent.handler.RequestSoftwareMessageHandler;
import nova.common.service.SimpleAddress;
import nova.common.util.Conf;
import nova.common.util.Pair;
import nova.common.util.Utils;
import nova.master.api.MasterProxy;

import org.apache.log4j.Logger;

/**
 * Static variable used in agent
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class GlobalPara {

	public static Conf conf = Utils.loadAgentConf();
	/**
	 * Agent parameter
	 */
	public static String AGENT_BIND_HOST = conf.getString("agent.bind_host");
	public static int AGENT_BIND_PORT = conf.getInteger("agent.bind_port");

	public static Map<SimpleAddress, MasterProxy> masterProxyMap = new HashMap<SimpleAddress, MasterProxy>();

	public static Object heartbeatSem = new Object();
	public static Object generalMonitorSem = new Object();

	/**
	 * Producer in {@link RequestSoftwareMessageHandler}, consumer in
	 * {@link PackageDownloadDaemon}
	 */
	public static ProducerAndConsumer downloadBuffer = new ProducerAndConsumer();

	/**
	 * Producer in DownloadProgressDaemon, consumer in InstallProgressDaemon
	 */
	public static ProducerAndConsumer downloadedBuffer = new ProducerAndConsumer();

	/**
	 * Software parameter
	 */
	public static String hostIp = conf.getString("agent.ftp.bind_host"); // ftpadress
	public static String userName = conf.getString("agent.ftp.user_name"); // ftp登陆用户名
	public static String password = conf.getString("agent.ftp.password"); // ftp登陆密码
	public static String myPicPath = Utils.pathJoin(Utils.NOVA_HOME,
			conf.getString("agent.software.image_path")); // 图片的保存地址
	public static String myPath = Utils.pathJoin(Utils.NOVA_HOME,
			conf.getString("agent.software.save_path")); // 本地文件下载保存地址
	public static ArrayList<String> softList = new ArrayList<String>(); // 所有可安装软件列表

	public static Map<String, Pair<String, String>> softInfo = new HashMap<String, Pair<String, String>>(); // 每个软件的信息
	public static long totalBytes = 0;
	public static long currentBytes = 0;

	public static JLabel statusInfo = new JLabel("Download process");
	public static JProgressBar downProcess = new JProgressBar(); // 安装进度条

	static Logger logger = Logger.getLogger(GlobalPara.class);

	public void createNewSoftwareList() {
		String s = null;
		String filePath = Utils.pathJoin(Utils.NOVA_HOME, "conf",
				"nova.agent.software.properties");
		File f = new File(filePath);
		if (f.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(f)));
				while ((s = br.readLine()) != null && !s.equals("")) {
					String[] infoOfSoft = s.split("="); // 0: 软件名字
														// 1：软件信息
					softList.add(infoOfSoft[0].trim());
					Pair<String, String> infoAndStatus = new Pair<String, String>(
							infoOfSoft[1].trim(), infoOfSoft[2].trim());
					softInfo.put(infoOfSoft[0].trim(), infoAndStatus);
				}
				br.close();
			} catch (Exception e) {
				logger.error("Error loading config info", e);
			}
		} else {
			logger.error("Can't find the configuration text!");
		}

		// System.out.println("hostIp = " + hostIp);
		// System.out.println("username = " + userName);
		// System.out.println("password = " + password);
		// System.out.println("myPicPath = " + myPicPath);
		// System.out.println("myPath = " + myPath);
		// for (int i = 0; i < softList.size(); i++)
		// System.out.println("soft: " + i + " " + softList.get(i)
		// + " Description: " + softInfo.get(softList.get(i)).first
		// + " Status: " + softInfo.get(softList.get(i)).second);

	}
}

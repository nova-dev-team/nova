package nova.agent.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import nova.agent.core.handler.RequestSoftwareMessageHandler;
import nova.agent.core.service.GeneralMonitorProxy;
import nova.agent.core.service.HeartbeatProxy;
import nova.agent.core.service.IntimeProxy;
import nova.agent.daemons.DownloadProgressDaemon;
import nova.common.service.SimpleAddress;

import org.apache.log4j.Logger;

/**
 * Static variable used in agent
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class GlobalPara {

	/**
	 * Agent parameter
	 */
	public static int BIND_PORT = 9876;

	public static Map<SimpleAddress, HeartbeatProxy> heartbeatProxyMap = new HashMap<SimpleAddress, HeartbeatProxy>();
	public static Map<SimpleAddress, GeneralMonitorProxy> generalMonitorProxyMap = new HashMap<SimpleAddress, GeneralMonitorProxy>();
	public static Map<SimpleAddress, IntimeProxy> intimeProxyMap = new HashMap<SimpleAddress, IntimeProxy>();

	public static Object heartbeatSem = new Object();
	public static Object generalMonitorSem = new Object();

	/**
	 * Producer in {@link RequestSoftwareMessageHandler}, consumer in
	 * {@link DownloadProgressDaemon}
	 */
	public static ProducerAndConsumer downloadBuffer = new ProducerAndConsumer();

	/**
	 * Producer in DownloadProgressDaemon, consumer in InstallProgressDaemon
	 */
	public static ProducerAndConsumer downloadedBuffer = new ProducerAndConsumer();

	/**
	 * Software parameter
	 */
	public static String hostIp = null; // ftpadress
	public static String userName = null; // ftp登陆用户名
	public static String password = null; // ftp登陆密码
	public static String myPicPath = null; // 图片的保存地址
	public static String myPath = null; // 本地文件下载保存地址
	public static ArrayList<String> softList = new ArrayList<String>(); // 所有可安装软件列表
	public static Map<String, String> softInfo = new HashMap<String, String>(); // 每个软件的信息
	public static long totalBytes = 0;
	public static long currentBytes = 0;

	public static JLabel statusInfo = new JLabel("Download process");
	public static JProgressBar downProcess = new JProgressBar(); // 安装进度条

	static Logger logger = Logger.getLogger(GlobalPara.class);

	public GlobalPara() {
		String s = null;
		File f = new File("d://config.txt");
		if (f.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(f)));
				while ((s = br.readLine()) != null) {
					if (s.startsWith("#hostIp")) {
						hostIp = s.split("=")[1].trim();
					} else if (s.startsWith("#myPicPath")) {
						myPicPath = s.split("=")[1].trim();
					} else if (s.startsWith("#userName")) {
						userName = s.split("=")[1].trim();
					} else if (s.startsWith("#password")) {
						password = s.split("=")[1].trim();
					} else if (s.startsWith("#myPath")) {
						myPath = s.split("=")[1].trim();
					} else if (s.startsWith("#softList")) {
						while ((s = br.readLine()) != null) {
							String[] infoOfSoft = s.split("="); // 0: 软件名字
																// 1：软件信息
							softList.add(infoOfSoft[0].trim());
							softInfo.put(infoOfSoft[0].trim(),
									infoOfSoft[1].trim());
						}
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logger.error("Can't find the configuration text!");
		}

		System.out.println("hostIp = " + hostIp);
		System.out.println("username = " + userName);
		System.out.println("password = " + password);
		System.out.println("myPicPath = " + myPicPath);
		System.out.println("myPath = " + myPath);
		for (int i = 0; i < softList.size(); i++)
			System.out.println("soft: " + i + " " + softList.get(i)
					+ " Description: " + softInfo.get(softList.get(i)));

	}
}

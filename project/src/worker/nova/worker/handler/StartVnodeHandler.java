package nova.worker.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;
import nova.master.models.Vnode;
import nova.storage.NovaStorage;
import nova.worker.NovaWorker;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.daemons.VdiskPoolDaemon;
import nova.worker.daemons.VnodeStatusDaemon;
import nova.worker.models.StreamGobbler;
import nova.worker.virt.Kvm;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import sun.net.ftp.FtpClient;

/**
 * Handler for "start new vnode" request.
 * 
 * @author santa
 * 
 */
public class StartVnodeHandler implements SimpleHandler<StartVnodeMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(StartVnodeHandler.class);

	/**
	 * Handle "start new vnode" request.
	 */
	@Override
	public void handleMessage(StartVnodeMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, SimpleAddress xreply) {
		final String virtService;
		if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
			virtService = "qemu:///system";
		} else {
			// TODO @shayf get correct xen service address
			virtService = "some xen address";
		}

		if (msg.getWakeupOnly()) {
			synchronized (NovaWorker.getInstance().getConnLock()) {
				try {
					NovaWorker.getInstance().connectToKvm("qemu:///system",
							true);

					Domain testDomain = NovaWorker.getInstance().getConn()
							.domainLookupByUUIDString(msg.getUuid());
					testDomain.resume();
					// NovaWorker.getInstance().closeConnectToKvm();
				} catch (LibvirtException ex) {
					log.error("Domain with UUID='" + msg.getUuid()
							+ "' can't be found!", ex);
				}
			}
		} else {
			if ((msg.getMemSize() != null) && (!msg.getMemSize().equals(""))) {
				if (Integer.parseInt(msg.getMemSize()) <= 0)
					msg.setMemSize("524288");
			} else {
				msg.setMemSize("524288");
			}

			if ((msg.getCpuCount() != null) && (!msg.getCpuCount().equals(""))) {
				// TODO @shayf get actual cpu nums
				if ((Integer.parseInt(msg.getCpuCount()) <= 0)
						|| (Integer.parseInt(msg.getCpuCount()) >= 10))
					msg.setCpuCount("1");
			} else {
				msg.setCpuCount("1");
			}

			if ((msg.getArch() == null) || msg.getArch().equals("")) {
				msg.setArch("i686");
			}

			// mv img files from vdiskpool
			String stdImgFile = "small.img";
			if ((msg.getHdaImage() != null) && (!msg.getHdaImage().equals(""))) {
				stdImgFile = msg.getHdaImage();
			}
			File stdFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
					stdImgFile));
			if (!stdFile.exists()) {
				if (NovaStorage.getInstance().getFtpServer() == null) {
					NovaStorage.getInstance().startFtpServer();
				}
				System.out.println(Conf.getString("worker.software.save_path"));
				try {
					FtpClient fc = FtpUtils.connect(
							Conf.getString("storage.ftp.bind_host"),
							Conf.getInteger("storage.ftp.bind_port"),
							Conf.getString("storage.ftp.admin.username"),
							Conf.getString("storage.ftp.admin.password"));
					fc.cd("img");
					FtpUtils.downloadFile(fc, Utils.pathJoin(stdImgFile),
							Utils.pathJoin(Utils.NOVA_HOME, "run", stdImgFile));
					System.out.println("download file " + stdImgFile);
					fc.closeServer();
				} catch (NumberFormatException e1) {
					log.error("port format error!", e1);
				} catch (IOException e1) {
					log.error("ftp connection fail!", e1);
				}
				NovaStorage.getInstance().shutdown();
			}
			long stdLen = stdFile.length();
			boolean found = false;
			for (int i = VdiskPoolDaemon.getPOOL_SIZE(); i >= 1; i--) {
				File srcFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
						"vdiskpool",
						stdImgFile + ".pool." + Integer.toString(i)));
				if (srcFile.exists() && (srcFile.length() == stdLen)) {
					System.out.println("file " + stdImgFile + ".pool."
							+ Integer.toString(i) + "exists!");
					File foder = new File(Utils.pathJoin(Utils.NOVA_HOME,
							"run", msg.getName()));
					if (!foder.exists()) {
						foder.mkdirs();
					}
					File dstFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
							"run", msg.getName(), stdImgFile));
					srcFile.renameTo(dstFile);
					found = true;
					break;
				} else {
					System.out.println("file " + stdImgFile + ".pool."
							+ Integer.toString(i) + "not exist!");
				}
			}
			if (!found) {
				// copy img files
				File foder = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
						msg.getName()));
				if (!foder.exists()) {
					foder.mkdirs();
				} else {
					// TODO @whoever rename or stop or what?
					log.error("vm name " + msg.getName() + " has been used!");
				}
				File file = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
						msg.getName(), stdImgFile));
				if (file.exists() == false) {
					try {
						System.out.println("copying file");
						String sourceUrl = Utils.pathJoin(Utils.NOVA_HOME,
								"run", stdImgFile);
						String destUrl = Utils.pathJoin(Utils.NOVA_HOME, "run",
								msg.getName(), stdImgFile);
						File sourceFile = new File(sourceUrl);
						if (sourceFile.isFile()) {
							FileInputStream input = new FileInputStream(
									sourceFile);
							FileOutputStream output = new FileOutputStream(
									destUrl);
							byte[] b = new byte[1024 * 5];
							int len;
							while ((len = input.read(b)) != -1) {
								output.write(b, 0, len);
							}
							output.flush();
							output.close();
							input.close();
						}
					} catch (IOException ex) {
						log.error("copy image fail", ex);
					}
				}
			}

			if (msg.getRunAgent()) {
				File pathFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
						"softwares"));
				if (!pathFile.exists()) {
					Utils.mkdirs(pathFile.getAbsolutePath());
				}
				File paramsFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
						"run", "softwares", "params"));
				if (!paramsFile.exists()) {
					Utils.mkdirs(paramsFile.getAbsolutePath());
				}
				File ipFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
						"softwares", "params", "ipconfig.txt"));

				try {
					if (!ipFile.exists()) {
						ipFile.createNewFile();
					}
					OutputStream os = new FileOutputStream(ipFile);
					os.write(msg.getIpAddr().getBytes());
					os.write("\n".getBytes());
					os.write(msg.getSubnetMask().getBytes());
					os.write("\n".getBytes());
					os.write(msg.getGateWay().getBytes());
					os.write("\n".getBytes());
				} catch (FileNotFoundException e1) {
					log.error("file not found!", e1);
				} catch (IOException e1) {
					log.error("file write fail!", e1);
				}

				if (msg.getApps() != null) {
					for (String appName : msg.getApps()) {
						if (NovaWorker.getInstance().getAppStatus()
								.containsKey(appName) == false) {
							try {
								if (NovaStorage.getInstance().getFtpServer() == null) {
									NovaStorage.getInstance().startFtpServer();
								}
								FtpClient fc = FtpUtils
										.connect(
												Conf.getString("storage.ftp.bind_host"),
												Conf.getInteger("storage.ftp.bind_port"),
												Conf.getString("storage.ftp.admin.username"),
												Conf.getString("storage.ftp.admin.password"));
								fc.cd("appliances");
								FtpUtils.downloadDir(fc, Utils
										.pathJoin(appName), Utils.pathJoin(
										Utils.NOVA_HOME, "run", "softwares",
										appName));
								System.out.println("download file " + appName);
								fc.closeServer();
							} catch (NumberFormatException e1) {
								log.error("port format error!", e1);
								return;
							} catch (IOException e1) {
								log.error("ftp connection fail!", e1);
								return;
							}
							NovaWorker.getInstance().getAppStatus()
									.put(appName, appName);
						}
					}
				}

				// write nova.agent.ipaddress.properties file
				File ipAddrFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
						"conf", "nova.agent.extrainfo.properties"));
				if (!ipAddrFile.exists()) {
					try {
						ipAddrFile.createNewFile();
					} catch (IOException e1) {
						log.error(
								"create nova.agent.extrainfo.properties file fail!",
								e1);
					}
				}

				try {
					PrintWriter outpw = new PrintWriter(new FileWriter(
							ipAddrFile));
					outpw.println("agent.bind_host=" + msg.getIpAddr());
					outpw.println("master.bind_host="
							+ Conf.getString("master.bind_host"));
					outpw.println("master.bind_port="
							+ Conf.getString("master.bind_port"));
					outpw.close();
				} catch (IOException e1) {
					log.error(
							"write nova.agent.extrainfo.properties file fail!",
							e1);
				}

				// copy files to Novahome/run/softwares
				File agentProgramFile = new File(Utils.pathJoin(
						Utils.NOVA_HOME, "run", "softwares", "run"));
				if (!agentProgramFile.exists()) {
					Utils.mkdirs(agentProgramFile.getAbsolutePath());
				}
				// String[] ignoreList = { "nova.properties" };
				// Utils.copyWithIgnore(Utils.pathJoin(Utils.NOVA_HOME, "conf"),
				// Utils.pathJoin(agentProgramFile.getAbsolutePath(),
				// "conf"), ignoreList);
				Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "conf"), Utils
						.pathJoin(agentProgramFile.getAbsolutePath(), "conf"));
				Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "bin"), Utils
						.pathJoin(agentProgramFile.getAbsolutePath(), "bin"));
				Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "lib"), Utils
						.pathJoin(agentProgramFile.getAbsolutePath(), "lib"));
				Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "data"), Utils
						.pathJoin(agentProgramFile.getAbsolutePath(), "data"));

				// pack iso files
				File agentCdFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
						"run", "agentcd"));
				if (!agentCdFile.exists()) {
					Utils.mkdirs(agentCdFile.getAbsolutePath());
				}
				System.out.println("packing iso");
				Process p;
				String cmd = "mkisofs -J -T -R -V cdrom -o "
						+ Utils.pathJoin(Utils.NOVA_HOME, "run", "agentcd",
								"agent-cd.iso") + " "
						+ Utils.pathJoin(Utils.NOVA_HOME, "run", "softwares");
				System.out.println(cmd);
				try {
					p = Runtime.getRuntime().exec(cmd);
					StreamGobbler errorGobbler = new StreamGobbler(
							p.getErrorStream(), "ERROR");
					errorGobbler.start();
					StreamGobbler outGobbler = new StreamGobbler(
							p.getInputStream(), "STDOUT");
					outGobbler.start();
					try {
						if (p.waitFor() != 0) {
							log.error("pack iso returned abnormal value!");
						}
					} catch (InterruptedException e1) {
						log.error("pack iso process terminated", e1);
					}
				} catch (IOException e1) {
					log.error("exec mkisofs cmd error!", e1);
					return;
				}
			}

			// create domain and show some info
			if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
				msg.setEmulatorPath("/usr/bin/kvm");
				synchronized (NovaWorker.getInstance().getConnLock()) {
					try {
						System.out.println();
						System.out.println(Kvm.emitDomain(msg.getHashMap()));
						System.out.println();
						NovaWorker.getInstance().connectToKvm(virtService,
								false);
						Domain testDomain = NovaWorker
								.getInstance()
								.getConn()
								.domainCreateLinux(
										Kvm.emitDomain(msg.getHashMap()), 0);

						if (testDomain != null) {
							VnodeStatusDaemon
									.putStatus(UUID.fromString(testDomain
											.getUUIDString()),
											Vnode.Status.PREPARING);
							NovaWorker
									.getInstance()
									.getVnodeIP()
									.put(UUID.fromString(testDomain
											.getUUIDString()), msg.getIpAddr());
						}
						System.out.println("Domain:" + testDomain.getName()
								+ " id " + testDomain.getID() + " running "
								+ testDomain.getOSType());
						// Domain testDomain = conn.domainLookupByName("test");
						// System.out.println("xml desc\n" +
						// testDomain.getXMLDesc(0));
						// NovaWorker.getInstance().closeConnectToKvm();
					} catch (LibvirtException ex) {
						log.error("Create domain failed", ex);
					}
				}
			} else if (msg.getHyperVisor().equalsIgnoreCase("xen")) {
				// TODO @shayf add xen process
				log.error("xen not supported yet");
			} else {
				log.error("so such type hypervisor " + msg.getHyperVisor());
			}
		}

	}
}

package nova.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import nova.agent.api.messages.InstallApplianceMessage;
import nova.agent.api.messages.QueryApplianceStatusMessage;
import nova.agent.appliance.Appliance;
import nova.agent.appliance.ApplianceFetcher;
import nova.agent.appliance.FtpApplianceFetcher;
import nova.agent.daemons.AgentHeartbeatDaemon;
import nova.agent.daemons.AgentPerfInfoDaemon;
import nova.agent.daemons.ApplianceDownloadDaemon;
import nova.agent.daemons.ApplianceInstallDaemon;
import nova.agent.handler.AgentQueryHeartbeatHandler;
import nova.agent.handler.AgentQueryPerfHandler;
import nova.agent.handler.InstallApplianceHandler;
import nova.agent.handler.QueryApplianceStatusHandler;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleServer;
import nova.common.service.message.QueryHeartbeatMessage;
import nova.common.service.message.QueryPerfMessage;
import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;
import nova.common.util.Utils;
import nova.master.api.MasterProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The agent server model of nova
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class NovaAgent extends SimpleServer {

	// TODO @santa read this uuid from conf.
	UUID uuid = UUID.randomUUID();

	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(NovaAgent.class);

	/**
	 * Singleton instance of AgentServer.
	 */
	private static NovaAgent instance = null;

	InetSocketAddress bindAddr = null;

	/**
	 * All background working daemons for agent node.
	 */
	public SimpleDaemon[] daemons = new SimpleDaemon[] {
			new ApplianceDownloadDaemon(), new AgentHeartbeatDaemon(),
			new ApplianceInstallDaemon(), new AgentPerfInfoDaemon() };

	/**
	 * Connection to nova master.
	 */
	MasterProxy master = null;

	ConcurrentHashMap<String, Appliance> appliances = new ConcurrentHashMap<String, Appliance>();
	Gson gson = new GsonBuilder().serializeNulls().create();

	// TODO [future] support protocols other than ftp
	ApplianceFetcher fetcher = new FtpApplianceFetcher();

	/**
	 * Start a server and register some handler.
	 */
	private NovaAgent() {

		// create db/agent folder
		File agentDbFolder = new File(Utils.pathJoin(Utils.NOVA_HOME, "db",
				"agent"));
		agentDbFolder.mkdirs();

		registerHandler(QueryHeartbeatMessage.class,
				new AgentQueryHeartbeatHandler());
		registerHandler(QueryPerfMessage.class, new AgentQueryPerfHandler());
		registerHandler(QueryApplianceStatusMessage.class,
				new QueryApplianceStatusHandler());
		registerHandler(InstallApplianceMessage.class,
				new InstallApplianceHandler());
	}

	@Override
	public Channel bind(InetSocketAddress bindAddr) {
		this.bindAddr = bindAddr;
		logger.info("Nova agent running @ " + this.bindAddr);
		Channel chnl = super.bind(this.bindAddr);
		for (SimpleDaemon daemon : this.daemons) {
			daemon.start();
		}
		return chnl;
	}

	/**
	 * Override the shutdown() function, do a few housekeeping work.
	 */
	@Override
	public void shutdown() {
		logger.info("Shutting down AgentServer");
		// stop all daemons
		for (SimpleDaemon daemon : this.daemons) {
			daemon.stopWork();
		}
		for (SimpleDaemon daemon : this.daemons) {
			try {
				daemon.join();
			} catch (InterruptedException e) {
				logger.error("Error joining thread " + daemon.getName(), e);
			}
		}
		logger.info("All deamons stopped");
		super.shutdown();
		this.bindAddr = null;
		NovaAgent.instance = null;
	}

	public ConcurrentHashMap<String, Appliance> getAppliances() {
		return this.appliances;
	}

	/**
	 * Read info of all appliances from the apps.json file
	 */
	public void loadAppliances() {
		String filePath = Utils.pathJoin(Utils.NOVA_HOME, "db", "agent",
				"apps.json");
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);

			String line = null;
			StringBuffer appsJson = new StringBuffer();
			while ((line = br.readLine()) != null) {
				appsJson.append(line);
				appsJson.append("\n");
			}
			fr.close();

			Appliance[] appsArray = gson.fromJson(appsJson.toString(),
					Appliance[].class);

			this.appliances.clear();
			for (int i = 0; i < appsArray.length; i++) {
				this.appliances.put(appsArray[i].getName(), appsArray[i]);
			}

		} catch (FileNotFoundException e) {
			logger.error("Can't find the appliances list file: " + filePath, e);
		} catch (IOException e) {
			logger.error("Can't read from appliances list file: " + filePath, e);
		}

	}

	/**
	 * Write new appliances' info to the file
	 */
	public synchronized void saveAppliances() {
		Appliance[] appsArray = new Appliance[this.appliances.size()];
		int i = 0;
		for (Appliance app : this.appliances.values()) {
			appsArray[i] = app;
			i++;
		}
		String appList = gson.toJson(appsArray);
		String filePath = Utils.pathJoin(Utils.NOVA_HOME, "db", "agent",
				"apps.json");

		try {
			FileWriter fw = new FileWriter(filePath);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(appList);

			bw.flush();
			bw.close();
			fw.close();
		} catch (IOException e) {
			logger.error("Can't write new appliances list into file: "
					+ filePath, e);
		}

	}

	public ApplianceFetcher getApplianceFetcher() {
		return this.fetcher;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public void registerMaster(SimpleAddress xreply) {
		// FIXME @santa: bindAddr should not be 0.0.0.0!
		this.master = new MasterProxy(this.bindAddr);
		master.connect(xreply.getInetSocketAddress());
	}

	/**
	 * Get current master proxy.
	 * 
	 * @return Current master proxy. Could be <code>NULL</code>, when no master
	 *         node has connected.
	 */
	public MasterProxy getMaster() {
		return this.master;
	}

	/**
	 * Get the singleton of AgentServer.
	 * 
	 * @return AgentServer instance, singleton.
	 */
	public static synchronized NovaAgent getInstance() {
		if (NovaAgent.instance == null) {
			NovaAgent.instance = new NovaAgent();
		}
		return NovaAgent.instance;
	}

	public static void main(String[] args) {

		// add a shutdown hook, so a Ctrl-C or kill signal will be handled
		// gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// do cleanup work
				this.setName("cleanup");
				NovaAgent.getInstance().shutdown();
				logger.info("Cleanup work done");
			}
		});

		String bindHost = Conf.getString("agent.bind_host");
		Integer bindPort = Conf.getInteger("agent.bind_port");
		InetSocketAddress bindAddr = new InetSocketAddress(bindHost, bindPort);
		NovaAgent.getInstance().bind(bindAddr);

	}
}

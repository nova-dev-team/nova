package nova.test.agent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import nova.agent.NovaAgent;
import nova.agent.api.AgentProxy;
import nova.agent.appliance.Appliance;
import nova.common.util.Utils;
import nova.master.NovaMaster;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestAgent {

	@Test
	public void testStartupAgent() {
		InetSocketAddress bindAddr = new InetSocketAddress("127.0.0.1", 7783);

		NovaAgent.getInstance().bind(bindAddr);
		NovaAgent.getInstance().shutdown();
	}

	@Test
	public void testInstallAgent() {
		String agentHost = "127.0.0.1";
		int agentPort = 8173;

		String masterHost = "127.0.0.1";
		int masterPort = 9912;

		InetSocketAddress agentAddr = new InetSocketAddress(agentHost,
				agentPort);

		InetSocketAddress masterAddr = new InetSocketAddress(masterHost,
				masterPort);

		NovaAgent.getInstance().bind(agentAddr);
		NovaMaster.getInstance().bind(masterAddr);

		AgentProxy proxy = new AgentProxy(masterAddr);
		proxy.connect(agentAddr);

		proxy.sendInstallAppliance("demo1", "demo3");

		String[] appList = new String[] { "demo2", "demo4" };

		proxy.sendInstallAppliance(appList);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		NovaAgent.getInstance().shutdown();
		NovaMaster.getInstance().shutdown();

	}

	@Test
	public void testAppArrayToJson() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		ConcurrentHashMap<String, Appliance> appliances = new ConcurrentHashMap<String, Appliance>();
		appliances.put("demo1", new Appliance("demo1"));
		appliances.put("demo2", new Appliance("demo2"));
		Appliance[] appsArray1 = new Appliance[appliances.size()];

		int i = 0;
		for (Appliance app : appliances.values()) {
			appsArray1[i] = app;
			i++;
		}

		String appList = gson.toJson(appsArray1);
		System.out.println("Array to Json type: ");
		System.out.println(appList);

		Appliance[] appsArray2 = gson.fromJson(appList, Appliance[].class);
		System.out.println("Json to array;");
		for (i = 0; i < appsArray2.length; i++) {
			System.out.println(appsArray2[i].toString());
		}

		String filePath = Utils.pathJoin(Utils.NOVA_HOME, "db", "agent",
				"apps.json");

		System.out.println("Reassignment of appliances: ");
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);

			String appsList = br.readLine();
			Appliance[] appsArray = gson.fromJson(appsList, Appliance[].class);

			appliances.clear();
			for (i = 0; i < appsArray.length; i++) {
				appliances.put(appsArray[i].getName(), appsArray[i]);
			}

			br.close();
			fr.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Appliance app : appliances.values()) {
			System.out.println(app.toString());
		}

	}
}

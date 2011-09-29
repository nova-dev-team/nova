package nova.test.functional.agent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;
import nova.agent.NovaAgent;
import nova.agent.api.AgentProxy;
import nova.agent.appliance.Appliance;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.master.NovaMaster;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestAgent {

	@Test
	public void testStartupAgent() {
		NovaAgent.getInstance().start();
		NovaAgent.getInstance().shutdown();
	}

	@Test
	public void testAgentSendToMaster() {
	}

	@Test
	public void testInstallAgent() {
		NovaAgent.getInstance().start();
		NovaMaster.getInstance().start();

		AgentProxy proxy = new AgentProxy(NovaMaster.getInstance().getAddr());

		proxy.connect(NovaAgent.getInstance().getAddr().getInetSocketAddress());
		String[] appList1 = new String[] { "demo1", "demo3" };
		proxy.sendInstallAppliance(appList1);

		String[] appList2 = new String[] { "demo2", "demo4" };
		proxy.sendInstallAppliance(appList2);

		proxy.sendRequestHeartbeat();

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (Appliance app : NovaAgent.getInstance().getAppliances().values())
			Assert.assertEquals(Appliance.Status.DOWNLOAD_FAILURE,
					app.getStatus());

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

		String relativePath = Conf.getString("agent.software.save_path");
		String filePath = Utils.pathJoin(Utils.NOVA_HOME, relativePath,
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

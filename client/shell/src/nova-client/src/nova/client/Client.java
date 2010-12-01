package nova.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import nova.Interface.IVMMigrationCallback;
import nova.Interface.IVMPowerCallback;
import nova.Interface.VM_HANDLE;
import nova.model.Pmachine;
import nova.model.PmachineStatus;
import nova.model.Vmachine;
import nova.shell.DebugEnv;

public class Client extends Thread{
	
	private String baseUrl = null;
	private String userName = null;
	private String passwd = null;
	DefaultHttpClient client = null;
	
	public Client(String baseUrl, String userName, String passwd) {
		if (baseUrl.startsWith("http://") == false) {
			baseUrl = "http://" + baseUrl;
		}
		if (baseUrl.endsWith("/")) {
			// discard the trailing '/'
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		this.baseUrl = baseUrl;
		this.userName = userName;
		this.passwd = passwd;
		this.client = new DefaultHttpClient();
		
		try {
			//getRequest("sessions/create.json?login=" + this.userName + "&password=" + this.passwd);
			
			HashMap<String, String> loginParams = new HashMap<String, String>();
			loginParams.put("login", this.userName);
			loginParams.put("password", this.passwd);
			
			postRequest("sessions/create", loginParams);
		} catch (IOException e) {
			this.client = null;
			e.printStackTrace();
		}
	}
	
	public String getBaseUrl() {
		return this.baseUrl;
	}
	
	private JsonElement getRequest(String reqHandler) throws IOException {
		if (reqHandler.startsWith("/")) {
			// remove the leading '/'
			reqHandler = reqHandler.substring(1);
		}
		if (reqHandler.endsWith(".json") == false) {
			reqHandler = reqHandler + ".json";
		}
		String fullUrl = this.baseUrl + '/' + reqHandler;
		
		if( DebugEnv.print_debug_info ) System.out.println(fullUrl);
		HttpGet get = new HttpGet(fullUrl);
	
		HttpResponse resp = client.execute(get);
		BufferedReader br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
//		if( DebugEnv.print_debug_info ) System.out.println(sb.toString());
		JsonParser parser = new JsonParser();
		JsonElement elem = parser.parse(sb.toString());
		br.close();
		return elem;
	}
	
	private JsonElement postRequest(String reqHandler, Map<String, String> params) throws IOException {
		
		if (reqHandler.startsWith("/")) {
			// remove the leading '/'
			reqHandler = reqHandler.substring(1);
		}
		if (reqHandler.endsWith(".json") == false) {
			reqHandler = reqHandler + ".json";
		}
		String fullUrl = this.baseUrl + '/' + reqHandler;
		if( DebugEnv.print_debug_info ) System.out.println(fullUrl);
		HttpPost post = new HttpPost(fullUrl);
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (String key : params.keySet()) {
			formparams.add(new BasicNameValuePair(key, params.get(key)));	
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		post.setEntity(entity);
		
		HttpResponse resp = client.execute(post);
		BufferedReader br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		if( DebugEnv.print_debug_info ) System.out.println(sb.toString());
		JsonParser parser = new JsonParser();
		JsonElement elem = parser.parse(sb.toString());
		br.close();
		return elem;
	}
	
	public List<Pmachine> listPmachines() {
		ArrayList<Pmachine> list = new ArrayList<Pmachine>();
		try {
			JsonElement elem = getRequest("pmachines/list");
			JsonObject obj = elem.getAsJsonObject();
			
			if (obj.get("success").getAsBoolean() == false) {
				list = null;
			} else {
				JsonArray pmachineArray = obj.get("data").getAsJsonArray();
				for (JsonElement pmElem : pmachineArray) {
					JsonObject pmObj = pmElem.getAsJsonObject();
					Pmachine pm = new Pmachine();
					
					String statusText = pmObj.get("status").getAsString();
					if (statusText.equals("working")) {
						pm.status = PmachineStatus.WORKING;
					} else if (statusText.equals("pending")) {
						pm.status = PmachineStatus.PENDING;
					} else if (statusText.equals("failure")) {
						pm.status = PmachineStatus.FAILURE;
					} else if (statusText.equals("retired")) {
						pm.status = PmachineStatus.RETIRED;
					}
					pm.vmCapacity = pmObj.get("vm_capacity").getAsInt();
					pm.vmPreparing = pmObj.get("vm_preparing").getAsInt();
					pm.vmFailure = pmObj.get("vm_failure").getAsInt();
					pm.hostname = pmObj.get("hostname").getAsString();
					pm.id = pmObj.get("id").getAsInt();
					pm.vmRunning = pmObj.get("vm_running").getAsInt();
					pm.ip = pmObj.get("ip").getAsString();
					
					if( DebugEnv.print_debug_info ) System.out.println(pm);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public List<Vmachine> listVclusters() {
		ArrayList<Vmachine> list = new ArrayList<Vmachine>();
		try {
			JsonElement elem = getRequest("vclusters/list");
			JsonObject obj = elem.getAsJsonObject();
			
			if (obj.get("success").getAsBoolean() == false) {
				list = null;
			} else {
				JsonArray vmachineArray = obj.get("data").getAsJsonArray();
				for (JsonElement vmElem : vmachineArray) {
					JsonObject vmObj = vmElem.getAsJsonObject();
					Vmachine vm = new Vmachine();
					
					vm.setName(vmObj.get("name").getAsString());
					list.add(vm);
//					(vm);		
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public JsonObject getVclustersInfo(String cluster_name) {
		try {
			HashMap<String, String> showParams = new HashMap<String, String>();
			showParams.put("name", cluster_name);
			JsonElement elem = postRequest("vclusters/show", showParams);
			JsonObject obj = elem.getAsJsonObject();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JsonObject getVmachineInfo(String uuid) {
		try {
			HashMap<String, String> showParams = new HashMap<String, String>();
			showParams.put("uuid", uuid);
			JsonElement elem = postRequest("vmachines/show_info", showParams);
			JsonObject obj = elem.getAsJsonObject();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
     * 获取指定物理机上所有已注册的虚拟机句柄
     *
     * @param addr 指定物理机的网络地址
     * @return 返回虚拟机操作句柄数组
     */
	public JsonObject getRunningVmachines(InetAddress addr) {
		try {
			HashMap<String, String> showParams = new HashMap<String, String>();
			showParams.put("ip", addr.getHostAddress());
			JsonElement elem = postRequest("pmachines/show_info", showParams);
			JsonObject obj = elem.getAsJsonObject();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean operateVmachine(String uuid, String sched_to, IVMPowerCallback callback, String operation) {
		try {
			HashMap<String, String> startParams = new HashMap<String, String>();
			startParams.put("uuid", uuid);
			startParams.put("sched_to", sched_to);
			JsonElement elem = postRequest("vmachines/" + operation, startParams);
			JsonObject obj = elem.getAsJsonObject();
			if( obj.get("success").getAsBoolean() == false) {
				VM_HANDLE vh = new VM_HANDLE();
				vh.value = uuid;
				callback.onError(vh, 0);//0 and the 1 below need to be macros
				return false;
			} else {
//				if( DebugEnv.print_debug_info ) System.out.println("startVmachine finished successfully");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			VM_HANDLE vh = new VM_HANDLE();
			vh.value = uuid;
			callback.onError(vh, 1);
			return false;
		}
	}
	
	/**
	 * start 并且指定运行物理机的Ip时使用
	 */
	public boolean operateVmachine(String uuid, IVMPowerCallback callback, String operation) {
		try {
			HashMap<String, String> startParams = new HashMap<String, String>();
			startParams.put("uuid", uuid);
			JsonElement elem = postRequest("vmachines/" + operation, startParams);
			JsonObject obj = elem.getAsJsonObject();
			if( obj.get("success").getAsBoolean() == false) {
				VM_HANDLE vh = new VM_HANDLE();
				vh.value = uuid;
				callback.onError(vh, 0);//0 and the 1 below need to be macros
				return false;
			} else {
//				if( DebugEnv.print_debug_info ) System.out.println("startVmachine finished successfully");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			VM_HANDLE vh = new VM_HANDLE();
			vh.value = uuid;
			callback.onError(vh, 1);
			return false;
		}
	}
	
	public boolean migrateVmachine(String uuid, InetAddress desaddr, IVMMigrationCallback callback) {
		InetAddress srcaddr = this.getHostIp(uuid);
		try {
			VM_HANDLE vh = new VM_HANDLE();
			vh.value = uuid;
			
			HashMap<String, String> migrateParams = new HashMap<String, String>();
			migrateParams.put("vm_uuid", uuid);
			migrateParams.put("dest_ip", desaddr.getHostAddress());
			JsonElement elem = postRequest("migration/live_migrate", migrateParams);
			JsonObject obj = elem.getAsJsonObject();
			if( obj.get("success").getAsBoolean() == false) {
				
				callback.OnError(vh, srcaddr, desaddr);//0 and the 1 below need to be macros
				return false;
			} else {
//				if( DebugEnv.print_debug_info ) System.out.println("startVmachine finished successfully");
				callback.onMigrationBegin(vh, srcaddr, desaddr);
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			VM_HANDLE vh = new VM_HANDLE();
			vh.value = uuid;
			callback.OnError(vh, srcaddr, desaddr);//0 and the 1 below need to be macros
			return false;
		}
	}
	
	public InetAddress getHostIp(String uuid) {
		try {
			HashMap<String, String> hostParams = new HashMap<String, String>();
			hostParams.put("uuid", uuid);
			JsonElement elem = postRequest("vmachines/host_ip", hostParams);
			JsonObject obj = elem.getAsJsonObject();
			String host_ip = obj.get("host_ip").getAsString();
//			if( DebugEnv.print_debug_info ) System.out.println(host_ip);
			return this.stringToInetAddress(host_ip);
		} catch (IOException e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public boolean createVcluster(String name , int size, String createpara) {
		try {
			HashMap<String, String> createParams = new HashMap<String, String>();
			createParams.put("name", name);
			createParams.put("size", String.valueOf(size));
			createParams.put("machines", createpara);
			JsonElement elem = postRequest("vclusters/create", createParams);
			JsonObject obj = elem.getAsJsonObject();
			return obj.get("success").getAsBoolean();

		} catch (IOException e) {
//			e.printStackTrace();
			return false;
		}
	}
	
	InetAddress stringToInetAddress(String strIp) {
        int[] intAddr = new int[4];
        int[] intAddrOK = new int[4];
        String[] strAddrOK = new String[4];
        byte[] byteAddrOK = new byte[4];
        InetAddress ia = null;
       //将String用正则表达式进行分解
        String[] strArray = strIp.split("\\.");
        for (int i = 0; i < 4; i++) {
           intAddr[i] = Integer.valueOf(strArray[i]).intValue();
           if ((intAddr[i] <= 127) && (intAddr[i] >= -127))
               intAddrOK[i] = intAddr[i];
           else
               intAddrOK[i] = intAddr[i] - 256;
           // int 转换到 string
           strAddrOK[i] = String.valueOf(intAddrOK[i]);
           // string 转换到byte
           byteAddrOK[i] = Byte.parseByte(strAddrOK[i]);
        }
        try {
           ia = InetAddress.getByAddress(byteAddrOK);
        } catch (UnknownHostException e) {
           e.printStackTrace();
        }
        return ia;
    }
}

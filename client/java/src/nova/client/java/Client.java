package nova.client.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import nova.client.java.model.Pmachine;
import nova.client.java.model.PmachineStatus;

public class Client {
	
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
		System.out.println(fullUrl);
		HttpGet get = new HttpGet(fullUrl);
	
		HttpResponse resp = client.execute(get);
		BufferedReader br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		System.out.println(sb.toString());
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
		System.out.println(fullUrl);
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
		System.out.println(sb.toString());
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
					
					System.out.println(pm);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

}

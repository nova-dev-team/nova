package nova.conf;

import java.util.HashMap;
import java.util.List;

import nova.shell.DebugEnv;

import org.jdom.Element;

public class Configuration {

	private HashMap<String,String> hm = null;
	private String key;
	
	public Configuration(String confDir) {
		super();
		hm = new HashMap();
		//set nova configuration
		XMLParser xmlparser = new XMLParser();
		List<Element> propertyList = xmlparser.parserXml(confDir + "./nova-conf.xml");
		for (int i = 0; i < propertyList.size(); i++) {
			Element property = (Element) propertyList.get(i);
			List<Element> propertyInfo = property.getChildren();
			for (int j = 0; j < propertyInfo.size(); j++) {
				if( DebugEnv.print_debug_info ) System.out.println((propertyInfo.get(j)).getName() + ":" + ((Element) propertyInfo.get(j)).getValue());
				if((propertyInfo.get(j)).getName().equals("name")) {
					key = ((Element) propertyInfo.get(j)).getValue();
				}  else if((propertyInfo.get(j)).getName().equals("value")) {
					hm.put(key, ((Element) propertyInfo.get(j)).getValue());
				}
			}
		}
	}
	
	public String getMaster_ip() {
		return hm.get("master_ip");
	}
	
	public String getUsername() {
		return hm.get("username");
	}
	
	public String getPasswd() {
		return hm.get("passwd");
	}
}

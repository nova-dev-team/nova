package nova.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nova.Interface.IVCControl;
import nova.Interface.Machine;
import nova.client.VCControl;
import nova.conf.XMLParser;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class VMCreateFile {
	private HashMap<String, String> hm = null;
	List<Machine> l = new ArrayList<Machine>();
	private String key;
	private String clusterName;

	public VMCreateFile(String filePath) {
		super();
		try {
			hm = new HashMap();
			// set nova configuration
			SAXBuilder builder = new SAXBuilder(false);
			Document doc = builder.build(filePath);
			Element cluster = doc.getRootElement();
			clusterName = cluster.getAttributeValue("name");
			List<Element> propertyList = cluster.getChildren("node");
			for (int i = 0; i < propertyList.size(); i++) {
				Element property = (Element) propertyList.get(i);
				List<Element> propertyInfo = property.getChildren();
				for (int j = 0; j < propertyInfo.size(); j++) {
					if( DebugEnv.print_debug_info ) System.out.println((propertyInfo.get(j)).getName() + ":"
							+ ((Element) propertyInfo.get(j)).getValue());
					hm.put((propertyInfo.get(j)).getName(),
							((Element) propertyInfo.get(j)).getValue());
				}
				Machine m = new Machine(this.getImage(), this.getName(),
						this.getCupn(), this.getMem(), this.getSoft(), this.getSched_to());
				l.add(m);
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void creatCluster(String ip, String user, String passwd) {
		IVCControl ivcc = new VCControl(ip, user, passwd);
		if( ivcc.createVM(clusterName, l) )
			System.out.println("vcluster create successful!");
		else
			System.out.println("vcluster create failure!");
	}

	public String getImage() {
		return hm.get("image");
	}

	public String getName() {
		return hm.get("name");
	}

	public String getCupn() {
		return hm.get("cupn");
	}

	public String getMem() {
		return hm.get("mem");
	}

	public String getSoft() {
		return hm.get("soft");
	}
	
	public String getSched_to() {
		return hm.get("sched_to");
	}
}

package nova.worker.virt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import nova.common.util.Utils;

public class Kvm {

	public static String emitDomain(Map<String, Object> params) {
		String template = null;
		URL url = Kvm.class.getResource("kvm-domain-template.xml");
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					url.getFile())));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			template = sb.toString();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Utils.expandTemplate(template, params);
	}

}

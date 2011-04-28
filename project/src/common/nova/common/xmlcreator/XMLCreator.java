package nova.common.xmlcreator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.stringtree.template.EasyTemplater;

public class XMLCreator {
	public static void createXML(String filepath, String filename,
			String[] templ, String[] values) {
		if (templ.length != values.length)
			return;
		EasyTemplater templater = new EasyTemplater(filepath);
		int n = templ.length;
		for (int i = 0; i < n; i++) {
			templater.put(templ[i], values[i]);
		}

		System.out.println(templater.toString(filename));

		String outfile = filename + ".xml";

		try {
			BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outfile)));
			BW.flush();
			BW.write(templater.toString("person"));
			BW.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
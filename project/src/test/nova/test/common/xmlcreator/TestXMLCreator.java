package nova.test.common.xmlcreator;

import nova.common.xmlcreator.XMLCreator;

import org.junit.Test;

public class TestXMLCreator {
	@Test
	public void testCreateXML() {
		String filepath = "./";
		String filename = "person";
		String[] templ = { "name", "phone" };
		String[] values = { "shayf", "123456" };
		XMLCreator.createXML(filepath, filename, templ, values);
	}
}

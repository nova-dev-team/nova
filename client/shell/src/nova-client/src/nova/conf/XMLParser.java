package nova.conf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author frankvictor@qq.com JDOM JDOM 生成与解析XML文档
 *
 */
public class XMLParser {

	public void createXml(String fileName) {
		Document document;
		Element root;
		root = new Element("employees");
		document = new Document(root);
		Element employee = new Element("employee");
		root.addContent(employee);
		Element name = new Element("name");
		name.setText("ddvip");
		employee.addContent(name);
		Element sex = new Element("sex");
		sex.setText("m");
		employee.addContent(sex);
		Element age = new Element("age");
		age.setText("23");
		employee.addContent(age);
		XMLOutputter XMLOut = new XMLOutputter();
		try {
			XMLOut.output(document, new FileOutputStream(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public List<Element> parserXml(String fileName) {
		SAXBuilder builder = new SAXBuilder(false);
		try {
			Document document = builder.build(fileName);
			Element properties = document.getRootElement();
			List<Element> propertyList = properties.getChildren("property");
			return propertyList;
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		XMLParser jDom = new XMLParser();
		jDom.parserXml("/home/frankvictor/lab/nova/novashell/conf/nova-conf.xml");
	}
}

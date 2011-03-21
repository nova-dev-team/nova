package nova.test.gson;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.google.gson.Gson;

public class TestGson {

	@Test
	public void testBasicTypes() {
		Gson gson = new Gson();
		System.out.println(gson.toJson(1));
		System.out.println(gson.toJsonTree(new ArrayList<String>()));

		String str = gson.fromJson("[\"sdf\"]", String.class);
		System.out.println(str);

		String[] l = {};
		String[] lst = gson.fromJson("[\"sd\", \"f\"]", l.getClass());
		for (String s : lst) {
			System.out.println(s);
		}

		HashMap<String, Integer> m = new HashMap<String, Integer>();

		m.put("wtf", 1);
		m.put("wtf2", 4);

		System.out.println(gson.toJson(m));

		SomeObject so = gson
				.fromJson("{text:\"a\", value:2}", SomeObject.class);

		System.out.println(so);
		System.out.println(gson.toJson(new SomeObject("blah", 3)));
	}
}

class SomeObject {

	public String text;

	public Integer value;

	public SomeObject() {
		this.text = null;
		this.value = null;
	}

	public SomeObject(String text, Integer value) {
		this.text = text;
		this.value = value;
	}

	@Override
	public String toString() {
		return this.text + ", " + value;
	}

}

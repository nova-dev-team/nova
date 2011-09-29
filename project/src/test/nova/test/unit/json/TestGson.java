package nova.test.unit.json;

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

		m.put("test", 1);
		m.put("test2", 4);

		System.out.println(gson.toJson(m));

		DemoJsonObject so = gson.fromJson("{text:\"a\", value:2}",
				DemoJsonObject.class);

		System.out.println(so);
		System.out.println(gson.toJson(new DemoJsonObject("blah", 3)));
	}
}

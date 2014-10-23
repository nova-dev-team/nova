package nova.test.unit.json;

import java.util.Map;

import junit.framework.Assert;

import org.json.simple.JSONValue;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestJsonSimple {

    @Test
    public void testBasicTypes() {

        Boolean b = (Boolean) JSONValue.parse("true");
        Assert.assertTrue(b.equals(true));
        Assert.assertTrue(b);

        b = (Boolean) JSONValue.parse("false");
        Assert.assertTrue(b.equals(false));
        Assert.assertFalse(b);

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testObject() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        DemoJsonObject empty = new DemoJsonObject();
        String emptyJson = gson.toJson(empty);
        System.out.println(emptyJson);

        Map m = (Map) JSONValue.parse(emptyJson);
        for (Object k : m.keySet()) {
            System.out.println(k + " -> " + m.get(k));
            Assert.assertNull(m.get(k));
        }

        DemoJsonObject so = new DemoJsonObject("foo", 34);
        String soJson = gson.toJson(so);
        System.out.println(soJson);

        m = (Map) JSONValue.parse(soJson);
        for (Object k : m.keySet()) {
            System.out.println(k + " -> " + m.get(k));
            Assert.assertNotNull(m.get(k));
        }

        m = (Map) JSONValue
                .parse("{\"text\":\"bar\", \"value\":\"not an int vlaue!\"}");
        for (Object k : m.keySet()) {
            System.out.println(k + " -> " + m.get(k));
            Assert.assertNotNull(m.get(k));
        }

    }
}

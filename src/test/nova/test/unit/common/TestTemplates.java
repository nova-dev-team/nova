package nova.test.unit.common;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import nova.common.util.Utils;

import org.junit.Test;

public class TestTemplates {

    @Test
    public void testCreateXML() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("name", "shayf");
        values.put("phone", "123456");
        String output = Utils.expandTemplate(
                "<person><name>${name}</name><phone>${phone}</phone></person>",
                values);
        System.out.println(output);
        Assert.assertEquals(output,
                "<person><name>shayf</name><phone>123456</phone></person>");
    }
}

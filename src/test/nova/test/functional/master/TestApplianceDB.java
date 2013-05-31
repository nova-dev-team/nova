package nova.test.functional.master;

import junit.framework.TestCase;
import nova.master.models.Appliance;

import org.junit.Test;

public class TestApplianceDB extends TestCase {

    @Test
    public void testSave() {
        Appliance soft = new Appliance();
        soft.setFileName("Eclipse");
        soft.setDisplayName("Eclips");
        soft.setDescription("Extensible Tool Platform and Java IDE");
        soft.setOsFamily("win7,Ubuntu");

        soft.save();

        Appliance.delete(soft);

    }

}

package nova.test.master;

import junit.framework.TestCase;
import nova.master.models.Appliance;

import org.junit.Test;

public class TestApplianceDB extends TestCase {

	@Test
	public void testSave() {
		Appliance soft = new Appliance();
		soft.setFileName("blah");
		soft.setDisplayName("haha");
		soft.setDescription("blahlalalal");
		soft.setOsFamily("win7,Ubuntu");

		soft.save();

	}

}

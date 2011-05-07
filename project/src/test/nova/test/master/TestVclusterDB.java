package nova.test.master;

import junit.framework.TestCase;
import nova.master.models.MasterDb;
import nova.master.models.Vcluster;

import org.junit.Test;

public class TestVclusterDB extends TestCase {

	@Test
	public void testSave() {
		Vcluster vcluster = new Vcluster();
		vcluster.setClusterName("cluster123");
		vcluster.setClusterSize(7);
		vcluster.setFristIp("10.0.0.1");
		vcluster.setOsUsername("zhaoxun");
		vcluster.setOsPassword("liquid");
		vcluster.setSshPrivateKey("sshprivatekey");
		vcluster.setSshPublicKey("sshpublickey");
		vcluster.setUserId(123);

		MasterDb.save(vcluster);

		Vcluster vclusterRead = (Vcluster) MasterDb.load(Vcluster.class,
				vcluster.getId());

		System.out.println(vclusterRead);

	}

}

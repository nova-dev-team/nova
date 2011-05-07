package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.master.models.Vcluster;

import org.hibernate.Session;
import org.hibernate.Transaction;
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

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(vcluster);
		tx.commit();
		session.close();
		HibernateUtil.shutdown();

		Session sessionread = HibernateUtil.getSessionFactory().openSession();
		Vcluster vclusterread = new Vcluster();
		sessionread.load(vclusterread, vcluster.getId());

	}

}

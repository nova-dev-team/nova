package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.master.models.Appliance;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestApplianceDB extends TestCase {
	public void testSave() {
		Appliance soft = new Appliance();
		soft.setFileName("blah");
		soft.setDisplayName("haha");
		soft.setDescription("blahlalalal");
		soft.setOsFamily("win7,Ubuntu");
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(soft);
		tx.commit();
		session.close();
		HibernateUtil.shutdown();

		// Session sessionread =
		// HibernateUtil.getSessionFactory().openSession();
		// SoftwarePackage softread = new SoftwarePackage();
		// sessionread.load(softread, soft.getId());
	}

}

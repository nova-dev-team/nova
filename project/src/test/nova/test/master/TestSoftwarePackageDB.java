package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.master.models.SoftwarePackage;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestSoftwarePackageDB extends TestCase {
	public void testSave() {
		SoftwarePackage soft = new SoftwarePackage();
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

		Session sessionread = HibernateUtil.getSessionFactory().openSession();
		SoftwarePackage softread = new SoftwarePackage();
		sessionread.load(softread, soft.getId());
	}

}

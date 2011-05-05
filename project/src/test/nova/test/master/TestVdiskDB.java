package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.master.models.Vdisk;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestVdiskDB extends TestCase {

	public void testSave() {
		Vdisk vdisk = new Vdisk();
		vdisk.setFileName("blah");
		vdisk.setDisplayName("haha");
		vdisk.setDiskFormat(".qcow2");
		vdisk.setOsFamily("linux");
		vdisk.setDescription("lalalala");
		vdisk.setOsName("Ubuntu 11.04");
		vdisk.setSoftList("blahblah");

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(vdisk);
		tx.commit();
		session.close();
		HibernateUtil.shutdown();
	}

}

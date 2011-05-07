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
		vdisk.setOsName("Ubuntu 9.10");
		vdisk.setSoftList("blahblah");

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(vdisk);
		tx.commit();
		session.close();
		HibernateUtil.shutdown();

		Session sessionread = HibernateUtil.getSessionFactory().openSession();
		Vdisk vdiskread = new Vdisk();
		sessionread.load(vdiskread, vdisk.getId());
		System.out.print("\n\nFileName: " + vdiskread.getFileName()
				+ "\nDisplayName: " + vdiskread.getDisplayName()
				+ "\nDiskFormat: " + vdiskread.getDiskFormat() + "\nOsFamily: "
				+ vdiskread.getOsFamily() + "\nDescription: "
				+ vdiskread.getDescription() + "\nOsName: "
				+ vdiskread.getOsName() + "\nSoftList: "
				+ vdiskread.getSoftList() + "\n\n");
		sessionread.close();
		HibernateUtil.shutdown();

		// Session sessionquery =
		// HibernateUtil.getSessionFactory().openSession();
		// String hql = "from User user";
		// List<Vdisk> listvdisk = sessionquery.createQuery(hql).list();

	}

}

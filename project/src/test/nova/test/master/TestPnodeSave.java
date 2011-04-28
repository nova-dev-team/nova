package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.master.models.Pnode1;

import org.hibernate.Session;
import org.hibernate.Transaction;


public class TestPnodeSave extends TestCase {
	public void testSave() {
		Pnode1 pnode = new Pnode1();
		pnode.setHostname("nina1");
		pnode.setIp("127.0.0.1");
		pnode.setMacAddress("lalalalalalalala");
		pnode.setStatus("running");
		pnode.setUuid("lalalalalalala");
		pnode.setVmCapacity(7);
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(pnode);
		tx.commit();
		session.close();
		HibernateUtil.shutdown();
	}

}
package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.common.service.SimpleAddress;
import nova.master.models.Pnode;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestPnodeSave extends TestCase {

	public void testSave() {
		SimpleAddress addr = new SimpleAddress("0.0.0.0", 2222);
		Pnode pnode = new Pnode();
		pnode.setAddr(addr);
		pnode.setHostname("nina1");
		// pnode.setIp("127.0.0.1");
		pnode.setMacAddress("lalalalalalalala");
		pnode.setStatus(Pnode.Status.RUNNING);
		pnode.setUuid("lalalalalalala");
		pnode.setVmCapacity(7);
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(pnode);
		tx.commit();

		// TODO @zhaoxun Load the saved pnode, check if all fields are correct

		session.close();
		HibernateUtil.shutdown();
	}

}

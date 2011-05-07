package nova.test.master;

import nova.common.db.HibernateUtil;
import nova.master.models.Pnode;
import nova.master.models.Pnode.Status;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

public class TestPnodeDB {

	@Test
	public void testSave() {

		Pnode pnode = new Pnode();
		pnode.setHostname("blah");
		pnode.setIp("127.0.0.1");
		pnode.setMacAddress("mac_addr");
		pnode.setPort(1234);
		pnode.setStatus(Status.ADD_PENDING);

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(pnode);
		tx.commit();

		Pnode pnodeLoad = (Pnode) session.load(Pnode.class, pnode.getId());
		System.out.println(pnodeLoad.getIp());

		session.close();
		HibernateUtil.shutdown();

	}
}

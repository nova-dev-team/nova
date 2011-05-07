package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.common.service.SimpleAddress;
import nova.master.models.Pnode;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestPnodeDB extends TestCase {

	public void testSave() {
		SimpleAddress addr = new SimpleAddress("0.0.0.0", 2222);
		Pnode pnode = new Pnode();

		pnode.setStatus(Pnode.Status.RUNNING);
		pnode.setAddr(addr);
		pnode.setPnodeId(1);
		pnode.setHostname("nina1");
		pnode.setUuid("lalalalalalala");
		pnode.setMacAddress("lalalalalalalala");
		pnode.setVmCapacity(7);

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(pnode);
		tx.commit();
		session.close();
		HibernateUtil.shutdown();

		System.out.println(pnode.getId());

		Session sessionread = HibernateUtil.getSessionFactory().openSession();
		Pnode pnoderead = new Pnode();
		sessionread.load(pnoderead, pnode.getId());
		/*
		 * System.out.print("\n\nHostName: " + pnoderead.getHostname() +
		 * "\nAddr: " + pnoderead.getAddr() + "\nMacAddress: " +
		 * pnoderead.getMacAddress() + "\nStatus: " + pnoderead.getStatus() +
		 * "\nUuid: " + pnoderead.getUuid() + "\nVmCapacity: " +
		 * pnoderead.getVmCapacity() + "\n\n"); //sessionread.close();
		 * HibernateUtil.shutdown();
		 */
	}
}

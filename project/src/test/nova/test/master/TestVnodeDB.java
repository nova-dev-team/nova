package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.common.service.SimpleAddress;
import nova.master.models.Vnode;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestVnodeDB extends TestCase {

	public void testSave() {
		SimpleAddress addr = new SimpleAddress("0.0.0.0", 2222);
		Vnode vnode = new Vnode();
		vnode.setAddr(addr);
		vnode.setName("vm1");
		vnode.setCdrom("lalalalalalalala");
		vnode.setStatus(Vnode.Status.RUNNING);
		vnode.setUuid("lalalalalalala");
		vnode.setCpuCount(7);
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(vnode);
		tx.commit();
		session.close();
		HibernateUtil.shutdown();

		Session sessionread = HibernateUtil.getSessionFactory().openSession();
		Vnode vnoderead = new Vnode();
		sessionread.load(vnoderead, vnode.getId());
	}

}

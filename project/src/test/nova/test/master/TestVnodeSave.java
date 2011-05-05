package nova.test.master;

import junit.framework.TestCase;
import nova.common.db.HibernateUtil;
import nova.common.service.SimpleAddress;
import nova.master.models.Vnode;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestVnodeSave extends TestCase {

	public void testSave() {
		SimpleAddress addr = new SimpleAddress("0.0.0.0", 2222);
		Vnode vnode = new Vnode();
		vnode.setAddr(addr);
		vnode.setName("vm1");
		// pnode.setIp("127.0.0.1");
		vnode.setCdrom("lalalalalalalala");
		vnode.setStatus(Vnode.Status.RUNNING);
		vnode.setUuid("lalalalalalala");
		vnode.setCpuCount(7);
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		session.save(vnode);
		tx.commit();

		// TODO @zhaoxun Load the saved pnode, check if all fields are correct

		session.close();
		HibernateUtil.shutdown();
	}

}

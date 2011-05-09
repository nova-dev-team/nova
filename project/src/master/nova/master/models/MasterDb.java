package nova.master.models;

import java.io.Serializable;
import java.util.List;

import nova.common.db.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class MasterDb {

	private static Session session = HibernateUtil.getSessionFactory()
			.openSession();

	@SuppressWarnings("rawtypes")
	public static synchronized List queryResult(String query) {
		List ret = session.createQuery(query).list();
		return ret;
	}

	@SuppressWarnings("rawtypes")
	public static synchronized Object load(Class klass, Serializable arg) {
		Object ret = session.load(klass, arg);
		return ret;
	}

	public static synchronized void save(Object obj) {
		Transaction tx = session.beginTransaction();
		session.save(obj);
		tx.commit();
	}

}

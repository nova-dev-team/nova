package nova.master.models;

import java.io.Serializable;

import nova.common.db.HibernateUtil;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class MasterDb {

	/**
	 * Database session.
	 */
	private static Session session = HibernateUtil.getSessionFactory()
			.openSession();

	public static Query createQuery(String query) {
		return session.createQuery(query);
	}

	public static void save(Object obj) {
		Transaction tx = session.beginTransaction();
		session.save(obj);
		tx.commit();
	}

	@SuppressWarnings("rawtypes")
	public static Object load(Class klass, Serializable arg) {
		return session.load(klass, arg);
	}

}

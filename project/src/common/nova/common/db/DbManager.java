package nova.common.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class DbManager {

	private static Session session = HibernateUtil.getSessionFactory()
			.openSession();

	private static Map<Class, DbManager> allDbManager = new HashMap<Class, DbManager>();

	private Class klass = null;

	private DbSpec spec = null;

	private List<Object> cache = new ArrayList();

	// TODO @santa make all members concurrnt
	private Map<String, Map<Serializable, Object>> allIndex = new HashMap<String, Map<Serializable, Object>>();

	private DbManager(Class klass, DbSpec spec) {
		this.spec = spec;
		this.klass = klass;
		for (String colName : this.spec.getAllIndex()) {
			allIndex.put(colName, new HashMap<Serializable, Object>());
		}
		// TODO @santa load data
		/*
		 * for (Object obj : session.createQuery("from " +
		 * klass.getSimpleName()) .list()) { cache.add((Object) obj); }
		 */
	}

	public Object findById(Serializable key) {
		return findBy("id", key);
	}

	public Object findBy(String colName, Serializable key) {
		Map<Serializable, Object> index = allIndex.get(colName);
		if (index == null) {
			throw new IllegalArgumentException("No DbSpec index on colume: "
					+ colName);
		}
		return index.get(key);
	}

	public List<Object> all() {
		return cache;
	}

	public Map<Serializable, Object> getIndex(String colName) {
		return this.allIndex.get(colName);
	}

	public void saveEx(Object obj) {
		Transaction tx = session.beginTransaction();
		session.save(obj);
		tx.commit();
	}

	public static DbManager forClass(Class klass, DbSpec spec) {
		if (allDbManager.containsKey(klass) == false) {
			DbManager dbm = new DbManager(klass, spec);
			allDbManager.put(klass, dbm);
		}
		return allDbManager.get(klass);
	}

}

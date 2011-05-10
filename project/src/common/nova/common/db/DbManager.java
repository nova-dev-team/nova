package nova.common.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DbManager {

	static final Map<Class, DbManager> allDbManager;

	static {
		System.err.println("called");

		allDbManager = new HashMap<Class, DbManager>();
		System.err.println(allDbManager);
	}

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

		String queryText = "from " + klass.getSimpleName();
		System.err.println(queryText);
		Session session = HibernateUtil.getSessionFactory().openSession();
		System.err.println(session);
		Query query = session.createQuery(queryText);
		for (Object obj : query.list()) {
			cache.add((Object) obj);
		}

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
		Session session = HibernateUtil.getSessionFactory().openSession();
		System.err.println(session);
		Transaction tx = session.beginTransaction();
		session.save(obj);
		tx.commit();
	}

	public static DbManager forClass(Class klass, DbSpec spec) {
		System.err.println(allDbManager);
		if (allDbManager.containsKey(klass) == false) {
			DbManager dbm = new DbManager(klass, spec);
			allDbManager.put(klass, dbm);
		}
		return allDbManager.get(klass);
	}

}

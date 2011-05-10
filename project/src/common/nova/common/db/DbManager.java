package nova.common.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nova.common.util.Utils;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

@SuppressWarnings("rawtypes")
public class DbManager {

	Logger log = Logger.getLogger(DbManager.class);

	static Session session = HibernateUtil.getSessionFactory().openSession();

	static final Map<Class, DbManager> allDbManager = new HashMap<Class, DbManager>();;

	private Class klass = null;

	private DbSpec spec = null;

	private Map<String, Map<Serializable, DbObject>> allIndex = new HashMap<String, Map<Serializable, DbObject>>();

	private DbManager(Class klass, DbSpec spec) {
		this.spec = spec;
		this.klass = klass;
		for (String colName : this.spec.getAllIndex()) {
			allIndex.put(colName, new HashMap<Serializable, DbObject>());
		}

		ArrayList<DbObject> queryResult = new ArrayList<DbObject>();
		synchronized (session) {
			String queryText = "from " + this.klass.getSimpleName();
			Query query = session.createQuery(queryText);
			for (Object obj : query.list()) {
				queryResult.add((DbObject) obj);
			}
		}

		// create index on other columns
		if (queryResult.size() > 0) {
			for (String indexName : spec.getAllIndex()) {
				Map<Serializable, DbObject> index = allIndex.get(indexName);

				if (indexName.equals(DbSpec.ID_COLUMN_NAME)) {
					for (DbObject obj : queryResult) {
						index.put(obj.getId(), obj);
					}
				} else {
					for (DbObject obj : queryResult) {
						index.put(
								(Serializable) Utils.getField(obj, indexName),
								obj);
					}
				}
			}
		}

	}

	public Object findById(Serializable key) {
		return findBy(DbSpec.ID_COLUMN_NAME, key);
	}

	public synchronized Object findBy(String fieldName, Serializable key) {
		Map<Serializable, DbObject> index = allIndex.get(fieldName);
		if (index == null) {
			throw new IllegalArgumentException("No DbSpec index on field: "
					+ fieldName);
		}
		return index.get(key);
	}

	public synchronized Collection<DbObject> all() {
		return allIndex.get(DbSpec.ID_COLUMN_NAME).values();
	}

	public Map<Serializable, DbObject> getIndex(String fieldName) {
		return this.allIndex.get(fieldName);
	}

	public void save(DbObject obj) {
		synchronized (session) {
			Transaction tx = session.beginTransaction();
			session.save(obj);
			tx.commit();
		}
		getIndex(DbSpec.ID_COLUMN_NAME).put(obj.getId(), obj);
	}

	public static synchronized DbManager forClass(Class klass, DbSpec spec) {
		if (allDbManager.containsKey(klass) == false) {
			DbManager dbm = new DbManager(klass, spec);
			allDbManager.put(klass, dbm);
		}
		return allDbManager.get(klass);
	}

}

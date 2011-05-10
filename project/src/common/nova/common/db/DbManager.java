package nova.common.db;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

	// TODO @santa make all members concurrnt
	private Map<String, Map<Serializable, Object>> allIndex = new HashMap<String, Map<Serializable, Object>>();

	private DbManager(Class klass, DbSpec spec) {
		this.spec = spec;
		this.klass = klass;
		for (String colName : this.spec.getAllIndex()) {
			allIndex.put(colName, new HashMap<Serializable, Object>());
		}

		String queryText = "from " + this.klass.getSimpleName();
		Query query = session.createQuery(queryText);
		ArrayList<Object> queryResult = new ArrayList<Object>();
		for (Object obj : query.list()) {
			queryResult.add(obj);
		}

		// create index
		for (String indexName : spec.getAllIndex()) {
			Map<Serializable, Object> index = new HashMap<Serializable, Object>();
			allIndex.put(indexName, index);
			if (queryResult.size() == 0) {
				continue;
			}

			try {
				Field field = queryResult.get(0).getClass()
						.getDeclaredField(indexName);
				field.setAccessible(true);
				for (Object obj : queryResult) {
					index.put((Serializable) field.get(obj), obj);
				}
			} catch (SecurityException e) {
				log.fatal("Error creating index on field " + indexName
						+ " of class " + klass.getSimpleName(), e);
				System.exit(1);
			} catch (NoSuchFieldException e) {
				log.fatal("Error creating index on field " + indexName
						+ " of class " + klass.getSimpleName(), e);
				System.exit(1);
			} catch (IllegalArgumentException e) {
				log.fatal("Error creating index on field " + indexName
						+ " of class " + klass.getSimpleName(), e);
				System.exit(1);
			} catch (IllegalAccessException e) {
				log.fatal("Error creating index on field " + indexName
						+ " of class " + klass.getSimpleName(), e);
				System.exit(1);
			}

		}

	}

	public Object findById(Serializable key) {
		return findBy("id", key);
	}

	public Object findBy(String fieldName, Serializable key) {
		Map<Serializable, Object> index = allIndex.get(fieldName);
		if (index == null) {
			throw new IllegalArgumentException("No DbSpec index on field: "
					+ fieldName);
		}
		return index.get(key);
	}

	public Collection<Object> all() {
		return allIndex.get("id").values();
	}

	public Map<Serializable, Object> getIndex(String fieldName) {
		return this.allIndex.get(fieldName);
	}

	public void save(Object obj) {
		Transaction tx = session.beginTransaction();
		session.save(obj);
		tx.commit();

		// TODO @santa move id into DbObject
		try {
			// update id index
			Field field = obj.getClass().getDeclaredField("id");
			field.setAccessible(true);
			long id = (Long) field.get(obj);
			getIndex("id").put(id, obj);
		} catch (SecurityException e) {
			log.fatal("Error fetching object index", e);
			System.exit(1);
		} catch (NoSuchFieldException e) {
			log.fatal("Error fetching object index", e);
			System.exit(1);
		} catch (IllegalArgumentException e) {
			log.fatal("Error fetching object index", e);
			System.exit(1);
		} catch (IllegalAccessException e) {
			log.fatal("Error fetching object index", e);
			System.exit(1);
		}

	}

	public static DbManager forClass(Class klass, DbSpec spec) {
		if (allDbManager.containsKey(klass) == false) {
			DbManager dbm = new DbManager(klass, spec);
			allDbManager.put(klass, dbm);
		}
		return allDbManager.get(klass);
	}

}

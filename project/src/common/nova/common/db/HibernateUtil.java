package nova.common.db;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	static Logger log = Logger.getLogger(HibernateUtil.class);

	private static SessionFactory sessionFactory = null;

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			try {
				sessionFactory = new Configuration().configure()
						.buildSessionFactory();
			} catch (Exception e) {
				log.error("Exception in creating hibernate session Factory", e);
			}
		}
		return sessionFactory;
	}

	public static void shutdown() {
		getSessionFactory().close();
	}
}

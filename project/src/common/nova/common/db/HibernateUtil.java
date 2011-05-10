package nova.common.db;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	static Logger log = Logger.getLogger(HibernateUtil.class);

	private static SessionFactory sessionFactory = null;

	public static synchronized SessionFactory getSessionFactory() {
		System.err.println("get session factory: " + sessionFactory);
		if (sessionFactory == null) {
			try {
				System.err.println("creating session factory");
				sessionFactory = new Configuration().configure()
						.buildSessionFactory();
				System.err.println("done creating session factory: "
						+ sessionFactory);
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

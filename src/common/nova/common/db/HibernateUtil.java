package nova.common.db;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    static Logger log = Logger.getLogger(HibernateUtil.class);

    private static SessionFactory sessionFactory;

    static {
        try {
            log.info("Starting Hibernate session");
            sessionFactory = new Configuration().configure()
                    .buildSessionFactory();
            log.info("Hibernate session started");
        } catch (Exception e) {
            log.error("Exception in creating hibernate session Factory", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}

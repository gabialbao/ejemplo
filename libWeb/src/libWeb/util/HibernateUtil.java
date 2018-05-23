package libWeb.util;



import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import libWeb.entities.Audit;
import libWeb.entities.Leftoverbook;
import libWeb.entities.Missingbook;
import libWeb.entities.User;

public class HibernateUtil {

	private static SessionFactory sessionFactory;

	private HibernateUtil() {

	}

	@SuppressWarnings("deprecation")
	public static SessionFactory getSessionFactory() {

		if (sessionFactory == null) {
			try {
				// Create the SessionFactory from standard (hibernate.cfg.xml)
				// config file.
				@SuppressWarnings("deprecation")
				AnnotationConfiguration ac = new AnnotationConfiguration();
				ac.addAnnotatedClass(User.class);
				ac.addAnnotatedClass(Audit.class);
				ac.addAnnotatedClass(Missingbook.class);
				ac.addAnnotatedClass(Leftoverbook.class);
				sessionFactory = ac.configure().buildSessionFactory();

			} catch (Throwable ex) {
				// Log the exception.
				System.err.println("Initial SessionFactory creation failed." + ex);
				throw new ExceptionInInitializerError(ex);
			}

			return sessionFactory;

		} else {
			return sessionFactory;
		}

	}

}
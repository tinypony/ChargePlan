package utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class JPAUtils {
	
	private static EntityManagerFactory emfactory;
	
	public static void init() {
		emfactory = Persistence.createEntityManagerFactory("defaultPersistenceUnit");
		
	}
	
	public static EntityManager em() {
		 return emfactory.createEntityManager();
	}
}

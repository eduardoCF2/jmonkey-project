package com.TFG1.repository;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import jakarta.persistence.EntityManagerFactory;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;
    private static final EntityManagerFactory entityManagerFactory;
    static {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Error al crear SessionFactory: " + e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null)
            sessionFactory.close();
    }
}

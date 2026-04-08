package com.TFG1.repository;

import com.TFG1.model.UserStats;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserStatsRepository {

    public UserStats findById(String userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(UserStats.class, userId);
        } catch (Exception e) {
            System.err.println("Error al buscar UserStats por ID: " + e.getMessage());
            return null;
        }
    }

    public void saveOrUpdate(UserStats stats) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(stats);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error al guardar/actualizar UserStats: " + e.getMessage());
            throw e;
        }
    }
}

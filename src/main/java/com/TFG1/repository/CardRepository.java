package com.TFG1.repository;

import com.TFG1.model.UserCard;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class CardRepository {

    public void save(UserCard userCard) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(userCard);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error al guardar UserCard: " + e.getMessage());
            throw e;
        }
    }
}

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

    public java.util.List<UserCard> findByUserId(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM UserCard WHERE user.id = :uid", UserCard.class)
                    .setParameter("uid", userId)
                    .list();
        } catch (Exception e) {
            System.err.println("Error al buscar cartas del usuario: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    public void deleteOneUserCard(int userId, int cardId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserCard uc = session.createQuery("FROM UserCard WHERE user.id = :uid AND cardId = :cid", UserCard.class)
                    .setParameter("uid", userId)
                    .setParameter("cid", cardId)
                    .setMaxResults(1)
                    .uniqueResult();
            if (uc != null) {
                session.remove(uc);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Error al borrar carta consumida: " + e.getMessage());
        }
    }
}

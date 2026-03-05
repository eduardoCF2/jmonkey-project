package com.TFG1.repository;

import com.TFG1.model.MatchHistory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

public class MatchRepository {

    public void save(MatchHistory match) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(match);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("Error al guardar MatchHistory: " + e.getMessage());
            throw e;
        }
    }

    public List<MatchHistory> findByUserId(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MatchHistory> query = session.createQuery(
                    "FROM MatchHistory m WHERE m.winner.id = :uid ORDER BY m.date DESC", MatchHistory.class);
            query.setParameter("uid", userId);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error al buscar MatchHistory por usuario: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<MatchHistory> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MatchHistory> query = session.createQuery("FROM MatchHistory m ORDER BY m.date DESC",
                    MatchHistory.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error al buscar todos los MatchHistory: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}

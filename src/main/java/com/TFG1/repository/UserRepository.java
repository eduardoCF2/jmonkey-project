package com.TFG1.repository;

import com.TFG1.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class UserRepository {

    // Busca un usuario por su nombre exacto.

    // @return

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE username = :name", User.class);
            query.setParameter("name", username);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
            return null;
        }
    }

    public User findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
            return null;
        }
    }

    // Evitamos duplicados

    public boolean existsByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(u) FROM User u WHERE u.username = :name";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("name", username)
                    .uniqueResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();
            session.persist(user);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error al guardar usuario: " + e.getMessage());
            throw e; // Relanzamos para que el Service sepa que algo fallo
        }
    }

    // ACtualizar datos del usuario

    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }
    }
}
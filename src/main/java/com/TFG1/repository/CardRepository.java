package com.TFG1.repository;

import com.TFG1.model.UserCard;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class CardRepository {

    // 1. Guardar una nueva carta comprada en la base de datos
    public void save(UserCard userCard) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Empezamos la transacción
            transaction = session.beginTransaction();
            
            // persist equivale al "INSERT INTO user_cards..."
            session.persist(userCard);
            
            // Confirmamos y guardamos los cambios
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Si algo falla, deshacemos la compra
            }
            e.printStackTrace();
        }
    }

    // 2. Buscar todas las cartas que tiene un User concreto
    public List<UserCard> findCardsByUserId(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            
            // Hacemos una consulta HQL (Hibernate Query Language). 
            // No usamos SQL nativo, sino los nombres de las CLASES de Java.
            Query<UserCard> query = session.createQuery("FROM UserCard uc WHERE uc.user.id = :userId", UserCard.class);
            query.setParameter("userId", userId);
            
            return query.list(); // Devuelve la lista de cartas de ese usuario
            
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Devuelve una lista vacía si falla
        }
    }
}

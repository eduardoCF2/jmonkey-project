package com.TFG1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_cards")
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Relación: Muchas cartas de inventario pueden pertenecer a un solo Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Guardaremos el ID lógico de la carta (por ejemplo, 1=As de Espadas, 2=Rey de Bastos...)
    @Column(name = "card_id", nullable = false)
    private int cardId;

    // Hibernate siempre necesita un constructor vacío
    public UserCard() {}

    // Constructor que usaremos nosotros al comprar la carta
    public UserCard(User user, int cardId) {
        this.user = user;
        this.cardId = cardId;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }
}

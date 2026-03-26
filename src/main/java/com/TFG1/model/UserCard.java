package com.TFG1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_cards")
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int cardId;

    public UserCard() {}

    public UserCard(User user, int cardId) {
        this.user = user;
        this.cardId = cardId;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public int getCardId() {
        return cardId;
    }
}

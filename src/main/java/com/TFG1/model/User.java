package com.TFG1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // "user" is a reserved word in many DBs
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    //Creación del campo int coins
    @Column(nullable = false)
    private int coins;

    // Hibernate siempre necesita un constructor vacío
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.coins = 100;//valor inicial en el constructor para probar, posteriormente cambiar
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public int getCoins() {
        return coins;
    }
    
    public void setCoins(int coins) {
        this.coins = coins;
    }
}

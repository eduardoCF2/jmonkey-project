package com.TFG1.model;

public class User {
    private int id;
    private String username;
    private String password;
    //Creación del campo int coins
    private int coins;

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
}

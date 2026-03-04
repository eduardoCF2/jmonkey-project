package com.TFG1.model;

public class User {
    private int id;
    private String username;
    private String password;
    private int coins;

    public User(int id, String username, String password, int coins) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.coins = coins;
    }
}

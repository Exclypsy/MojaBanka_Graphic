package com.example.mojabanka_graficky.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role;
    private String fullName;

    public User(int id, String username, String passwordHash, String role, String fullName) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
}

package com.example.memoryaidapp;

public class Account {
    private String email;
    private String username;
    private String birthday;
    private String condition;

    // Public no-arg constructor required by Firestore
    public Account() {}

    public Account(String email, String username, String birthday, String condition) {
        this.email = email;
        this.username = username;
        this.birthday = birthday;
        this.condition = condition;
    }

    // Getters (Firestore uses these)
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getBirthday() { return birthday; }
    public String getCondition() { return condition; }
}

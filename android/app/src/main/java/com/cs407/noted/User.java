package com.cs407.noted;

public class User {
    private String name;
    private String email;
    private String id;

    public User(String name, String email, String id) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // for firebase purposes
    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

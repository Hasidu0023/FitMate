package com.example.fitmate.models;

public class User {
    public String name, email;
    public int age;
    public double weight, height;

    public User() {} // Required for Firestore

    public User(String name, String email, int age, double weight, double height) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.weight = weight;
        this.height = height;
    }
}

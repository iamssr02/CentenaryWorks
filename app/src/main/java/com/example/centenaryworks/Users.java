package com.example.centenaryworks;

public class Users {
    private String name;
    private String uid;
    private String age;
    private String gender;
    private String workYears;

    // Default constructor (needed for Firebase)
    public Users() {
    }

    // Parameterized constructor
    public Users(String name, String uid, String age, String gender, String workYears) {
        this.name = name;
        this.uid = uid;
        this.age = age;
        this.gender = gender;
        this.workYears = workYears;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWorkYears() {
        return workYears;
    }

    public void setWorkYears(String workYears) {
        this.workYears = workYears;
    }
}


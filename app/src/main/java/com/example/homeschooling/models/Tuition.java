package com.example.homeschooling.models;

public class Tuition {
    String subject, classLevel, fee, timing;

    public Tuition() {} // required for Firebase

    public String getSubject() { return subject; }
    public String getClassLevel() { return classLevel; }
    public String getFee() { return fee; }
    public String getTiming() { return timing; }
}
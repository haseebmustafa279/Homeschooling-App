package com.example.homeschooling.models;

public class TutorSearchModel {

    private String userId;
    private String name;
    private String city;
    private String subjects;
    private String classLevels;
    private String hourlyFee;
    private double distance;

    public TutorSearchModel() {}

    public TutorSearchModel(String userId, String name,
                            String city, String subjects,
                            String classLevels, String hourlyFee) {
        this.userId = userId;
        this.name = name;
        this.city = city;
        this.subjects = subjects;
        this.classLevels = classLevels;
        this.hourlyFee = hourlyFee;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getSubjects() { return subjects; }
    public String getClassLevels() { return classLevels; }
    public String getHourlyFee() { return hourlyFee; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
}

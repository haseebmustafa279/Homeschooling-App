package com.example.homeschooling.models;

public class User {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String role;
    private String registration_date;
    private double latitude;
    private double longitude;

    public User() {
    }

    public User(String id, String name, String email, String phone, String city, String role, String registration_date) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.role = role;
        this.registration_date = registration_date;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
    public String getRole() { return role; }
    public String getRegistration_date() { return registration_date; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCity(String city) { this.city = city; }
    public void setRole(String role) { this.role = role; }
    public void setRegistration_date(String registration_date) { this.registration_date = registration_date; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
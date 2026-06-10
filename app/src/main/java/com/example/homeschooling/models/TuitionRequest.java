package com.example.homeschooling.models;

public class TuitionRequest {

    private String requestId, parentId, tutorId;
    private String subject, classLevel, timing, fee, city, status;
    private int agreedDays; 
    private double currentEscrowBalance;

    public TuitionRequest() {
        this.agreedDays = 23; // Safe default
        this.status = "open";
        this.tutorId = "";
        this.currentEscrowBalance = 0.0;
    }

    public TuitionRequest(String requestId, String parentId,
                          String subject, String classLevel,
                          String timing, String fee,
                          String city, String status) {
        this.requestId = requestId;
        this.parentId = parentId;
        this.subject = subject;
        this.classLevel = classLevel;
        this.timing = timing;
        this.fee = fee;
        this.city = city;
        this.status = status;
        this.agreedDays = 23;
        this.tutorId = "";
        this.currentEscrowBalance = 0.0;
    }

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getClassLevel() { return classLevel; }
    public void setClassLevel(String classLevel) { this.classLevel = classLevel; }

    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }

    public String getFee() { return fee; }
    public void setFee(String fee) { this.fee = fee; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAgreedDays() { return agreedDays; }
    public void setAgreedDays(int agreedDays) { this.agreedDays = agreedDays; }

    public double getCurrentEscrowBalance() { return currentEscrowBalance; }
    public void setCurrentEscrowBalance(double currentEscrowBalance) { this.currentEscrowBalance = currentEscrowBalance; }
}

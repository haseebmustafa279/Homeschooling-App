package com.example.homeschooling.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class TutorProfile {
    private String subjects;
    private String class_levels;
    private String monthly_fee;
    private String experience;
    private String availability;
    private String qualification;
    private boolean verified;

    public TutorProfile() {
        this.verified = false;
    }

    public TutorProfile(String subjects, String class_levels, String monthly_fee, 
                        String experience, String availability, String qualification, boolean verified) {
        this.subjects = subjects;
        this.class_levels = class_levels;
        this.monthly_fee = monthly_fee;
        this.experience = experience;
        this.availability = availability;
        this.qualification = qualification;
        this.verified = verified;
    }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    public String getClass_levels() { return class_levels; }
    public void setClass_levels(String class_levels) { this.class_levels = class_levels; }

    public String getMonthly_fee() { return monthly_fee; }
    public void setMonthly_fee(String monthly_fee) { this.monthly_fee = monthly_fee; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}

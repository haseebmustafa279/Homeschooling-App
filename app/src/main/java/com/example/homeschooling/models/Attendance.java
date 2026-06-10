package com.example.homeschooling.models;

public class Attendance {
    private String attendanceId;
    private String tuitionId;
    private String date;
    private String status; // Present, Absent

    public Attendance() {}

    public Attendance(String attendanceId, String tuitionId, String date, String status) {
        this.attendanceId = attendanceId;
        this.tuitionId = tuitionId;
        this.date = date;
        this.status = status;
    }

    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }

    public String getTuitionId() { return tuitionId; }
    public void setTuitionId(String tuitionId) { this.tuitionId = tuitionId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

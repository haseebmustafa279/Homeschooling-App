package com.example.homeschooling.models;

public class Review {
    private String reviewId;
    private String tuitionId;
    private String reviewerId;
    private String revieweeId;
    private float rating;
    private String comment;
    private long timestamp;

    public Review() {}

    public Review(String reviewId, String tuitionId, String reviewerId, String revieweeId, float rating, String comment, long timestamp) {
        this.reviewId = reviewId;
        this.tuitionId = tuitionId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getReviewId() { return reviewId; }
    public String getTuitionId() { return tuitionId; }
    public String getReviewerId() { return reviewerId; }
    public String getRevieweeId() { return revieweeId; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public long getTimestamp() { return timestamp; }
}

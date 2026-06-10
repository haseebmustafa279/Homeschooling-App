package com.example.homeschooling.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeschooling.R;
import com.example.homeschooling.models.Review;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddReviewActivity extends AppCompatActivity {

    RatingBar ratingBar;
    TextInputEditText etComment;
    MaterialButton btnSubmit;
    
    String tuitionId, tutorId, parentId;
    DatabaseReference reviewRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmit = findViewById(R.id.btnSubmitReview);

        tuitionId = getIntent().getStringExtra("tuitionId");
        tutorId = getIntent().getStringExtra("tutorId");
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        reviewRef = FirebaseDatabase.getInstance().getReference("Reviews");

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReview(rating, comment);
        });
    }

    private void submitReview(float rating, String comment) {
        String reviewId = reviewRef.push().getKey();
        Review review = new Review(reviewId, tuitionId, parentId, tutorId, rating, comment, System.currentTimeMillis());

        if (reviewId != null) {
            reviewRef.child(reviewId).setValue(review)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddReviewActivity.this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(AddReviewActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show());
        }
    }
}

package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.homeschooling.R;
import com.example.homeschooling.models.TutorProfile;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TutorDashboardActivity extends AppCompatActivity {

    MaterialCardView btnLogout, btnProfile, btnFindTuitions, btnActiveTuitions, btnEarnings, btnMessages;
    DatabaseReference tutorRef;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuotor_dashboard);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tutorRef = FirebaseDatabase.getInstance().getReference("TutorProfiles");

        btnFindTuitions = findViewById(R.id.cardFindTuitions);
        btnActiveTuitions = findViewById(R.id.cardActiveTuitions);
        btnEarnings = findViewById(R.id.cardEarnings);
        btnMessages = findViewById(R.id.cardMessages);
        btnProfile = findViewById(R.id.cardProfile);
        btnLogout = findViewById(R.id.cardLogout);

        btnFindTuitions.setOnClickListener(v -> checkVerificationAndOpen(TuitionListActivity.class));
        btnActiveTuitions.setOnClickListener(v -> checkVerificationAndOpen(TutorActiveTuitionsActivity.class));
        btnEarnings.setOnClickListener(v -> checkVerificationAndOpen(TutorEarningsActivity.class));
        
        btnMessages.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatListActivity.class));
        });

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, TutorProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void checkVerificationAndOpen(Class<?> targetActivity) {
        tutorRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TutorProfile profile = snapshot.getValue(TutorProfile.class);
                    if (profile != null && profile.isVerified()) {
                        startActivity(new Intent(TutorDashboardActivity.this, targetActivity));
                    } else {
                        Toast.makeText(TutorDashboardActivity.this, 
                            "Your profile is pending admin verification. Please complete your profile and wait for approval.", 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(TutorDashboardActivity.this, 
                        "Please complete your profile first.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(TutorDashboardActivity.this, TutorProfileActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

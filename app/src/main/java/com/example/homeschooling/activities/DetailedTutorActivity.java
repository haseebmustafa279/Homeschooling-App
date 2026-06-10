package com.example.homeschooling.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homeschooling.R;
import com.example.homeschooling.models.TutorProfile;
import com.example.homeschooling.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailedTutorActivity extends AppCompatActivity {

    TextView tvName, tvCity, tvSubjects,
            tvClassLevels, tvHourlyFee,
            tvExperience, tvAvailability;

    DatabaseReference userRef, tutorRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_tutor);

        tvName = findViewById(R.id.tvName);
        tvCity = findViewById(R.id.tvCity);
        tvSubjects = findViewById(R.id.tvSubjects);
        tvClassLevels = findViewById(R.id.tvClassLevels);
        tvHourlyFee = findViewById(R.id.tvHourlyFee);
        tvExperience = findViewById(R.id.tvExperience);
        tvAvailability = findViewById(R.id.tvAvailability);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        tutorRef = FirebaseDatabase.getInstance().getReference("TutorProfiles");
        
        String tutorId = getIntent().getStringExtra("tutorId");
        if (tutorId == null) {
            Toast.makeText(this, "Tutor ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTutorDetails(tutorId);
    }

    private void loadTutorDetails(String userId) {

        userRef.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            tvName.setText(user.getName());
                            tvCity.setText("City: " + (user.getCity() != null ? user.getCity() : "N/A"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        tutorRef.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        TutorProfile profile = snapshot.getValue(TutorProfile.class);
                        if (profile != null) {
                            tvSubjects.setText("Subjects: " + (profile.getSubjects() != null ? profile.getSubjects() : "N/A"));
                            tvClassLevels.setText("Class Levels: " + (profile.getClass_levels() != null ? profile.getClass_levels() : "N/A"));
                            // Using getMonthly_fee() which matches the updated model
                            tvHourlyFee.setText("Monthly Fee: Rs " + (profile.getMonthly_fee() != null ? profile.getMonthly_fee() : "0"));
                            tvExperience.setText("Experience: " + (profile.getExperience() != null ? profile.getExperience() : "0") + " years");
                            tvAvailability.setText("Availability: " + (profile.getAvailability() != null ? profile.getAvailability() : "N/A"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}

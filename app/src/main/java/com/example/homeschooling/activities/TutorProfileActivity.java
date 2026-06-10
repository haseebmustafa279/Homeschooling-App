package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homeschooling.R;
import com.example.homeschooling.models.TutorProfile;
import com.example.homeschooling.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class TutorProfileActivity extends AppCompatActivity {

    private static final int MAP_PICKER_REQUEST = 103;
    TextInputEditText etSubjects, etClassLevels, etMonthlyFee,
            etExperience, etQualification;
    AutoCompleteTextView autoAvailability;
    MaterialButton btnSave, btnPickLocation;
    TextView tvLocationStatus;

    DatabaseReference tutorRef, userRef;
    String userId;
    boolean isVerified = false;
    double latitude = 0, longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_profile);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tutorRef = FirebaseDatabase.getInstance().getReference("TutorProfiles");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        etSubjects = findViewById(R.id.etSubjects);
        etClassLevels = findViewById(R.id.etClassLevels);
        etMonthlyFee = findViewById(R.id.etMonthlyFee);
        etExperience = findViewById(R.id.etExperience);
        autoAvailability = findViewById(R.id.autoCompleteAvailability);
        etQualification = findViewById(R.id.etQualification);
        btnSave = findViewById(R.id.btnSave);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);

        // Setup Availability Dropdown
        String[] items = {"Morning", "Evening"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        autoAvailability.setAdapter(adapter);

        loadProfile();

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivityForResult(intent, MAP_PICKER_REQUEST);
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("lat", 0);
            longitude = data.getDoubleExtra("lng", 0);
            tvLocationStatus.setText("Location Selected: " + String.format("%.4f, %.4f", latitude, longitude));
            tvLocationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvLocationStatus.setVisibility(TextView.VISIBLE);
        }
    }

    private void loadProfile() {
        tutorRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TutorProfile profile = snapshot.getValue(TutorProfile.class);
                    if (profile != null) {
                        etSubjects.setText(profile.getSubjects());
                        etClassLevels.setText(profile.getClass_levels());
                        etMonthlyFee.setText(profile.getMonthly_fee());
                        etExperience.setText(profile.getExperience());
                        autoAvailability.setText(profile.getAvailability(), false);
                        etQualification.setText(profile.getQualification());
                        isVerified = profile.isVerified();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getLatitude() != 0) {
                        latitude = user.getLatitude();
                        longitude = user.getLongitude();
                        tvLocationStatus.setText("Location set: " + String.format("%.4f, %.4f", latitude, longitude));
                        tvLocationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        tvLocationStatus.setVisibility(TextView.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveProfile() {
        String subjects = etSubjects.getText().toString().trim();
        String classLevels = etClassLevels.getText().toString().trim();
        String monthlyFee = etMonthlyFee.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String availability = autoAvailability.getText().toString().trim();
        String qualification = etQualification.getText().toString().trim();

        if (TextUtils.isEmpty(subjects) || TextUtils.isEmpty(classLevels) ||
                TextUtils.isEmpty(monthlyFee) || TextUtils.isEmpty(experience) ||
                TextUtils.isEmpty(availability) || TextUtils.isEmpty(qualification)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        TutorProfile profile = new TutorProfile(
                subjects,
                classLevels,
                monthlyFee,
                experience,
                availability,
                qualification,
                isVerified
        );

        tutorRef.child(userId).setValue(profile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userRef.child(userId).child("latitude").setValue(latitude);
                        userRef.child(userId).child("longitude").setValue(longitude);
                        Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

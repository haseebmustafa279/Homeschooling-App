package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.homeschooling.R;
import com.example.homeschooling.models.User;
import com.example.homeschooling.models.TutorProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TutorRegisterActivity extends AppCompatActivity {

    private static final int MAP_PICKER_REQUEST = 102;
    TextInputEditText etName, etEmail, etPhone, etCity, etSubjects,
            etClassLevels, etExperience, etMonthlyFee, etQualification, etPassword, etConfirmPassword;
    MaterialButton btnRegister, btnPickLocation;
    TextView tvLocationStatus;
    RadioGroup radioAvailability;

    FirebaseAuth mAuth;
    DatabaseReference userRef, tutorProfileRef;

    double latitude = 0, longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_register);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        tutorProfileRef = FirebaseDatabase.getInstance().getReference("TutorProfiles");

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etCity = findViewById(R.id.etCity);
        etSubjects = findViewById(R.id.etSubjects);
        etClassLevels = findViewById(R.id.etClassLevels);
        etMonthlyFee = findViewById(R.id.etMonthlyFee);
        etExperience = findViewById(R.id.etExperience);
        etQualification = findViewById(R.id.etQualification);
        radioAvailability = findViewById(R.id.radioAvailability);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivityForResult(intent, MAP_PICKER_REQUEST);
        });

        btnRegister.setOnClickListener(v -> registerTutor());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("lat", 0);
            longitude = data.getDoubleExtra("lng", 0);
            tvLocationStatus.setText("Location Selected: " + String.format("%.4f, %.4f", latitude, longitude));
            tvLocationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void registerTutor() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String subjects = etSubjects.getText().toString().trim();
        String classLevels = etClassLevels.getText().toString().trim();
        String monthlyFee = etMonthlyFee.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String qualification = etQualification.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(city) ||
                TextUtils.isEmpty(subjects) || TextUtils.isEmpty(classLevels) ||
                TextUtils.isEmpty(monthlyFee) || TextUtils.isEmpty(experience) ||
                TextUtils.isEmpty(qualification) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == 0 || longitude == 0) {
            Toast.makeText(this, "Please select your teaching location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioAvailability.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Select Availability", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRadio = findViewById(selectedId);
        String availability = selectedRadio.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                        User user = new User(userId, name, email, phone, city, "Tutor", date);
                        user.setLatitude(latitude);
                        user.setLongitude(longitude);
                        userRef.child(userId).setValue(user);

                        TutorProfile tutorProfile = new TutorProfile(
                                subjects,
                                classLevels,
                                monthlyFee,
                                experience,
                                availability,
                                qualification,
                                false
                        );

                        tutorProfileRef.child(userId).setValue(tutorProfile);

                        Toast.makeText(this, "Tutor Registered Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(TutorRegisterActivity.this, TutorDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.homeschooling.R;
import com.example.homeschooling.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ParentRegisterActivity extends AppCompatActivity {

    private static final int MAP_PICKER_REQUEST = 101;
    TextInputEditText etName, etPhone, etCity, etPassword, etConfirmPassword, etEmail;
    MaterialButton btnRegister, btnPickLocation;
    TextView tvLocationStatus;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    double latitude = 0, longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etCity = findViewById(R.id.etCity);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivityForResult(intent, MAP_PICKER_REQUEST);
        });

        btnRegister.setOnClickListener(v -> registerParent());
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

    private void registerParent() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(city) ||
                TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == 0 || longitude == 0) {
            Toast.makeText(this, "Please select your location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

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

                        User user = new User(userId, name, email, phone, city, "Parent", date);
                        user.setLatitude(latitude);
                        user.setLongitude(longitude);

                        databaseReference.child(userId).setValue(user)
                                .addOnCompleteListener(t -> {
                                    Toast.makeText(this, "Parent Registered Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ParentRegisterActivity.this, ParentDashboardActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                });
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

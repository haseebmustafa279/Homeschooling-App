package com.example.homeschooling.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homeschooling.R;
import com.example.homeschooling.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    TextInputEditText etEmail, etPassword;
    MaterialButton btnLogin;
    TextView btnRegister;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RoleSelectionActivity.class)));

        // Session Check - only redirect if session exists
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "Existing session found for: " + mAuth.getCurrentUser().getUid());
            checkUserRole(mAuth.getCurrentUser().getUid());
        }

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter email first", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        Toast.makeText(this, "Reset email sent", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Login successful");
                        String userId = mAuth.getCurrentUser().getUid();
                        checkUserRole(userId);
                    } else {
                        progressDialog.dismiss();
                        Log.e(TAG, "Login failed", task.getException());
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        if (!progressDialog.isShowing() && !isFinishing()) {
            progressDialog.setMessage("Checking role...");
            progressDialog.show();
        }

        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    Log.d(TAG, "User role found: " + role);

                    Intent intent;
                    if ("Tutor".equalsIgnoreCase(role)) {
                        intent = new Intent(LoginActivity.this, TutorDashboardActivity.class);
                    } else if ("Parent".equalsIgnoreCase(role)) {
                        intent = new Intent(LoginActivity.this, ParentDashboardActivity.class);
                    } else if ("Admin".equalsIgnoreCase(role)) {
                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    } else {
                        Log.w(TAG, "Unknown role: " + role + ". Defaulting to Parent Dashboard.");
                        intent = new Intent(LoginActivity.this, ParentDashboardActivity.class);
                    }
                    
                    startActivity(intent);
                    finishAffinity();
                } else {
                    Log.d(TAG, "No user data in database");
                    Toast.makeText(LoginActivity.this, "User data not found. Please register.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

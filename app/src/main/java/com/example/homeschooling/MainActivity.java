package com.example.homeschooling;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeschooling.activities.AdminDashboardActivity;
import com.example.homeschooling.activities.LoginActivity;
import com.example.homeschooling.activities.ParentDashboardActivity;
import com.example.homeschooling.activities.TutorDashboardActivity;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ImageView logo;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDHvn5q3mC6Rltry5uW8yzs8gGLcU0pauM");
        }

        logo = findViewById(R.id.imgLogo);
        auth = FirebaseAuth.getInstance();

        logo.setAlpha(0f);
        logo.animate().alpha(1f).setDuration(1500).start();

        new Handler().postDelayed(() -> {
            if (auth.getCurrentUser() != null) {
                FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(auth.getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {
                                String role = snapshot.child("role").getValue(String.class);
                                Log.d(TAG, "User role detected on startup: " + role);

                                if ("Tutor".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(MainActivity.this, TutorDashboardActivity.class));
                                } else if ("Admin".equalsIgnoreCase(role)) {
                                    // Requirement: Show login screen for Admin even if previously logged in
                                    auth.signOut(); 
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                } else {
                                    // Default/Parent role
                                    startActivity(new Intent(MainActivity.this, ParentDashboardActivity.class));
                                }
                            } else {
                                auth.signOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            }
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        });
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, 2000);
    }
}

package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.homeschooling.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    MaterialButton btnVerifyTutors, btnMonitorChats, btnManagePayments, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnVerifyTutors = findViewById(R.id.btnVerifyTutors);
        btnMonitorChats = findViewById(R.id.btnMonitorChats);
        btnManagePayments = findViewById(R.id.btnManagePayments);
        btnLogout = findViewById(R.id.btnLogout);

        btnVerifyTutors.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminVerifyTutorsActivity.class));
        });

        btnMonitorChats.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminMonitorChatsActivity.class));
        });

        btnManagePayments.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminManagePaymentsActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}

package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.homeschooling.R;
import com.google.android.material.button.MaterialButton;

public class RoleSelectionActivity extends AppCompatActivity {

    MaterialButton btnParent, btnTutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        btnParent = findViewById(R.id.btnParent);
        btnTutor = findViewById(R.id.btnTutor);

        btnParent.setOnClickListener(v ->
                startActivity(new Intent(this, ParentRegisterActivity.class)));

        btnTutor.setOnClickListener(v ->
                startActivity(new Intent(this, TutorRegisterActivity.class)));
    }
}
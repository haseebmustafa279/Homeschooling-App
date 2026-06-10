package com.example.homeschooling.activities;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeschooling.R;

public class TutorDetailActivity extends AppCompatActivity {

    TextView tvName, tvCity, tvSubjects, tvClassLevels,
            tvExperience, tvQualification, tvAvailability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_detail);

        tvName = findViewById(R.id.tvName);
        tvCity = findViewById(R.id.tvCity);
        tvSubjects = findViewById(R.id.tvSubjects);
        tvClassLevels = findViewById(R.id.tvClassLevels);
        tvExperience = findViewById(R.id.tvExperience);
        tvQualification = findViewById(R.id.tvQualification);
        tvAvailability = findViewById(R.id.tvAvailability);

        Intent intent = getIntent();

        tvName.setText(intent.getStringExtra("name"));
        tvCity.setText("City: " + intent.getStringExtra("city"));
        tvSubjects.setText("Subjects: " + intent.getStringExtra("subjects"));
        tvClassLevels.setText("Class Levels: " + intent.getStringExtra("classLevels"));
        tvExperience.setText("Experience: " + intent.getStringExtra("experience") + " Years");
        tvQualification.setText("Qualification: " + intent.getStringExtra("qualification"));
        tvAvailability.setText("Availability: " + intent.getStringExtra("availability"));
    }
}
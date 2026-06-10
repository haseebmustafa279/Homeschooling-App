package com.example.homeschooling.activities;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.homeschooling.R;
import com.example.homeschooling.adapters.TutorAdapter;
import com.example.homeschooling.models.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;
import android.widget.TextView;

public class SearchTutorActivity extends AppCompatActivity {

    TextInputEditText etSubject, etClassLevel, etCity;
    MaterialButton btnSearch;
    RecyclerView recyclerView;
    TextView tvNoResult;

    DatabaseReference userRef, tutorRef;

    List<TutorSearchModel> tutorList;
    TutorAdapter adapter;

    User currentParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tutor);

        etSubject = findViewById(R.id.etSubject);
        etClassLevel = findViewById(R.id.etClassLevel);
        etCity = findViewById(R.id.etCity);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerView = findViewById(R.id.recyclerView);
        tvNoResult = findViewById(R.id.tvNoResult);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        tutorRef = FirebaseDatabase.getInstance().getReference("TutorProfiles");

        tutorList = new ArrayList<>();
        adapter = new TutorAdapter(this, tutorList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Fetch parent location to calculate distance to tutors
        fetchParentData();

        btnSearch.setOnClickListener(v -> searchTutors());
    }

    private void fetchParentData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentParent = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void searchTutors() {
        String subject = etSubject.getText().toString().trim();
        String classLevel = etClassLevel.getText().toString().trim();
        String city = etCity.getText().toString().trim();

        tutorList.clear();
        adapter.notifyDataSetChanged();
        tvNoResult.setVisibility(View.GONE);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    User user = userSnap.getValue(User.class);

                    if (user != null && "Tutor".equalsIgnoreCase(user.getRole())) {
                        tutorRef.child(userSnap.getKey())
                                .get()
                                .addOnSuccessListener(tutorSnap -> {
                                    if (tutorSnap.exists()) {
                                        TutorProfile profile = tutorSnap.getValue(TutorProfile.class);
                                        if (profile == null) return;

                                        boolean matches = true;

                                        if (!TextUtils.isEmpty(subject) &&
                                                !profile.getSubjects().toLowerCase()
                                                        .contains(subject.toLowerCase()))
                                            matches = false;

                                        if (!TextUtils.isEmpty(classLevel) &&
                                                !profile.getClass_levels().toLowerCase()
                                                        .contains(classLevel.toLowerCase()))
                                            matches = false;

                                        if (!TextUtils.isEmpty(city) &&
                                                !user.getCity().toLowerCase()
                                                        .contains(city.toLowerCase()))
                                            matches = false;

                                        if (matches) {
                                            TutorSearchModel model = new TutorSearchModel(
                                                    userSnap.getKey(),
                                                    user.getName(),
                                                    user.getCity(),
                                                    profile.getSubjects(),
                                                    profile.getClass_levels(),
                                                    profile.getMonthly_fee()
                                            );

                                            // Location-based proximity calculation
                                            if (currentParent != null && user.getLatitude() != 0) {
                                                float[] results = new float[1];
                                                Location.distanceBetween(currentParent.getLatitude(), currentParent.getLongitude(),
                                                        user.getLatitude(), user.getLongitude(), results);
                                                model.setDistance(results[0] / 1000.0); // Meters to KM
                                            }

                                            tutorList.add(model);
                                            
                                            // Sort by proximity: nearest tutors appear first
                                            Collections.sort(tutorList, (o1, o2) -> Double.compare(o1.getDistance(), o2.getDistance()));
                                            
                                            adapter.notifyDataSetChanged();
                                        }

                                        if (tutorList.isEmpty()) {
                                            tvNoResult.setVisibility(View.VISIBLE);
                                        } else {
                                            tvNoResult.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

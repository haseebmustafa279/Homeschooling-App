package com.example.homeschooling.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeschooling.R;
import com.example.homeschooling.models.TutorProfile;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminVerifyTutorsActivity extends AppCompatActivity {

    RecyclerView rvVerifyTutors;
    List<TutorProfileWithId> unverifiedList;
    VerifyTutorAdapter adapter;
    DatabaseReference tutorRef, userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verify_tutors);

        tutorRef = FirebaseDatabase.getInstance().getReference("TutorProfiles");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        rvVerifyTutors = findViewById(R.id.rvVerifyTutors);
        rvVerifyTutors.setLayoutManager(new LinearLayoutManager(this));

        unverifiedList = new ArrayList<>();
        adapter = new VerifyTutorAdapter(unverifiedList);
        rvVerifyTutors.setAdapter(adapter);

        loadUnverifiedTutors();
    }

    private void loadUnverifiedTutors() {
        tutorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unverifiedList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    TutorProfile profile = ds.getValue(TutorProfile.class);
                    if (profile != null && !profile.isVerified()) {
                        unverifiedList.add(new TutorProfileWithId(ds.getKey(), profile));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    static class TutorProfileWithId {
        String id;
        TutorProfile profile;

        TutorProfileWithId(String id, TutorProfile profile) {
            this.id = id;
            this.profile = profile;
        }
    }

    class VerifyTutorAdapter extends RecyclerView.Adapter<VerifyTutorAdapter.ViewHolder> {
        List<TutorProfileWithId> list;

        VerifyTutorAdapter(List<TutorProfileWithId> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_verify_tutor, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TutorProfileWithId item = list.get(position);
            TutorProfile profile = item.profile;

            holder.tvSubjects.setText("Subjects: " + profile.getSubjects());
            holder.tvQualification.setText("Qualification: " + profile.getQualification());

            userRef.child(item.id).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        holder.tvTutorName.setText(snapshot.getValue(String.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            holder.btnVerify.setOnClickListener(v -> {
                profile.setVerified(true);
                tutorRef.child(item.id).setValue(profile).addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminVerifyTutorsActivity.this, "Tutor Approved!", Toast.LENGTH_SHORT).show();
                });
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTutorName, tvSubjects, tvQualification;
            MaterialButton btnVerify;

            ViewHolder(View v) {
                super(v);
                tvTutorName = v.findViewById(R.id.tvTutorName);
                tvSubjects = v.findViewById(R.id.tvSubjects);
                tvQualification = v.findViewById(R.id.tvQualification);
                btnVerify = v.findViewById(R.id.btnVerify);
            }
        }
    }
}

package com.example.homeschooling.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeschooling.R;
import com.example.homeschooling.models.Child;
import com.example.homeschooling.models.ParentProfile;
import com.example.homeschooling.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParentProfileActivity extends AppCompatActivity {

    private static final int MAP_PICKER_REQUEST = 104;
    private RecyclerView rvChildren;
    private MaterialButton btnAddChild, btnSaveProfile, btnPickLocation;
    private TextView tvLocationStatus;
    private List<Child> childList;
    private ChildAdapter adapter;

    private DatabaseReference parentRef, userRef;
    private String userId;
    private double latitude = 0, longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_profile);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        parentRef = FirebaseDatabase.getInstance().getReference("ParentProfiles").child(userId);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        rvChildren = findViewById(R.id.rvChildren);
        btnAddChild = findViewById(R.id.btnAddChild);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);

        childList = new ArrayList<>();
        adapter = new ChildAdapter(childList);
        rvChildren.setLayoutManager(new LinearLayoutManager(this));
        rvChildren.setAdapter(adapter);

        loadProfile();

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivityForResult(intent, MAP_PICKER_REQUEST);
        });

        btnAddChild.setOnClickListener(v -> showAddChildDialog());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
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

    private void loadProfile() {
        parentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ParentProfile profile = snapshot.getValue(ParentProfile.class);
                    if (profile != null && profile.getChildren() != null) {
                        childList.clear();
                        childList.addAll(profile.getChildren());
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getLatitude() != 0) {
                        latitude = user.getLatitude();
                        longitude = user.getLongitude();
                        tvLocationStatus.setText("Location set: " + String.format("%.4f, %.4f", latitude, longitude));
                        tvLocationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddChildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);
        builder.setView(view);

        TextInputEditText etName = view.findViewById(R.id.etChildName);
        TextInputEditText etClass = view.findViewById(R.id.etChildClass);
        TextInputEditText etSubjects = view.findViewById(R.id.etSubjectsNeeded);
        TextInputEditText etBudget = view.findViewById(R.id.etBudget);
        MaterialButton btnAdd = view.findViewById(R.id.btnAdd);

        AlertDialog dialog = builder.create();

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String className = etClass.getText().toString().trim();
            String subjects = etSubjects.getText().toString().trim();
            String budget = etBudget.getText().toString().trim();

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(className)) {
                childList.add(new Child(name, className, subjects, budget));
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Name and Class are required", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveProfile() {
        ParentProfile profile = new ParentProfile(childList);
        parentRef.setValue(profile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Also save location to User node
                userRef.child("latitude").setValue(latitude);
                userRef.child("longitude").setValue(longitude);
                Toast.makeText(ParentProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ParentProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ViewHolder> {
        private List<Child> list;

        public ChildAdapter(List<Child> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Child child = list.get(position);
            holder.tvName.setText(child.getName());
            holder.tvClass.setText("Class: " + child.getChildClass());
            holder.tvSubjects.setText("Subjects: " + child.getSubjectsNeeded());
            holder.tvBudget.setText("Budget: Rs " + (child.getBudget() != null ? child.getBudget() : "0"));
            
            holder.itemView.setOnLongClickListener(v -> {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    list.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, list.size());
                    Toast.makeText(ParentProfileActivity.this, "Child removed", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvClass, tvSubjects, tvBudget;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvChildName);
                tvClass = itemView.findViewById(R.id.tvChildClass);
                tvSubjects = itemView.findViewById(R.id.tvSubjectsNeeded);
                tvBudget = itemView.findViewById(R.id.tvBudget);
            }
        }
    }
}

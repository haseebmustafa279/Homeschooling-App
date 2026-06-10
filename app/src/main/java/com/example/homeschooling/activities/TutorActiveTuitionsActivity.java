package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeschooling.R;
import com.example.homeschooling.models.TuitionRequest;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TutorActiveTuitionsActivity extends AppCompatActivity {

    private static final String TAG = "TutorActiveTuitions";
    RecyclerView recyclerView;
    List<TuitionRequest> list;
    ActiveAdapter adapter;
    DatabaseReference tuitionRef;
    String tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_active_tuitions);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tuitionRef = FirebaseDatabase.getInstance().getReference("TuitionRequests");

        recyclerView = findViewById(R.id.rvTutorActiveTuitions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new ActiveAdapter(list);
        recyclerView.setAdapter(adapter);

        loadActiveTuitions();
    }

    private void loadActiveTuitions() {
        tuitionRef.orderByChild("tutorId").equalTo(tutorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            TuitionRequest tr = ds.getValue(TuitionRequest.class);
                            if (tr != null && "active".equals(tr.getStatus())) {
                                list.add(tr);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                    }
                });
    }

    class ActiveAdapter extends RecyclerView.Adapter<ActiveAdapter.ViewHolder> {
        List<TuitionRequest> list;

        ActiveAdapter(List<TuitionRequest> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_tuition, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TuitionRequest tr = list.get(position);
            
            holder.tvSubject.setText("Subject: " + (tr.getSubject() != null ? tr.getSubject() : "N/A"));
            holder.btnAction.setText("Chat with Parent");

            if (tr.getParentId() != null && !tr.getParentId().isEmpty()) {
                FirebaseDatabase.getInstance().getReference("Users").child(tr.getParentId()).child("name")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.getValue() != null) {
                                    holder.tvName.setText("Parent: " + snapshot.getValue(String.class));
                                } else {
                                    holder.tvName.setText("Parent: Unknown");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                holder.btnAction.setOnClickListener(v -> {
                    Intent intent = new Intent(TutorActiveTuitionsActivity.this, ChatActivity.class);
                    intent.putExtra("receiverId", tr.getParentId());
                    startActivity(intent);
                });
            } else {
                holder.tvName.setText("Parent: ID Missing");
                holder.btnAction.setEnabled(false);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSubject, tvName;
            MaterialButton btnAction;

            ViewHolder(View v) {
                super(v);
                tvSubject = v.findViewById(R.id.tvSubject);
                tvName = v.findViewById(R.id.tvTutorName); 
                btnAction = v.findViewById(R.id.btnMarkAttendance);
            }
        }
    }
}

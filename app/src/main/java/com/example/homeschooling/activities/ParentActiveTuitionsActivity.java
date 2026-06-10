package com.example.homeschooling.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.homeschooling.models.Attendance;
import com.example.homeschooling.models.TuitionRequest;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentActiveTuitionsActivity extends AppCompatActivity {

    RecyclerView rvActiveTuitions;
    List<TuitionRequest> activeList;
    ActiveTuitionAdapter adapter;
    DatabaseReference tuitionRef;
    String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_active_tuitions);

        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tuitionRef = FirebaseDatabase.getInstance().getReference("TuitionRequests");

        rvActiveTuitions = findViewById(R.id.rvActiveTuitions);
        rvActiveTuitions.setLayoutManager(new LinearLayoutManager(this));
        
        activeList = new ArrayList<>();
        adapter = new ActiveTuitionAdapter(activeList);
        rvActiveTuitions.setAdapter(adapter);

        loadActiveTuitions();
    }

    private void loadActiveTuitions() {
        tuitionRef.orderByChild("parentId").equalTo(parentId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                activeList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    TuitionRequest tr = ds.getValue(TuitionRequest.class);
                    if (tr != null && "active".equals(tr.getStatus())) {
                        activeList.add(tr);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    class ActiveTuitionAdapter extends RecyclerView.Adapter<ActiveTuitionAdapter.ViewHolder> {
        List<TuitionRequest> list;

        ActiveTuitionAdapter(List<TuitionRequest> list) {
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
            holder.tvSubject.setText(tr.getSubject());
            
            // Get Tutor Name
            FirebaseDatabase.getInstance().getReference("Users").child(tr.getTutorId())
                    .child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        holder.tvTutorName.setText("Tutor: " + snapshot.getValue(String.class));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            holder.btnMarkAttendance.setOnClickListener(v -> showAttendanceDialog(tr));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSubject, tvTutorName;
            MaterialButton btnMarkAttendance;
            ViewHolder(View v) {
                super(v);
                tvSubject = v.findViewById(R.id.tvSubject);
                tvTutorName = v.findViewById(R.id.tvTutorName);
                btnMarkAttendance = v.findViewById(R.id.btnMarkAttendance);
            }
        }
    }

    private void showAttendanceDialog(TuitionRequest tr) {
        String[] options = {"Present", "Absent"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mark Attendance for today");
        builder.setItems(options, (dialog, which) -> {
            saveAttendance(tr.getRequestId(), options[which]);
        });
        builder.show();
    }

    private void saveAttendance(String tuitionId, String status) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference attendRef = FirebaseDatabase.getInstance().getReference("Attendance");
        
        // Use a composite key or nested structure to avoid duplicate marking for the same day
        attendRef.child(tuitionId).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(ParentActiveTuitionsActivity.this, "Attendance already marked for today", Toast.LENGTH_SHORT).show();
                } else {
                    Attendance attendance = new Attendance(attendRef.push().getKey(), tuitionId, date, status);
                    attendRef.child(tuitionId).child(date).setValue(attendance)
                            .addOnSuccessListener(aVoid -> Toast.makeText(ParentActiveTuitionsActivity.this, "Attendance Marked: " + status, Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

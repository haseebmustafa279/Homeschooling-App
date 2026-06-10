package com.example.homeschooling.activities;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class EscrowParentActivity extends AppCompatActivity {

    RecyclerView rvEscrowTuitions;
    List<TuitionRequest> activeList;
    EscrowAdapter adapter;
    DatabaseReference tuitionRef, attendRef;
    String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escrow_parent);

        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tuitionRef = FirebaseDatabase.getInstance().getReference("TuitionRequests");
        attendRef = FirebaseDatabase.getInstance().getReference("Attendance");

        rvEscrowTuitions = findViewById(R.id.rvEscrowTuitions);
        rvEscrowTuitions.setLayoutManager(new LinearLayoutManager(this));

        activeList = new ArrayList<>();
        adapter = new EscrowAdapter(activeList);
        rvEscrowTuitions.setAdapter(adapter);

        loadTuitions();
    }

    private void loadTuitions() {
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

    class EscrowAdapter extends RecyclerView.Adapter<EscrowAdapter.ViewHolder> {
        List<TuitionRequest> list;

        EscrowAdapter(List<TuitionRequest> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_escrow_tuition, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TuitionRequest tr = list.get(position);
            holder.tvSubject.setText(tr.getSubject());
            holder.tvTotalFee.setText("Total Monthly Fee: Rs " + tr.getFee());

            FirebaseDatabase.getInstance().getReference("Users").child(tr.getTutorId()).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) holder.tvTutorName.setText("Tutor: " + snapshot.getValue(String.class));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            attendRef.child(tr.getRequestId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int presentCount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Attendance att = ds.getValue(Attendance.class);
                        if (att != null && "Present".equals(att.getStatus())) {
                            presentCount++;
                        }
                    }

                    holder.tvAttendanceCount.setText("Presents: " + presentCount + " / " + tr.getAgreedDays());

                    try {
                        double totalFee = Double.parseDouble(tr.getFee());
                        double dailyFee = totalFee / tr.getAgreedDays();
                        double payable = dailyFee * presentCount;
                        holder.tvPayableAmount.setText("Currently Payable: Rs " + String.format("%.2f", payable));
                        
                        holder.btnReleasePayment.setOnClickListener(v -> releasePayment(tr, payable));
                    } catch (Exception e) {
                        Log.e("Escrow", "Error parsing fee", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSubject, tvTutorName, tvTotalFee, tvAttendanceCount, tvPayableAmount;
            MaterialButton btnReleasePayment;
            ViewHolder(View v) {
                super(v);
                tvSubject = v.findViewById(R.id.tvSubject);
                tvTutorName = v.findViewById(R.id.tvTutorName);
                tvTotalFee = v.findViewById(R.id.tvTotalFee);
                tvAttendanceCount = v.findViewById(R.id.tvAttendanceCount);
                tvPayableAmount = v.findViewById(R.id.tvPayableAmount);
                btnReleasePayment = v.findViewById(R.id.btnReleasePayment);
            }
        }
    }

    private void releasePayment(TuitionRequest tr, double amount) {
        tr.setStatus("completed");
        tr.setCurrentEscrowBalance(amount);

        tuitionRef.child(tr.getRequestId()).setValue(tr)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EscrowParentActivity.this, "Payment released! Please rate the tutor.", Toast.LENGTH_LONG).show();
                    
                    // After payment, redirect to review screen
                    Intent intent = new Intent(EscrowParentActivity.this, AddReviewActivity.class);
                    intent.putExtra("tuitionId", tr.getRequestId());
                    intent.putExtra("tutorId", tr.getTutorId());
                    startActivity(intent);
                    finish();
                });
    }
}

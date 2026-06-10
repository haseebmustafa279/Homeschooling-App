package com.example.homeschooling.activities;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManagePaymentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<TuitionRequest> list;
    AdminPaymentAdapter adapter;
    DatabaseReference tuitionRef, attendRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verify_tutors); // Reusing list layout

        TextView tvTitle = findViewById(android.R.id.content).getRootView().findViewById(R.id.rvVerifyTutors).getRootView().findViewById(R.id.rvVerifyTutors).getRootView().findViewWithTag("title");
        // Reusing activity_admin_verify_tutors.xml which has a TextView and RecyclerView

        recyclerView = findViewById(R.id.rvVerifyTutors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new AdminPaymentAdapter(list);
        recyclerView.setAdapter(adapter);

        tuitionRef = FirebaseDatabase.getInstance().getReference("TuitionRequests");
        attendRef = FirebaseDatabase.getInstance().getReference("Attendance");

        loadTuitions();
    }

    private void loadTuitions() {
        tuitionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    TuitionRequest tr = ds.getValue(TuitionRequest.class);
                    if (tr != null && ("active".equals(tr.getStatus()) || "completed".equals(tr.getStatus()))) {
                        list.add(tr);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    class AdminPaymentAdapter extends RecyclerView.Adapter<AdminPaymentAdapter.ViewHolder> {
        List<TuitionRequest> list;

        AdminPaymentAdapter(List<TuitionRequest> list) {
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
            holder.tvSubject.setText(tr.getSubject() + " (" + tr.getStatus().toUpperCase() + ")");
            holder.tvTotalFee.setText("Monthly Fee: Rs " + tr.getFee());

            // Load Names
            FirebaseDatabase.getInstance().getReference("Users").child(tr.getParentId()).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot s) {
                    String pName = s.exists() ? s.getValue(String.class) : "Parent";
                    FirebaseDatabase.getInstance().getReference("Users").child(tr.getTutorId()).child("name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot s2) {
                            String tName = s2.exists() ? s2.getValue(String.class) : "Tutor";
                            holder.tvTutorName.setText("P: " + pName + " | T: " + tName);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {}
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError e) {}
            });

            // Attendance Check
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
                    holder.tvAttendanceCount.setText("Attendance: " + presentCount + " presents");

                    if ("active".equals(tr.getStatus())) {
                        double totalFee = Double.parseDouble(tr.getFee());
                        double payable = (totalFee / tr.getAgreedDays()) * presentCount;
                        holder.tvPayableAmount.setText("Pending: Rs " + String.format("%.2f", payable));
                        holder.btnRelease.setVisibility(View.VISIBLE);
                        holder.btnRelease.setText("Force Release");
                        holder.btnRelease.setOnClickListener(v -> forceRelease(tr, payable));
                    } else {
                        holder.tvPayableAmount.setText("Released: Rs " + String.format("%.2f", tr.getCurrentEscrowBalance()));
                        holder.btnRelease.setVisibility(View.GONE);
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
            MaterialButton btnRelease;
            ViewHolder(View v) {
                super(v);
                tvSubject = v.findViewById(R.id.tvSubject);
                tvTutorName = v.findViewById(R.id.tvTutorName);
                tvTotalFee = v.findViewById(R.id.tvTotalFee);
                tvAttendanceCount = v.findViewById(R.id.tvAttendanceCount);
                tvPayableAmount = v.findViewById(R.id.tvPayableAmount);
                btnRelease = v.findViewById(R.id.btnReleasePayment);
            }
        }
    }

    private void forceRelease(TuitionRequest tr, double amount) {
        tr.setStatus("completed");
        tr.setCurrentEscrowBalance(amount);
        tuitionRef.child(tr.getRequestId()).setValue(tr)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Payment Force Released by Admin", Toast.LENGTH_SHORT).show());
    }
}

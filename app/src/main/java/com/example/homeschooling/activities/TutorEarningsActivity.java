package com.example.homeschooling.activities;

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
import com.example.homeschooling.models.Attendance;
import com.example.homeschooling.models.TuitionRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TutorEarningsActivity extends AppCompatActivity {

    TextView tvTotalBalance;
    RecyclerView rvEarnings;
    List<TuitionRequest> historyList;
    EarningsAdapter adapter;
    DatabaseReference tuitionRef;
    String tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_earnings);

        tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tuitionRef = FirebaseDatabase.getInstance().getReference("TuitionRequests");

        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        rvEarnings = findViewById(R.id.rvEarnings);
        rvEarnings.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        adapter = new EarningsAdapter(historyList);
        rvEarnings.setAdapter(adapter);

        loadEarnings();
    }

    private void loadEarnings() {
        tuitionRef.orderByChild("tutorId").equalTo(tutorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        double totalReleased = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            TuitionRequest tr = ds.getValue(TuitionRequest.class);
                            if (tr != null) {
                                // Include both active and completed in the history
                                if ("completed".equals(tr.getStatus()) || "active".equals(tr.getStatus())) {
                                    historyList.add(tr);
                                }
                                if ("completed".equals(tr.getStatus())) {
                                    totalReleased += tr.getCurrentEscrowBalance();
                                }
                            }
                        }
                        tvTotalBalance.setText("Rs " + String.format("%.2f", totalReleased));
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    class EarningsAdapter extends RecyclerView.Adapter<EarningsAdapter.ViewHolder> {
        List<TuitionRequest> list;

        EarningsAdapter(List<TuitionRequest> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_earning_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TuitionRequest tr = list.get(position);
            holder.tvSubject.setText(tr.getSubject());

            if ("completed".equals(tr.getStatus())) {
                holder.tvAmount.setText("Rs " + String.format("%.2f", tr.getCurrentEscrowBalance()));
                holder.tvStatus.setText("Status: Paid");
                holder.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                // For active tuitions, calculate accrued amount from attendance
                FirebaseDatabase.getInstance().getReference("Attendance").child(tr.getRequestId())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int presentCount = 0;
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    Attendance att = ds.getValue(Attendance.class);
                                    if (att != null && "Present".equals(att.getStatus())) {
                                        presentCount++;
                                    }
                                }
                                try {
                                    double totalFee = Double.parseDouble(tr.getFee());
                                    double accrued = (totalFee / tr.getAgreedDays()) * presentCount;
                                    holder.tvAmount.setText("Rs " + String.format("%.2f", accrued));
                                    holder.tvStatus.setText("Status: Pending (Accruing)");
                                    holder.tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                                } catch (Exception e) {
                                    holder.tvAmount.setText("Rs 0.00");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSubject, tvAmount, tvStatus;
            ViewHolder(View v) {
                super(v);
                tvSubject = v.findViewById(R.id.tvSubject);
                tvAmount = v.findViewById(R.id.tvAmount);
                tvStatus = v.findViewById(R.id.tvStatus);
            }
        }
    }
}

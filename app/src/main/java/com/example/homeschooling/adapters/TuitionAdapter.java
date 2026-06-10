package com.example.homeschooling.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.homeschooling.R;
import com.example.homeschooling.activities.ChatActivity;
import com.example.homeschooling.models.TuitionRequest;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TuitionAdapter extends RecyclerView.Adapter<TuitionAdapter.ViewHolder> {

    private List<TuitionRequest> list;

    public TuitionAdapter(List<TuitionRequest> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvClass, tvFee, tvTiming;
        MaterialButton btnAccept;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvFee = itemView.findViewById(R.id.tvFee);
            tvTiming = itemView.findViewById(R.id.tvTiming);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tuition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TuitionRequest model = list.get(position);

        // Fixed Labels
        holder.tvSubject.setText("Subject: " + (model.getSubject() != null ? model.getSubject() : "N/A"));
        holder.tvClass.setText("Class: " + (model.getClassLevel() != null ? model.getClassLevel() : "N/A"));
        holder.tvFee.setText("Monthly Fee: Rs " + (model.getFee() != null ? model.getFee() : "0"));
        holder.tvTiming.setText("Timing: " + (model.getTiming() != null ? model.getTiming() : "N/A"));

        if ("open".equals(model.getStatus())) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnAccept.setText("Accept Tuition");
        } else {
            holder.btnAccept.setVisibility(View.GONE);
        }

        holder.btnAccept.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            model.setStatus("active");
            model.setTutorId(currentUserId);
            
            FirebaseDatabase.getInstance().getReference("TuitionRequests")
                    .child(model.getRequestId()).setValue(model)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Tuition Accepted successfully!", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

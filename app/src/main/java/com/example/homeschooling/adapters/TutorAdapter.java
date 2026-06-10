package com.example.homeschooling.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeschooling.R;
import com.example.homeschooling.activities.DetailedTutorActivity;
import com.example.homeschooling.models.TutorSearchModel;

import java.util.List;
import java.util.Locale;

public class TutorAdapter extends RecyclerView.Adapter<TutorAdapter.ViewHolder> {

    Context context;
    List<TutorSearchModel> list;

    public TutorAdapter(Context context, List<TutorSearchModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_tutor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TutorSearchModel model = list.get(position);

        holder.tvName.setText(model.getName());
        holder.tvSubject.setText("Subjects: " + model.getSubjects());
        holder.tvFee.setText("Monthly Fee: Rs " + model.getHourlyFee());
        holder.tvCity.setText("City: " + model.getCity());

        if (model.getDistance() > 0) {
            holder.tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", model.getDistance()));
            holder.tvDistance.setVisibility(View.VISIBLE);
        } else {
            holder.tvDistance.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedTutorActivity.class);
            intent.putExtra("tutorId", model.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSubject, tvCity, tvFee, tvDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSubject = itemView.findViewById(R.id.tvSubjects);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvFee = itemView.findViewById(R.id.tvFee);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}

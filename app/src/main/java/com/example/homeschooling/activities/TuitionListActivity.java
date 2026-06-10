package com.example.homeschooling.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.homeschooling.R;
import com.example.homeschooling.adapters.TuitionAdapter;
import com.example.homeschooling.models.TuitionRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TuitionListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<TuitionRequest> list;
    TuitionAdapter adapter;

    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuition_list);

        recyclerView = findViewById(R.id.recyclerView);
        list = new ArrayList<>();
        adapter = new TuitionAdapter(list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ref = FirebaseDatabase.getInstance().getReference("TuitionRequests");

        loadData();
    }

    private void loadData() {

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    TuitionRequest model = snap.getValue(TuitionRequest.class);

                    if (model != null && model.getStatus() != null && model.getStatus().equals("open")) {
                        list.add(model);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

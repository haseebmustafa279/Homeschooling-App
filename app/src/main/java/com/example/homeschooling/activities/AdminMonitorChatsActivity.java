package com.example.homeschooling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeschooling.R;
import com.example.homeschooling.models.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminMonitorChatsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatPairAdapter adapter;
    private List<ChatPair> pairList;
    private DatabaseReference chatRef, userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list); // Reusing layout

        TextView tvTitle = findViewById(android.R.id.content).getRootView().findViewById(R.id.rvChatList).getRootView().findViewById(R.id.rvChatList).getRootView().findViewWithTag("title");
        // Actually activity_chat_list.xml has a TextView with text "My Messages". 
        // I should probably have used a better ID or just find it.
        
        recyclerView = findViewById(R.id.rvChatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pairList = new ArrayList<>();
        adapter = new ChatPairAdapter(pairList);
        recyclerView.setAdapter(adapter);

        chatRef = FirebaseDatabase.getInstance().getReference("Chats");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        loadChatPairs();
    }

    private void loadChatPairs() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uniquePairs = new HashSet<>();
                pairList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    if (msg != null) {
                        String u1 = msg.getSenderId();
                        String u2 = msg.getReceiverId();
                        // Sort IDs to ensure (A, B) and (B, A) are treated as the same pair
                        String pairKey = u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
                        if (!uniquePairs.contains(pairKey)) {
                            uniquePairs.add(pairKey);
                            pairList.add(new ChatPair(u1, u2));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    static class ChatPair {
        String user1Id, user2Id;
        ChatPair(String u1, String u2) {
            this.user1Id = u1;
            this.user2Id = u2;
        }
    }

    class ChatPairAdapter extends RecyclerView.Adapter<ChatPairAdapter.ViewHolder> {
        List<ChatPair> list;
        ChatPairAdapter(List<ChatPair> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatPair pair = list.get(position);
            
            // Fetch names for both users
            userRef.child(pair.user1Id).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot s1) {
                    String name1 = s1.exists() ? s1.getValue(String.class) : "Unknown";
                    userRef.child(pair.user2Id).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot s2) {
                            String name2 = s2.exists() ? s2.getValue(String.class) : "Unknown";
                            holder.tvNames.setText(name1 + " & " + name2);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError e) {}
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError e) {}
            });

            holder.tvDetail.setText("View Conversation");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AdminMonitorChatsActivity.this, ChatActivity.class);
                intent.putExtra("receiverId", pair.user2Id);
                intent.putExtra("senderIdOverride", pair.user1Id); // Special extra for admin to view specific chat
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNames, tvDetail;
            ViewHolder(View v) {
                super(v);
                tvNames = v.findViewById(R.id.tvUserName);
                tvDetail = v.findViewById(R.id.tvLastMessage);
            }
        }
    }
}

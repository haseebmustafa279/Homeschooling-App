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
import com.example.homeschooling.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    private String currentUserId;
    private Set<String> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.rvChatList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersList = new HashSet<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);
                    if (chat.getSenderId().equals(currentUserId)) {
                        usersList.add(chat.getReceiverId());
                    }
                    if (chat.getReceiverId().equals(currentUserId)) {
                        usersList.add(chat.getSenderId());
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void readChats() {
        mUsers = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (String id : usersList) {
                        if (user != null && user.getId() != null && user.getId().equals(id)) {
                            mUsers.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(mUsers);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<User> mUsers;

        public UserAdapter(List<User> mUsers) {
            this.mUsers = mUsers;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = mUsers.get(position);
            holder.username.setText(user.getName());
            holder.lastMessage.setText(user.getRole()); // Showing role as "last message" for now

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("receiverId", user.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView username;
            public TextView lastMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.tvUserName);
                lastMessage = itemView.findViewById(R.id.tvLastMessage);
            }
        }
    }
}

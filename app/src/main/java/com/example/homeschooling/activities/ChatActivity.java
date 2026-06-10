package com.example.homeschooling.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeschooling.R;
import com.example.homeschooling.adapters.ChatAdapter;
import com.example.homeschooling.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText etMessage;
    ImageButton btnSend;
    LinearLayout layoutSend;

    ChatAdapter chatAdapter;
    List<Message> mChat;

    DatabaseReference reference;
    String receiverId, senderId;
    boolean isAdminView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chatRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        layoutSend = findViewById(R.id.layout_send);

        receiverId = getIntent().getStringExtra("receiverId");
        
        // Admin monitoring logic
        String senderOverride = getIntent().getStringExtra("senderIdOverride");
        if (senderOverride != null) {
            senderId = senderOverride;
            isAdminView = true;
            if (layoutSend != null) {
                layoutSend.setVisibility(View.GONE); // Admin can't send messages in monitor mode
            }
            
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Monitoring Chat");
            }
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                String msg = etMessage.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    sendMessage(senderId, receiverId, msg);
                } else {
                    Toast.makeText(ChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                etMessage.setText("");
            });
        }

        readMessages(senderId, receiverId);
    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Message msg = new Message(sender, receiver, message, System.currentTimeMillis());
        reference.child("Chats").push().setValue(msg);
    }

    private void readMessages(String myId, String userId) {
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);
                    if (chat != null && chat.getReceiverId() != null && chat.getSenderId() != null) {
                        if ((chat.getReceiverId().equals(myId) && chat.getSenderId().equals(userId)) ||
                                (chat.getReceiverId().equals(userId) && chat.getSenderId().equals(myId))) {
                            mChat.add(chat);
                        }
                    }
                }
                
                chatAdapter = new ChatAdapter(mChat, myId); 
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}

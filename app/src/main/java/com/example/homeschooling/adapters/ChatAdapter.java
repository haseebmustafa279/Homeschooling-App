package com.example.homeschooling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeschooling.R;
import com.example.homeschooling.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private List<Message> mChat;
    private String showAsRightUserId;

    // Standard constructor for Parent/Tutor
    public ChatAdapter(List<Message> mChat) {
        this.mChat = mChat;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.showAsRightUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    // Constructor for Admin view to specify which side is which
    public ChatAdapter(List<Message> mChat, String showAsRightUserId) {
        this.mChat = mChat;
        this.showAsRightUserId = showAsRightUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new ChatViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message chat = mChat.get(position);
        holder.show_message.setText(chat.getMessage());
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;

        public ChatViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mChat.get(position).getSenderId().equals(showAsRightUserId)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}

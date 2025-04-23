package com.dev.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.authdemo.R;
import com.dev.model.ChatMessage;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private String requestId;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter<ChatMessage, MessageViewHolder> adapter;
    private EditText etMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        requestId = getIntent().getStringExtra("requestId");
        db        = FirebaseFirestore.getInstance();

        RecyclerView rv = findViewById(R.id.recyclerViewChat);
        rv.setLayoutManager(new LinearLayoutManager(this));

        etMessage = findViewById(R.id.etMessage);
        btnSend   = findViewById(R.id.btnSend);

        Query query = db.collection("Requests")
                .document(requestId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatMessage> options =
                new FirestoreRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(query, ChatMessage.class)
                        .build();

        adapter = new FirestoreRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat_message, parent, false);
                return new MessageViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ChatMessage model) {
                holder.tvMessage.setText(model.getText());

                boolean isMe = FirebaseAuth.getInstance().getUid().equals(model.getSenderId());
                holder.tvMessage.setBackgroundResource(
                        isMe ? R.drawable.chat_bubble_right : R.drawable.chat_bubble_left
                );
                holder.tvMessage.setTextAlignment(isMe ? View.TEXT_ALIGNMENT_VIEW_END : View.TEXT_ALIGNMENT_VIEW_START);
            }
        };


        rv.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            String uid = FirebaseAuth.getInstance().getUid();
            ChatMessage msg = new ChatMessage(uid, text, Timestamp.now());
            db.collection("Requests")
                    .document(requestId)
                    .collection("messages")
                    .add(msg);
            etMessage.setText("");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        MessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}

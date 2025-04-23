package com.dev.authdemo;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.model.RequestModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.BreakIterator;

public class RequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirestoreRecyclerAdapter<RequestModel, RequestViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        recyclerView = findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get current user's UID
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Query Firestore for requests where userId equals the current user's id
        Query query = db.collection("Requests").whereEqualTo("userId", currentUserId);

        FirestoreRecyclerOptions<RequestModel> options = new FirestoreRecyclerOptions.Builder<RequestModel>()
                .setQuery(query, RequestModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<RequestModel, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull RequestModel model) {
                holder.bind(model);
                holder.btnStatus.setOnClickListener(v -> {
                    // Get the document ID of the request from FirestoreRecyclerAdapter
                    String documentId = getSnapshots().getSnapshot(position).getId();

                    // Launch RequestDetailActivity and pass the documentId (and/or details)
                    Intent intent = new Intent(RequestsActivity.this, RequestDetailMapActivity.class);
                    intent.putExtra("docId", documentId);
                    // Optionally, pass other details if you prefer (or fetch them in the detail activity)
                    startActivity(intent);
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.item_request, group, false);
                return new RequestViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);

        FloatingActionButton fabRequest = findViewById(R.id.fabAddRequest);
        fabRequest.setOnClickListener(v -> {
            // Show the bottom sheet with requests
            RequestBottomSheetFragment bottomSheet = new RequestBottomSheetFragment();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
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

    // ViewHolder class for the RecyclerView
    private class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFirstName, tvLastName, tvAddress, tvServiceType, tvDescription, tvAmount;
        private Button btnStatus,btnChat;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFirstName = itemView.findViewById(R.id.tvItemFirstName);
            tvLastName = itemView.findViewById(R.id.tvItemLastName);
            tvAddress = itemView.findViewById(R.id.tvItemAddress);
            tvServiceType = itemView.findViewById(R.id.tvItemServiceType);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvAmount = itemView.findViewById(R.id.tvItemAmount);
            btnStatus = itemView.findViewById(R.id.btnStatus);
            btnChat = itemView.findViewById(R.id.btnChat);
        }

        public void bind(RequestModel model) {
            tvFirstName.setText("First Name: " + model.getFirstName());
            tvLastName.setText("Last Name: " + model.getLastName());
            tvAddress.setText("Address: " + model.getAddress());
            tvServiceType.setText("Service: " + model.getServiceType());
            tvDescription.setText("Description: " + model.getDescription());
            tvAmount.setText("Amount: $" + model.getAmount());
            btnStatus.setText(model.getStatus());
            if("Accepted".equals(model.getStatus())){
                btnChat.setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish the current activity
    }
}

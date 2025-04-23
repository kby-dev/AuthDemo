package com.dev.sp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.authdemo.R;
import com.dev.model.RequestModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestViewHolder extends RecyclerView.ViewHolder {
    TextView tvFirstName, tvLastName, tvServiceType, tvAddress, tvDescription, tvAmount, tvStatus;
    Button btnAccept, btnReject;

    public RequestViewHolder(@NonNull View itemView) {
        super(itemView);
        tvFirstName   = itemView.findViewById(R.id.tvItemFirstName);
        tvLastName    = itemView.findViewById(R.id.tvItemLastName);
        tvServiceType = itemView.findViewById(R.id.tvItemServiceType);
        tvAddress     = itemView.findViewById(R.id.tvItemAddress);
        tvDescription = itemView.findViewById(R.id.tvItemDescription);
        tvAmount      = itemView.findViewById(R.id.tvItemAmount);
        tvStatus      = itemView.findViewById(R.id.btnStatus);
        btnAccept     = itemView.findViewById(R.id.btnAccept);
        btnReject     = itemView.findViewById(R.id.btnReject);
    }

    @SuppressLint("SetTextI18n")
    public void bind(RequestModel model,
                     FirebaseFirestore db,
                     int position,
                     FirestoreRecyclerAdapter<RequestModel, RequestViewHolder> adapter,
                     Context context) {
        tvFirstName  .setText("First Name: " + defaultIfNull(model.getFirstName()));
        tvLastName   .setText("Last Name: "  + defaultIfNull(model.getLastName()));
        tvServiceType.setText("Service: "    + defaultIfNull(model.getServiceType()));
        tvAddress    .setText("Address: "    + defaultIfNull(model.getAddress()));
        tvDescription.setText("Desc: "       + defaultIfNull(model.getDescription()));
        tvAmount     .setText("Amount: $"    + model.getAmount());
        tvStatus     .setText(defaultIfNull(model.getStatus()));

        boolean pending = "Pending".equalsIgnoreCase(model.getStatus());
        btnAccept.setVisibility(pending ? View.VISIBLE : View.GONE);
        btnReject.setVisibility(pending ? View.VISIBLE : View.GONE);

        if (pending) {
            final String docId = adapter.getSnapshots()
                    .getSnapshot(position)
                    .getId();

            btnAccept.setOnClickListener(v ->
                    db.collection("Requests").document(docId)
                            .update("status","Accepted")
                            .addOnSuccessListener(a-> Toast
                                    .makeText(context,"Accepted",Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e-> Toast
                                    .makeText(context,"Error",Toast.LENGTH_SHORT).show())
            );

            btnReject.setOnClickListener(v ->
                    db.collection("Requests").document(docId)
                            .update("status","Rejected")
                            .addOnSuccessListener(a-> Toast
                                    .makeText(context,"Rejected",Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e-> Toast
                                    .makeText(context,"Error",Toast.LENGTH_SHORT).show())
            );
        }
    }

    private String defaultIfNull(String s) {
        return s != null ? s : "N/A";
    }
}

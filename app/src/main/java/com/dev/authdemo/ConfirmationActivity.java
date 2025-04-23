package com.dev.authdemo;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ConfirmationActivity extends AppCompatActivity {

    private TextView tvFirstName, tvLastName, tvAddress, tvServiceType, tvDescription, tvAmount;
    private Button btnConfirmPayment;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude, longitude;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

   // FirebaseFirestore db = FirebaseFirestore.getInstance();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        tvFirstName = findViewById(R.id.tvFirstName);
        tvLastName = findViewById(R.id.tvLastName);
        tvAddress = findViewById(R.id.tvAddress);
        tvServiceType = findViewById(R.id.tvServiceType);
        tvDescription = findViewById(R.id.tvDescription);
        tvAmount = findViewById(R.id.tvAmount);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        // Retrieve data from intent
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String address = intent.getStringExtra("address");
        String serviceType = intent.getStringExtra("serviceType");
        String description = intent.getStringExtra("description");
        double amount = intent.getDoubleExtra("amount", 0.0);
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);

        // Set the data to the UI
        tvFirstName.setText("First Name: " + firstName);
        tvLastName.setText("Last Name: " + lastName);
        tvAddress.setText("Address: " + address);
        tvServiceType.setText("Service Type: " + serviceType);
        tvDescription.setText("Description: " + description);
        tvAmount.setText("Amount: $" + amount);
        this.latitude = latitude;
        this.longitude = longitude;

        // Confirm Payment Button Click
        btnConfirmPayment.setOnClickListener(v -> {
            // Create a map to store the request details
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("firstName", firstName);
            requestData.put("lastName", lastName);
            requestData.put("address", address);
            requestData.put("serviceType", serviceType);
            requestData.put("description", description);
            requestData.put("amount", amount);
            // Optional: add a timestamp
            requestData.put("timestamp", FieldValue.serverTimestamp());
            requestData.put("status", "Pending"); // Pending, Accepted, Cancelled, Completed
            requestData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            requestData.put("latitude", latitude);
            requestData.put("longitude", longitude);



            // Get Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Requests")
                    .add(requestData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Request stored with ID: " + documentReference.getId());
                            Toast.makeText(ConfirmationActivity.this, "Request stored successfully!", Toast.LENGTH_LONG).show();
                            // After successful storage, navigate to the Welcome page (or any other page)
                            Intent homeIntent = new Intent(ConfirmationActivity.this, WelcomeActivity.class);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(homeIntent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error storing request", e);
                        Toast.makeText(ConfirmationActivity.this, "Failed to store request. Please try again.", Toast.LENGTH_LONG).show();
                    });
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Intent intent = new Intent(ConfirmationActivity.this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

    }
}

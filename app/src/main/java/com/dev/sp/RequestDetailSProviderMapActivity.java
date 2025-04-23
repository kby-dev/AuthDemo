package com.dev.sp;



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

import com.dev.authdemo.R;
import com.dev.authdemo.WelcomeActivity;
import com.dev.chat.ChatActivity;
import com.dev.model.RequestModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestDetailSProviderMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "RequestDetailMapActivity";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String docId;
    private TextView tvRequestDetails, tvServiceType, tvDescription;
    private Button btnAcceptRequest;
    private LatLng customerLocation;

    Making canges

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_sprovider_detail_map);

        tvRequestDetails = findViewById(R.id.tvRequestDetails);
        tvServiceType = findViewById(R.id.tvServiceType);
        tvDescription = findViewById(R.id.tvDescription);
        btnAcceptRequest = findViewById(R.id.btnAcceptRequest);

        db = FirebaseFirestore.getInstance();
        docId = getIntent().getStringExtra("docId");
        if (docId == null) {
            Toast.makeText(this, "No request ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fetchRequestDetails();

        btnAcceptRequest.setOnClickListener(v -> acceptRequest());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RequestDetailSProviderMapActivity.this, ServiceProviderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    private void fetchRequestDetails() {
        DocumentReference docRef = db.collection("Requests").document(docId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                RequestModel request = documentSnapshot.toObject(RequestModel.class);
                if (request != null) {
                    String fullName = request.getFirstName() + " " + request.getLastName();
                    tvRequestDetails.setText("Request from: " + fullName);
                    tvServiceType.setText("Service: " + request.getServiceType());
                    tvDescription.setText("Description: " + request.getDescription());
                    // Get customer's location from document fields "latitude" and "longitude"
                    Double lat = documentSnapshot.getDouble("latitude");
                    Double lng = documentSnapshot.getDouble("longitude");
                    if (lat != null && lng != null) {
                        customerLocation = new LatLng(lat, lng);
                        if (mMap != null) {
                            mMap.addMarker(new MarkerOptions().position(customerLocation).title("Customer Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 14f));
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching request details", e);
            Toast.makeText(this, "Error fetching details", Toast.LENGTH_SHORT).show();
        });
    }

    private void acceptRequest() {
        String spId = FirebaseAuth.getInstance().getUid(); // Get SP's user ID

        if (spId == null) {
            Toast.makeText(this, "You must be logged in to accept requests", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Requests").document(docId)
                .update("status", "Accepted",
                        "serviceProviderId", spId) // ← Add this line
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RequestDetailSProviderMapActivity.this, ChatActivity.class);
                    intent.putExtra("requestId", docId);  // Pass the requestId to ChatActivity
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error accepting request", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error accepting request", e);
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // If customerLocation is already fetched, add marker and move camera
        if (customerLocation != null) {
            mMap.addMarker(new MarkerOptions().position(customerLocation).title("Customer Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 14f));
        }
    }
}

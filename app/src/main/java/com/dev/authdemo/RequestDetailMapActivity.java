package com.dev.authdemo;

import static android.content.ContentValues.TAG;

import android.Manifest;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dev.chat.ChatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dev.model.RequestModel;


public class RequestDetailMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Circle pulseCircle;
    private ValueAnimator pulseAnimator;
    private FirebaseFirestore db;
    private TextView tvSearchStatus, tvRequestSummary;
    private Button btnCancelRequest,btnBack,btnChat;
    private String docId;
    private FusedLocationProviderClient fusedLocationClient;
    // Dummy user location (replace with actual user location)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail_map);

        tvSearchStatus = findViewById(R.id.tvSearchStatus);
        tvRequestSummary = findViewById(R.id.tvRequestSummary);
        btnCancelRequest = findViewById(R.id.btnCancelRequest);
        btnBack = findViewById(R.id.btnBack);
        btnChat = findViewById(R.id.btnChat);

        db = FirebaseFirestore.getInstance();
        // Assume docId is passed via intent
        docId = getIntent().getStringExtra("docId");
        if (docId == null) {
            Toast.makeText(this, "No request found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Load the map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        fetchRequestDetails();

        // Set cancel button action
        btnCancelRequest.setOnClickListener(v -> cancelRequest());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Intent intent = new Intent(RequestDetailMapActivity.this, RequestsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RequestDetailMapActivity.this, RequestsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("requestId", docId);
            startActivity(intent);
        });
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent(this, RequestsActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        finish(); // Finish the current activity
//    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Center map on user location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get the actual location
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng actualUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actualUserLocation, 15f));
                // Start the pulse animation at the actual user location
                startPulseAnimation(actualUserLocation);
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPulseAnimation(LatLng center) {
        // Create or reset the circle at the user's location
        if (pulseCircle == null) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(center)
                    .radius(0) // Start with 0 radius
                    .strokeColor(Color.BLUE)
                    .strokeWidth(2)
                    .fillColor(Color.argb(100, 0, 0, 255)); // Semi-transparent blue
            pulseCircle = mMap.addCircle(circleOptions);
        } else {
            pulseCircle.setCenter(center);
            pulseCircle.setRadius(0);
        }

        // Animate from radius 0 to 500 meters (adjust as needed)
        pulseAnimator = ValueAnimator.ofFloat(0, 700);
        pulseAnimator.setDuration(2000); // Duration in milliseconds
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            float animatedRadius = (float) animation.getAnimatedValue();
            // Update the circle's radius
            pulseCircle.setRadius(animatedRadius);
            // Calculate fade: alpha goes from 100 to 0
            float fraction = animatedRadius / 700;
            int alpha = (int) (100 * (1 - fraction));
            int fillColor = Color.argb(alpha, 0, 0, 255);
            pulseCircle.setFillColor(fillColor);
        });
        pulseAnimator.start();
    }

    // Call this method when the request is cancelled or no longer pending
    private void stopPulseAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        if (pulseCircle != null) {
            pulseCircle.remove();
            pulseCircle = null;
        }
    }

    @SuppressLint("SetTextI18n")
    private void fetchRequestDetails() {
        // Get the request document from Firestore and update UI overlay
        DocumentReference docRef = db.collection("Requests").document(docId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                RequestModel request = documentSnapshot.toObject(RequestModel.class);
                if (request != null) {
                    // Update overlay details
                    String summary = "Service: " + request.getServiceType() + "\n"
                            + "Description: " + request.getDescription();
                    tvRequestSummary.setText(summary);
                    tvSearchStatus.setText("Searching for provider... (Status: " + request.getStatus() + ")");

                    if ("Cancelled".equalsIgnoreCase(request.getStatus())) {
                        tvSearchStatus.setText("Request Cancelled! ");
                        btnCancelRequest.setText("Cancelled");
                        btnCancelRequest.setEnabled(false);
                        // Optionally stop animation here
                        stopPulseAnimation();
                    }

                    // If the status is not Pending, stop animation or update UI accordingly
                    else if ("Accepted".equalsIgnoreCase(request.getStatus())) {   // edit to add chat button.
                        tvSearchStatus.setText("Provider Found! (Status: " + request.getStatus() + ")");
                        // Optionally stop animation here
                        btnCancelRequest.setEnabled(false);
                        btnCancelRequest.setText("Accepted");
                        btnChat.setVisibility(View.VISIBLE);
                        stopPulseAnimation();
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching request", e);
            Toast.makeText(this, "Error loading request details.", Toast.LENGTH_SHORT).show();
        });
    }

    // Example method: cancel the request and stop the animation
    private void cancelRequest() {
        DocumentReference docRef = db.collection("Requests").document(docId);
        docRef.update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RequestDetailMapActivity.this, "Request cancelled.", Toast.LENGTH_SHORT).show();
                    stopPulseAnimation(); // Stop the pulse animation when cancelled
                    btnCancelRequest.setText("Cancelled");
                    btnCancelRequest.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RequestDetailMapActivity.this, "Failed to cancel request.", Toast.LENGTH_SHORT).show();
                });
    }
}

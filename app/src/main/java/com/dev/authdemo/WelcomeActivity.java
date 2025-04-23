package com.dev.authdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.dev.sp.ServiceProviderActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private GoogleMap mMap;
    private FloatingActionButton fabRequest;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Set up the Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Initialize DrawerLayout and NavigationView (if you have a drawer)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the Request FAB
        fabRequest = findViewById(R.id.fab_request);
        fabRequest.setOnClickListener(v -> {
            // Show the bottom sheet with requests
            RequestBottomSheetFragment bottomSheet = new RequestBottomSheetFragment();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        // Pseudocode snippet inside onCreate() or in your navigation setup:
        Button btnSwitchProfile = findViewById(R.id.btnSwitchProfile);
        btnSwitchProfile.setOnClickListener(v -> {
            // Update the user's profile type to serviceProvider
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .update("profileType", "serviceProvider")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(WelcomeActivity.this, "Switched to Service Provider mode", Toast.LENGTH_SHORT).show();
                        // Navigate to the Service Provider dashboard/activity
                        Intent intent = new Intent(WelcomeActivity.this, ServiceProviderActivity.class);
                        startActivity(intent);
                        // Optionally finish WelcomeActivity if you want to prevent going back
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(WelcomeActivity.this, "Failed to switch profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


    }
    private String getCurrentProfile() {
        return getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("profileType", "customer"); // default is customer
    }

    private void setCurrentProfile(String profileType) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                .putString("profileType", profileType).apply();
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // You can set markers, camera position, etc. here.
        mMap = googleMap;

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Move the camera to the user's current location
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    }
                }
            });


        } else {
            // Request the permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location layer
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Permission denied: inform the user that location permission is required for live location display
                Toast.makeText(this, "Location permission is needed to show your live location.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    // Inflate your custom three-dots menu (if still needed)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate your menu resource if you want to use it for other actions.
        getMenuInflater().inflate(R.menu.welcome_menu, menu);
        return true;
    }

    // Handle the three-dots icon click event
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_open_drawer) {
            // For example, open the navigation drawer
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            } else {
                // Or simply show a Toast if you don't use a drawer
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle NavigationView item clicks (if you use a drawer)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_requests) {
//            RequestBottomSheetFragment bottomSheet = new RequestBottomSheetFragment();
//            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            Toast.makeText(this, "Requests Selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(WelcomeActivity.this, RequestsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
        return true;
    }
}

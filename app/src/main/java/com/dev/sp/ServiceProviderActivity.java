package com.dev.sp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dev.authdemo.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dev.sp.RequestViewHolder;


public class ServiceProviderActivity extends AppCompatActivity {
    private FloatingActionButton fabOnline;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private boolean isOnline = false;
    private LatLng providerLocation;

    /** Launcher to request ACCESS_FINE_LOCATION at runtime **/
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchLocation();
                } else {
                    Toast.makeText(
                            this,
                            "Location permission is required to go online.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider);

        // 1) Initialize services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // 2) FAB: toggle online/offline
        fabOnline = findViewById(R.id.fab_online);
        fabOnline.setOnClickListener(view -> toggleOnlineStatus());

        // 3) BottomNavigationView: switch between three status tabs
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();
            if (id == R.id.nav_accepted) {
                selected = StatusListFragment.newInstance("Accepted");
            } else if (id == R.id.nav_ignored) {
                selected = StatusListFragment.newInstance("Rejected");
            } else {
                // default to Pending
                selected = StatusListFragment.newInstance("Pending");
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selected)
                    .commit();
            return true;
        });
        // Trigger default tab
        bottomNav.setSelectedItemId(R.id.nav_pending);
    }

    /** Toggles FAB & online state. When going online, fetches location. **/
    private void toggleOnlineStatus() {
        isOnline = !isOnline;
        if (isOnline) {
            // need permission to fetch location
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            fabOnline.setImageResource(R.drawable.ic_online_active);
        } else {
            providerLocation = null;
            Toast.makeText(this, "You are now offline", Toast.LENGTH_SHORT).show();
            fabOnline.setImageResource(R.drawable.ic_online);
            // refresh current fragment so Pending is hidden if needed
            refreshCurrentFragment();
        }
    }

    /** Retrieves last-known location and stores it; then refreshes visible fragment. **/
    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, (Location loc) -> {
                    if (loc != null) {
                        providerLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
                        Toast.makeText(
                                this,
                                "Location obtained: "
                                        + loc.getLatitude() + ", " + loc.getLongitude(),
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                        providerLocation = null;
                    }
                    refreshCurrentFragment();
                    Toast.makeText(this, "You are now online", Toast.LENGTH_SHORT).show();
                });
    }

    /** Helper: reload whichever fragment is currently visible so it picks up new state. **/
    private void refreshCurrentFragment() {
        Fragment curr = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (curr instanceof StatusListFragment) {
            String stat = ((StatusListFragment) curr).getStatus();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, StatusListFragment.newInstance(stat))
                    .commit();
        }
    }

    /** Expose to fragments **/
    public LatLng getProviderLocation() { return providerLocation; }
    public boolean isOnline()               { return isOnline; }
}

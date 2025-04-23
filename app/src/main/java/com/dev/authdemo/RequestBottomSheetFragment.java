package com.dev.authdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.Manifest;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RequestBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText etFirstName, etLastName, etAddress, etOtherService, etDescription;
    private double etlatitude, etlongitude;
    private Spinner spinnerServiceType;
    private Button btnSubmitRequest;

    private FusedLocationProviderClient fusedLocationClient;

    public RequestBottomSheetFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_request_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etAddress = view.findViewById(R.id.etAddress);
        spinnerServiceType = view.findViewById(R.id.spinnerServiceType);
        etOtherService = view.findViewById(R.id.etOtherService);
        etDescription = view.findViewById(R.id.etDescription);
        btnSubmitRequest = view.findViewById(R.id.btnSubmitRequest);

        // Setup Spinner with sample service types
        String[] serviceTypes = new String[]{"Plumbing", "Electrical", "Cleaning", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, serviceTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServiceType.setAdapter(adapter);

        spinnerServiceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (selected.equalsIgnoreCase("Other")) {
                    etOtherService.setVisibility(View.VISIBLE);
                } else {
                    etOtherService.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etOtherService.setVisibility(View.GONE);
            }
        });

        // Initialize fused location provider to fetch the address
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        fetchAndSetAddress();

        // Handle submit button click
        btnSubmitRequest.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String serviceType = spinnerServiceType.getSelectedItem().toString();
            if (serviceType.equalsIgnoreCase("Other")) {
                serviceType = etOtherService.getText().toString().trim();
            }
            String description = etDescription.getText().toString().trim();
            double latitude = etlatitude;
            double longitude = etlongitude;

            if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() ||
                    serviceType.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Here you can process the request details (e.g., send to your backend)


            Toast.makeText(getContext(), "Request submitted!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), PaymentDetailsActivity.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("address", address);
            intent.putExtra("serviceType", serviceType);
            intent.putExtra("description", description);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            startActivity(intent);
            dismiss();
        });
    }

    private void fetchAndSetAddress() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            // Optionally, you can request the permission here
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address addressObj = addresses.get(0);
                            StringBuilder addressText = new StringBuilder();
                            if (addressObj.getAddressLine(0) != null) {
                                addressText.append(addressObj.getAddressLine(0));
                            }
                            etAddress.setText(addressText.toString());
                            etlatitude = location.getLatitude();
                            etlongitude = location.getLongitude();
                        } else {
                            Toast.makeText(getContext(), "Unable to retrieve address", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Unable to retrieve address", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Location not available", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Security Exception: Location permission missing", Toast.LENGTH_SHORT).show();
        }
    }

}

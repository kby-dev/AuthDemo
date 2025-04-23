package com.dev.model;

import com.google.firebase.Timestamp;

public class RequestModel {
    private String firstName;
    private String lastName;
    private String address;
    private String serviceType;
    private String description;
    private double amount;
    private String userId;  // ID of the user who made the request
    private Timestamp timestamp; // Timestamp for the request
    private String status;    // "Pending", "Accepted", "Rejected", "Cancelled"

    // Empty constructor is required for Firestore
    public RequestModel() {}

    // Getters (and optionally setters) for each field
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public String getServiceType() { return serviceType; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public Timestamp getTimestamp() { return timestamp; }
}
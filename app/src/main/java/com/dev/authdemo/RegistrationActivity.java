package com.dev.authdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone, etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;  // Firestore instance for storing additional details

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName  = etLastName.getText().toString().trim();
            String phone     = etPhone.getText().toString().trim();
            String email     = etEmail.getText().toString().trim();
            String password  = etPassword.getText().toString().trim();

            // Validate input
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(RegistrationActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user with email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                // Update the user profile with the display name (first and last name)
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(firstName + " " + lastName)
                                        .build();
                                currentUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileUpdateTask -> {
                                            if (profileUpdateTask.isSuccessful()) {
                                                // Prepare additional user details to store in Firestore
                                                Map<String, String> userData = new HashMap<>();
                                                userData.put("firstName", firstName);
                                                userData.put("lastName", lastName);
                                                userData.put("phone", phone);
                                                userData.put("email", email);
                                                userData.put("role", "customer");
                                                userData.put("approved", "false");

                                                // Save user details to Firestore under a "Users" collection
                                                db.collection("Users")
                                                        .document(currentUser.getUid())
                                                        .set(userData)
                                                        .addOnCompleteListener(dbTask -> {
                                                            Toast.makeText(RegistrationActivity.this, "Registration on going ", Toast.LENGTH_SHORT).show();
                                                            if (dbTask.isSuccessful()) {
                                                                Toast.makeText(RegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(RegistrationActivity.this, WelcomeActivity.class);
                                                                startActivity(intent);
                                                                finish();  // Finish RegistrationActivity to prevent going back
                                                            } else {
                                                                Toast.makeText(RegistrationActivity.this, "Failed to store user data", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(RegistrationActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Registration Failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

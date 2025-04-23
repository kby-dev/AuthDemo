package com.dev.authdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLinkActivity extends AppCompatActivity {

    private static final String TAG = "PhoneLinkActivity";
    private EditText etPhone, etOtp;
    private Button btnSendOtp, btnVerifyOtp;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_link);

        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        mAuth = FirebaseAuth.getInstance();

        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = etPhone.getText().toString().trim();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(PhoneLinkActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendVerificationCode(phoneNumber);
            }
        });

        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = etOtp.getText().toString().trim();
                if (code.isEmpty()) {
                    Toast.makeText(PhoneLinkActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                    return;
                }
                verifyCodeAndLink(code);
            }
        });
    }

    // Step 1: Send OTP to the entered phone number.
    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)              // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS)          // Timeout duration
                .setActivity(this)                        // Activity for callback binding
                .setCallbacks(mCallbacks)                 // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Callback for phone number verification events.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Auto-retrieval or instant verification succeeded.
                    linkPhoneCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.w(TAG, "onVerificationFailed", e);
                    Toast.makeText(PhoneLinkActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(verificationId, token);
                    // Save the verification ID and resending token so we can use them later.
                    mVerificationId = verificationId;
                    mResendToken = token;
                    // Reveal the OTP input and verification button.
                    etOtp.setVisibility(View.VISIBLE);
                    btnVerifyOtp.setVisibility(View.VISIBLE);
                    Toast.makeText(PhoneLinkActivity.this, "OTP Sent! Check your messages.", Toast.LENGTH_SHORT).show();
                }
            };

    // Step 2: Verify the OTP code and link the phone number to the current user.
    private void verifyCodeAndLink(String code) {
        if (mVerificationId == null) {
            Toast.makeText(PhoneLinkActivity.this, "Please request OTP first", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        linkPhoneCredential(credential);
    }

    // Link the phone authentication credential with the currently signed-in user.
    private void linkPhoneCredential(PhoneAuthCredential credential) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(PhoneLinkActivity.this, "No authenticated user found.", Toast.LENGTH_SHORT).show();
            return;
        }

        user.linkWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(PhoneLinkActivity.this, "Phone number linked successfully!", Toast.LENGTH_SHORT).show();
                            // Optionally, navigate to the next screen or finish this activity.
                            Intent intent = new Intent(PhoneLinkActivity.this, WelcomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(PhoneLinkActivity.this, "Linking failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

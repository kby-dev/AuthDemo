package com.dev.authdemo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentDetailsActivity extends AppCompatActivity {

    private EditText etCardNumber, etExpiryDate, etCVV;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etCVV = findViewById(R.id.etCVV);
        btnPay = findViewById(R.id.btnPay);

        btnPay.setOnClickListener(v -> {
            // Collect payment details
            String cardNumber = etCardNumber.getText().toString().trim();
            String expiryDate = etExpiryDate.getText().toString().trim();
            String cvv = etCVV.getText().toString().trim();

//            if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
//                Toast.makeText(PaymentDetailsActivity.this, "Please fill in all payment details", Toast.LENGTH_SHORT).show();
//                return;
//            }


            // Process payment here (e.g., integrate with a payment gateway API)

            Intent receivedIntent = getIntent();
            String firstName = receivedIntent.getStringExtra("firstName");
            String lastName = receivedIntent.getStringExtra("lastName");
            String address = receivedIntent.getStringExtra("address");
            String serviceType = receivedIntent.getStringExtra("serviceType");
            String description = receivedIntent.getStringExtra("description");
            double latitude = receivedIntent.getDoubleExtra("latitude", 0.0);
            double longitude = receivedIntent.getDoubleExtra("longitude", 0.0);

            double amount = 00.00; // Replace this with actual calculated amount

            // Navigate to Confirmation Page
            Intent intent = new Intent(PaymentDetailsActivity.this, ConfirmationActivity.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("address", address);
            intent.putExtra("serviceType", serviceType);
            intent.putExtra("description", description);
            intent.putExtra("amount", amount);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            startActivity(intent);


            Toast.makeText(PaymentDetailsActivity.this, "Payment processed successfully!", Toast.LENGTH_SHORT).show();
            // Optionally finish the activity or navigate to a confirmation page.
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Intent intent = new Intent(PaymentDetailsActivity.this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}

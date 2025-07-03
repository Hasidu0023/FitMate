package com.example.fitmate.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class WaterTrackerActivity extends AppCompatActivity {

    private static final String TAG = "WaterTracker";

    private TextView tvUserEmail, tvWaterAmount, tvPreviousWater;
    private Button btnAdd100, btnAdd250, btnAdd500, btnClearWater;

    private int waterAmount = 0;             // current water intake
    private int previousSavedWater = 0;      // previously saved amount
    private String email = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tracker);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind views
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvWaterAmount = findViewById(R.id.tvWaterAmount);
        tvPreviousWater = findViewById(R.id.tvPreviousWater);
        btnAdd100 = findViewById(R.id.btnAdd100);
        btnAdd250 = findViewById(R.id.btnAdd250);
        btnAdd500 = findViewById(R.id.btnAdd500);
        btnClearWater = findViewById(R.id.btnClearWater);

        // Get email from intent
        email = getIntent().getStringExtra("USER_EMAIL");

        if (email != null && !email.isEmpty()) {
            tvUserEmail.setText("Email: " + email);
            Log.d(TAG, "Received email: " + email);
            fetchInitialWaterIntake();  // ✅ Fetch previous value from Firestore
        } else {
            Log.e(TAG, "No email received");
            Toast.makeText(this, "User email not found!", Toast.LENGTH_SHORT).show();
            tvUserEmail.setText("Email: Unknown");
        }

        // Add water buttons
        btnAdd100.setOnClickListener(v -> addWater(100));
        btnAdd250.setOnClickListener(v -> addWater(250));
        btnAdd500.setOnClickListener(v -> addWater(500));

        // Clear water level
        btnClearWater.setOnClickListener(v -> clearWaterLevel());
    }

    // Save on exit
    @Override
    protected void onPause() {
        super.onPause();
        if (email != null && !email.isEmpty() && waterAmount != previousSavedWater) {
            updateWaterIntakeInFirestore(email, waterAmount);
        }
    }

    // Fetch saved water from Firestore
    private void fetchInitialWaterIntake() {
        db.collection("Reports").document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long waterCountLong = documentSnapshot.getLong("finalWaterCount");
                        if (waterCountLong != null) {
                            waterAmount = waterCountLong.intValue();
                            previousSavedWater = waterAmount;
                        } else {
                            waterAmount = 0;
                            previousSavedWater = 0;
                        }
                    } else {
                        waterAmount = 0;
                        previousSavedWater = 0;
                    }
                    updateWaterAmountText();
                    updatePreviousWaterText();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch water intake", e);
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Add water
    private void addWater(int amount) {
        waterAmount += amount;
        updateWaterAmountText();
        Toast.makeText(this, "Added " + amount + " ml", Toast.LENGTH_SHORT).show();
    }

    // Clear water
    private void clearWaterLevel() {
        waterAmount = 0;
        updateWaterAmountText();
        Toast.makeText(this, "Water level cleared!", Toast.LENGTH_SHORT).show();
    }

    // Show current amount
    private void updateWaterAmountText() {
        tvWaterAmount.setText("Current Water Intake: " + waterAmount + " ml");
    }

    // Show previous saved amount
    private void updatePreviousWaterText() {
        tvPreviousWater.setText("Previous saved water intake: " + previousSavedWater + " ml");
    }

    // Update Firestore
    private void updateWaterIntakeInFirestore(String email, int amount) {
        db.collection("Reports").document(email)
                .update("finalWaterCount", amount)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Water intake updated");
                    previousSavedWater = amount;  // ✅ Update saved copy
                    updatePreviousWaterText();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Document missing, creating new", e);
                    createWaterIntakeDocument(email, amount);
                });
    }

    // Create Firestore doc if not found
    private void createWaterIntakeDocument(String email, int amount) {
        db.collection("Reports").document(email)
                .set(new WaterCountModel(amount))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Document created");
                    previousSavedWater = amount;
                    updatePreviousWaterText();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create document", e);
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Data model
    private static class WaterCountModel {
        int finalWaterCount;

        WaterCountModel(int count) {
            this.finalWaterCount = count;
        }
    }
}

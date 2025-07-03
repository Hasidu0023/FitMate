package com.example.fitmate.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class BMIResultActivity extends AppCompatActivity {

    private TextView tvBmiValue, tvBmiStatus, tvEmail;
    private Button btnMealPlans, btnExercisePlans;
    private Button btnEmergency, btnStepCounter, btnHealthTips, btnReports;
    private Button btnAmbientLight, btnThermo;  // New buttons

    private String email = "";
    private FirebaseFirestore db;  // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_result);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // TextViews
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);
        tvEmail = findViewById(R.id.tvEmail);

        // Buttons
        btnMealPlans = findViewById(R.id.btnMealPlans);
        btnExercisePlans = findViewById(R.id.btnExercisePlans);
        btnEmergency = findViewById(R.id.btnEmergency);
        btnStepCounter = findViewById(R.id.btnWaterTracker);
        btnHealthTips = findViewById(R.id.btnHealthTips);
        btnReports = findViewById(R.id.btnReports);

        // New buttons
        btnAmbientLight = findViewById(R.id.btnAmbientLight);
        btnThermo = findViewById(R.id.btnThermo);

        // Get Intent data
        float bmi = getIntent().getFloatExtra("BMI_VALUE", 0f);
        String status = getIntent().getStringExtra("BMI_STATUS");
        email = getIntent().getStringExtra("USER_EMAIL");

        // Set BMI TextViews
        tvBmiValue.setText(String.format("Your BMI: %.2f", bmi));
        tvBmiStatus.setText("Status: " + status);

        // Load user name by email and show welcome message
        if (email != null && !email.isEmpty()) {
            loadUserNameFromEmail(email);
        } else {
            tvEmail.setText("Welcome, Guest!");
        }

        // Button Listeners
        btnMealPlans.setOnClickListener(v -> {
            Intent intent = new Intent(BMIResultActivity.this, MealPlansActivity.class);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
        });

        btnExercisePlans.setOnClickListener(v -> {
            Intent intent = new Intent(BMIResultActivity.this, ExercisePlansActivity.class);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
        });

        btnStepCounter.setOnClickListener(v -> {
            Intent intent = new Intent(BMIResultActivity.this, StepCounterActivity.class);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
        });

        btnEmergency.setOnClickListener(v -> makeEmergencyCall());

        btnHealthTips.setOnClickListener(v -> {
            Intent intent = new Intent(BMIResultActivity.this, HealthTipsActivity.class);
            intent.putExtra("BMI_VALUE", bmi);
            startActivity(intent);
        });

        btnReports.setOnClickListener(v -> {
            String url = "https://www.echannelling.com/";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        // Ambient Light button
        btnAmbientLight.setOnClickListener(v -> {
            Intent intent = new Intent(BMIResultActivity.this, AmbientLightActivity.class);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
        });

        // Thermo button — Pass email here!
        btnThermo.setOnClickListener(v -> {
            Intent intent = new Intent(BMIResultActivity.this, WaterTrackerActivity.class);
            intent.putExtra("USER_EMAIL", email);  // Pass the email to WaterTrackerActivity
            startActivity(intent);
        });
    }

    private void loadUserNameFromEmail(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String name = snapshot.getDocuments().get(0).getString("name");
                        tvEmail.setText("Welcome, " + (name != null ? name : "User") + "!");
                    } else {
                        tvEmail.setText("Welcome, Guest!");
                    }
                })
                .addOnFailureListener(e -> {
                    tvEmail.setText("Welcome, User!");
                });
    }

    private void makeEmergencyCall() {
        String phoneNumber = "1990"; // Sri Lanka emergency number
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(dialIntent);
    }
}

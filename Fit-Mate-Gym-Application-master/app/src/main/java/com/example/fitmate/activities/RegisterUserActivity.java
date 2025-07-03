package com.example.fitmate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterUserActivity extends AppCompatActivity {

    private EditText edtUserId, edtAge, edtHeight, edtWeight;
    private CheckBox cbCancer, cbHeart, cbDiabetes, cbCholesterol;
    private Button btnCalculateBMI, btnViewBMI, btnAction;
    private TextView tvBmiResult, tvBmiStatus, tvGreeting;

    private FirebaseFirestore db;
    private float bmi = 0f;
    private String statusText = "";
    private int color;
    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        edtUserId = findViewById(R.id.edtUserId);
        edtAge = findViewById(R.id.edtAge);
        edtHeight = findViewById(R.id.edtHeight);
        edtWeight = findViewById(R.id.edtWeight);

        cbCancer = findViewById(R.id.cbCancer);
        cbHeart = findViewById(R.id.cbHeart);
        cbDiabetes = findViewById(R.id.cbDiabetes);
        cbCholesterol = findViewById(R.id.cbCholesterol);

        btnCalculateBMI = findViewById(R.id.btnSubmitUserData);
        btnViewBMI = findViewById(R.id.btnViewBMI);
        btnAction = findViewById(R.id.btnAction);

        tvBmiResult = findViewById(R.id.tvBmiResult);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);
        tvGreeting = findViewById(R.id.tvGreeting);

        db = FirebaseFirestore.getInstance();

        email = getIntent().getStringExtra("USER_EMAIL");
        if (!TextUtils.isEmpty(email)) {
            edtUserId.setText(email);
            edtUserId.setEnabled(false);
            fetchAndSetAge(email);
            fetchAndSetName(email);
        } else {
            setGreeting("User");
        }

        btnCalculateBMI.setText("Calculate");

        btnCalculateBMI.setOnClickListener(v -> {
            if (validateAndCalculateBMI()) {
                tvBmiResult.setText("BMI: " + String.format(Locale.US, "%.2f", bmi));
                tvBmiResult.setVisibility(TextView.VISIBLE);

                tvBmiStatus.setText(statusText);
                tvBmiStatus.setTextColor(color);
                tvBmiStatus.setVisibility(TextView.VISIBLE);

                btnViewBMI.setVisibility(Button.VISIBLE);
                Toast.makeText(this, "BMI Report submitted successfully!", Toast.LENGTH_LONG).show();
            }
        });

        btnViewBMI.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterUserActivity.this, BMIResultActivity.class);
            intent.putExtra("BMI_VALUE", bmi);
            intent.putExtra("BMI_STATUS", statusText);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
        });

        btnAction.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterUserActivity.this, HistoryActivity.class);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
        });
    }

    private void fetchAndSetAge(String userEmail) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String dob = document.getString("dob");
                            if (dob != null && !dob.isEmpty()) {
                                int age = calculateAgeFromDOB(dob);
                                if (age != -1) {
                                    edtAge.setText(String.valueOf(age));
                                }
                            }
                            break;
                        }
                    }
                });
    }

    private void fetchAndSetName(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String name = doc.getString("name");
                        setGreeting(name != null ? name : "User");
                    } else {
                        setGreeting("User");
                    }
                })
                .addOnFailureListener(e -> setGreeting("User"));
    }

    private void setGreeting(String name) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        tvGreeting.setText(greeting + ", " + name);
    }

    private int calculateAgeFromDOB(String dob) {
        try {
            String[] parts = dob.split("/");
            if (parts.length != 3) return -1;

            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            Calendar dobCal = Calendar.getInstance();
            dobCal.set(year, month - 1, day);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (today.get(Calendar.MONTH) < dobCal.get(Calendar.MONTH) ||
                    (today.get(Calendar.MONTH) == dobCal.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) < dobCal.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }

            return Math.max(age, 0);
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean validateAndCalculateBMI() {
        String ageStr = edtAge.getText().toString().trim();
        String heightStr = edtHeight.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(ageStr)
                || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float heightCm = Float.parseFloat(heightStr);
            float weightKg = Float.parseFloat(weightStr);

            if (age <= 0 || heightCm <= 0 || weightKg <= 0) {
                Toast.makeText(this, "Invalid input values", Toast.LENGTH_SHORT).show();
                return false;
            }

            float heightM = heightCm / 100f;
            bmi = weightKg / (heightM * heightM);

            if (bmi < 18.5) {
                statusText = "Underweight – Eat more!";
                color = getResources().getColor(android.R.color.holo_blue_dark);
            } else if (bmi < 25) {
                statusText = "Normal – Keep it up!";
                color = getResources().getColor(android.R.color.holo_green_dark);
            } else if (bmi < 30) {
                statusText = "Overweight – Time to exercise!";
                color = getResources().getColor(android.R.color.holo_orange_dark);
            } else {
                statusText = "Obese – High risk!";
                color = getResources().getColor(android.R.color.holo_red_dark);
            }

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("email", email);
            reportData.put("age", age);
            reportData.put("height", heightCm);
            reportData.put("weight", weightKg);
            reportData.put("bmi", bmi);
            reportData.put("bmiStatus", statusText);

            // ✅ Boolean values for diseases
            reportData.put("hasDiabetes", cbDiabetes.isChecked());
            reportData.put("hasCholesterol", cbCholesterol.isChecked());
            reportData.put("hasHeartDisease", cbHeart.isChecked());
            reportData.put("hasCancer", cbCancer.isChecked());

            reportData.put("date", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

            db.collection("Reports").document(email).set(reportData);
            db.collection("History").add(reportData);
            db.collection("newHistory").add(reportData);

            return true;

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}

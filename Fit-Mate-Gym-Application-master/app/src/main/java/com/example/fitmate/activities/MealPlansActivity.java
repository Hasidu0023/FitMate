package com.example.fitmate.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fitmate.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MealPlansActivity extends AppCompatActivity {

    private TextView tvUserName, tvBmiValue, tvConditions, tvMealPlan;
    private Button btnDownloadMealPlan;
    private FirebaseFirestore db;
    private String currentMealPlanText = "";

    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plans);

        tvUserName = findViewById(R.id.tvUserName);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvConditions = findViewById(R.id.tvConditions);
        tvMealPlan = findViewById(R.id.tvMealPlan);
        btnDownloadMealPlan = findViewById(R.id.btnDownloadMealPlan);

        db = FirebaseFirestore.getInstance();

        String email = getIntent().getStringExtra("USER_EMAIL");

        if (email != null && !email.isEmpty()) {
            fetchUserDetails(email);
            fetchUserReport(email);
        } else {
            Toast.makeText(this, "User email not received!", Toast.LENGTH_SHORT).show();
            tvUserName.setText("Name: Not Available");
            tvBmiValue.setText("BMI: Unknown");
            tvConditions.setText("Conditions: Unknown");
            tvMealPlan.setText("No meal plan available");
            btnDownloadMealPlan.setEnabled(false);
        }

        btnDownloadMealPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMealPlanText.isEmpty()) {
                    Toast.makeText(MealPlansActivity.this, "Meal plan is empty. Nothing to download.", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkPermissionAndSaveFile();
            }
        });
    }

    private void fetchUserDetails(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        String name = doc.getString("name");
                        tvUserName.setText("👤 Name: " + (name != null ? name : "Not Available"));
                    } else {
                        tvUserName.setText("👤 Name: Not Found");
                    }
                })
                .addOnFailureListener(e -> tvUserName.setText("👤 Name: Error"));
    }

    private void fetchUserReport(String email) {
        db.collection("Reports")
                .document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double bmi = doc.getDouble("bmi");
                        if (bmi != null) {
                            tvBmiValue.setText(String.format("📊 Your Latest BMI: %.2f", bmi));
                        } else {
                            tvBmiValue.setText("📊 BMI data not found.");
                        }

                        boolean hasCancer = Boolean.TRUE.equals(doc.getBoolean("hasCancer"));
                        boolean hasHeart = Boolean.TRUE.equals(doc.getBoolean("hasHeartDisease"));
                        boolean hasDiabetes = Boolean.TRUE.equals(doc.getBoolean("hasDiabetes"));
                        boolean hasChol = Boolean.TRUE.equals(doc.getBoolean("hasCholesterol"));

                        StringBuilder conditionList = new StringBuilder();
                        if (hasCancer || hasHeart || hasDiabetes || hasChol) {
                            conditionList.append("🩺 Health Conditions:\n");
                            if (hasCancer) conditionList.append("• 🎗️ Cancer\n");
                            if (hasHeart) conditionList.append("• 💓 Heart Disease\n");
                            if (hasDiabetes) conditionList.append("• 🩸 Diabetes\n");
                            if (hasChol) conditionList.append("• 🧬 Cholesterol\n");
                        } else {
                            conditionList.append("✅ No major health conditions.");
                        }
                        tvConditions.setText(conditionList.toString());

                        String mealPlan = generateMealPlan(bmi, hasCancer, hasHeart, hasDiabetes, hasChol);
                        tvMealPlan.setText(mealPlan);
                        currentMealPlanText = mealPlan;  // Save for download

                        saveMealPlanToFirestore(email, mealPlan);

                        btnDownloadMealPlan.setEnabled(true);

                    } else {
                        tvBmiValue.setText("📊 No report found.");
                        tvConditions.setText("🩺 No health conditions recorded.");
                        tvMealPlan.setText("🍽️ No meal plan available.");
                        btnDownloadMealPlan.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    tvBmiValue.setText("❌ Error loading BMI.");
                    tvConditions.setText("❌ Error loading conditions.");
                    tvMealPlan.setText("❌ Meal plan not available.");
                    btnDownloadMealPlan.setEnabled(false);
                });
    }

    private String generateMealPlan(Double bmi, boolean hasCancer, boolean hasHeart, boolean hasDiabetes, boolean hasChol) {
        StringBuilder plan = new StringBuilder("🍽️ Recommended Meal Plan:\n\n");

        if (bmi != null) {
            if (bmi < 18.5) {
                plan.append("🍞 Underweight Tips:\n");
                plan.append("• 🥛 Whole milk, 🥜 peanut butter, 🧀 cheese\n");
                plan.append("• 🍌 Snack often and include healthy fats\n\n");
            } else if (bmi < 25) {
                plan.append("🥗 Normal BMI:\n");
                plan.append("• 🍚 Rice, 🥦 veggies, 🍗 lean meats\n");
                plan.append("• 🍎 Include fruits and 💧 stay hydrated\n\n");
            } else if (bmi < 30) {
                plan.append("⚖️ Overweight Plan:\n");
                plan.append("• 🥬 Steamed veggies, 🐟 grilled fish\n");
                plan.append("• 🚫 Avoid sugary drinks, 🍽️ portion control\n\n");
            } else {
                plan.append("📉 Obesity Plan:\n");
                plan.append("• 🥗 Salads, 🍲 soups, 🥦 boiled vegetables\n");
                plan.append("• ❌ No fried food, junk food, or full-fat dairy\n\n");
            }
        }

        if (hasHeart) {
            plan.append("💓 Heart-Healthy Tips:\n");
            plan.append("• 🧂 Low sodium\n");
            plan.append("• 🚫 Avoid red meat and saturated fats\n");
            plan.append("• 🥜 Nuts, 🫒 olive oil, 🌾 whole grains\n\n");
        }

        if (hasDiabetes) {
            plan.append("🩸 Diabetes-Friendly Tips:\n");
            plan.append("• ❌ Low sugar\n");
            plan.append("• 🥬 Green leafy veggies, 🌾 whole grains\n");
            plan.append("• 🚫 No white rice or sugary drinks\n\n");
        }

        if (hasChol) {
            plan.append("🧬 Cholesterol Control:\n");
            plan.append("• 🌾 Oats, 🥣 legumes, fiber-rich foods\n");
            plan.append("• 🚫 No egg yolks, butter, fried food\n");
            plan.append("• 🐟 Prefer grilled proteins\n\n");
        }

        if (hasCancer) {
            plan.append("🎗️ Cancer Nutrition Tips:\n");
            plan.append("• 🍇 Antioxidant fruits and veggies\n");
            plan.append("• 🚫 No processed meats\n");
            plan.append("• 🌿 Turmeric, 🍵 green tea, 🧄 garlic\n\n");
        }

        return plan.toString().trim();
    }

    private void saveMealPlanToFirestore(String email, String mealPlan) {
        if (email == null || email.isEmpty()) return;

        Map<String, Object> mealData = new HashMap<>();
        mealData.put("email", email);
        mealData.put("mealPlan", mealPlan);
        mealData.put("updatedAt", System.currentTimeMillis());

        db.collection("Meals")
                .document(email)
                .set(mealData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(MealPlansActivity.this, "Meal plan saved successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(MealPlansActivity.this, "Failed to save meal plan.", Toast.LENGTH_SHORT).show());
    }

    private void checkPermissionAndSaveFile() {
        boolean hasPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            saveMealPlanToFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveMealPlanToFile();
            } else {
                Toast.makeText(this, "Permission denied. Cannot save file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveMealPlanToFile() {
        String fileName = "MealPlan_" + System.currentTimeMillis() + ".txt";
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (!downloadsFolder.exists()) {
            downloadsFolder.mkdirs();
        }

        File file = new File(downloadsFolder, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(currentMealPlanText.getBytes());
            Toast.makeText(this, "Meal plan saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save meal plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

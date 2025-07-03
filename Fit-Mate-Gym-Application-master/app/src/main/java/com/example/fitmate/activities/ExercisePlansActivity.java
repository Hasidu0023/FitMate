package com.example.fitmate.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fitmate.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExercisePlansActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    private TextView tvUserName, tvGender, tvBmiValue, tvConditions, tvExercisePlan;
    private MaterialButton btnDownloadPdf;
    private FirebaseFirestore db;

    private String currentExercisePlanText = "";
    private String currentUserName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_plans);

        tvUserName = findViewById(R.id.tvUserName);
        tvGender = findViewById(R.id.tvGender);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvConditions = findViewById(R.id.tvConditions);
        tvExercisePlan = findViewById(R.id.tvExercisePlan);
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);

        db = FirebaseFirestore.getInstance();

        String email = getIntent().getStringExtra("USER_EMAIL");

        if (email != null && !email.isEmpty()) {
            fetchUserDetails(email);
            fetchUserReport(email);
        } else {
            Toast.makeText(this, "User email not received!", Toast.LENGTH_SHORT).show();
            tvUserName.setText("👤 Name: Not Available");
            tvGender.setText("🚻 Gender: Unknown");
            tvBmiValue.setText("📊 BMI: Unknown");
            tvConditions.setText("🩺 Conditions: Unknown");
            tvExercisePlan.setText("🏋️ No exercise plan available");
            btnDownloadPdf.setEnabled(false);
        }

        btnDownloadPdf.setOnClickListener(v -> {
            if (currentExercisePlanText.isEmpty()) {
                Toast.makeText(this, "No exercise plan available to download", Toast.LENGTH_SHORT).show();
                return;
            }
            if (checkPermission()) {
                generateAndSavePdf(currentExercisePlanText);
            } else {
                requestPermission();
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
                        String gender = doc.getString("gender");

                        currentUserName = name != null ? name : "User";
                        tvUserName.setText("👤 Name: " + currentUserName);
                        tvGender.setText("🚻 Gender: " + (gender != null ? gender : "Not Available"));
                    } else {
                        tvUserName.setText("👤 Name: Not Found");
                        tvGender.setText("🚻 Gender: Not Found");
                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText("👤 Name: Error");
                    tvGender.setText("🚻 Gender: Error");
                });
    }

    private void fetchUserReport(String email) {
        db.collection("Reports")
                .document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double bmi = doc.getDouble("bmi");
                        boolean hasCancer = Boolean.TRUE.equals(doc.getBoolean("hasCancer"));
                        boolean hasHeart = Boolean.TRUE.equals(doc.getBoolean("hasHeartDisease"));
                        boolean hasDiabetes = Boolean.TRUE.equals(doc.getBoolean("hasDiabetes"));
                        boolean hasChol = Boolean.TRUE.equals(doc.getBoolean("hasCholesterol"));

                        tvBmiValue.setText(bmi != null ? String.format("📊 BMI: %.2f", bmi) : "📊 BMI not found");

                        StringBuilder conditions = new StringBuilder("🩺 Health Conditions:\n");
                        if (hasCancer) conditions.append("• 🎗️ Cancer\n");
                        if (hasHeart) conditions.append("• 💓 Heart Disease\n");
                        if (hasDiabetes) conditions.append("• 🩸 Diabetes\n");
                        if (hasChol) conditions.append("• 🧬 Cholesterol\n");

                        if (!hasCancer && !hasHeart && !hasDiabetes && !hasChol) {
                            conditions.append("• ✅ None");
                        }

                        tvConditions.setText(conditions.toString());

                        String exercisePlan = generateExercisePlan(bmi, hasCancer, hasHeart, hasDiabetes, hasChol);
                        tvExercisePlan.setText(exercisePlan);
                        currentExercisePlanText = exercisePlan;

                        // Save exercise plan to Firestore "Exercise" collection
                        saveExercisePlanToFirestore(email, exercisePlan);

                        btnDownloadPdf.setEnabled(true);

                    } else {
                        tvBmiValue.setText("📊 No report found");
                        tvConditions.setText("🩺 No conditions found");
                        tvExercisePlan.setText("🏋️ No plan available");
                        btnDownloadPdf.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    tvBmiValue.setText("❌ Error loading BMI");
                    tvConditions.setText("❌ Error loading conditions");
                    tvExercisePlan.setText("❌ Error generating plan");
                    btnDownloadPdf.setEnabled(false);
                });
    }

    private String generateExercisePlan(Double bmi, boolean hasCancer, boolean hasHeart, boolean hasDiabetes, boolean hasChol) {
        StringBuilder plan = new StringBuilder("🏋️ Personalized Gym Workout Plan:\n\n");

        if (bmi != null) {
            if (bmi < 18.5) {
                plan.append("⚖️ Underweight:\n")
                        .append("• 🚶‍♂️ Warm-up: Treadmill walk – 10 mins\n")
                        .append("• 🏋️‍♂️ Resistance Training (low weight, 3x12):\n   - Chest press\n   - Leg press\n   - Lat pulldown\n")
                        .append("• 💪 Core: Plank – 3 sets (30 sec)\n")
                        .append("• 🧘 Cooldown: Light stretching\n\n");
            } else if (bmi < 25) {
                plan.append("✅ Healthy BMI:\n")
                        .append("• 🏃 Warm-up: Treadmill jog – 5 mins\n")
                        .append("• 🔁 Strength Circuit (3x10 reps):\n   - Bench Press\n   - Deadlifts\n   - Squats\n   - Overhead Press\n")
                        .append("• 🚣 Cardio: Rowing – 15 mins\n")
                        .append("• 💪 Core: Bicycle crunch – 3 sets\n\n");
            } else if (bmi < 30) {
                plan.append("📉 Overweight:\n")
                        .append("• 🚴 Warm-up: Elliptical – 10 mins\n")
                        .append("• 🏋️ Resistance (3x10):\n   - Goblet Squats\n   - Dumbbell Rows\n   - Chest Machine\n")
                        .append("• 🚲 Cardio: Stationary bike – 20 mins\n")
                        .append("• 🧘 Stretch: Hamstrings, back, chest\n\n");
            } else {
                plan.append("🚨 Obesity:\n")
                        .append("• 🚴 Warm-up: Recumbent bike – 10 mins\n")
                        .append("• 💪 Light Resistance (high reps):\n   - Seated Leg Press\n   - Shoulder Press\n   - Lat Pulldown\n")
                        .append("• 🏃 Cardio: Incline treadmill walk – 15 mins\n")
                        .append("• 🪑 Cooldown: Chair yoga + breathing\n\n");
            }
        } else {
            plan.append("• General Plan:\n")
                    .append("   - 🏃 Light cardio: 10–15 mins\n")
                    .append("   - 🏋️ Machine weights: 2 sets\n")
                    .append("   - 🧘 Stretch + Foam rolling\n\n");
        }

        if (hasHeart) {
            plan.append("💓 *Heart Tip*: Avoid max lifts. Monitor heart rate during sets.\n");
        }
        if (hasDiabetes) {
            plan.append("🩸 *Diabetes Tip*: Prefer steady cardio. Meal timing is crucial.\n");
        }
        if (hasChol) {
            plan.append("🧬 *Cholesterol Tip*: Focus on aerobic exercises. Reduce rest intervals.\n");
        }
        if (hasCancer) {
            plan.append("🎗️ *Cancer Recovery*: Gentle exercises only. Prefer bands & bodyweight.\n");
        }

        return plan.toString().trim();
    }

    private void saveExercisePlanToFirestore(String email, String exercisePlan) {
        if (email == null || email.isEmpty()) return;

        Map<String, Object> exerciseData = new HashMap<>();
        exerciseData.put("email", email);
        exerciseData.put("exercisePlan", exercisePlan);
        exerciseData.put("updatedAt", System.currentTimeMillis());

        db.collection("Exercise")
                .document(email)
                .set(exerciseData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ExercisePlansActivity.this, "Exercise plan saved successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(ExercisePlansActivity.this, "Failed to save exercise plan.", Toast.LENGTH_SHORT).show());
    }

    // PDF generation methods

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission automatically granted on older versions
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndSavePdf(currentExercisePlanText);
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot save PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateAndSavePdf(String textContent) {
        PdfDocument pdfDocument = new PdfDocument();

        // Page info and size
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size in points

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(android.graphics.Color.BLACK);
        paint.setTextSize(14f);

        int x = 40, y = 50;
        int lineHeight = 22;

        // Draw Title
        paint.setTextSize(20f);
        paint.setFakeBoldText(true);
        canvas.drawText("Exercise Plan for " + currentUserName, x, y, paint);

        paint.setTextSize(14f);
        paint.setFakeBoldText(false);

        y += 40;

        // Split text into lines (handling multi-line text)
        String[] lines = textContent.split("\n");
        for (String line : lines) {
            // If line is too long, wrap (basic)
            if (line.length() > 90) {
                int start = 0;
                while (start < line.length()) {
                    int end = Math.min(start + 90, line.length());
                    String part = line.substring(start, end);
                    canvas.drawText(part, x, y, paint);
                    y += lineHeight;
                    start += 90;
                }
            } else {
                canvas.drawText(line, x, y, paint);
                y += lineHeight;
            }
        }

        pdfDocument.finishPage(page);

        // Save to external storage directory
        String fileName = "ExercisePlan_" + System.currentTimeMillis() + ".pdf";
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(this, "PDF saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }
}

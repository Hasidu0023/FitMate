package com.example.fitmate.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtDob, edtPassword, edtConfirmPassword;
    private Spinner spinnerGender;
    private Button btnRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtDob = findViewById(R.id.edtDob);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Select Gender", "Male", "Female", "Other")
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Date picker for DOB
        edtDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) ->
                            edtDob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Register button click
        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(name)
                || TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                || TextUtils.isEmpty(dob)
                || gender.equals("Select Gender")
                || TextUtils.isEmpty(password) || password.length() < 6
                || !password.equals(confirmPassword)) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_LONG).show();
            return;
        }

        // 🔍 Check if user with same email already exists
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "An account with this email already exists.", Toast.LENGTH_LONG).show();
                    } else {
                        // Proceed to register
                        registerNewUser(name, email, dob, gender, password);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void registerNewUser(String name, String email, String dob, String gender, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("dob", dob);
        user.put("gender", gender);
        user.put("password", password);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RegisterActivity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Optionally move to another screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

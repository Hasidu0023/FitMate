package com.example.fitmate.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginUser extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView btnRegister;
    private ProgressDialog progressDialog;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginUser.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            edtEmail.setError("Enter valid email");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password required");
            edtPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        progressDialog.show();

        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    btnLogin.setEnabled(true);
                    progressDialog.dismiss();

                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Start RegisterUserActivity and pass email
                        Intent intent = new Intent(LoginUser.this, RegisterUserActivity.class);
                        intent.putExtra("USER_EMAIL", email);
                        startActivity(intent);

                        finish();
                    } else {
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    progressDialog.dismiss();
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

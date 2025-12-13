package com.example.memoryaidapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText inputEmail, inputPassword;
    Button loginBtn;
    TextView createAccount;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ImmersiveUtil.enableImmersiveMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginBtn = findViewById(R.id.loginBtn);
        createAccount = findViewById(R.id.createAccount);

        loginBtn.setOnClickListener(v -> loginUser());

        createAccount.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, GetToKnow.class));
            finish();
        });
    }

    private void loginUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
            return;
        }

        loginBtn.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    // Save login state
                    SharedPreferences prefs = getSharedPreferences("AppCache", MODE_PRIVATE);
                    prefs.edit().putBoolean("isLoggedIn", true).apply();

                    // GO TO HOME PAGE
                    startActivity(new Intent(Login.this, Home.class));
                    finish();
                })

                .addOnFailureListener(e -> {
                    loginBtn.setEnabled(true);
                    Toast.makeText(Login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

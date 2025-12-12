package com.example.memoryaidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GetToKnow extends AppCompatActivity {

    private static final String TAG = "GetToKnow";
    private TextView alreadyHaveAccount;
    private EditText inputEmail, inputUsername, inputBirthday, inputPassword;
    private RadioGroup conditionGroup;
    private Button continueBtn, backBtn;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    // Simple progress indicator
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_to_know);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // INPUT FIELDS
        inputEmail = findViewById(R.id.inputEmail);
        inputUsername = findViewById(R.id.inputUsername);
        inputBirthday = findViewById(R.id.inputBirthday);
        inputPassword = findViewById(R.id.inputPassword);
        conditionGroup = findViewById(R.id.conditionGroup);

        continueBtn = findViewById(R.id.continueBtn);
        backBtn = findViewById(R.id.backBtn);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);

        // Make text black so user can see it (API >= 23). If you target older APIs use getResources().getColor(...)
        try {
            inputEmail.setTextColor(getColor(android.R.color.black));
            inputUsername.setTextColor(getColor(android.R.color.black));
            inputBirthday.setTextColor(getColor(android.R.color.black));
            inputPassword.setTextColor(getColor(android.R.color.black));
        } catch (Exception e) {
            Log.w(TAG, "getColor() may not be supported on this API level", e);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating account...");

        // ---------------------------------
        // BIRTHDAY → OPEN DATE PICKER
        // ---------------------------------
        alreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(GetToKnow.this, Login.class);
            startActivity(intent);
            finish();
        });

        inputBirthday.setFocusable(false);
        inputBirthday.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog picker = new DatePickerDialog(
                    GetToKnow.this,
                    (DatePicker view, int y, int m, int d) -> {
                        String date = String.format("%02d/%02d/%04d", d, (m + 1), y);
                        inputBirthday.setText(date);
                    },
                    year, month, day
            );
            picker.show();
        });

        // ---------------------------------
        // CONTINUE BUTTON → CREATE ACCOUNT
        // ---------------------------------
        continueBtn.setOnClickListener(v -> createAccountAndProceed());

        // BACK BUTTON
        backBtn.setOnClickListener(v -> finish());
    }

    /**
     * Creates Firebase account first. If successful, saves user info to Firestore
     * and navigates to Home. If Firestore save fails, navigation still occurs
     * (so user isn't stuck); failure is logged and shown as a toast.
     */
    private void createAccountAndProceed() {
        String email = inputEmail.getText().toString().trim();
        String username = inputUsername.getText().toString().trim();
        String birthday = inputBirthday.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        int selectedId = conditionGroup.getCheckedRadioButtonId();
        RadioButton selectedCondition = selectedId != -1 ? findViewById(selectedId) : null;
        String condition = selectedCondition != null ? selectedCondition.getText().toString() : "None";

        if (email.isEmpty() || username.isEmpty() || birthday.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // disable button & show progress to prevent multiple taps
        continueBtn.setEnabled(false);
        progressDialog.show();

        // Create Firebase account
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Account created successfully
                        FirebaseUser firebaseUser = null;
                        try {
                            if (task.getResult() != null) {
                                firebaseUser = task.getResult().getUser();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Could not get user from task result", e);
                        }
                        if (firebaseUser == null) {
                            firebaseUser = auth.getCurrentUser(); // fallback
                        }

                        if (firebaseUser == null) {
                            // Unexpected: user still null after successful task
                            Log.e(TAG, "User is null after successful account creation!");
                            Toast.makeText(GetToKnow.this, "Account created but user is null. Check logs.", Toast.LENGTH_LONG).show();
                            // Still attempt navigation so user isn't stuck
                            navigateToHome();
                            return;
                        }

                        final String uid = firebaseUser.getUid();
                        Log.d(TAG, "Account created, uid=" + uid);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("username", username);
                        userData.put("birthday", birthday);
                        userData.put("condition", condition);

                        // Save additional user info to Firestore
                        firestore.collection("users").document(uid)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User profile saved to Firestore for uid=" + uid);
                                    // navigate after successful Firestore save
                                    navigateToHome();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Saving to Firestore failed: " + e.getMessage(), e);
                                    Toast.makeText(GetToKnow.this, "Saving profile failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    // Even if saving fails, navigate so user can continue (optionally retry later)
                                    navigateToHome();
                                });
                    } else {
                        // Account creation failed
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Account creation failed: " + error);
                        Toast.makeText(GetToKnow.this, "Account creation failed: " + error, Toast.LENGTH_LONG).show();
                        // re-enable button and hide progress
                        continueBtn.setEnabled(true);
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    // This should be redundant because addOnCompleteListener handles failures, but keep for extra logging
                    Log.e(TAG, "createUserWithEmailAndPassword failed with exception: " + e.getMessage(), e);
                });
    }

    /**
     * Navigate to Home activity and finish this one.
     * If starting the activity fails, we log and show a toast.
     */
    private void navigateToHome() {
        runOnUiThread(() -> {
            try {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            } catch (Exception ignored) {}

            try {
                Intent intent = new Intent(GetToKnow.this, Home.class);
                // If you want to clear backstack:
                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception ex) {
                Log.e(TAG, "Failed to start Home activity: " + ex.getMessage(), ex);
                Toast.makeText(GetToKnow.this, "Navigation error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                continueBtn.setEnabled(true);
            }
        });
    }
}

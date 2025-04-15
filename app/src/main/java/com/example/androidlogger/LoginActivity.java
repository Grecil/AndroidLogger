package com.example.androidlogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    private static final String CORRECT_PIN = "123456";
    

    private EditText pinEditText;
    private Button loginButton;
    private Button biometricLoginButton;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pinEditText = findViewById(R.id.pinEditText);
        loginButton = findViewById(R.id.loginButton);
        biometricLoginButton = findViewById(R.id.biometricLoginButton);

        setupBiometricAuthentication();

        loginButton.setOnClickListener(v -> verifyPin());

        biometricLoginButton.setOnClickListener(v -> {
            if (biometricPrompt != null && promptInfo != null) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
    }

    private void verifyPin() {
        String enteredPin = pinEditText.getText().toString();
        if (CORRECT_PIN.equals(enteredPin)) {
            
            Log.d(TAG, "PIN Login successful");
            navigateToDashboard();
        } else {
            
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            pinEditText.setError("Incorrect PIN");
        }
    }

    private void setupBiometricAuthentication() {
        executor = ContextCompat.getMainExecutor(this);
        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");
                biometricLoginButton.setVisibility(View.VISIBLE);
                initializeBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No biometric features available on this device.");
                biometricLoginButton.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Biometric features are currently unavailable.");
                biometricLoginButton.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.w(TAG, "The user hasn't associated any biometric credentials with their account.");
                
                biometricLoginButton.setVisibility(View.GONE); 
                break;
             default:
                Log.e(TAG, "Biometric status unknown or error.");
                biometricLoginButton.setVisibility(View.GONE);
                break;
        }
    }

    private void initializeBiometricPrompt() {
        biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                 Log.e(TAG, "Biometric authentication error: (" + errorCode + ") " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Biometric authentication successful");
                navigateToDashboard();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
                Log.w(TAG, "Biometric authentication failed");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using your fingerprint or face")
                
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                
                
                .build();
    }

    private void navigateToDashboard() {
        
        pinEditText.setText(""); 
        
        Intent intent = new Intent(LoginActivity.this, MainActivity.class); 
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); 
        startActivity(intent);
        finish(); 
    }
} 
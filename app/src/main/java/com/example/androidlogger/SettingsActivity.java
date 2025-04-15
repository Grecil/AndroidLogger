package com.example.androidlogger;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    
    public static final String PREFS_NAME = "AppLoggerSettings";
    public static final String KEY_TRACKED_APPS = "trackedAppsSet"; 
    public static final String KEY_USAGE_THRESHOLD_MINUTES = "usageThresholdMinutes";

    private SharedPreferences sharedPreferences;
    private Button buttonSelectApps;
    private EditText editTextUsageThreshold;
    private Button buttonSaveThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        buttonSelectApps = findViewById(R.id.buttonSelectApps);
        editTextUsageThreshold = findViewById(R.id.editTextUsageThreshold);
        buttonSaveThreshold = findViewById(R.id.buttonSaveThreshold);

        loadPreferences();
        setupListeners();
    }

    private void loadPreferences() {
        
        int threshold = sharedPreferences.getInt(KEY_USAGE_THRESHOLD_MINUTES, -1);
        if (threshold > 0) {
            editTextUsageThreshold.setText(String.valueOf(threshold));
        } else {
            editTextUsageThreshold.setText(""); 
        }
        
    }

    private void setupListeners() {
        buttonSelectApps.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AppSelectActivity.class);
            startActivity(intent);
        });

        
        buttonSaveThreshold.setOnClickListener(v -> {
            String thresholdStr = editTextUsageThreshold.getText().toString();
            if (TextUtils.isEmpty(thresholdStr)) {
                
                saveIntPreference(KEY_USAGE_THRESHOLD_MINUTES, -1); 
                Toast.makeText(this, "Usage threshold cleared", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int threshold = Integer.parseInt(thresholdStr);
                    if (threshold > 0) {
                        saveIntPreference(KEY_USAGE_THRESHOLD_MINUTES, threshold);
                        Toast.makeText(this, "Usage threshold saved: " + threshold + " minutes", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter a positive number for the threshold", Toast.LENGTH_SHORT).show();
                        editTextUsageThreshold.setError("Must be positive");
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                    editTextUsageThreshold.setError("Invalid number");
                }
            }
        });
    }

    private void saveIntPreference(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    

    public static void saveTrackedApps(Context context, Set<String> trackedApps) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_TRACKED_APPS, trackedApps);
        editor.apply();
    }

    
    public static Set<String> getTrackedApps(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        return new HashSet<>(prefs.getStringSet(KEY_TRACKED_APPS, new HashSet<>())); 
    }

    public static int getUsageThresholdMinutes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USAGE_THRESHOLD_MINUTES, -1); 
    }
} 
package com.example.androidlogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageStats;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;
    private static final int REQUEST_CODE_USAGE_STATS = 102;

    
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        executorService = Executors.newSingleThreadExecutor();

        Button buttonViewDashboard = findViewById(R.id.buttonViewDashboard);
        Button buttonOpenSettings = findViewById(R.id.buttonOpenSettings);
        Button buttonGenerateDemoData = findViewById(R.id.buttonGenerateDemoData);

        
        buttonGenerateDemoData.setVisibility(View.VISIBLE);

        buttonViewDashboard.setOnClickListener(v -> {
            
            if (hasUsageStatsPermission()) {
                 startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            } else {
                requestUsageStatsPermission();
            }
        });

        buttonOpenSettings.setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, SettingsActivity.class))
        );

        buttonGenerateDemoData.setOnClickListener(v -> {
            generateDemoData();
        });

        
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        
        
        if (!hasUsageStatsPermission()) {
             
             Toast.makeText(this, "Usage Stats permission needed for logging.", Toast.LENGTH_LONG).show();
             
        }

        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
            
        }
    }

    private boolean hasUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.app.usage.UsageStatsManager usageStatsManager = 
                (android.app.usage.UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager == null) return false;
            
            long time = System.currentTimeMillis();
            List<android.app.usage.UsageStats> stats = usageStatsManager.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
            return (stats != null && !stats.isEmpty());
        } 
        return true; 
    }

    private void requestUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(this, "Please grant Usage Access permission for AndroidLogger.", Toast.LENGTH_LONG).show();
            try {
                 startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_CODE_USAGE_STATS);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open Usage Access settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Threshold alerts will not be shown.", Toast.LENGTH_LONG).show();
                
            }
        }
    }

     @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_USAGE_STATS) {
             
             if (hasUsageStatsPermission()) {
                 Toast.makeText(this, "Usage Stats permission granted.", Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(this, "Usage Stats permission is still needed for full functionality.", Toast.LENGTH_LONG).show();
             }
        }
    }
    
    

    private void generateDemoData() {
        Toast.makeText(this, "Generating demo data in background...", Toast.LENGTH_SHORT).show();
        executorService.submit(() -> {
            EncryptedDatabaseHelper dbHelper = new EncryptedDatabaseHelper(MainActivity.this);
            Random random = new Random();
            Set<String> trackedApps = SettingsActivity.getTrackedApps(MainActivity.this);
            
            if (trackedApps.isEmpty()) {
                trackedApps.add("com.android.chrome"); 
                trackedApps.add("com.google.android.youtube"); 
                trackedApps.add("com.whatsapp"); 
            }

            try {
                Calendar calendar = Calendar.getInstance();
                for (int i = 0; i < 7; i++) { 
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.add(Calendar.DAY_OF_YEAR, -i);
                    long dayStartMillis = getStartOfDayMillis(calendar);
                    long dayEndMillis = getEndOfDayMillis(calendar);

                    
                    int unlocks = 5 + random.nextInt(50); 
                    for (int u = 0; u < unlocks; u++) {
                        
                        long randomTimestamp = dayStartMillis + random.nextInt((int)(dayEndMillis - dayStartMillis));
                        dbHelper.insertLog(randomTimestamp, "SCREEN_UNLOCK", "Demo Unlock");
                    }

                    
                    for (String packageName : trackedApps) {
                        
                        long usageMillis = TimeUnit.MINUTES.toMillis(5 + random.nextInt(115));
                        String data = packageName + "," + usageMillis;
                         
                         dbHelper.insertLog(dayEndMillis - 1000, "APP_USAGE", data);
                    }

                    Log.d("DemoData", "Generated data for day offset: " + i);
                }

                
                runOnUiThread(() -> 
                   Toast.makeText(MainActivity.this, "Demo data generated!", Toast.LENGTH_LONG).show()
                );

            } catch (Exception e) {
                 Log.e("DemoData", "Error generating demo data", e);
                 runOnUiThread(() -> 
                     Toast.makeText(MainActivity.this, "Error generating demo data.", Toast.LENGTH_SHORT).show()
                 );
            } finally {
                 
            }
        });
    }

    private long getStartOfDayMillis(Calendar calendar) {
        Calendar start = (Calendar) calendar.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start.getTimeInMillis();
    }

    private long getEndOfDayMillis(Calendar calendar) {
        Calendar end = (Calendar) calendar.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end.getTimeInMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    
    
    
    
    
    
    
} 
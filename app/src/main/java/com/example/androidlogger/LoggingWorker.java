package com.example.androidlogger;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.List;
import android.app.usage.UsageStats;
import java.util.concurrent.TimeUnit;
import java.util.Set;

public class LoggingWorker extends Worker {

    private static final String TAG = "LoggingWorker";
    private final Context applicationContext;

    public LoggingWorker(
        @NonNull Context context,
        @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.applicationContext = context.getApplicationContext();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1) // Needed for UsageLogger constructor
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "LoggingWorker starting work.");

        try {
            // Fetch Usage Stats
            UsageLogger usageLogger = new UsageLogger(applicationContext);
            List<UsageStats> stats = usageLogger.getUsageStatsLast24Hours();
            Log.d(TAG, "Fetched " + (stats != null ? stats.size() : 0) + " usage stats entries.");

            // --- Process Usage Stats and Check Threshold --- 
            if (stats != null) {
                processUsageStats(stats);
            }
            
            // TODO: Persist screen unlock events from Receiver and process them here
            // Log.d(TAG, "Placeholder for processing screen unlock events periodically.");

            Log.d(TAG, "LoggingWorker finished work successfully.");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error during worker execution", e);
            return Result.failure();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) // Needed for UsageStats
    private void processUsageStats(List<UsageStats> stats) {
        // Get the set of package names to track
        Set<String> trackedApps = SettingsActivity.getTrackedApps(applicationContext);
        EncryptedDatabaseHelper dbHelper = new EncryptedDatabaseHelper(applicationContext);

        // If no apps are specifically selected, maybe track nothing or everything?
        // Current logic: Track only selected apps. If set is empty, total time remains 0.
        if (trackedApps.isEmpty()) {
            Log.d(TAG, "No apps selected for tracking in settings.");
            // Optionally, you could revert to tracking all usage here if desired.
        }

        long totalForegroundTimeMillis = 0;
        long currentTime = System.currentTimeMillis();

        for (UsageStats usageStats : stats) {
            String packageName = usageStats.getPackageName();
            // Check if this app's package name is in the tracked set
            if (trackedApps.contains(packageName)) {
                long timeInForeground = usageStats.getTotalTimeInForeground();
                if (timeInForeground > 0) {
                    totalForegroundTimeMillis += timeInForeground;
                    // Store individual app usage stat
                    // Using a simple format: "package_name,duration_ms"
                    String data = packageName + "," + timeInForeground;
                    // Use the end time of the stat interval, approximating current time for simplicity here
                    long timestamp = usageStats.getLastTimeUsed(); 
                    dbHelper.insertLog(timestamp, "APP_USAGE", data);
                }
            }
        }
        // dbHelper.close() is handled within insertLog calls
        
        long totalForegroundTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(totalForegroundTimeMillis);

        Log.d(TAG, "Total foreground usage for tracked apps in last 24h: " + totalForegroundTimeMinutes + " minutes.");

        // Get threshold from SharedPreferences
        int thresholdMinutes = SettingsActivity.getUsageThresholdMinutes(applicationContext);

        // Check if threshold is set and exceeded
        if (thresholdMinutes > 0 && totalForegroundTimeMinutes > thresholdMinutes) {
            Log.i(TAG, "Tracked app usage threshold exceeded! (" + totalForegroundTimeMinutes + "/" + thresholdMinutes + " min). Sending notification.");
            String notificationMessage = "Tracked apps used for " + totalForegroundTimeMinutes +
                                         " minutes today (limit: " + thresholdMinutes + " min).";
            // Send notification using the helper
            NotificationHelper.sendThresholdExceededNotification(applicationContext, notificationMessage);
        }
    }
} 
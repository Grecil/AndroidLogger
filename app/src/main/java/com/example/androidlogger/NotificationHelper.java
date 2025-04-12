package com.example.androidlogger;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final int THRESHOLD_NOTIFICATION_ID = 1001; // Unique ID for this type of notification

    /**
     * Sends a notification indicating a usage threshold has been exceeded.
     *
     * @param context The application context.
     * @param message The main message body for the notification.
     */
    public static void sendThresholdExceededNotification(Context context, String message) {
        // Intent to open when notification is tapped (e.g., open Dashboard or Settings)
        Intent intent = new Intent(context, DashboardActivity.class); // Change to desired Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Use FLAG_IMMUTABLE or FLAG_MUTABLE based on needs. IMMUTABLE is safer.
        int pendingIntentFlags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 
                                 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE 
                                 : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MyApplication.CHANNEL_ID_THRESHOLD)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a proper notification icon!
                .setContentTitle("Usage Limit Reached")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Set the intent that fires when tapped
                .setAutoCancel(true); // Automatically removes the notification when tapped

        // Get an instance of NotificationManagerCompat
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check for permission (Required for API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (notificationManager.areNotificationsEnabled()) {
                 // notificationId is a unique int for each notification that you must define
                 notificationManager.notify(THRESHOLD_NOTIFICATION_ID, builder.build());
            } else {
                // TODO: Handle the case where notifications are disabled. 
                // Maybe guide the user to settings?
                // Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                //        .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                // context.startActivity(settingsIntent);
            }
        } else {
             // For older versions, permission is implicitly granted
             notificationManager.notify(THRESHOLD_NOTIFICATION_ID, builder.build());
        }
    }

     // TODO: Create a small, monochrome notification icon (e.g., ic_notification.xml) in res/drawable
     // The default ic_launcher_foreground might not render correctly.
} 
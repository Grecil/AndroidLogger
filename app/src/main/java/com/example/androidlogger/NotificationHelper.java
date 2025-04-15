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

    private static final int THRESHOLD_NOTIFICATION_ID = 1001; 

    /**
     * Sends a notification indicating a usage threshold has been exceeded.
     *
     * @param context The application context.
     * @param message The main message body for the notification.
     */
    public static void sendThresholdExceededNotification(Context context, String message) {
        
        Intent intent = new Intent(context, DashboardActivity.class); 
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        int pendingIntentFlags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 
                                 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE 
                                 : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags);

        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MyApplication.CHANNEL_ID_THRESHOLD)
                .setSmallIcon(R.drawable.ic_launcher_foreground) 
                .setContentTitle("Usage Limit Reached")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) 
                .setAutoCancel(true); 

        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (notificationManager.areNotificationsEnabled()) {
                 
                 notificationManager.notify(THRESHOLD_NOTIFICATION_ID, builder.build());
            } else {
                
                
                
                
                
            }
        } else {
             
             notificationManager.notify(THRESHOLD_NOTIFICATION_ID, builder.build());
        }
    }

     
     
} 
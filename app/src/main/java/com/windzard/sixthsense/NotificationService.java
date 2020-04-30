package com.windzard.sixthsense;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class NotificationService extends Service {

    private String notificationId = "SixthSense";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private android.app.Notification getNotification() {
        android.app.Notification.Builder builder = new android.app.Notification.Builder(this)
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("Sixth Sense")
                .setContentText("Running Background");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        return builder.build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String notificationName = "SixthSense";
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1,getNotification());
    }
}

package com.windzard.sixthsense;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent request = new Intent();
                request.setAction(".MainActivity");
                sendBroadcast(request);
            }
        };
        timer.schedule(timerTask, 0, 500);

        return START_STICKY;
    }
}

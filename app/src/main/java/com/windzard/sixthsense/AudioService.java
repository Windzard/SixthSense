package com.windzard.sixthsense;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class AudioService extends Service {

    //Generate variables of audio.
    public float freq;
    public float NS;
    public float WE;
    public float W;
    public float E;
    public int durationMs = 500;
    public int count = (int) (44100.0 * 2.0 * (durationMs / 1000.0)) & ~1;
    public short[] data = new short[count];
    public AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC,
            44100,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            count * (Short.SIZE / 8),
            AudioTrack.MODE_STREAM);

    private float accel;
    private float orientation;
    private boolean soundFlag;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audio.play();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        accel = intent.getFloatExtra("accel", accel);
        orientation = intent.getFloatExtra("orientation", orientation);
        soundFlag = intent.getBooleanExtra("soundFlag", soundFlag);

        if (soundFlag) {
            audioGen(accel, orientation);
        }

        return START_STICKY;
    }

    public void audioGen(float accel, float orientation) {
        freq = 200 + (accel / 20) * 800;
        NS = Math.abs(180 - orientation) / 180;
        WE = (int) (orientation / 180);
        float temp = 1 - Math.abs(orientation % 180 - 90) / 90;
        W = 0.5f + 0.5f * (float) Math.pow(-1, WE) * temp;
        E = 0.5f + 0.5f * (float) Math.pow(-1, WE + 1) * temp;

        for (int i = 0; i < count; i += 2) {
            short sample = (short) (Math.sin(2 * Math.PI * i / (44100.0 / freq)) * 0x7FFF);
            data[i] = sample;
            data[i + 1] = sample;
        }
        audio.setStereoVolume(NS * W, NS * E);
        audio.write(data, 0, count);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audio.stop();
    }
}

package com.windzard.sixthsense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private TextView text5;

    //Generate variables of sensors.
    private SensorManager manager;
    private Sensor acceleration;
    private Sensor accelerometer;
    private Sensor magnetic;

    //Generate variables of the readings.
    private float[] accelerationValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private float[] magneticValues = new float[3];
    public int eventType;
    public float accel;
    public float orientation;
    private static final float value_drift = 0.1f;

    //Generate variables used when calculating orientation.
    private float[] rotationMatrix = new float[9];
    private float[] orientationValues = new float[3];

    public boolean soundFlag = true;
    public boolean sensorFlag = true;

    private static final int EXIT_TIME = 2000;
    private long firstExitTime = 0L;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            Intent intent = new Intent(MainActivity.this, NotificationService.class);
            startForegroundService(intent);
        }else{
            startService(new Intent(MainActivity.this, NotificationService.class));
        }

        startService(new Intent(MainActivity.this, AudioService.class));

        startService(new Intent(MainActivity.this, TimerService.class));

        initReceiver();
        initSensorService();
        findViews();
    }
    
    private void initReceiver() {
        MyReceiver receiver = new MyReceiver();
        IntentFilter inf = new IntentFilter(".MainActivity");
        registerReceiver(receiver,inf);
    }

    private void findViews() {
        //Generate variables of visual elements.
        Button sensor = findViewById(R.id.sensor);
        Button sound = findViewById(R.id.sound);
        text1 = findViewById(R.id.textView1);
        text2 = findViewById(R.id.textView2);
        text3 = findViewById(R.id.textView3);
        text4 = findViewById(R.id.textView4);
        text5 = findViewById(R.id.textView5);
        sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sensorFlag) {
                    sensorFlag = false;
                    onPause();
                } else if (!sensorFlag) {
                    sensorFlag = true;
                    onResume();
                }
            }
        });
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundFlag) {
                    soundFlag = false;
                } else if (!soundFlag) {
                    soundFlag = true;
                }
            }
        });
    }

    private void initSensorService() {
        //Find the sensors.
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert manager != null;
        acceleration = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensorService();
    }

    private void registerSensorService() {
        manager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregister();
    }

    private void unregister() {
        if (manager != null) {
            manager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        eventType = event.sensor.getType();
        switch (eventType) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values;
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                accelerationValues = event.values;
                if (accelerationValues[0] < value_drift) {
                    accelerationValues[0] = 0;
                }
                if (accelerationValues[1] < value_drift) {
                    accelerationValues[1] = 0;
                }
                if (accelerationValues[2] < value_drift) {
                    accelerationValues[2] = 0;
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values;
                break;
            default:
                return;
        }

        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticValues);
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
        }
        orientation = ((float) Math.toDegrees(orientationValues[0]) + 360) % 360;
        accel = (float) Math.sqrt(accelerationValues[0] * accelerationValues[0] + accelerationValues[1] * accelerationValues[1]);
        text1.setText(getString(R.string.Orientation, orientation));
        text2.setText(getString(R.string.Accel_X, accelerationValues[0]));
        text3.setText(getString(R.string.Accel_Y, accelerationValues[1]));
        text4.setText(getString(R.string.Accel_Z, accelerationValues[2]));
        text5.setText(getString(R.string.Accel, accel));

//        Intent audioIntent = new Intent(MainActivity.this, AudioService.class);
//        audioIntent.putExtra("accel", accel);
//        audioIntent.putExtra("orientation", orientation);
//        audioIntent.putExtra("soundFlag", soundFlag);
//        startService(audioIntent);

//        if (soundFlag) {
//            audioPlay(accel, orientation);
//            audio.play();
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent audioIntent = new Intent(MainActivity.this, AudioService.class);
            audioIntent.putExtra("accel", accel);
            audioIntent.putExtra("orientation", orientation);
            audioIntent.putExtra("soundFlag", soundFlag);
            startService(audioIntent);
        }
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        if (curTime - firstExitTime < EXIT_TIME) {
            stopService(new Intent(MainActivity.this, NotificationService.class));
            stopService(new Intent(MainActivity.this, AudioService.class));
            stopService(new Intent(MainActivity.this, TimerService.class));
            finish();
        } else {
            Toast.makeText(this, R.string.exit_toast, Toast.LENGTH_SHORT).show();
            firstExitTime = curTime;
        }
    }
}



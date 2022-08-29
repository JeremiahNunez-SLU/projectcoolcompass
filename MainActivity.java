package com.jnunez.coolcompassapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView compassView;
    private TextView directionView;
    private TextView textViewStepsTaken;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
    private Sensor myStepCounter;
    private boolean isCounterSensorPresent;
    int stepCount = 0;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)== PackageManager.PERMISSION_DENIED) {//ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        compassView = findViewById(R.id.compassView);
        directionView = findViewById(R.id.heading1);
        textViewStepsTaken = findViewById(R.id.textViewStepsTaken);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            myStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isCounterSensorPresent = true;
        } else {
            textViewStepsTaken.setText("Sensor not found!");
            isCounterSensorPresent = false;
        }
        sensorManager.registerListener(sensorEventListenerAccelerometer,sensorAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField,sensorMagneticField,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == myStepCounter){
            stepCount = (int) sensorEvent.values[0];
            textViewStepsTaken.setText(String.valueOf(stepCount));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null)
            sensorManager.registerListener(this, myStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null)
            sensorManager.unregisterListener(this, myStepCounter);
    }

    SensorEventListener sensorEventListenerAccelerometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            floatGravity = event.values;

            SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

            float azimut = floatOrientation[0];
            int compassDirection = (int)(-azimut * 360 / (2 * 3.14159f));
            compassView.setRotation(compassDirection);
            directionView.setText(changeDirection(compassDirection));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            floatGeoMagnetic = event.values;

            SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
            compassView.setRotation((float)(-floatOrientation[0]*180/3.14159));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public void ResetButton(View view){
        textViewStepsTaken.setText(0);
    }

    public String changeDirection(int direction) {
        if (direction >= 0 && direction <= 90){
            if (direction == 0){
                return "N";
            }
            if (direction == 90){
                return "W";
            }
            if (direction > 0){
                return "NW";
            }
        }
        if (direction > 90 && direction <= 180){
            if(direction < 180){
                return "SW";
            }
            if(direction == 180){
                return "S";
            }
        }
        if (direction <= -90){
            if (direction == -90){
                return "E";
            }
            if(direction == -180){
                return "S";
            }
            else{
                return "SE";
            }
        }
        if(direction > -90 && direction < 0){
            return "NE";
        }
        return "ERROR";
    }
}
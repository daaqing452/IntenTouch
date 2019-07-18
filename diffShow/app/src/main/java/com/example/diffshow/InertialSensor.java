package com.example.diffshow;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class InertialSensor implements SensorEventListener  {
    MainActivity father;
    SensorManager sensorManager;
    Sensor sensorLinearAccelerometer;
    Sensor sensorGyroscope;
    float[] dataLinearAccelerometer;
    float[] dataGyroscope;

    public InertialSensor(MainActivity father) {
        this.father = father;
        sensorManager = (SensorManager)father.getSystemService(father.SENSOR_SERVICE);

        sensorLinearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensorLinearAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        dataLinearAccelerometer = new float[] { 0, 0, 0 };

        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        dataGyroscope = new float[] { 0, 0, 0 };
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == sensorLinearAccelerometer) {
            dataLinearAccelerometer = event.values;
        }
        if (event.sensor == sensorGyroscope) {
            dataGyroscope = event.values;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
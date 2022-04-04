package com.example.imusensors;

////////////////////////////////////////
// Author: Dawei Sun s2225079
// PGEE111152021-2SS1SEM2: Embedded Mobile and Wireless Systems (EWireless) (MSc) (2021-2022)[SEM2]
////////////////////////////////////////

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class IMUSensorManager implements SensorEventListener{
    private OnIMUSensorListener onIMUSensorListener;

    private final SensorManager sensorManager;
    private final Sensor Accelerometer;
    private final Sensor MagneticField;
    private final Sensor Gyroscope;
    private final Sensor StepDetector;
    private final Sensor RotationVector;

    private int stp_ctr;


    // Constructor of the class
    public IMUSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        MagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        StepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        RotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        stp_ctr = 0;
    }

    // Allow setting the listener through this function
    public void setOnIMUSensorListener(OnIMUSensorListener onIMUSensorListener) {
        this.onIMUSensorListener = onIMUSensorListener;
    }

    public void registerIMUSensors() {
        sensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, MagneticField, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, Gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, StepDetector, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, RotationVector, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void unregisterIMUSensors() {
        sensorManager.unregisterListener(this);
    }


    final float alpha = (float) 0.8;
    private float[] gravity = new float[3];

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // Isolate g
                gravity[0] = alpha * gravity[0] + (1-alpha) * sensorEvent.values[0];
                gravity[1] = alpha * gravity[1] + (1-alpha) * sensorEvent.values[1];
                gravity[2] = alpha * gravity[2] + (1-alpha) * sensorEvent.values[2];

                // Remove g
                float[] linear_acceleration = new float[3];
                linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
                linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
                linear_acceleration[2] = sensorEvent.values[2] - gravity[2];

                onIMUSensorListener.onAccValuesUpdate(linear_acceleration,
                        sensorEvent.values, sensorEvent.timestamp);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                onIMUSensorListener.onMagValuesUpdate(
                        sensorEvent.values, sensorEvent.timestamp);
                break;

            case Sensor.TYPE_GYROSCOPE:
                double h = Math.sqrt(sensorEvent.values[0] * sensorEvent.values[0] +
                        sensorEvent.values[1] * sensorEvent.values[1] +
                        sensorEvent.values[2] * sensorEvent.values[2]);
                onIMUSensorListener.onGyrValuesUpdate(h,
                        sensorEvent.values, sensorEvent.timestamp);
                break;

            case Sensor.TYPE_STEP_DETECTOR:
                stp_ctr = stp_ctr + 1;
                onIMUSensorListener.onStpValuesUpdate(stp_ctr, sensorEvent.timestamp);
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                onIMUSensorListener.onRotValuesUpdate(
                        sensorEvent.values, sensorEvent.timestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    // Interface
    public interface OnIMUSensorListener {
        void onAccValuesUpdate(float[] accFltValues, float[] accValues, long timestamp);
        void onMagValuesUpdate(float[] magValues, long timestamp);
        void onGyrValuesUpdate(double gyrHValue, float[] gyrValues, long timestamp);
        void onStpValuesUpdate(int stpCtr, long timestamp);
        void onRotValuesUpdate(float[] rotValues, long timestamp);
    }
}

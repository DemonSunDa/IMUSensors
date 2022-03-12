package com.example.imusensors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements IMUSensorManager.OnIMUSensorListener {

    private Button btIMUStart;
    private Button btIMUStop;

    private TextView tvAccX;
    private TextView tvAccY;
    private TextView tvAccZ;

    private TextView tvMagX;
    private TextView tvMagY;
    private TextView tvMagZ;

    private TextView tvGyrX;
    private TextView tvGyrY;
    private TextView tvGyrZ;
    private TextView tvGyrH;

    private TextView tvStpCtr;

    private IMUSensorManager imuSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btIMUStart = (Button) findViewById(R.id.bt_imu_start);
        btIMUStop = (Button) findViewById(R.id.bt_imu_stop);

        tvAccX = (TextView) findViewById(R.id.tv_acc_value0);
        tvAccY = (TextView) findViewById(R.id.tv_acc_value1);
        tvAccZ = (TextView) findViewById(R.id.tv_acc_value2);

        tvMagX = (TextView) findViewById(R.id.tv_mag_value0);
        tvMagY = (TextView) findViewById(R.id.tv_mag_value1);
        tvMagZ = (TextView) findViewById(R.id.tv_mag_value2);

        tvGyrX = (TextView) findViewById(R.id.tv_gyr_value0);
        tvGyrY = (TextView) findViewById(R.id.tv_gyr_value1);
        tvGyrZ = (TextView) findViewById(R.id.tv_gyr_value2);
        tvGyrH = (TextView) findViewById(R.id.tv_gyr_value3);

        tvStpCtr = (TextView) findViewById(R.id.tv_stp_value0);

        askStoragePermission();

        imuSensorManager = new IMUSensorManager(this);
        imuSensorManager.setOnIMUSensorListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        imuSensorManager.registerIMUSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();

        imuSensorManager.unregisterIMUSensors();
    }


    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;

    private void askStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Check if we have read/write permission
            int readStoragePermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeStoragePermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (readStoragePermission != PackageManager.PERMISSION_GRANTED ||
                    writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user
                this.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_ID_READ_WRITE_PERMISSION
                );
                return;
            }
        }
    }

    private static final int REQUEST_ID_ACTIVITY_RECOGNITION_PERMISSION = 98;

    private void askActivityPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Check if we have read/write permission
            int activityRecognitionPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACTIVITY_RECOGNITION);

            if (activityRecognitionPermission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user
                this.requestPermissions(
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        REQUEST_ID_ACTIVITY_RECOGNITION_PERMISSION
                );
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_READ_WRITE_PERMISSION: {
                //If request is cancelled, the result array is empty
                //Permissions granted: read/write
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_LONG).show();
                    askActivityPermission();
                }
                //cancel or denied
                else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                    askStoragePermission();
                }
                break;
            }

            case REQUEST_ID_ACTIVITY_RECOGNITION_PERMISSION: {
                //If request is cancelled, the result array is empty
                //Permissions granted: activity recognition
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_LONG).show();
                }
                //cancel or denied
                else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                    askActivityPermission();
                }
                break;
            }
        }
    }


    public void onBtIMUStart(View view) {
        btIMUStart.setEnabled(false);
        btIMUStop.setEnabled(true);
    }

    public void onBtIMUStop(View view) {
        btIMUStart.setEnabled(true);
        btIMUStop.setEnabled(false);
    }

    @Override
    public void onAccValuesUpdate(float[] accValues) {
        tvAccX.setText("acc_X: " + accValues[0]);
        tvAccY.setText("acc_Y: " + accValues[1]);
        tvAccZ.setText("acc_Z: " + accValues[2]);
    }

    @Override
    public void onMagValuesUpdate(float[] magValues) {
        tvMagX.setText("mag_X: " + magValues[0]);
        tvMagY.setText("mag_Y: " + magValues[1]);
        tvMagZ.setText("mag_Z: " + magValues[2]);
    }

    @Override
    public void onGyrValuesUpdate(float[] gyrValues) {
        tvGyrX.setText("gyr_X: " + gyrValues[0]);
        tvGyrY.setText("gyr_Y: " + gyrValues[1]);
        tvGyrZ.setText("gyr_Z: " + gyrValues[2]);
        tvGyrH.setText("gyr_H: " + gyrValues[3]);
    }

    @Override
    public void onStpValuesUpdate(int stpCtr) {
        tvStpCtr.setText("step count: " + stpCtr);
    }
}
package com.example.imusensors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements IMUSensorManager.OnIMUSensorListener {

    Button btIMUStart;
    Button btIMUStop;

    private IMUSensorManager imuSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btIMUStart = (Button) findViewById(R.id.bt_imu_start);
        btIMUStop = (Button) findViewById(R.id.bt_imu_stop);

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
                }
                //cancel or denied
                else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
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

    }

    @Override
    public void onMagValuesUpdate(float[] magValues) {

    }

    @Override
    public void onGyrValuesUpdate(float[] gyrValues) {

    }

//    @Override
//    public void onStpValuesUpdate(float[] stpValues) {
//
//    }
}
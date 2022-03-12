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

public class MainActivity extends AppCompatActivity {

    Button btIMUStart;
    Button btIMUStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btIMUStart = (Button) findViewById(R.id.bt_imu_start);
        btIMUStop = (Button) findViewById(R.id.bt_imu_stop);

        askStoragePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;

    private void askStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Check if we have read/write permission
            int readStoragPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeStoragPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (readStoragPermission != PackageManager.PERMISSION_GRANTED ||
                    writeStoragPermission != PackageManager.PERMISSION_GRANTED) {
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
                //Permissions granted: read/write/camera
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

    }

    public void onBtIMUStop(View view) {

    }
}
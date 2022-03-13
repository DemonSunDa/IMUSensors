package com.example.imusensors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

    private final String TAG = "IMUSensorLog";
    private boolean idcWrite; // indicator true to start, false to stop
    File dataFile;
    FileOutputStream fileOutputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btIMUStart = findViewById(R.id.bt_imu_start);
        btIMUStop = findViewById(R.id.bt_imu_stop);

        tvAccX = findViewById(R.id.tv_acc_value0);
        tvAccY = findViewById(R.id.tv_acc_value1);
        tvAccZ = findViewById(R.id.tv_acc_value2);

        tvMagX = findViewById(R.id.tv_mag_value0);
        tvMagY = findViewById(R.id.tv_mag_value1);
        tvMagZ = findViewById(R.id.tv_mag_value2);

        tvGyrX = findViewById(R.id.tv_gyr_value0);
        tvGyrY = findViewById(R.id.tv_gyr_value1);
        tvGyrZ = findViewById(R.id.tv_gyr_value2);
        tvGyrH = findViewById(R.id.tv_gyr_value3);

        tvStpCtr = findViewById(R.id.tv_stp_value0);

        askStoragePermission();

        // Instantiate SensorManager
        imuSensorManager = new IMUSensorManager(this);
        imuSensorManager.setOnIMUSensorListener(this);

        idcWrite = false;
        dataFile = null;
        fileOutputStream = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (idcWrite) {
            try {
                fileOutputStream = new FileOutputStream(dataFile, true);
                Log.d(TAG, "Resume writing to " + currentDataPath);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        imuSensorManager.registerIMUSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (idcWrite) {
            try {
                fileOutputStream.close(); // temporary close the stream
                Log.d(TAG, "Pause saved to " + currentDataPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
            // Check if we have activity recognition permission
            //! API 29+ required
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
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_READ_WRITE_PERMISSION: {
                // If request is cancelled, the result array is empty
                // Permissions granted: read/write
                if (grantResults.length >= 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                    askActivityPermission();
                }
                //cancel or denied
                else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    askStoragePermission();
                }
                break;
            }

            case REQUEST_ID_ACTIVITY_RECOGNITION_PERMISSION: {
                //If request is cancelled, the result array is empty
                //Permissions granted: activity recognition
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                }
                //cancel or denied
                else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    askActivityPermission();
                }
                break;
            }
        }
    }


    public void onBtIMUStart(View view) {
        btIMUStart.setEnabled(false);
        btIMUStop.setEnabled(true);

        try {
            dataFile = createDataFile(); // create a file in internal storage
            // Create a file output stream to write to the file
            // true for appending to the end, false for writing from the start
            fileOutputStream = new FileOutputStream(dataFile, false);
            Log.d(TAG, "Writing to " + currentDataPath);
            if (fileOutputStream != null) {
                fileOutputStream.write("Timestamp,Sensor_Type,Value_1,Value_2,Value_3,Value_4\n"
                        .getBytes(StandardCharsets.UTF_8)); // title row
            }
            else {
                Toast.makeText(this, "Write file error.", Toast.LENGTH_SHORT).show();
                idcWrite = false;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        idcWrite = true;
    }

    public void onBtIMUStop(View view) {
        btIMUStart.setEnabled(true);
        btIMUStop.setEnabled(false);

        try {
            fileOutputStream.close();
            Log.d(TAG, "Saved to " + currentDataPath);
            Toast.makeText(this, "Internal file saved.", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        idcWrite = false;
        dataFile = null;
        fileOutputStream = null;
    }


    private String currentDataPath;

    private File createDataFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String dataFileName = "IMUData_" + timeStamp; // file name
        File mAppStorageDir = getFilesDir(); // internal storage directory
        File imuData = File.createTempFile(
                dataFileName,
                ".csv",
                mAppStorageDir
        );

        currentDataPath = imuData.getAbsolutePath();
        return imuData;
    }

    private void writeStream(float[] values, long timestamp, String sensorType) {
        if (idcWrite) {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.write(String.format(Locale.getDefault(),
                            "%d,%s,%f,%f,%f\n",
                            timestamp, sensorType, values[0], values[1], values[2])
                            .getBytes(StandardCharsets.UTF_8));
                }
                else {
                    Toast.makeText(this, "Write file error.", Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onAccValuesUpdate(float[] accFltValues, float[] accValues, long timestamp) {
        tvAccX.setText("acc_X: " + accFltValues[0]);
        tvAccY.setText("acc_Y: " + accFltValues[1]);
        tvAccZ.setText("acc_Z: " + accFltValues[2]);

        writeStream(accValues, timestamp, "ACC");
    }

    @Override
    public void onMagValuesUpdate(float[] magValues, long timestamp) {
        tvMagX.setText("mag_X: " + magValues[0]);
        tvMagY.setText("mag_Y: " + magValues[1]);
        tvMagZ.setText("mag_Z: " + magValues[2]);

        writeStream(magValues, timestamp, "EMF");
    }

    @Override
    public void onGyrValuesUpdate(double h, float[] gyrValues, long timestamp) {
        tvGyrX.setText("gyr_X: " + gyrValues[0]);
        tvGyrY.setText("gyr_Y: " + gyrValues[1]);
        tvGyrZ.setText("gyr_Z: " + gyrValues[2]);
        tvGyrH.setText("gyr_H: " + h);

        writeStream(gyrValues, timestamp, "GYR");
    }

    @Override
    public void onStpValuesUpdate(int stpCtr, long timestamp) {
        tvStpCtr.setText("step count: " + stpCtr);

//        if (idcWrite) {
//            try {
//                if (fileOutputStream != null) {
//                    fileOutputStream.write(String.format(Locale.getDefault(),
//                            "%d,STP,%d\n",
//                            timestamp, stpCtr)
//                            .getBytes(StandardCharsets.UTF_8));
//                }
//                else {
//                    Toast.makeText(this, "Write file error.", Toast.LENGTH_SHORT).show();
//                }
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
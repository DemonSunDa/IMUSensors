package com.example.imusensors;

////////////////////////////////////////
// Author: Dawei Sun s2225079
// PGEE111152021-2SS1SEM2: Embedded Mobile and Wireless Systems (EWireless) (MSc) (2021-2022)[SEM2]
////////////////////////////////////////

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements IMUSensorManager.OnIMUSensorListener, IMUWifiManager.OnWifiReceiver {

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

    private TextView tvRotX;
    private TextView tvRotY;
    private TextView tvRotZ;
    private TextView tvRotS;

    private ListView lv;

    private IMUSensorManager imuSensorManager;
    private IMUWifiManager imuWifiManager;

    private ScheduledExecutorService scheduledExecutor;

    private final String TAG = "IMUSensorLog";
    private boolean idcWrite; // indicator true to start, false to stop
    private File dataFile;
    private FileOutputStream fileOutputStream;
    private String currentDataPath;


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

        tvRotX = findViewById(R.id.tv_rot_value0);
        tvRotY = findViewById(R.id.tv_rot_value1);
        tvRotZ = findViewById(R.id.tv_rot_value2);
        tvRotS = findViewById(R.id.tv_rot_value3);

        lv = findViewById(R.id.lv_wifi);

        askWifiPermissions();

        // Instantiate SensorManager
        imuSensorManager = new IMUSensorManager(this);
        imuSensorManager.setOnIMUSensorListener(this);

        imuWifiManager = new IMUWifiManager(this);
        imuWifiManager.setOnWifiReceiver(this);

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scanWifiPer30Sec();

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
        imuWifiManager.registerIMUWifi(this);
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
        imuWifiManager.unregisterIMUWifi(this);
    }


    private static final int REQUEST_ID_WIFI_PERMISSION = 99;

    private void askWifiPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Check if we have read/write permission
            int wifiAccessPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_WIFI_STATE);
            int wifiChangePermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CHANGE_WIFI_STATE);
            int coarseLocationPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            int fineLocationPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (wifiAccessPermission != PackageManager.PERMISSION_GRANTED ||
                    wifiChangePermission != PackageManager.PERMISSION_GRANTED ||
                    coarseLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ID_WIFI_PERMISSION
                );
                return;
            }
        }
    }

    private static final int REQUEST_ID_ACTIVITY_RECOGNITION_PERMISSION = 98;

    private void askActivityPermissions() {
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
            case REQUEST_ID_WIFI_PERMISSION: {
                // If request is cancelled, the result array is empty
                // Permissions granted: read/write
                if (grantResults.length >= 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                    askActivityPermissions();
                }
                //cancel or denied
                else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    askWifiPermissions();
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
                    askActivityPermissions();
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
                fileOutputStream.write("Timestamp,Sensor_Type,Value_0,Value_1,Value_2,Value_3\n"
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

    private void scanWifiPer30Sec() {
        Runnable scanner = () -> imuWifiManager.scanIMUWifi(this);
        // Log.e("TESTSCH", "This is a scheduler test!");
        // imuWifiManager.scanIMUWifi(this);
        ScheduledFuture<?> scannerHandle = scheduledExecutor.scheduleAtFixedRate(
                scanner, 0, 30, TimeUnit.SECONDS);
        // Initial delay = 0 s. Period = 30 s.
        Runnable canceller = () -> scannerHandle.cancel(false);
        scheduledExecutor.schedule(canceller, 2, TimeUnit.HOURS);
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

        if (idcWrite) {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.write(String.format(Locale.getDefault(),
                            "%d,STP,%d\n",
                            timestamp, stpCtr)
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
    public void onRotValuesUpdate(float[] rotValues, long timestamp) {
        tvRotX.setText("rot_X: " + rotValues[0]);
        tvRotY.setText("rot_Y: " + rotValues[1]);
        tvRotZ.setText("rot_Z: " + rotValues[2]);
        tvRotS.setText("rot_S: " + rotValues[3]);

        float[] rotMat = new float[9];
        float[] oriValues = new float[3];
        SensorManager.getRotationMatrixFromVector(rotMat, rotValues);
        SensorManager.getOrientation(rotMat, oriValues);

        if (idcWrite) {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.write(String.format(Locale.getDefault(),
                            "%d,ROT,%f,%f,%f,%f\n%d,ORI,%f,%f,%f\n",
                            timestamp, rotValues[0], rotValues[1], rotValues[2], rotValues[3],
                            timestamp, oriValues[0], oriValues[1], oriValues[2])
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
    public void onWifiScanResult(List<ScanResult> wifiScanList) {
        String[] wifis = new String[wifiScanList.size()];
        Log.e("WiFi", String.valueOf(wifiScanList.size()));
        for (int ctr_l = 0; ctr_l < wifiScanList.size(); ctr_l++) { // generate output strings and store in wifis
            wifis[ctr_l] = wifiScanList.get(ctr_l).SSID + "," + wifiScanList.get(ctr_l).BSSID + "," +
                    String.valueOf(wifiScanList.get(ctr_l).level);
            Log.d("WiFi", String.valueOf(wifis[ctr_l]));
        }

        lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, wifis));

        if (idcWrite) {
            try {
                if (fileOutputStream != null) {
                    for (int ctr_w = 0; ctr_w < wifiScanList.size(); ctr_w ++) {
                        fileOutputStream.write(String.format(Locale.getDefault(),
                                "%d,WIFI,%s\n",
                                wifiScanList.get(ctr_w).timestamp, wifis[ctr_w])
                                .getBytes(StandardCharsets.UTF_8));
                    }
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
}

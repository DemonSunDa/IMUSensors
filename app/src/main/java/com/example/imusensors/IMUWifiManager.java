package com.example.imusensors;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class IMUWifiManager {
    private OnWifiReceiver onWifiReceiver;
    private WifiManager wifiManager;

    // Constructor of the class
    public IMUWifiManager(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.getWifiState() == wifiManager.WIFI_STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);
            context.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }

    // Allow setting the listener through this function
    public void setOnWifiReceiver(OnWifiReceiver onWifiReceiver) {
        this.onWifiReceiver= onWifiReceiver;
    }

    public void registerIMUWifi(Context context) {
        context.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void unregisterIMUWifi(Context context) {
        context.unregisterReceiver(wifiScanReceiver);
    }

    public void scanIMUWifi(Context context) {
        wifiManager.startScan();
        Toast.makeText(context, "Scanning WiFi...", Toast.LENGTH_LONG).show();
    }

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifiManager.getScanResults();

            onWifiReceiver.onWifiScanResult(wifiScanList);
        }
    };

    // Interface
    public interface OnWifiReceiver {
        void onWifiScanResult(List<ScanResult> wifiScanList);
    }
}

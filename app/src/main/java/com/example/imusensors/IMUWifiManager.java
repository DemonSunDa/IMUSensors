package com.example.imusensors;

////////////////////////////////////////
// Author: Dawei Sun s2225079
// PGEE111152021-2SS1SEM2: Embedded Mobile and Wireless Systems (EWireless) (MSc) (2021-2022)[SEM2]
////////////////////////////////////////

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

public class IMUWifiManager {
    private OnWifiReceiver onWifiReceiver;
    private WifiManager wifiManager;

    // Constructor of the class
    public IMUWifiManager(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
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

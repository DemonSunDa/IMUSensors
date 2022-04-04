package com.example.imusensors;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IPS {
    public IPS() {

    }

    static class WIFI {
        public String _id;
        public String EntryId;
        public String TIME;
        public String CoordinateX;
        public String CoordinateY;
        public String WIFI_SSID;
        public String WIFI_BSSID;
        public String WIFI_LEVEL;

        public WIFI() {}
    }

    public static ArrayList<WIFI> dbParse(InputStream inputStream) {

        final int COL_ID = 0;
        final int COL_ENTRYID = 1;
        final int COL_TIME = 2;
        final int COL_COORDINATEX = 3;
        final int COL_COORDINATEY = 4;
        final int COL_WIFI_SSID = 5;
        final int COL_WIFI_BSSID = 6;
        final int COL_WIFI_LEVEL = 7;

        ArrayList<WIFI> results = new ArrayList<WIFI>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String nextLine = null;
        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                String[] tokens = nextLine.split(",");
                if (tokens.length != 8) {
                    Log.w("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                // Add new parsed result
                WIFI current = new WIFI();
                current._id = tokens[COL_ID];
                current.EntryId = tokens[COL_ENTRYID];
                current.TIME = tokens[COL_TIME];
                current.CoordinateX = tokens[COL_COORDINATEX];
                current.CoordinateY = tokens[COL_COORDINATEY];
                current.WIFI_SSID = tokens[COL_WIFI_SSID];
                current.WIFI_BSSID = tokens[COL_WIFI_BSSID];
                current.WIFI_LEVEL = tokens[COL_WIFI_LEVEL];

                results.add(current);
            }

            inputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return results;
    }

    public static void dbReorg(ArrayList<WIFI> DB_RAW, String[][][] DB_REORG) {
        
    }

    public static int wifiPositioning(List<ScanResult> wifiScanList, String[][][] DB_WIFI_REORG) {
        String[][] wifiResArr = new String[wifiScanList.size()][2];
        listToWifiRes(wifiScanList, wifiResArr);

        double[] probArr = new double[DB_WIFI_REORG.length];
        double prob = 1;
        boolean idc_same = false;
        double diff;
        double tmp_p;
        double sum = 0;

        for (int i = 0; i < DB_WIFI_REORG.length; i++){ // init probability
            probArr[i] = 1 / DB_WIFI_REORG.length;
        }
        for (int i = 0; i < DB_WIFI_REORG.length; i++){ // loop for database points
            prob = 1;
            for (int j = 0; j < wifiResArr.length; j++){ // loop for measured wifi
                idc_same = false;
                tmp_p = 0;
                for (int k = 0; k < DB_WIFI_REORG[i].length; k++){ // loop on single database for comparison
                    if (wifiResArr[j][0] == DB_WIFI_REORG[i][1][0]) { // if same BSSID
                        diff = Double.parseDouble(wifiResArr[j][1]) - Double.parseDouble(DB_WIFI_REORG[i][1][1]);
                        // diff = map[i] - mea[state_index]
                        tmp_p = Math.exp(-0.5 * (diff - diff) / (2 * 2));
                        idc_same = true;
                    }
                }

                if (idc_same) { // if same wifi exists
                    prob *= tmp_p;
                }
                else { // if no such wifi
                    prob *= 0.000001;
                }
            }
            probArr[i] *= Math.pow(prob, 1 / wifiResArr.length);
            sum += probArr[i];
        }
        // find index of max prob
        double max = 0;
        int max_ind = 0;
        for (int i = 0; i < probArr.length; i++) {
            max = Math.max(probArr[i], max);
            max_ind = ((probArr[i] > max) ? i : max_ind);
        }

        return max_ind;
    }

    private static void listToWifiRes(List<ScanResult> wifiScanList, String[][] wifiResArr) {
        for (int i = 0; i < wifiScanList.size(); i++) {
            wifiResArr[i][0] = wifiScanList.get(i).BSSID;
            wifiResArr[i][1] = String.valueOf(wifiScanList.get(i).level);
        }
    }
}

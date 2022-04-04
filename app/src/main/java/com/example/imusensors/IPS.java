package com.example.imusensors;

////////////////////////////////////////
// Author: Dawei Sun s2225079
// PGEE111152021-2SS1SEM2: Embedded Mobile and Wireless Systems (EWireless) (MSc) (2021-2022)[SEM2]
////////////////////////////////////////

import android.net.wifi.ScanResult;
import android.util.Log;

import java.io.BufferedReader;
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

    static class EMF {
        public String _id;
        public String EntryId;
        public String TIME;
        public String CoordinateX;
        public String CoordinateY;
        public String GPSLat;
        public String GPSLong;
        public String EMFx;
        public String EMFy;
        public String EMFz;
        public String EMFh;
        public String RotationVectorX;
        public String RotationVectorY;
        public String RotationVectorZ;
        public String RotationVectorS;

        public EMF() {}
    }

    public static ArrayList<WIFI> dbWIFIParse(InputStream inputStream) {
        final int COL_ID = 0;
        final int COL_ENTRYID = 1;
        final int COL_TIME = 2;
        final int COL_COORDINATEX = 3;
        final int COL_COORDINATEY = 4;
        final int COL_WIFI_SSID = 5;
        final int COL_WIFI_BSSID = 6;
        final int COL_WIFI_LEVEL = 7;

        ArrayList<WIFI> results = new ArrayList<>();

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

    public static ArrayList<EMF> dbEMFParse(InputStream inputStream) {
        final int COL_ID = 0;
        final int COL_ENTRYID = 1;
        final int COL_TIME = 2;
        final int COL_COORDINATEX = 3;
        final int COL_COORDINATEY = 4;
        final int COL_GPSLAT = 5;
        final int COL_GPSLONG = 6;
        final int COL_EMFX = 7;
        final int COL_EMFY = 8;
        final int COL_EMFZ = 9;
        final int COL_EMFH = 10;
        final int COL_ROTATIONVECTORX = 11;
        final int COL_ROTATIONVECTORY = 12;
        final int COL_ROTATIONVECTORZ = 13;
        final int COL_ROTATIONVECTORS = 14;

        ArrayList<EMF> results = new ArrayList<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String nextLine = null;
        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                String[] tokens = nextLine.split(",");
                if (tokens.length != 15) {
                    Log.w("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                // Add new parsed result
                EMF current = new EMF();
                current._id = tokens[COL_ID];
                current.EntryId = tokens[COL_ENTRYID];
                current.TIME = tokens[COL_TIME];
                current.CoordinateX = tokens[COL_COORDINATEX];
                current.CoordinateY = tokens[COL_COORDINATEY];
                current.GPSLat = tokens[COL_GPSLAT];
                current.GPSLong = tokens[COL_GPSLONG];
                current.EMFx = tokens[COL_EMFX];
                current.EMFy = tokens[COL_EMFY];
                current.EMFz = tokens[COL_EMFZ];
                current.EMFh = tokens[COL_EMFH];
                current.RotationVectorX = tokens[COL_ROTATIONVECTORX];
                current.RotationVectorY = tokens[COL_ROTATIONVECTORY];
                current.RotationVectorZ = tokens[COL_ROTATIONVECTORZ];
                current.RotationVectorS = tokens[COL_ROTATIONVECTORS];

                results.add(current);
            }

            inputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return results;
    }

    public static ArrayList<Integer> dbWifiReorg(ArrayList<WIFI> DB_WIFI_RAW) {
        // DB_WIFI_REORG
        // a 3d-array [[[x0, y0], [BSSID0, LEVEL0]], [[x1, y1], [BSSID1, LEVEL1]], ...]
        ArrayList<Integer> DB_WIFI_CTR = new ArrayList<>();
        DB_WIFI_CTR.add(1);
        int counter = 1;
        for (int i = 2; i < DB_WIFI_RAW.size(); i++) {
            if (!(DB_WIFI_RAW.get(i).CoordinateX.equals(DB_WIFI_RAW.get(i - 1).CoordinateX) &&
                    DB_WIFI_RAW.get(i).CoordinateY.equals(DB_WIFI_RAW.get(i - 1).CoordinateY))) {
                // if different coordinate point
                counter++;
                DB_WIFI_CTR.add(counter);
            }
            else {
                counter++;
            }
        }
        counter++;
        DB_WIFI_CTR.add(counter);

        return DB_WIFI_CTR;
    }

    public static void dbEMFReorg(ArrayList<EMF> DB_EMF_RAW, String DB_EMF_REORG) {

    }

    public static double[] wifiPositioning(List<ScanResult> wifiScanList, ArrayList<WIFI> DB_WIFI_RAW, ArrayList<Integer> DB_WIFI_CTR) {
        String[][] wifiResArr = new String[wifiScanList.size()][2];
        listToWifiRes(wifiScanList, wifiResArr);

        double[] probArr = new double[DB_WIFI_CTR.size() - 1];
        double prob;
        boolean idc_same;
        double diff;
        double tmp_p;

        for (int i = 0; i < (DB_WIFI_CTR.size() - 1); i++){ // init probability
            probArr[i] = 1 / (double) (DB_WIFI_CTR.size() - 1);
        }
        for (int i = 0; i < (DB_WIFI_CTR.size() - 1); i++){ // loop for database points
            prob = 1;
            for (int j = 0; j < wifiResArr.length; j++){ // loop for measured wifi
                idc_same = false;
                tmp_p = 0;
                for (int k = 0; k < (DB_WIFI_CTR.get(i + 1) - DB_WIFI_CTR.get(i)); k++){ // loop on single database for comparison
                    // Log.e("i+k", String.valueOf(i + k));
                    if (wifiResArr[j][0].equals(DB_WIFI_RAW.get(i + k).WIFI_BSSID)) { // if same BSSID
                        diff = Double.parseDouble(wifiResArr[j][1]) - Double.parseDouble(DB_WIFI_RAW.get(i + k).WIFI_LEVEL);
                        // diff = map[i] - mea[state_index]
                        tmp_p = Math.exp(-0.5 * (diff * diff) / (2 * 2));
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
            probArr[i] *= Math.pow(prob, 1 / (double) wifiResArr.length);
        }
        // find index of max prob
        double max = 0;
        int max_ind = 0;
        for (int i = 0; i < probArr.length; i++) {
            max = Math.max(probArr[i], max);
            max_ind = ((probArr[i] > max) ? i : max_ind);
        }

        return new double[] {Double.parseDouble(DB_WIFI_RAW.get(DB_WIFI_CTR.get(max_ind)).CoordinateX),
                Double.parseDouble(DB_WIFI_RAW.get(DB_WIFI_CTR.get(max_ind)).CoordinateY)};
    }

    private static void listToWifiRes(List<ScanResult> wifiScanList, String[][] wifiResArr) {
        for (int i = 0; i < wifiScanList.size(); i++) {
            wifiResArr[i][0] = wifiScanList.get(i).BSSID;
            wifiResArr[i][1] = String.valueOf(wifiScanList.get(i).level);
        }
    }
}

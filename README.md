# IMUSensors

> Author: Dawei Sun s2225079  
> PGEE111152021-2SS1SEM2: Embedded Mobile and Wireless Systems (EWireless) (MSc) (2021-2022)  

This is a project for the final assignment of the above course. An android app is developed in order to constantly record values from IMU sensors as well as wifi who's scanned every 30 seconds as the throttling required (<https://developer.android.com/guide/topics/connectivity/wifi-scan>).  
Sensor data are collected upon sensor update with their timestamp in nanosecond. Collected data are then directly write to a csv file auto generated each time of a full sequence of pressing start and stop.  

Currently collecting data:  

- `ACC` Accelerometer SENSOR_DELAY_FASTEST  
- `GYR` Gyroscope SENSOR_DELAY_FASTEST  
- `EMF` Magnetic Field SENSOR_DELAY_FASTEST  
- `ROT` Rotation Vector SENSOR_DELAY_FASTEST  
- `ORI` Results from function SensorManager.getOrientation Based on `ROT`
- `WIFI` WiFi scanResult() Every 30 seconds

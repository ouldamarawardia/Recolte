package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MyService extends Service implements SensorEventListener {

    private static final String TAG = "MyActivity";
    public static final String My_Prefs = "My_prefs";

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    Sensor accelerometer;
    SensorManager sm;

    boolean bump;
    boolean firstUpdate = true;
    double x,y,z,vilocity;
    double xAccel,yAccel,zAccel;
    double xPreviousAccel,yPreviousAccel,zPreviousAccel;
    double accuracyDirection, accuracySpeed, latitude, longitude,speed, direction;

    int interval,FastestInterval,SmallestDisplacement;

    MyDatabase db ;

    IntentFilter intentFilter;

    @Override
    public void onCreate() {
        super.onCreate();
        intentFilter = new IntentFilter();
        intentFilter.addAction("bump"); // Action1 to filter
        intentFilter.addAction("stop"); // Action2 to filter
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);

        Toast.makeText(this, "Service start", Toast.LENGTH_SHORT).show();

        db = MyDatabase.instance(this);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener( this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

        bump =false;

        SharedPreferences preferences=getSharedPreferences(My_Prefs,MODE_PRIVATE);
        interval=preferences.getInt("Interval",500);
        FastestInterval=preferences.getInt("FastestInterval",500);
        SmallestDisplacement =preferences.getInt("SmallestDisplacement",1);
    }

    public void onDestroy(){
        super.onDestroy();
    }



    private BroadcastReceiver mReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("bump")) {
                // message From button save 
                bump =true;
            } else if (intent.getAction().equals("stop")) {
                // message from button( interval or fastestInterval or smallestDisplacement )
                Toast.makeText(context, "Service stop", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MyService.this,MyService.class);
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                stopService(i);
            }


        }
    };
    private void buildLocationCallBack() {

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location:locationResult.getLocations()) {
                    Log.i(TAG,"interval ------->  "+ interval);
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    speed = location.getSpeed();
                    direction = location.getBearing();

                    if (Build.VERSION.SDK_INT < 26) {
                        accuracySpeed = 0;
                        accuracyDirection = 0;
                    }
                    else {
                        accuracySpeed = location.getSpeedAccuracyMetersPerSecond();
                        accuracyDirection = location.getBearingAccuracyDegrees();
                    }

                    Recolt donnees = new Recolt(latitude, longitude, speed, accuracySpeed, direction, accuracyDirection,vilocity ,x, y, z, bump);
                    db.daoAccess().insertRacolt(donnees);
                    int h=db.daoAccess().loadalldatas().size()-1;
                    Log.i(TAG, ""+ db.daoAccess().loadalldatas().get(h));

                    bump =false;
                }
            }
        };
    }



    @SuppressLint("SetTextI18n")
    public void onSensorChanged(SensorEvent event) {

        x = event.values[0];
        y = event.values[1];
        z = event.values[2];



        updateAccel(x,y,z);

        double deltaX = Math.abs(xPreviousAccel - xAccel);
        double deltaY = Math.abs(yPreviousAccel - yAccel);
        double deltaZ = Math.abs(yPreviousAccel - yAccel);
        vilocity = Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2)+Math.pow(deltaZ,2))/SensorManager.GRAVITY_EARTH;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateAccel(double xNew, double yNew, double zNew) {
        if(firstUpdate){
            xPreviousAccel = xNew;
            yPreviousAccel = yNew;
            zPreviousAccel = zNew;
            firstUpdate = false ;
        }
        else {
            xPreviousAccel = xAccel;
            yPreviousAccel = yAccel;
            zPreviousAccel = zAccel;
        }

        xAccel = xNew;
        yAccel = yNew;
        zAccel = zNew;
    }


    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(FastestInterval);
        locationRequest.setSmallestDisplacement(SmallestDisplacement);


    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

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

import javax.net.ssl.SSLContext;

public class MyService extends Service implements SensorEventListener {



    private static final String TAG = "MyActivity";

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Sensor accelerometer;
    SensorManager sm;
    boolean mark;
    double x,y,z,vilocity;
    double xAccel,yAccel,zAccel;
    double xPreviousAccel,yPreviousAccel,zPreviousAccel;
    boolean firstUpdate = true;
    double directprec,speedprec,alt ,longi,sped,dir;
    int interval,FastestInterval,SmallestDisplacement;
    public static final String My_Prefs = "My_prefs";
    MyDatabase db ;
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "-------------Service start ", Toast.LENGTH_SHORT).show();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("true_reg"));
        db = MyDatabase.instance(this);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener( this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

        mark=false;


        SharedPreferences preferences=getSharedPreferences(My_Prefs,MODE_PRIVATE);
        interval=preferences.getInt("interval",100);
        FastestInterval=preferences.getInt("FastestInterval",100);
        SmallestDisplacement =preferences.getInt("SmallestDisplacement",100);



    }
    public void onDestroy(){

        super.onDestroy();
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mark=true;
        }
    };
    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location:locationResult.getLocations()) {
                    Log.i(TAG,"interval ------->  "+ interval);
                    alt = location.getLatitude();
                    longi = location.getLongitude();
                    sped = location.getSpeed();
                    dir = location.getBearing();
                    if (Build.VERSION.SDK_INT < 26) {
                        speedprec = 0;
                        directprec = 0;
                    }
                    else {
                        speedprec = location.getSpeedAccuracyMetersPerSecond();
                        directprec = location.getBearingAccuracyDegrees();
                    }
                    Recolt donnees = new Recolt(alt, longi, sped, speedprec, dir, directprec,vilocity ,x, y, z,mark);
                    db.daoAccess().insertRacolt(donnees);
                    int h=db.daoAccess().loadalldatas().size()-1;
                    Log.i(TAG, ""+ db.daoAccess().loadalldatas().get(h));


                    mark=false;
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
        vilocity = Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2)+Math.pow(deltaZ,2))-SensorManager.GRAVITY_EARTH;

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

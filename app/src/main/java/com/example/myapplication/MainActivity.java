package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import static com.example.myapplication.MyService.My_Prefs;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
//    private static final int REQUEST_CHECK_SETTINGS =989 ;
    Context context;
    Button ralentisseur, save;
    ImageView icon;


    TextView textView; // show progress of seek bar
    TextView ok, cancel; //ok
    TextView title; // titre de popup
    SeekBar seekBar; // seekBar for interval fastestInterval and SmallestDisplacement
    int MinDisplacement = 0, MaxDisplacement = 50, currentDisplacement = 1; // for seekBar min , max , acurrent valeur
    int MinInterval = 100, MaxInterval = 1000, currentInterval = 500;
    int MinFastestInterval = 100, MaxFastestInterval = 1000, currentFastestInterval = 500;// for seekBar min , max , acurrent valeur
    int last1, last2, last3;
    int choix; // 1: interval , 2: Fastest interval , 3: SmallestDisplacement
    int PERMISSION_ALL = 1;
//    LocationManager locationManager;
    boolean update = false;


    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    MyDatabase recolt;




   /* public boolean CheckGpsStatus() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = this;

        //permissions
        if (!update) {
            update = true;

        }

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

//appel de creation de location request
      //  createLocationRequest();


        recolt = MyDatabase.instance(this);


       // start service;
        final Intent intent = new Intent().setClass(this, MyService.class);
        startService(intent);





       // preferences of location parametre
        SharedPreferences preferences = getSharedPreferences(My_Prefs, MODE_PRIVATE);
        last1 = preferences.getInt("last1", 500);
        last2 = preferences.getInt("last2", 500);
        last3 = preferences.getInt("last3", 1);



        save = findViewById(R.id.save);
        ralentisseur = findViewById(R.id.ralentisseur);


        //export data to csv file
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveData();
                recolt.daoAccess().deleteRacolt();
                Toast toast = Toast.makeText(context, "saved!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        //make marke true *existance de rallentisseur*
        ralentisseur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent("bump");
                LocalBroadcastManager.getInstance(context).sendBroadcast((i));
                Toast toast = Toast.makeText(context, "done!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


    //menu setting
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.parametere, menu);

        return true;
    }


    //menu opptions
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {


            case R.id.interval:

                // code de interval
                Intent i = new Intent("stop");
                LocalBroadcastManager.getInstance(context).sendBroadcast((i));
                choix = 1;
                showDialog();
                break;

            case R.id.FastestInterval:

                // code de FastestInterval
                Intent ii = new Intent("stop");
                LocalBroadcastManager.getInstance(context).sendBroadcast((ii));
                choix = 2;
                showDialog();
                break;

            case R.id.SmallestDisplacement:

                // code de SmallestDisplacement
                Intent iii = new Intent("stop");
                LocalBroadcastManager.getInstance(context).sendBroadcast((iii));
                choix = 3;
                showDialog();

                break;
        }


        return super.onOptionsItemSelected(item);
    }


    //Dialog window "parameters" interval fastestInterval smallestDisplacement
    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(context).inflate(
                R.layout.activity_parameters,
                (ConstraintLayout) findViewById(R.id.settingDialogContainer)
        );
        builder.setView(view);


        seekBar = view.findViewById(R.id.seekBar); // make seekBar object

        textView = view.findViewById(R.id.textView);

        title = view.findViewById(R.id.title);

        cancel = view.findViewById(R.id.cancel);

        icon = view.findViewById(R.id.icon);

        ok = view.findViewById(R.id.ok);

        ok.setText(R.string.ok);
        cancel.setText(R.string.cancel);

        switch (choix) {
            case 1:

                seekBar.setMax(MaxInterval - MinInterval);
                seekBar.setProgress(last1 - MinInterval);
                title.setText("interval");
                textView.setText(""+last1);
                icon.setImageResource(R.drawable.ic_interval);
                break;

            case 2:

                seekBar.setMax(MaxFastestInterval - MinFastestInterval);
                title.setText("fastest interval");
                seekBar.setProgress(last2 - MinFastestInterval);
                textView.setText(""+last2);
                icon.setImageResource(R.drawable.ic_fast_interval);
                break;

            case 3:
                seekBar.setMax(MaxDisplacement - MinDisplacement);
                title.setText("smallest_displacement");
                seekBar.setProgress(last3 - MinDisplacement);
                textView.setText(""+last3);
                icon.setImageResource(R.drawable.ic_distance);
                break;
        }


        seekBar.setOnSeekBarChangeListener(MainActivity.this);


        final AlertDialog alertDialog = builder.create();


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final SharedPreferences.Editor editor = getSharedPreferences(My_Prefs, MODE_PRIVATE).edit();
                switch (choix) {
                    case 1:
                        editor.putInt("Interval", currentInterval);

                        last1 = currentInterval;

                        editor.putInt("last1", last1);
                        editor.apply();


                        break;

                    case 2:
                        editor.putInt("FastestInterval", currentFastestInterval);

                        last2 = currentFastestInterval;

                        editor.putInt("last2", last2);
                        editor.apply();


                        break;

                    case 3:
                        editor.putInt("SmallestDisplacement", currentDisplacement);

                        last3 = currentDisplacement;

                        editor.putInt("last3", last3);
                        editor.apply();

                        break;

                }



                final Intent intent = new Intent().setClass(context, MyService.class);
                startService(intent);
                alertDialog.dismiss();


            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent().setClass(context, MyService.class);
                startService(intent);
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {

            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        }

        alertDialog.show();
    }


    //methode of seek bar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        switch (choix) {

            case 1:
                currentInterval = progress + MinInterval;
                textView.setText(""+currentInterval);
                break;

            case 2:
                currentFastestInterval = progress + MinInterval;
                textView.setText(""+currentFastestInterval);
                break;

            case 3:
                currentDisplacement = progress + MinDisplacement;
                textView.setText(""+currentDisplacement);
                break;
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }







//permissions of location and storage true false

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }




    // fichier csv

    public File creatFile(){
        Date d=new Date();
        return new File(context.getExternalFilesDir(""), "Infos " +d.toString().replaceAll("[^a-zA-Z0-9]", " ")+ ".csv");
    }

 public void saveData(){
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdir();
        }

     File file=creatFile();
        while(file.exists()){

            file=creatFile();
        }
        try {

            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = recolt.query("select * from recolts",null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                String arrStr[] = new String[curCSV.getColumnCount()];
                for ( int i = 0; i < curCSV.getColumnCount() ; i++)
                    arrStr[i] = curCSV.getString(i);
                csvWrite.writeNext(arrStr);
            }

            csvWrite.close();
            curCSV.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }

    }


}

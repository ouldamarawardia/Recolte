package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import static com.example.myapplication.MyService.My_Prefs;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    Context context;
    Button ralentisseur,save,interval,FastestInterval,SmallestDisplacement;


    TextView textView; // show progress of seek bar
    TextView termine; //ok
    TextView title; // titre de popup
    SeekBar seekBar; // seekBar for interval fastestInterval and SmallestDisplacement
    int Min=100,Max=1000,current=100; // for seekBar min , max , acurrent valeur
    int last1=current,last2=current,last3=current;
    int choix; // 1: interval , 2: Fastest interval , 3: SmallestDisplacement
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    MyDatabase recolt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        recolt = MyDatabase.instance(this);
        final Intent intent = new Intent().setClass(this, MyService.class);
        startService(intent);
        context = this;
        save = findViewById(R.id.save);
        ralentisseur = findViewById(R.id.ralentisseur);
        interval= findViewById(R.id.interval);
        FastestInterval= findViewById(R.id.FastestInterval);
        SmallestDisplacement= findViewById(R.id.SmallestDisplacement);

        interval.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // code de interval
                                            choix=1;
                                            initiatePopupWindow();


                                        }
                                    }
        );

        FastestInterval.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                                     // code de FastestInterval
                                                     choix=2;
                                                     initiatePopupWindow();


                                        }
                                    }
        );

        SmallestDisplacement.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   // code de SmallestDisplacement
                                                   choix=3;
                                                   initiatePopupWindow();
                                               }
                                           }
        );

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                                    saveData();

                                                    Toast toast=Toast.makeText(context,"saved!",Toast.LENGTH_SHORT);
                                                    toast.show();
            }
        });
        ralentisseur.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                                    Intent i = new Intent("true_reg");
                                                    LocalBroadcastManager.getInstance(context).sendBroadcast((i));
                                                    Toast toast=Toast.makeText(context,"done!",Toast.LENGTH_SHORT);
                                                    toast.show();
                                    }
        });
    }






 //pop up window "parameters" interval fastestInterval smallestDisplacement

    private void initiatePopupWindow() {
        try {
            LayoutInflater inflater = (LayoutInflater) MainActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.activity_parameters, (ViewGroup) findViewById(R.id.layout));

            final PopupWindow pwindo = new PopupWindow(layout, 900, LinearLayout.LayoutParams.WRAP_CONTENT, true);


            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

            seekBar = layout.findViewById(R.id.seekBar); // make seekBar object
            seekBar.setMax(Max-Min);

            textView =  layout.findViewById(R.id.textView);

            title= layout.findViewById(R.id.title);

            switch (choix){
                case 1:

                    seekBar.setProgress(last1-Min);
                    textView.setText(""+last1);
                    break;

                case 2:
                    title.setText("** FastestInterval **");
                    seekBar.setProgress(last2-Min);
                    textView.setText(""+last2);
                    break;

                case 3:
                    title.setText("** SmallestDisplacement **");
                    seekBar.setProgress(last3-Min);
                    textView.setText(""+last3);
                    break;
            }


            seekBar.setOnSeekBarChangeListener(this);




            termine = layout.findViewById(R.id.termine);
            termine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final SharedPreferences.Editor editor=getSharedPreferences(My_Prefs,MODE_PRIVATE).edit();
                    switch (choix){
                        case 1:
                            editor.putInt("interval",current);
                            last1=current;
                            break;

                        case 2:
                            editor.putInt("FastestInterval",current);
                            last2=current;
                            break;

                        case 3:
                            editor.putInt("SmallestDisplacement",current);
                            last3=current;
                            break;

                    }

                    editor.apply();
                    pwindo.dismiss();

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }







//methode of seek bar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        current=progress+Min;
        textView.setText(""+current);


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }











//permissions of location and storage

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
        File file = new File(context.getExternalFilesDir(""), "Infos " +d.toString().replaceAll("[^a-zA-Z0-9]", " ")+ ".csv");
        return file;
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

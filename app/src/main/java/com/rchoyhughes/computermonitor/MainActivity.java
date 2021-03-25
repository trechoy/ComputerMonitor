package com.rchoyhughes.computermonitor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {

    private TextView TimeView;
    private TextView DateView;

    private CustomGauge CPUGauge;
    private TextView CPUGaugeText;
    private TextView CPUGaugeDescriptor;
    private CustomGauge GPUGauge;
    private TextView GPUGaugeText;
    private TextView GPUGaugeDescriptor;

    private ImageView LogoImage;

    private Handler timeHandler;
    private Handler dataHandler;
    private Handler OSHandler;
    private Handler responsiveHandler;
    private Handler movementHandler;

    private String[] data;
    private int file_num;
    private boolean isUnresponsive;

    private boolean headedRight;
    private boolean headedDown;
    private int x_coordinate;
    private int y_coordinate;
    private static final int X_Shift = 6;
    private static final int Y_Shift = 4;
    private static final int Max_X = 228;
    private static final int Max_Y = 156;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TimeView = (TextView) findViewById(R.id.timeView);
        DateView = (TextView) findViewById(R.id.dateView);
        LogoImage = (ImageView) findViewById(R.id.logoImage);

        data = new String[10];

        CPUGauge = (CustomGauge) findViewById(R.id.gauge1);
        CPUGaugeText = (TextView) findViewById(R.id.gauge1text);
        CPUGaugeDescriptor = (TextView) findViewById(R.id.guage1descriptor);
        GPUGauge = (CustomGauge) findViewById(R.id.gauge2);
        GPUGaugeText = (TextView) findViewById(R.id.gauge2text);
        GPUGaugeDescriptor = (TextView) findViewById(R.id.guage2descriptor);
        /*setGaugeValue(CPUGauge, CPUGaugeText, 57);
        setGaugeValue(GPUGauge, GPUGaugeText, 42);*/
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        file_num = 0;
        isUnresponsive = false;

        dataHandler = new Handler();
        dataRepeatTask();

        timeHandler = new Handler();
        timeRepeatTask();

        OSHandler = new Handler();
        updateOSRepeatTask();

        responsiveHandler = new Handler();
        checkResponsiveRepeatTask();

        headedDown = true;
        headedRight = true;
        x_coordinate = 0;
        y_coordinate = 0;
        movementHandler = new Handler();
        moveUIRepeatTask();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    finish();
                    System.exit(1);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void moveUIRepeatTask() { movementUpdater.run(); }

    Runnable movementUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                moveUI();
            } finally {
                movementHandler.postDelayed(movementUpdater, 59991);
            }
        }
    };

    private void moveUI()
    {
        if ((y_coordinate == Max_Y && headedDown) || (y_coordinate == 0 && !headedDown))
        {
            ViewGroup.MarginLayoutParams gauge_mlp = (ViewGroup.MarginLayoutParams) CPUGauge.getLayoutParams();
            ViewGroup.MarginLayoutParams clock_mlp = (ViewGroup.MarginLayoutParams) TimeView.getLayoutParams();
            // move horizontally
            if (headedRight)
            {
                // move right
                clock_mlp.rightMargin -= X_Shift;
                gauge_mlp.leftMargin += X_Shift;
                x_coordinate += X_Shift;
            }
            else
            {
                // move left
                gauge_mlp.leftMargin -= X_Shift;
                clock_mlp.rightMargin += X_Shift;
                x_coordinate -= X_Shift;
            }
            CPUGauge.setLayoutParams(gauge_mlp);
            TimeView.setLayoutParams(clock_mlp);

            headedDown = !headedDown;

            if (x_coordinate == Max_X && headedRight) {
                headedRight = false;
            }
            else if (x_coordinate == 0 && !headedRight) {
                headedRight = true;
            }
        }
        else
        {
            //move vertically
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) CPUGauge.getLayoutParams();
            if (headedDown)
            {
                // move down
                mlp.topMargin += Y_Shift;
                y_coordinate += Y_Shift;
            }
            else
            {
                // move up
                mlp.topMargin -= Y_Shift;
                y_coordinate -= Y_Shift;
            }
            CPUGauge.setLayoutParams(mlp);
        }
    }


    private void updateOSRepeatTask()
    {
        OSUpdater.run();
    }

    Runnable OSUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                updateOS();
            } finally {
                OSHandler.postDelayed(OSUpdater, 6600);
            }
        }
    };

    private void updateOS()
    {
        if (isUnresponsive)
        {
            hideOSLogo();
        }
        else {
            char OS = data[0].charAt(0);
            LogoImage.setVisibility(View.VISIBLE);
            switch (OS) {
                case 'M':
                    LogoImage.setImageResource(R.drawable.macos);
                    break;
                case 'W':
                    LogoImage.setImageResource(R.drawable.windows);
                    break;
                default:
                    hideOSLogo();
            }
        }
    }


    private void timeRepeatTask() {
        timeUpdater.run();
    }

    Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                updateTime();
            } finally {
                timeHandler.postDelayed(timeUpdater, 700);
            }
        }
    };

    private void updateTime()
    {
        DateFormat tf = new SimpleDateFormat("h:mm a");
        DateFormat df = new SimpleDateFormat("EEE, MMM d");
        Date now = Calendar.getInstance().getTime();
        String timeString = tf.format(now);
        String dateString = df.format(now).toUpperCase();
        TimeView.setText(timeString);
        DateView.setText(dateString);
    }


    private void dataRepeatTask() {
        dataUpdater.run();
    }

    Runnable dataUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                readData();
                onscreenChangeData();
            } finally {
                dataHandler.postDelayed(dataUpdater, 2580);
            }
        }
    };

    private void readData()
    {
        //Find the directory for the SD Card using the API
        //*Don't* hardcode "/sdcard"
        File sdcard = Environment.getExternalStorageDirectory();

        //Get the text file
        File file = new File(sdcard,"data");

        //Read text from file

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                data[i] = line;
                i++;
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            //...or not
            e.printStackTrace();
        }
    }

    private void onscreenChangeData()
    {
        if (isUnresponsive)
        {
            hideGauge(CPUGauge, CPUGaugeText, CPUGaugeDescriptor);
            hideGauge(GPUGauge, GPUGaugeText, GPUGaugeDescriptor);
        }
        else
        {
            int cpuTemp = (int) (Double.parseDouble(data[1]));
            int gpuTemp = (int) (Double.parseDouble(data[2]));
            updateTemps(cpuTemp, gpuTemp);
        }
    }


    private void checkResponsiveRepeatTask() {
        isResponsiveUpdater.run();
    }

    Runnable isResponsiveUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                checkResponsive();
            } finally {
                responsiveHandler.postDelayed(isResponsiveUpdater, 5300);
            }
        }
    };

    private void checkResponsive()
    {
        //try {
        int newFileNum = Integer.parseInt(data[3]);
        isUnresponsive = newFileNum == file_num;
        file_num = newFileNum;
        /*} catch (NumberFormatException e) {
            file_num++;
        }*/
    }


    private void setGaugeValue(CustomGauge Gauge, TextView GaugeText, TextView GaugeDescriptor, int val)
    {
        Gauge.setValue(val);
        GaugeText.setText(Integer.toString(val) + "°");
        Gauge.setVisibility(View.VISIBLE);
        GaugeText.setVisibility(View.VISIBLE);
        GaugeDescriptor.setVisibility(View.VISIBLE);
    }

    private void updateTemps(int cpuTemp, int gpuTemp)
    {
        setGaugeValue(CPUGauge, CPUGaugeText, CPUGaugeDescriptor, cpuTemp);
        setGaugeValue(GPUGauge, GPUGaugeText, GPUGaugeDescriptor, gpuTemp);
    }

    private void hideOSLogo() {
        LogoImage.setVisibility(View.INVISIBLE);
    }

    private void hideGauge(CustomGauge Gauge, TextView GaugeText, TextView GaugeDescriptor){
        Gauge.setVisibility(View.INVISIBLE);
        GaugeText.setVisibility(View.INVISIBLE);
        GaugeDescriptor.setVisibility(View.INVISIBLE);
    }

}

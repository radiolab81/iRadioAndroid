package com.example.iradioandroid;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iradioandroid.displayd.displayd;
import com.example.iradioandroid.displayd.displaydCassette;
import com.example.iradioandroid.displayd.displaydCassetteVideoAnimated;
import com.example.iradioandroid.displayd.displaydRadioAndTV;
import com.example.iradioandroid.displayd.displaydRadioAndTVAndWebSDR;
import com.example.iradioandroid.displayd.displaydRoundScale;
import com.example.iradioandroid.displayd.displaydSkaleMagischesAuge;
import com.example.iradioandroid.gpiod.gpiodSerialOTG;
import com.example.iradioandroid.gpiod.gpiodSerialOTG_magiceye_support;
import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;
import com.example.iradioandroid.iRadioSDR.iRadioKiwiSDRPlayer;
import com.example.iradioandroid.iRadioSDR.iRadioWebSDRPlayer;
import com.example.iradioandroid.noised.noised;

public class iRadioStartup extends AppCompatActivity {
    
    public static final boolean WAIT_UNTIL_RADIO_DIAL_STOPS = false;
    public static final boolean AUTOMATIC_NEXT_STATION_OR_TRACK_WHEN_PLAYBACK_IS_DONE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setVisible(false);
        //setContentView(R.layout.activity_main);
        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[ ]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        // transfer playlist.m3u from PC to Android device via USB: adb push playlist.m3u /sdcard/Download

        /******************************************************************************************************************/
        /* start all Services and Activities for iRadioAndroid here, like rc.local in iRadio on Raspi
        /******************************************************************************************************************/

        // start player service
        startService(new Intent( this, iRadioPlayer.class ) );

        // start displayd as Activity
        Intent displayd_intent = new Intent(this, displayd.class);
        //Intent displayd_intent = new Intent(this, displaydCassette.class);
        //Intent displayd_intent = new Intent(this, displaydCassetteVideoAnimated.class);
        //Intent displayd_intent = new Intent(this, displaydRoundScale.class);
        //Intent displayd_intent = new Intent(this, displaydSkaleMagischesAuge.class);
        //Intent displayd_intent = new Intent(this, displaydRadioAndTV.class);
        //Intent displayd_intent = new Intent(this, displaydRadioAndTVAndWebSDR.class);
        startActivity(displayd_intent);

        // start gpiod service
        startService(new Intent( this, gpiodSerialOTG.class ) );
        //startService(new Intent( this, gpiodSerialOTG_magiceye_support.class ) );

        // start noised service
        //startService(new Intent( this, noised.class ) );

        // start WebSDRPlayer Service
        startService(new Intent(this, iRadioWebSDRPlayer.class));
        // start KiwiSDRPlayer Service
        startService(new Intent(this, iRadioKiwiSDRPlayer.class));

    }

}

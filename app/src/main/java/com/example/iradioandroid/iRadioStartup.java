package com.example.iradioandroid;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iradioandroid.displayd.displayd;
import com.example.iradioandroid.displayd.displaydCassette;
import com.example.iradioandroid.displayd.displaydRoundScale;
import com.example.iradioandroid.gpiod.gpiodSerialOTG;
import com.example.iradioandroid.gpiod.gpiodSerialOTG_magiceye_support;
import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;

public class iRadioStartup extends AppCompatActivity {

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
        //Intent displayd_intent = new Intent(this, displaydRoundScale.class);
        startActivity(displayd_intent);

        // start gpiod service
        startService(new Intent( this, gpiodSerialOTG.class ) );
        //startService(new Intent( this, gpiodSerialOTG_magiceye_support.class ) );

    }

}

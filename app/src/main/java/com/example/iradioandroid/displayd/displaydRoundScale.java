package com.example.iradioandroid.displayd;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.iradioandroid.R;
import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;

import java.util.Timer;
import java.util.TimerTask;

public class displaydRoundScale extends Activity {
    private static final String TAG = "displayd";

    private final int ZEIGERANSCHLAG_LINKS = 10;  // in Grad wg. Rotation
    private final int ZEIGERANSCHLAG_RECHTS = 170; // in Grad wg. Rotation

    iRadioPlayer iRadioPlayerService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displaydroundscale);

        // erzwinge Querformat
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Zeiger fuer Rundscale
        ImageView imageZeiger = (ImageView) findViewById(R.id.imageZeiger_r);
        imageZeiger.bringToFront();

        // "unsichtbare" Buttons für Playersteuerung am Display
        Button button_nextPrg = (Button) findViewById(R.id.btn_nextPrg);
        button_nextPrg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iRadioPlayerService.nextProg();
            }
        });

        Button button_prevPrg = (Button) findViewById(R.id.btn_prevPrg);
        button_prevPrg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iRadioPlayerService.prevProg();
            }
        });

        // schaue zyklisch nach Programmumschaltungen und zeige bei Umschaltung einen OSD-Text an.
        Timer timerOSD = new Timer();
        TimerTask timerOSDTaskInstance = new TimerTask() {
            private int oldChNum, nowChNum = 0;
            int rotationswinkel=ZEIGERANSCHLAG_LINKS;

            public void run() {
                //perform your action here
                if (mBound) {
                    int SENDERABSTAND = (ZEIGERANSCHLAG_RECHTS-ZEIGERANSCHLAG_LINKS) / (iRadioPlayerService.getNumberOfChannelsInPlaylist()-1);
                    nowChNum = iRadioPlayerService.getActualChannelNo();
                    runOnUiThread(new Runnable() { @Override
                    public void run() { imageZeiger.setRotation(rotationswinkel); } });

                    // program switched? move dial
                    if (nowChNum != oldChNum) {
                        while ( rotationswinkel != (SENDERABSTAND*nowChNum) + ZEIGERANSCHLAG_LINKS ) {
                            if (rotationswinkel > (SENDERABSTAND * nowChNum) + ZEIGERANSCHLAG_LINKS) {
                                rotationswinkel--;
                            }

                            if (rotationswinkel < (SENDERABSTAND * nowChNum) + ZEIGERANSCHLAG_LINKS) {
                                rotationswinkel++;
                            }

                            runOnUiThread(new Runnable() { @Override
                            public void run() { imageZeiger.setRotation(rotationswinkel); } });

                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        runOnUiThread(() -> Toast.makeText(displaydRoundScale.this, iRadioPlayerService.getActualChannelNo() + " : " +
                                iRadioPlayerService.getPlayerURL(), Toast.LENGTH_SHORT).show());

                        Log.i(TAG, "displayd heartbeat");
                        oldChNum = nowChNum;
                    }
                }
            }
        };
        timerOSD.schedule(timerOSDTaskInstance, 1000, 1000);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService.
        Intent intent = new Intent(this, iRadioPlayer.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    /** Defines callbacks for service binding, passed to bindService(). */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            iRadioPlayer.LocalBinder binder = (iRadioPlayer.LocalBinder) service;
            iRadioPlayerService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}

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

public class displaydCassette extends Activity {
    private static final String TAG = "displayd";

    iRadioPlayer iRadioPlayerService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displaydcassette);

        // erzwinge Querformat
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Spulen + manuelle Positionierung [Settings im UI-Builder werden überschrieben] je nach Displaygröße/Auflösung
        ImageView imageSpule_links = (ImageView) findViewById(R.id.imageSpule);
        ImageView imageSpule_rechts = (ImageView) findViewById(R.id.imageSpule2);
        imageSpule_links.setX(305); imageSpule_links.setY(235);
        imageSpule_rechts.setX(832); imageSpule_rechts.setY(235);

        // "unsichtbare" Buttons für Playersteuerung am Display
        Button button_nextPrg = (Button) findViewById(R.id.btn_nextPrg);
        button_nextPrg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iRadioPlayerService.nextProg();
                String num = iRadioPlayerService.getPlayerURL();
                Toast.makeText(iRadioPlayerService, iRadioPlayerService.getActualChannelNo() + " : " + num, Toast.LENGTH_LONG).show();
            }
        });

        Button button_prevPrg = (Button) findViewById(R.id.btn_prevPrg);
        button_prevPrg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iRadioPlayerService.prevProg();
                String num = iRadioPlayerService.getPlayerURL();
                Toast.makeText(iRadioPlayerService, iRadioPlayerService.getActualChannelNo() + " : " + num, Toast.LENGTH_LONG).show();
            }
        });


        // Spulenrotation der Cassette durch Timer gesteuert
        Timer timerInstance = new Timer();
        TimerTask timerTaskInstance = new TimerTask() {
            float rot = 0;
            public void run() {
                //perform your action here
                runOnUiThread(new Runnable() { @Override
                public void run() {
                       imageSpule_links.setRotation(rot);
                       imageSpule_rechts.setRotation(rot);
                } });
                rot = rot + 1;
                if (rot == 359)
                    rot = 0;
            }
        };
        timerInstance.schedule(timerTaskInstance, 1000, 20);

        // schaue zyklisch nach Programmumschaltungen und zeige bei Umschaltung einen OSD-Text an.
        Timer timerOSD = new Timer();
        TimerTask timerOSDTaskInstance = new TimerTask() {
            private int oldChNum, nowChNum = 0;

            public void run() {
                //perform your action here
                if (mBound) {
                    nowChNum = iRadioPlayerService.getActualChannelNo();

                    // program switched? move dial
                    if (nowChNum != oldChNum) {

                        runOnUiThread(() -> Toast.makeText(displaydCassette.this, iRadioPlayerService.getActualChannelNo() + " : " +
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

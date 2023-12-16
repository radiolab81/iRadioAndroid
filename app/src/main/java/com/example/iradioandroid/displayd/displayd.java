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

public class displayd extends Activity {
    private static final String TAG = "displayd";

    private final int ZEIGERANSCHLAG_LINKS = 160;
    private final int ZEIGERANSCHLAG_RECHTS = 1150;

    iRadioPlayer iRadioPlayerService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displayd);

        // erzwinge Querformat
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Skalenzeiger
        ImageView imageZeiger = (ImageView) findViewById(R.id.imageZeiger);
        imageZeiger.setX(ZEIGERANSCHLAG_LINKS);

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


        // schaue zyklisch nach Programmumschaltungen und schiebe dann den Skalenzeiger auf die neue Position
        Timer timerInstance = new Timer();
        TimerTask timerTaskInstance = new TimerTask() {
            private int oldChNum, nowChNum = 0;
            private int oldDialPos, toDialPos, delta = 0;

            public void run() {
                //perform your action here
                if (mBound) {
                    int SENDERABSTAND = (ZEIGERANSCHLAG_RECHTS-ZEIGERANSCHLAG_LINKS) / (iRadioPlayerService.getNumberOfChannelsInPlaylist()-1);
                    nowChNum = iRadioPlayerService.getActualChannelNo();

                    // program switched? move dial
                    if (nowChNum != oldChNum) {

                        runOnUiThread(() -> Toast.makeText(displayd.this, iRadioPlayerService.getActualChannelNo() + " : " +
                                iRadioPlayerService.getPlayerURL(), Toast.LENGTH_SHORT).show());

                        toDialPos = ZEIGERANSCHLAG_LINKS + nowChNum * SENDERABSTAND;
                        oldDialPos = ZEIGERANSCHLAG_LINKS + oldChNum * SENDERABSTAND;
                        delta = oldDialPos - toDialPos;

                        if (delta < 0) {
                            while (imageZeiger.getX() != (ZEIGERANSCHLAG_LINKS + iRadioPlayerService.getActualChannelNo() * SENDERABSTAND)) {
                                runOnUiThread(new Runnable() { @Override
                                public void run() {
                                    imageZeiger.setX(imageZeiger.getX() + 1);
                                } });
                                if ((imageZeiger.getX() < ZEIGERANSCHLAG_LINKS) || (imageZeiger.getX() > ZEIGERANSCHLAG_RECHTS)) {
                                    Log.w(TAG, "Limits ZEIGERANSCHLAG_LINKS/RECHTS erreicht!");
                                    break;
                                }
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        if (delta > 0) {
                            while (imageZeiger.getX() != (ZEIGERANSCHLAG_LINKS + iRadioPlayerService.getActualChannelNo() * SENDERABSTAND)) {
                                runOnUiThread(new Runnable() { @Override
                                public void run() {
                                    imageZeiger.setX(imageZeiger.getX() - 1);
                                } });
                                if ((imageZeiger.getX() < ZEIGERANSCHLAG_LINKS) || (imageZeiger.getX() > ZEIGERANSCHLAG_RECHTS)) {
                                    Log.w(TAG, "Limits ZEIGERANSCHLAG_LINKS/RECHTS erreicht!");
                                    break;
                                }
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                    }
                    Log.i(TAG, "displayd heartbeat");
                    oldChNum = nowChNum;
                }
            }
        };
        timerInstance.schedule(timerTaskInstance, 1000, 1000);

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

package com.example.iradioandroid.displayd;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.iradioandroid.R;
import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;
import com.example.iradioandroid.iRadioStartup;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class displaydCassetteVideoAnimated extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "displayd";
    iRadioPlayer iRadioPlayerService;
    boolean mBound = false;

    private MediaPlayer CassetteSimPlayer = null;
    // videowall
    private SurfaceHolder bg_videoHolder = null;
    private SurfaceView bg_videoSurface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displaydcassettevideoanimated);

        // erzwinge Querformat
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Mediaplayer für Cassettensimulation
        CassetteSimPlayer = new MediaPlayer();
        bg_videoSurface = findViewById(R.id.surfaceView);

        try {
            CassetteSimPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cassetteanimated2));
            CassetteSimPlayer.setLooping(true);

            bg_videoHolder = bg_videoSurface.getHolder();
            if (bg_videoHolder != null) {
                bg_videoHolder.addCallback(this);
            }

            CassetteSimPlayer.prepare();
        } catch (IOException e) {
            CassetteSimPlayer.stop();
            CassetteSimPlayer.reset();
        }

        // "unsichtbare" Buttons für Playersteuerung am Display
        Button button_nextPrg = findViewById(R.id.btn_nextPrg);
        button_nextPrg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iRadioPlayerService.nextProg();
            }
        });

        Button button_prevPrg = findViewById(R.id.btn_prevPrg);
        button_prevPrg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                iRadioPlayerService.prevProg();
            }
        });


        // schaue zyklisch nach Programmumschaltungen und zeige bei Umschaltung einen OSD-Text an.
        Timer timerOSD = new Timer();
        TimerTask timerOSDTaskInstance = new TimerTask() {
            private int oldChNum, nowChNum = 0;

            public void run() {
                //perform your action here
                if (mBound) {
                    nowChNum = iRadioPlayerService.getActualChannelNo();

                    // program switched?
                    if (nowChNum != oldChNum) {

                        // for API23+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // simulate visual fast forward until next station starts playing
                            if (iRadioStartup.WAIT_UNTIL_RADIO_DIAL_STOPS) {
                                CassetteSimPlayer.setPlaybackParams(CassetteSimPlayer.getPlaybackParams().setSpeed(8f));
                                iRadioPlayerService.startPlayer();
                                int timeout = 0;
                                while ((!iRadioPlayerService.isPlaying()) && (timeout < 100)) {
                                    try {
                                        Thread.sleep(100);
                                        timeout++;
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                CassetteSimPlayer.setPlaybackParams(CassetteSimPlayer.getPlaybackParams().setSpeed(1f));
                            }
                        } else { // < API23: no ff, just start player
                            if (iRadioStartup.WAIT_UNTIL_RADIO_DIAL_STOPS) {
                                iRadioPlayerService.startPlayer();
                            }
                        }

                        runOnUiThread(() -> Toast.makeText(displaydCassetteVideoAnimated.this, iRadioPlayerService.getActualChannelNo() + " : " +
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

    @Override
    public void onDestroy() {
        // cleanup
        if (CassetteSimPlayer != null) {
            if (CassetteSimPlayer.isPlaying()) {
                CassetteSimPlayer.stop();
            }
            bg_videoHolder.removeCallback(this);
            CassetteSimPlayer.release();
            CassetteSimPlayer = null;
        }
        super.onDestroy();
    }


    /**
     * Defines callbacks for service binding, passed to bindService().
     */
    private final ServiceConnection connection = new ServiceConnection() {
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


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        CassetteSimPlayer.setDisplay(surfaceHolder);
        CassetteSimPlayer.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }
}

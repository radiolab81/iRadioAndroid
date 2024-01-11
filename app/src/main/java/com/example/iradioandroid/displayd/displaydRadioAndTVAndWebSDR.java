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
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.iradioandroid.R;
import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;
import com.example.iradioandroid.iRadioSDR.SDRControlView;
import com.example.iradioandroid.iRadioSDR.iRadioKiwiSDRPlayer;
import com.example.iradioandroid.iRadioSDR.iRadioWebSDRPlayer;
import com.example.iradioandroid.iRadioStartup;

import java.util.Timer;
import java.util.TimerTask;

public class displaydRadioAndTVAndWebSDR extends Activity {
    private static final String TAG = "displayd";
    private final int ZEIGERANSCHLAG_LINKS = 140;
    private final int ZEIGERANSCHLAG_RECHTS = 1350;

    public enum Modes {
        IRADIO, SDR
    }

    Modes mMode = Modes.IRADIO;


    public enum SDR_Type {
        WEBSDR, KIWISDR
    }

    SDR_Type sdrType = SDR_Type.WEBSDR;

    iRadioPlayer iRadioPlayerService;
    boolean mBound = false;
    SurfaceView videoSurface = null;



    iRadioWebSDRPlayer iRadioWebSDRPlayerService;
    boolean mBoundToWebSDRPlayer = false;

    iRadioKiwiSDRPlayer iRadioKiwiSDRPlayerService;
    boolean mBoundToKiwiSDRPlayer = false;



    WebView webViewSDRPlayer = null; // this is the view for the webbrowser running the websdr site
    SDRControlView sdrControlView = null; // this is the overlay control panel for SDRs like WebSDR, KiwiSDR, ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displaydradioandtvandwebsdr);

        // erzwinge Querformat
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Skalenzeiger
        ImageView imageZeiger = (ImageView) findViewById(R.id.imageZeiger);
        imageZeiger.setX(ZEIGERANSCHLAG_LINKS);

        // Videooberfläche
        videoSurface = findViewById(R.id.surfaceView);
        // will be given to iRadioPlayer after binding service (onServiceConnected)

        // (invisible) view to running WebSDR, KiwiSDR
        webViewSDRPlayer = findViewById(R.id.webViewSDRPlayer);

        // "unsichtbare" Buttons für Playersteuerung am Display
        Button button_nextPrg = (Button) findViewById(R.id.btn_nextPrg);
        button_nextPrg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mMode == Modes.IRADIO) {
                    iRadioPlayerService.nextProg();
                }
            }
        });

        Button button_prevPrg = (Button) findViewById(R.id.btn_prevPrg);
        button_prevPrg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mMode == Modes.IRADIO) {
                    iRadioPlayerService.prevProg();
                }
            }
        });

        Button button_swMode = (Button) findViewById(R.id.btn_swMode);
        button_swMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (mMode) {
                    case IRADIO: { // from Internetradio/TV-mode to  SDR mode
                        mMode = Modes.SDR;
                        // here you dont need next/prex-Btn from IRADIO
                        button_nextPrg.setEnabled(false);
                        button_prevPrg.setEnabled(false);

                        sdrControlView.setAlpha(1f);
                        sdrControlView.setVisibility(View.VISIBLE);

                        iRadioPlayerService.pausePlayer();

                        if (sdrType == SDR_Type.WEBSDR) {
                            if (sdrControlView != null) {
                                sdrControlView.setSDRPlayerService(iRadioWebSDRPlayerService);
                            }
                            iRadioWebSDRPlayerService.startPlayer();
                        }

                        if (sdrType == SDR_Type.KIWISDR) {
                            if (sdrControlView != null) {
                                sdrControlView.setSDRPlayerService(iRadioKiwiSDRPlayerService);
                            }
                            iRadioKiwiSDRPlayerService.startPlayer();
                        }
                        break;
                    }

                    case SDR: {  // from SDR mode to Internetradio/TV-mode
                        mMode = Modes.IRADIO;

                        button_nextPrg.setEnabled(true);
                        button_prevPrg.setEnabled(true);

                        //here you dont need the SDRControlView in this mode
                        sdrControlView.setAlpha(0f);

                        if (sdrType == SDR_Type.WEBSDR) {
                            iRadioWebSDRPlayerService.stopPlayer();
                        }

                        if (sdrType == SDR_Type.KIWISDR) {
                            iRadioKiwiSDRPlayerService.stopPlayer();
                        }


                        iRadioPlayerService.startPlayer();
                        break;
                    }
                }
            }
        });

        // control panel for SDR mode, invisible because App starting within IRADIO mode
        sdrControlView = findViewById(R.id.SDRControlView);
        sdrControlView.setEnabled(false);
        sdrControlView.setVisibility(View.INVISIBLE);

        // schaue zyklisch nach Programmumschaltungen und schiebe dann den Skalenzeiger auf die neue Position
        Timer timerInstance = new Timer();
        TimerTask timerTaskInstance = new TimerTask() {
            private int oldChNum, nowChNum = 0;
            private int oldDialPos, toDialPos, delta = 0;

            public void run() {
                //perform your action here
                if (mBound) {
                    // tv or radio service?
                    if ((iRadioPlayerService.isVideoStream()) && (mMode == Modes.IRADIO)) {
                        runOnUiThread(() -> videoSurface.setAlpha(1f));
                        runOnUiThread(() -> videoSurface.setAlpha(1f));
                    } else {
                        runOnUiThread(() -> videoSurface.setAlpha(0f));
                    }

                    if (mMode == Modes.SDR) {
                        // fix: läuft noch ein prepareAsync des Mediaplayers während des Umschaltens auf SDR-Mode, muss der Mediaplayer nachträglich pausiert werden
                        iRadioPlayerService.pausePlayer();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // SDR frequency to dial position
                                if (sdrType == SDR_Type.WEBSDR) {
                                    imageZeiger.setX(SDRFreq2DialPos(iRadioWebSDRPlayerService.getFrequency()));
                                }

                                if (sdrType == SDR_Type.KIWISDR) {
                                    imageZeiger.setX(SDRFreq2DialPos(iRadioKiwiSDRPlayerService.getFrequency()));
                                }
                            }
                        });

                    }

                    int SENDERABSTAND = (ZEIGERANSCHLAG_RECHTS - ZEIGERANSCHLAG_LINKS) / (iRadioPlayerService.getNumberOfChannelsInPlaylist() - 1);
                    nowChNum = iRadioPlayerService.getActualChannelNo();

                    // program switched? move dial
                    if (nowChNum != oldChNum) {
                        runOnUiThread(() -> Toast.makeText(displaydRadioAndTVAndWebSDR.this, iRadioPlayerService.getActualChannelNo() + " : " +
                                iRadioPlayerService.getPlayerURL(), Toast.LENGTH_SHORT).show());

                        toDialPos = ZEIGERANSCHLAG_LINKS + nowChNum * SENDERABSTAND;
                        oldDialPos = ZEIGERANSCHLAG_LINKS + oldChNum * SENDERABSTAND;
                        delta = oldDialPos - toDialPos;

                        if (delta < 0) {
                            while (oldDialPos != toDialPos) {
                                oldDialPos++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageZeiger.setX(oldDialPos);
                                    }
                                });

                                if ((oldDialPos < ZEIGERANSCHLAG_LINKS) || (oldDialPos > ZEIGERANSCHLAG_RECHTS)) {
                                    Log.w(TAG, "Limits ZEIGERANSCHLAG_LINKS/RECHTS erreicht!");
                                    break;
                                }
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (iRadioStartup.WAIT_UNTIL_RADIO_DIAL_STOPS) {
                                iRadioPlayerService.startPlayer();
                            }
                        }

                        if (delta > 0) {
                            while (oldDialPos != toDialPos) {
                                oldDialPos--;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageZeiger.setX(oldDialPos);
                                    }
                                });
                                if ((oldDialPos < ZEIGERANSCHLAG_LINKS) || (oldDialPos > ZEIGERANSCHLAG_RECHTS)) {
                                    Log.w(TAG, "Limits ZEIGERANSCHLAG_LINKS/RECHTS erreicht!");
                                    break;
                                }
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (iRadioStartup.WAIT_UNTIL_RADIO_DIAL_STOPS) {
                                if (mMode == Modes.IRADIO) {
                                    iRadioPlayerService.startPlayer();
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

    private int SDRFreq2DialPos(int freq) {
        int f_marker[] = {0, 550, 600, 650, 700, 800, 900, 1100, 1300, 1500, 1700, 1701, 5900, 6000, 7000, 8000, 10000, 12000, 14000, 16000, 18001, 99999};
        double scale_marker[] = {0.916, 0.916, 0.771, 0.697, 0.642, 0.554, 0.488, 0.380, 0.292, 0.211, 0.126, 0.916, 0.916, 0.841, 0.692, 0.597, 0.467, 0.364, 0.278, 0.202, 0.107, 0.107};

        int lower_i_f_marker = 0;
        for (int i = 0; i < f_marker.length - 1; i++) {
            // find needle position in array -> get index
            if (f_marker[i] <= freq) {
                lower_i_f_marker = i;
            } else {
                // calc needle position between two markers or outbound and map to x-screen coordinate
                int delta_f_marker = f_marker[i] - f_marker[lower_i_f_marker];
                double ratio_to_freq = (f_marker[lower_i_f_marker] - freq) / (double) delta_f_marker;

                double delta_scale_marker = scale_marker[lower_i_f_marker] - scale_marker[i];
                double ratio_scale = (ratio_to_freq * delta_scale_marker) + scale_marker[lower_i_f_marker];

                //Log.i("TAG", "Df=" + delta_f_marker + " Rtf=" + ratio_to_freq + " Ds=" + delta_scale_marker + "Rs=" + ratio_scale);
                // return needle x-position mapped to photo width
                return (int) (((ImageView) findViewById(R.id.imageView_bg)).getWidth() * ((ImageView) findViewById(R.id.imageView_bg)).getScaleX() * ratio_scale);
            }
        }

        return 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to Internetradio/TV Service.
        Intent intent = new Intent(this, iRadioPlayer.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // Bind to WebSDR Service.
        Intent intentWebSDRPlayer = new Intent(this, iRadioWebSDRPlayer.class);
        bindService(intentWebSDRPlayer, connectionWebSDRPlayer, Context.BIND_AUTO_CREATE);

        // Bind to WebSDR Service.
        Intent intentKiwiSDRPlayer = new Intent(this, iRadioKiwiSDRPlayer.class);
        bindService(intentKiwiSDRPlayer, connectionKiwiSDRPlayer, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;

        unbindService(connectionWebSDRPlayer);
        mBoundToWebSDRPlayer = false;

        unbindService(connectionKiwiSDRPlayer);
        mBoundToKiwiSDRPlayer = false;
    }

    /**
     * Defines callbacks for service binding, passed to bindService().
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            iRadioPlayer.LocalBinder binder = (iRadioPlayer.LocalBinder) service;
            iRadioPlayerService = binder.getService();
            mBound = true;

            // after binding to iRadioPlayer service, set video wall to player
            if (videoSurface != null) {
                iRadioPlayerService.setVideoSurface(videoSurface);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    /**
     * Defines callbacks for service binding, passed to bindService().
     */
    private ServiceConnection connectionWebSDRPlayer = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            iRadioWebSDRPlayer.LocalBinder binder = (iRadioWebSDRPlayer.LocalBinder) service;
            iRadioWebSDRPlayerService = binder.getService();
            mBoundToWebSDRPlayer = true;

            // after binding to WebSDRPlayer service, set view for a web browser
            if (webViewSDRPlayer != null) {
                iRadioWebSDRPlayerService.setWebView(webViewSDRPlayer);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundToWebSDRPlayer = false;
        }
    };


    /**
     * Defines callbacks for service binding, passed to bindService().
     */
    private ServiceConnection connectionKiwiSDRPlayer = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            iRadioKiwiSDRPlayer.LocalBinder binder = (iRadioKiwiSDRPlayer.LocalBinder) service;
            iRadioKiwiSDRPlayerService = binder.getService();
            mBoundToKiwiSDRPlayer = true;

            // after binding to WebSDRPlayer service, set view for a web browser
            if (webViewSDRPlayer != null) {
                iRadioKiwiSDRPlayerService.setWebView(webViewSDRPlayer);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundToKiwiSDRPlayer = false;
        }
    };
}

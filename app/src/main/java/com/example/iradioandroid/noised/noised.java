package com.example.iradioandroid.noised;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.example.iradioandroid.R;
import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class noised extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "noisedPlayer";

    // declaring object of MediaPlayer for noise playback
    private MediaPlayer noisePlayer = null;

    // bind to iRadioPlayer to check if webradio is running
    iRadioPlayer iRadioPlayerService;

    boolean mBound = false; // bound to iRadioPlayer ?

    @Override
    // execution of service will start
    // on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Bind to LocalService.
        Intent intentConnectToPlayer = new Intent(this, iRadioPlayer.class);
        bindService(intentConnectToPlayer, connection, Context.BIND_AUTO_CREATE);

        // create a mediaplayer for streaming audio
        Log.i(TAG, "initialize noisePlayer");
        noisePlayer = new MediaPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            noisePlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        }
        noisePlayer.setLooping(true);

        // set URL to noise file in res.raw folder, buffering in async process, calling onPrepared when ready to play
        try {
            noisePlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tuning));
            noisePlayer.setOnPreparedListener(this);
            noisePlayer.setOnErrorListener(this);
            noisePlayer.prepareAsync();
        } catch (IOException e) {
            noisePlayer.stop();
            noisePlayer.reset();
            Log.w(TAG, e.toString() + "noisePlayer reset");
        }

        // watching continuously for iPlayerService is playing, if not start noise playback
        Timer timerInstance = new Timer();
        TimerTask timerTaskInstance = new TimerTask() {
            @Override
            public void run() {
                if (mBound) {
                    if (!iRadioPlayerService.isPlaying()) {
                        startPlayer(); // play noise
                    } else {
                        pausePlayer(); // pause noise
                    }
                }
            }
        };
        timerInstance.schedule(timerTaskInstance, 1000, 250);
        // returns the status
        // of the program
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        // cleanup
        if (noisePlayer != null) {
            noisePlayer.stop();
            noisePlayer.release();
            noisePlayer = null;
        }

        // unbind iRadioPlayerService
        unbindService(connection);
        mBound = false;

        super.onDestroy();
    }


    /**
     * Called when MediaPlayer for noise is ready
     */
    public void onPrepared(MediaPlayer player) {
        Log.i(TAG, "noisePlayer is ready");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        Log.w(TAG, "noisePlayer throw error " + what + " : " + extra);
        mp.reset();
        return true;
    }

    //internal PLAYER CONTROLS here
    private void pausePlayer() {
        if (noisePlayer != null) {
            if (noisePlayer.isPlaying()) {
                noisePlayer.pause();
                Log.i(TAG, "noisePlayer paused");
            }
        }
    }

    private void startPlayer() {
        if (noisePlayer != null) {
            if (!noisePlayer.isPlaying()) {
                noisePlayer.start();
                Log.i(TAG, "starting noisePlayer");
            }
        }
    }


    /**
     * Defines callbacks for service binding, passed to bindService().
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            iRadioPlayer.LocalBinder binder = (iRadioPlayer.LocalBinder) service;
            iRadioPlayerService = binder.getService();
            mBound = true;
            Log.i(TAG, "connection to iRadioPlayerService established");

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

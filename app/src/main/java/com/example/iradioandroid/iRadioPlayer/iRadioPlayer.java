package com.example.iradioandroid.iRadioPlayer;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.example.iradioandroid.iRadioStartup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class iRadioPlayer extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, SurfaceHolder.Callback, MediaPlayer.OnInfoListener {
    private static final String TAG = "iRadioPlayer";

    private boolean firstStartup = true;

    // declaring object of MediaPlayer
    private MediaPlayer mediaPlayer = null;

    // videowall
    SurfaceHolder videoHolder = null;
    SurfaceView videoSurface = null;
    private boolean stream_is_video = false;

    // playlist stuff
    private Vector playlist = new Vector<String>();
    private int channelID_now = 0;

    // stay online even in background
    private WifiManager.WifiLock wifiLock;

    // Binder given to clients.
    private final IBinder binder = new LocalBinder();


    @Override
    // execution of service will start
    // on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "try to read playlist.m3u from folder Download ...");
        readPlaylistFromFile();

        // create a mediaplayer for streaming
        Log.i(TAG, "initialize mediaplayer");
        mediaPlayer = new MediaPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        // stay awake even in background
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // set wifi lock to stay online
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();

        // set 1st URL from (default) playlist, buffering in async process, calling onPrepared when ready to play
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(playlist.firstElement().toString()));
            channelID_now = 0;
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            Log.w(TAG,e.toString() + "mediaplayer resetted");
        }

        // end of stream or mediafile ? -> call onCompletion()
        mediaPlayer.setOnCompletionListener(this);

        // get information from actual stream (is video or just audio? ,...)
        mediaPlayer.setOnInfoListener(this);
        
        // returns the status
        // of the program
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onDestroy() {
        // cleanup
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer=null;
        }

        wifiLock.release();
        playlist.clear();
        super.onDestroy();
    }


    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        Log.i(TAG, "starting mediaplayer");
        if (!iRadioStartup.WAIT_UNTIL_RADIO_DIAL_STOPS) {
            mediaPlayer.start();
        } else {
            if (this.firstStartup) {
                firstStartup = false;
                mediaPlayer.start();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
          // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        Log.w(TAG, "mediaplayer throw error " + what + " : " + extra);
        mp.stop();
        mp.reset();

        // retry to set URL to mediaplayer and prepare again
        if (what == -38 ) {
            Log.i(TAG, "next try open URL :" + playlist.elementAt(channelID_now).toString());
            try {
                mp.setDataSource(getApplicationContext(), Uri.parse(playlist.elementAt(channelID_now).toString()));
                this.firstStartup = true;
                mp.prepareAsync();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer){
        // Whatever you want to do when the audio playback is done...
        if (iRadioStartup.AUTOMATIC_NEXT_STATION_OR_TRACK_WHEN_PLAYBACK_IS_DONE) {
            this.nextProg();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // is actual stream a video or just audio/radio stream?
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
            stream_is_video = true;
            return true;
        }

        return false;
    }

    //PLAYER CONTROLS here
    public void pausePlayer() {
        if (mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Log.i(TAG, "mediaplayer paused");
            }
        }
    }

    public void startPlayer() {
        if (mediaPlayer!=null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                Log.i(TAG, "starting mediaplayer");
            }
        }
    }

    public void nextProg() {
        if (mediaPlayer!=null) {
            if (channelID_now < (playlist.size() - 1)) {
                channelID_now++;
            } else {
                channelID_now = 0;
            }
            switchToChannel(channelID_now);
        }
    }

    public void prevProg() {
        if (mediaPlayer != null) {
            if (channelID_now > 0) {
                channelID_now--;
            } else {
                channelID_now = (playlist.size() - 1);
            }
            switchToChannel(channelID_now);
        }
    }

    public void gotoProg(int channel) {
        if (mediaPlayer!= null) {
            if ((channel > 0 ) && (channel < playlist.size()-1)) {
                channelID_now = channel;
                switchToChannel(channelID_now);
            }
        }
    }

    private boolean switchToChannel(int channel) {
        if (mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                stream_is_video = false; // will be set to true in onInfo, when videostream is detected
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(playlist.elementAt(channelID_now).toString()));
                mediaPlayer.prepareAsync();
                return true;
            } catch (IOException e) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                Log.w(TAG,e.toString() + "mediaplayer resetted");
            }
        }
        return false;
    }

    public String getPlayerURL() {
        return (String) playlist.elementAt(channelID_now);
    }

    public int getActualChannelNo() {
        return channelID_now;
    }

    public int getNumberOfChannelsInPlaylist() {
       return playlist.size();
    }

    public boolean isPlaying() { return mediaPlayer.isPlaying(); }

    public boolean isVideoStream() { return stream_is_video; }


    public void setVideoSurface(SurfaceView videoSurface) {
        if (videoSurface!=null) {
            this.videoSurface = videoSurface;
            videoHolder = this.videoSurface.getHolder();
            if (videoHolder != null) {
                mediaPlayer.setDisplay(videoHolder);
                videoHolder.addCallback(this);
            }
        }
    }

    // Surface Holder Callback methods
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { }



    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public iRadioPlayer getService() {
            // Return this instance of LocalService so clients can call public methods.
            return iRadioPlayer.this;
        }
    }



    //****** read playlist file playlist.m3u from /sdcard/Downloads/ and fill internal list for mediaplayer (Vector<String> playlist)
    //****** if no playlist.m3u is available, fill internal list with some pre-defined URLs
    @SuppressLint("NewApi")
     void readPlaylistFromFile() {
        if ((playlist != null) && (playlist.isEmpty())) {
            FileInputStream is;
            BufferedReader reader;
            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/playlist.m3u");

            try {
                is = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null) {
                    Log.i(TAG, line);
                    playlist.add(line);
                    line = reader.readLine();
                }
                Log.i(TAG, playlist.size() + " stations in playlist now ");

            } catch (java.io.IOException ex) {
                Log.w(TAG, ex.toString());
                Log.w(TAG, "using internal default playlist");
                playlist.add("http://live-icy.gss.dr.dk:8000/A/A04H.mp3");
                playlist.add("http://live-icy.gss.dr.dk:8000/A/A05H.mp3");
                playlist.add("http://live-icy.gss.dr.dk:8000/A/A08H.mp3");
                playlist.add("http://live-icy.gss.dr.dk:8000/A/A25H.mp3");
                playlist.add("http://live-icy.gss.dr.dk:8000/A/A29H.mp3");
                playlist.add("http://live-icy.gss.dr.dk:8000/A/A22H.mp3");
            }
        }
    }

}

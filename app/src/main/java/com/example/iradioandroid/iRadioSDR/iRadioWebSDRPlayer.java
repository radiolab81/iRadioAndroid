package com.example.iradioandroid.iRadioSDR;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;

public class iRadioWebSDRPlayer extends iRadioSDRPlayer {
    private static final String TAG = "iRadioWebSDRPlayer";

    private String URL = "http://websdr.ewi.utwente.nl:8901//?";
    //private String URL = "http://websdr.ewi.utwente.nl:8901//?tune=6150am"; see http://websdr1.kfsdr.com:8901/url_params.html

    private WebView playerWebView = null;
    // view for a web browser to respect PA3FWMs licence and will (access websdr-site just over web browser)

    // Binder given to clients.
    private final IBinder binder = new LocalBinder();


    public void setWebView(WebView wv) {
        this.playerWebView = wv;
    }

    @Override
    public void startPlayer() {
        if (URL.isEmpty()) {
            Toast.makeText(iRadioWebSDRPlayer.this, TAG + " says: no URL to WebSDR defined", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "starting iRadioWebSDRPlayer");
        if (playerWebView != null) {
            playerWebView.clearHistory();
            playerWebView.clearCache(true);
            playerWebView.getSettings().setJavaScriptEnabled(true);
            playerWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            playerWebView.loadUrl(URL+"tune="+getFrequency()+getModulation());
            playerWebView.setVisibility(View.INVISIBLE); // we just want the audio, not the rendered website
        }
    }

    @Override
    public void stopPlayer() {
        if (playerWebView != null) {
            // load a non-existing site to stop playback
            playerWebView.loadUrl("127.0.0.1");
            playerWebView.setVisibility(View.INVISIBLE);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public iRadioWebSDRPlayer getService() {
            // Return this instance of LocalService so clients can call public methods.
            return iRadioWebSDRPlayer.this;
        }
    }

}

package com.example.iradioandroid.iRadioSDR;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;

/* KiwiSDR control parameters
You can add parameters to the end of a Kiwi URL (web link). This is very handy especially with bookmarked
URLs/links.
parameter description
f= set frequency with optional passband, mode and zoom
pbw= or pb= set passband: width (carrier centered) or low,high
pbc= set passband: center using current width or center,width
ext= load extension including optional parameters
mute mute audio
vol= volume: 0 to 200
sp display spectrum
wf= waterfall speed: 0, off, 1, 1hz, s, slow, m, med, f, fast
cmap= waterfall colormap: 0=default, 1=CuteSDR, 2=greyscale, 3=user
sqrt= waterfall contrast: 0=default, 1, 2, 3, 4
no_wf open a no-waterfall / audio-FFT channel on 8 channel Kiwis
winN open multiple windows/tabs with one URL/bookmark
Use the "f=" parameter to tune to a specific frequency when connecting instead of the last value saved in browser
cookies, e.g.
mykiwi/?f=14020 tune to 14200 kHz
mykiwi/?f=7021.3cw 7021.3 kHz and cw mode
mykiwi/?f=19.2cwnz11 19.2 kHz, cwn (narrow) and zoom (11)
mykiwi/?f=1440/12000am 1440 kHz, passband 12 kHz, am
mykiwi/?f=4625/300,3300usb 4625 kHz, passband 300-3300 Hz, usb
mykiwi/?f=100:400 pb 400 Hz center, current width
mykiwi/?f=100:400,100 pb 400 Hz center, 100 Hz width
Passband is /width or /low,high, or :pbc or :pbc,pbw all in Hz
Modes are: am, amn, lsb, usb, cw, cwn, nbfm, iq
Zoom is 0 (max-out) to 14 (max-in)

see -> http://www.la6lu.no/files/LA6LU-KiwiSDR_User_Manual.pdf
 */


public class iRadioKiwiSDRPlayer extends iRadioSDRPlayer {
    private static final String TAG = "iRadioKiwiSDRPlayer";

    private String URL = "";

    private WebView playerWebView = null;

    // Binder given to clients.
    private final IBinder binder = new LocalBinder();


    public void setWebView(WebView wv) {
        this.playerWebView = wv;
    }

    @Override
    public void startPlayer() {
        if (URL.isEmpty()) {
          Toast.makeText(iRadioKiwiSDRPlayer.this, TAG + " says: no URL to KiwiSDR defined", Toast.LENGTH_SHORT).show();
          return;
        }
        Log.i(TAG, "starting iRadioKiwiSDRPlayer");
        if (playerWebView != null) {
            playerWebView.clearHistory();
            playerWebView.clearCache(true);
            playerWebView.getSettings().setJavaScriptEnabled(true);
            playerWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            playerWebView.loadUrl(URL+"?f="+getFrequency()+getModulation()+"&vol=200");
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
        public iRadioKiwiSDRPlayer getService() {
            // Return this instance of LocalService so clients can call public methods.
            return iRadioKiwiSDRPlayer.this;
        }
    }

}

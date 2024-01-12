package com.example.iradioandroid.iRadioSDR;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

abstract class iRadioSDRPlayer extends Service {
    static enum Modulation {
        AM, FM, DRM, LSB, USB, CW
    }

   // private final IBinder binder = new LocalBinder();

    private int frequency = 0;
    private short bandwidth = 0;
    private Modulation modulation;

    public void setFrequency(int f) { frequency = f; }
    public int getFrequency() { return frequency; }

    public void setBandwidth(short bw) { bandwidth = bw; }
    public short getBandwidth() { return bandwidth; }

    public void setModulation(Modulation mod) { modulation = mod; }
    public Modulation getModulation() { return modulation; }

    abstract public void startPlayer();
    abstract public void stopPlayer();

    /*
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
 /*   public class LocalBinder extends Binder {
        public iRadioSDRPlayer getService() {
            // Return this instance of LocalService so clients can call public methods.
            return iRadioSDRPlayer.this;
        }
    }
*/
}

package com.example.iradioandroid.gpiod;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;
import com.hoho.android.usbserial.BuildConfig;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class gpiodSerialOTG extends Service implements SerialInputOutputManager.Listener {
    private static final String TAG = "gpiod";

    private enum UsbPermission { Unknown, Requested, Granted, Denied }
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.LIBRARY_PACKAGE_NAME + ".GRANT_USB";
    private SerialInputOutputManager usbIoManager;

    private UsbSerialPort port;
    private boolean connected = false;

    iRadioPlayer iRadioPlayerService;
    boolean mBound = false;

    @Override
    // execution of service will start
    // on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "gpiod started");

        // Bind to LocalService.
        Intent intentConnectToPlayer = new Intent(this, iRadioPlayer.class);
        bindService(intentConnectToPlayer, connection, Context.BIND_AUTO_CREATE);

        // USB stuff starts here
        if(INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
            usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    ? UsbPermission.Granted : UsbPermission.Denied;
            Log.i(TAG, " try to connect to USB OTG");
            Log.i(TAG, usbPermission.toString());
        }
        // connect to OTG Serial
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Log.e(TAG,"no USB OTG driver detected");
        }
        // Open a connection to the first available driver.
        if (!availableDrivers.isEmpty()) {
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());

            if (connection == null) {
                // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                // request Access rights to a special USB port
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), flags);
                manager.requestPermission(driver.getDevice(), usbPermissionIntent);
            } else {
                port = driver.getPorts().get(0); // Most devices have just one port (port 0)
                try {
                    port.open(connection);
                    port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                } catch (IOException e) {
                    Log.e(TAG,e.toString());
                    Log.e(TAG,"unsupported COM parameters");
                    //throw new RuntimeException(e);
                }
                if (port != null ) {
                    usbIoManager = new SerialInputOutputManager(port, this);
                    usbIoManager.start();
                    Log.i(TAG,"OTG-USB-Serial is running ... waiting for incoming data");
                } else {
                    Log.w(TAG,"OTG-USB-Serial not running!");
                }
            }
        }

        // returns the status
        // of the program
        return START_REDELIVER_INTENT;
    }


    @Override
    // this is the callback when receiving new data over OTG-USB-Serial
    public void onNewData(byte[] data) {
        try {
            String dataUTF8 = new String(data, "UTF-8");
            Log.i(TAG, "Data received : " + dataUTF8 + "size of " + dataUTF8.length()/*data.toString()*/);

            //*************************************************************************************************
            //*** hier die Umsetzung der empfangenen Daten auf die Befehle fuer (zum Beispie) iRadioPlayer ,...
            //*************************************************************************************************
            if (iRadioPlayerService != null) {
                if (dataUTF8.contains("PNX")) {
                    Log.i(TAG, "command P -> nextProgram");
                    iRadioPlayerService.nextProg();
                }
            }

            if (iRadioPlayerService != null) {
                if (dataUTF8.contains("PPR")) {
                    Log.i(TAG, "command PDW -> prevProgram");
                    iRadioPlayerService.prevProg();
                }
            }


            if (iRadioPlayerService != null) {
                if (dataUTF8.contains("VUP")) {
                    ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                }
            }

            if (iRadioPlayerService != null) {
                if (dataUTF8.contains("VDW")) {
                    ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                }
            }


            //*** add commands you need, implement the cmds also your sending device !


        } catch (UnsupportedEncodingException e) {
            Log.w(TAG,"failed to convert data to UTF-8 ");
            //throw new RuntimeException(e);
        }
    }

    @Override
    public void onRunError(Exception e) {
        Log.e(TAG,e.toString());
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
            Log.i(TAG,"connection to iRadioPlayerService established");

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        mBound = false;
        connected = false;

        if(usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        try {
            port.close();
        } catch (IOException ignored) {}
        port = null;
    }

    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

package com.example.iradioandroid.tx;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
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

public class gpiodSerialOTG_inet2RF extends Service implements SerialInputOutputManager.Listener {
    private static final String TAG = "gpiod_inet2RF";

    private static inet2RF rf = null;

    private enum UsbPermission {Unknown, Requested, Granted, Denied}

    private UsbPermission usbPermission = UsbPermission.Unknown;
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.LIBRARY_PACKAGE_NAME + ".GRANT_USB";
    private SerialInputOutputManager usbIoManager;

    private UsbSerialPort port;
    private boolean connected = false;

    iRadioPlayer iRadioPlayerService;
    boolean mBound = false;

    boolean firstRun = true;

    @Override
    // execution of service will start
    // on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "gpiod started");

        // Bind to LocalService.
        Intent intentConnectToPlayer = new Intent(this, iRadioPlayer.class);
        bindService(intentConnectToPlayer, connection, Context.BIND_AUTO_CREATE);

        Intent intentView = new Intent(this, inet2RFView.class);
        intentView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentView);

        // USB stuff starts here
        if (INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
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
            Log.e(TAG, "no USB OTG driver detected");
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
                    port.setDTR(true);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    Log.e(TAG, "unsupported COM parameters");
                    //throw new RuntimeException(e);
                }
                if (port != null) {
                    usbIoManager = new SerialInputOutputManager(port, this);
                    usbIoManager.start();
                    Log.i(TAG, "OTG-USB-Serial is running ... waiting for incoming data");
                } else {
                    Log.w(TAG, "OTG-USB-Serial not running!");
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
            //*** hier die Umsetzung der empfangenen Daten auf die Befehle fuer (zum Beispiel) iRadioPlayer ,...
            //*************************************************************************************************
            if (iRadioPlayerService != null) {
                if (dataUTF8.contains("FRX=")) {
                    Log.i(TAG, "new frequency from counter command");
                    dataUTF8 = dataUTF8.substring(0, new String("FRX=xxxx").length());
                    dataUTF8 = dataUTF8.replace("FRX=", "");
                    dataUTF8 = dataUTF8.replace(" ", "");
                    dataUTF8 = dataUTF8.replace("\n", "");
                    dataUTF8 = dataUTF8.replace("\r", "");

                    try {
                        if (rf != null) {
                            int frequency_RX = Integer.parseInt(dataUTF8);
                            Log.i(TAG, "new frequency from counter is " + frequency_RX + " kHz");

                            int actual_channel = iRadioPlayerService.getActualChannelNo();
                            int channel_by_freq = rf.getChannelNoFromFreq(frequency_RX);

                            // fixing: state 4 problem on asyc after startup
                            if (firstRun) {
                                firstRun = false;
                                while (!iRadioPlayerService.isPlaying()) {
                                    // wait MP service is comming up for sure for the 1st time
                                }
                            }

                            //Log.i(TAG, "actual_channel = " + actual_channel + " channel_by_freq = " + channel_by_freq);
                            if (actual_channel != channel_by_freq) {
                                try {
                                    iRadioPlayerService.gotoProg(channel_by_freq);
                                } catch (IllegalStateException ex) {
                                    Log.i(TAG, "channel switch not yet, we will try again");
                                }

                                if (port.isOpen()) {
                                    String cmd = "FTX=" + rf.getNewTXFrequency(frequency_RX) + '\n';
                                    try {
                                        port.write(cmd.getBytes("UTF-8"), 100);
                                        Log.i(TAG, "sending data ... " + cmd);
                                    } catch (IOException e) {
                                        Log.w(TAG, "error while sending data ...");
                                        //throw new RuntimeException(e);
                                    }
                                }
                            }
                        } // if (rf != null) {

                    } catch (NumberFormatException nfe) {
                        Log.i(TAG, "NumberFormat Exception: invalid frequency string");
                    }
                } //if (dataUTF8.contains("FRX=")) {

            }

        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "failed to convert data to UTF-8 ");
            //throw new RuntimeException(e);
        }
    }

    @Override
    public void onRunError(Exception e) {
        Log.e(TAG, e.toString());
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
            Log.i(TAG, "connection to iRadioPlayerService established");
            rf = new inet2RF(iRadioPlayerService.getNumberOfChannelsInPlaylist());
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

        if (usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        try {
            port.close();
        } catch (IOException ignored) {
        }
        port = null;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

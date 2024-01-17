package com.example.iradioandroid.tx;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.iradioandroid.R;
import com.example.iradioandroid.iRadioPlayer.iRadioPlayer;

import java.util.Timer;
import java.util.TimerTask;

public class inet2RFView extends Activity {
    private static final String TAG = "inet2RFView";
    private final int ZEIGERANSCHLAG_LINKS = 140;
    private final int ZEIGERANSCHLAG_RECHTS = 1350;

    iRadioPlayer iRadioPlayerService;
    boolean mBound = false;

    private DrawingLayer drawingLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inet2rfview);

        // erzwinge Querformat
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Skalenzeiger
        ImageView imageZeiger = (ImageView) findViewById(R.id.imageZeiger);
        imageZeiger.setX(ZEIGERANSCHLAG_RECHTS);

        // Overlay fuer inet2RF
        drawingLayer = findViewById(R.id.drawingLayer);
        drawingLayer.setParent(this);

        // schaue zyklisch nach der Oszillatorfrequenz und setze Skalenzeiger auf die neue Position
        Timer timerInstance = new Timer();
        TimerTask timerTaskInstance = new TimerTask() {
            public void run() {
                //perform your action here
                if (mBound) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (drawingLayer.rf != null)
                                // draw the needle at current frequeny counter input ( +/- intermediate frequency offset is corrected)
                                runOnUiThread(() -> imageZeiger.setX(SDRFreq2DialPos(drawingLayer.rf.getActual_frequency()) - imageZeiger.getWidth() / 2));
                        }
                    });
                } //  if (mBound) {
            } // public void run() {
        };
        timerInstance.schedule(timerTaskInstance, 1000, 50);

    }

    public static class DrawingLayer extends View {
        iRadioPlayer iRadioPlayerService = null;
        inet2RFView outer = null;
        inet2RF rf = null;
        Paint paint = new Paint();

        public DrawingLayer(Context context) {
            super(context);
        }
        public DrawingLayer(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public DrawingLayer(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (this.iRadioPlayerService != null) {
                rf = new inet2RF(iRadioPlayerService.getNumberOfChannelsInPlaylist());

                for (int i = 0; i < iRadioPlayerService.getNumberOfChannelsInPlaylist(); i++) {
                    // zeichne die Fangbereiche der einzelnen Sendefrequenezen
                    int xpos_minus_fangbereich = outer.SDRFreq2DialPos(rf.getMinimalTxFrq() + (rf.getSENDERABSTAND() * i) - rf.getSENDERABSTAND() / 2);
                    int xpos_plus_fangbereich = outer.SDRFreq2DialPos(rf.getMinimalTxFrq() + (rf.getSENDERABSTAND() * i) + rf.getSENDERABSTAND() / 2);

                    //Log.i(TAG, i + " : " + (rf.getMinimalTxFrq() + rf.getSENDERABSTAND()*i) +" kHz : "+ xpos);
                    if (i % 2 == 1)
                        fillRect(canvas, xpos_minus_fangbereich, 10, xpos_plus_fangbereich, getHeight()/2, Color.argb(70, 0, 0, 255));
                    else
                        fillRect(canvas, xpos_minus_fangbereich, 10, xpos_plus_fangbereich, getHeight()/2, Color.argb(70, 255, 0, 0));

                    // zeichne alle moeglichen Sendefrequenzen
                    int xpos = outer.SDRFreq2DialPos(rf.getMinimalTxFrq() + rf.getSENDERABSTAND() * i);
                    fillRect(canvas, xpos - 1, 10, xpos + 1, getHeight()/2, Color.argb(155, 0, 255, 0));

                    // zeichne Marker fuer aktuell aktiven Sender
                    int xpos_TX_marker = outer.SDRFreq2DialPos(rf.getNewTXFrequency(rf.getActual_frequency()));
                    fillRect(canvas, xpos_TX_marker - 10, 10, xpos_TX_marker + 10, 40, Color.argb(255, 255, 255, 255));


                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(15);
                    canvas.drawText("TX",xpos_TX_marker-9,30, paint);

                    paint.setColor(Color.WHITE);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(30);
                    canvas.drawText("IF offset = " + String.valueOf(rf.getIFOffset()) + " kHz",10,getHeight()/2, paint);
                    canvas.drawText("FreqCounter (W/o IF) = " + String.valueOf(rf.getActual_frequency()) + " kHz",10,getHeight()/2 + 30, paint);
                    canvas.drawText("TX frequency = " + String.valueOf(rf.getNewTXFrequency(rf.getActual_frequency())) + " kHz",10,getHeight()/2 + 60, paint);
                    canvas.drawText("Modulation : " + iRadioPlayerService.getActualChannelNo() + " : " + iRadioPlayerService.getPlayerURL(),10,getHeight()/2 + 90, paint);

                }
            }
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float touchX = event.getX();
            float touchY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // do your stuff here
                    break;
                case MotionEvent.ACTION_MOVE:
                    // do your stuff here
                    break;
                case MotionEvent.ACTION_UP:
                    // do your stuff here
                    break;
                default:
                    return false;
            }

            invalidate();
            return true;
        }

        private void fillRect(Canvas canvas, int left, int top, int right, int bottom, int fillcolor) {
            Path path = new Path();
            path.addRect(left, top, right, bottom, Path.Direction.CW);
            path.close();

            Paint fill = new Paint();
            fill.setColor(fillcolor);
            canvas.drawPath(path, fill);
        }

        public void setPlayerService(iRadioPlayer iRadioPlayerService) {
            this.iRadioPlayerService = iRadioPlayerService;
        }

        public void setParent(inet2RFView inet2RFView) {
            this.outer = inet2RFView;
        }
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;

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
            drawingLayer.setPlayerService(iRadioPlayerService);
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}

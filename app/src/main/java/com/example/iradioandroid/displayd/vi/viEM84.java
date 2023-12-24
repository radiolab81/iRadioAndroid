package com.example.iradioandroid.displayd.vi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.iradioandroid.R;
import com.example.iradioandroid.displayd.displayd;

//*******************************************************************************
// Insert to your xml-layout, set dimension and position (x,y)
/*
 <com.example.iradioandroid.displayd.vi.viEM84
        android:id="@+id/imageMagicEye"
        android:layout_width="45px"
        android:layout_height="220px"
        android:visibility="visible"
        android:translationX="10px"
        android:translationY="20px"
        tools:ignore="MissingConstraints"
        tools:visibility="visible" />
*/
public class viEM84 extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = "viEM84";
    Paint paint = new Paint();
    Bitmap bMap = null;

    private int lengthShadow = 0;

    public viEM84(Context context) {
        super(context);
        initVirtualInstrument();
    }

    public viEM84(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVirtualInstrument();
    }

    public viEM84(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVirtualInstrument();
    }

    private void initVirtualInstrument() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bMap = BitmapFactory.decodeResource(getResources(), R.drawable.em84, options);
        bMap = bMap.copy(Bitmap.Config.RGB_565, true);
    }

    public void setNewEyeValue(int newEyeValue) {
        if ((newEyeValue >=0) || (newEyeValue <=this.getHeight()/2)) {
            this.lengthShadow = newEyeValue;
            Log.i(TAG, "new angle value received: " + lengthShadow);
            invalidate();
        }
    }

    private void fillRect(Canvas canvas, int left, int top, int right, int bottom, int fillcolor) {
        Path path = new Path();
        path.addRect(left,top,right,bottom, Path.Direction.CW);
        path.close();

        Paint fill = new Paint();
        fill.setColor(fillcolor);
        canvas.drawPath(path, fill);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bMap != null) {
            canvas.drawBitmap(Bitmap.createScaledBitmap(bMap, canvas.getWidth(), canvas.getHeight(), true), 0, 0, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

            fillRect(canvas, 0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2-lengthShadow, Color.argb(255, 41, 51, 33));
            fillRect(canvas, 0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2+lengthShadow, Color.argb(255, 41, 51, 33));

        } else {
            Log.e(TAG, "cant load bitmap from drawable folder");
        }
        super.onDraw(canvas);
    }
}

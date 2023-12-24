package com.example.iradioandroid.displayd.vi;

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

import com.example.iradioandroid.R;

//*******************************************************************************
// Insert to your xml-layout, set dimension[1:1] and position (x,y)
/*
 <com.example.iradioandroid.displayd.vi.viEM34
        android:id="@+id/imageMagicEye"
        android:layout_width="160px"
        android:layout_height="160px"
        android:visibility="visible"
        android:translationX="10px"
        android:translationY="10px"
        tools:ignore="MissingConstraints"
        tools:visibility="visible" />
*/
public class viEM34 extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = "viEM34";
    Paint paint = new Paint();
    Bitmap bMap = null;
    private int angleEye = 0;

    public viEM34(Context context) {
        super(context);
        initVirtualInstrument();
    }

    public viEM34(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVirtualInstrument();
    }

    public viEM34(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVirtualInstrument();
    }

    private void initVirtualInstrument() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bMap = BitmapFactory.decodeResource(getResources(), R.drawable.em34, options);
        bMap = bMap.copy(Bitmap.Config.RGB_565, true);
    }

    public void setNewEyeValue(int newEyeValue) {
        if ((newEyeValue >=0) || (newEyeValue <=90)) {
            this.angleEye = newEyeValue;
            Log.i(TAG, "new angle value received: " + angleEye);
            invalidate();
        }
    }
    private void fillArc(Canvas canvas, Point center, int inner_radius, int outer_radius, int arc_begin, int arc_size, int fillcolor) {
        RectF outer_rect = new RectF(center.x - outer_radius, center.y - outer_radius, center.x + outer_radius, center.y + outer_radius);
        RectF inner_rect = new RectF(center.x - inner_radius, center.y - inner_radius, center.x + inner_radius, center.y + inner_radius);

        Path path = new Path();
        path.arcTo(outer_rect, arc_begin, arc_size);
        path.arcTo(inner_rect, arc_begin + arc_size, -arc_size);
        path.close();

        Paint fill = new Paint();
        fill.setColor(fillcolor);
        canvas.drawPath(path, fill);

        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(0);
        canvas.drawPath(path, border);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bMap != null) {
            canvas.drawBitmap(Bitmap.createScaledBitmap(bMap, canvas.getWidth(), canvas.getHeight(), true), 0, 0, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

            Point center = new Point(canvas.getWidth() / 2, canvas.getHeight() / 2);
            int inner_radius = (int) (center.x * (55.0 / 120.0));
            int outer_radius = canvas.getWidth() / 2;
            int arc_sweep = 120;
            int arc_ofset = 0;

            //fillArc(canvas, center, inner_radius, outer_radius, arc_ofset, arc_sweep, Color.argb(255, 40, 64, 49));
            fillArc(canvas, center, inner_radius, outer_radius, 0 + angleEye, 180 - (2*angleEye), Color.argb(255, 40, 64, 49));
            fillArc(canvas, center, inner_radius, outer_radius, 180 + angleEye, 180 - (2*angleEye), Color.argb(255, 40, 64, 49));

        } else {
            Log.e(TAG, "cant load bitmap from drawable folder");
        }
        super.onDraw(canvas);
    }
}

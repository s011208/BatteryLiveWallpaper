
package com.bj4.yhh.batterylivewallpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.Surface;
import android.view.WindowManager;

/**
 * @author Yen-Hsun_Huang
 */
public class Battery {
    private static final int VALUE_CONTANT = 25;

    private Context mContext;

    private int mWidth, mHeight;

    private float mBatteryLevel = 0;

    private boolean mIsCharging = false, mIsUsbCharge = false, mIsAcCharge = false;

    private Paint mLevelPaint = new Paint();

    private float mYValue, mXValue;

    private final Path mRectPath = new Path();

    public Battery(Context context) {
        mContext = context.getApplicationContext();
        mLevelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mLevelPaint.setAntiAlias(true);
        mRectPath.setFillType(Path.FillType.EVEN_ODD);
    }

    public void setXYValue(float x, float y) {
        mXValue = x;
        mYValue = y;
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setBatteryStatus(float level, boolean isCharging, boolean isUsb, boolean isAc) {
        mBatteryLevel = level;
        mIsCharging = isCharging;
        mIsUsbCharge = isUsb;
        mIsAcCharge = isAc;
    }

    public void draw(Canvas canvas) {
        final int height = Math.round(mBatteryLevel * mHeight * 0.8f);
        if (mBatteryLevel > 0.15f) {
            mLevelPaint.setColor(Color.argb(60, 0, 240, 10));
        } else if (mBatteryLevel > 0.5f) {
            mLevelPaint.setColor(Color.argb(60, 0, 240, 240));
        } else {
            mLevelPaint.setColor(Color.argb(60, 255, 0, 0));
        }
        if (mYValue != 0 && mXValue != 0) {
            mRectPath.reset();
            final int rotation = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    mRectPath.moveTo(0, height - VALUE_CONTANT * mYValue);
                    mRectPath.lineTo(mWidth, height + VALUE_CONTANT * mYValue);
                    mRectPath.lineTo(mWidth, mHeight);
                    mRectPath.lineTo(0, mHeight);
                    mRectPath.lineTo(0, height - VALUE_CONTANT * mYValue);
                    break;
                case Surface.ROTATION_90:
                    mRectPath.moveTo(0, height + VALUE_CONTANT * mXValue);
                    mRectPath.lineTo(mWidth, height - VALUE_CONTANT * mXValue);
                    mRectPath.lineTo(mWidth, mHeight);
                    mRectPath.lineTo(0, mHeight);
                    mRectPath.lineTo(0, height + VALUE_CONTANT * mXValue);
                    break;
                case Surface.ROTATION_180:
                    mRectPath.moveTo(0, height + VALUE_CONTANT * mYValue);
                    mRectPath.lineTo(mWidth, height - VALUE_CONTANT * mYValue);
                    mRectPath.lineTo(mWidth, mHeight);
                    mRectPath.lineTo(0, mHeight);
                    mRectPath.lineTo(0, height + VALUE_CONTANT * mYValue);
                    break;
                case Surface.ROTATION_270:
                    mRectPath.moveTo(0, height - VALUE_CONTANT * mXValue);
                    mRectPath.lineTo(mWidth, height + VALUE_CONTANT * mXValue);
                    mRectPath.lineTo(mWidth, mHeight);
                    mRectPath.lineTo(0, mHeight);
                    mRectPath.lineTo(0, height - VALUE_CONTANT * mXValue);
                    break;
            }
            mRectPath.close();
            canvas.drawPath(mRectPath, mLevelPaint);
        } else {
            canvas.drawRect(0, height, mWidth, mHeight, mLevelPaint);
        }
    }
}

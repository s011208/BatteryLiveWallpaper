
package com.bj4.yhh.batterylivewallpaper;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * @author Yen-Hsun_Huang
 */
public class BatteryLiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new BatteryLiveWallpaper(this);
    }

    private class BatteryLiveWallpaper extends Engine implements SensorEventListener {
        private boolean mIsVisible = true;

        private ValueAnimator mInternalVa;

        private Context mContext;

        private BitmapDrawable mBackground;

        private SensorManager mSensorManager;

        private Battery mBattery;

        private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processBatteryIntent(intent);
            }
        };

        public BatteryLiveWallpaper(Context context) {
            mContext = context.getApplicationContext();
            // battery
            mBattery = new Battery(mContext);
            loadInfo();
        }

        private final void processBatteryIntent(Intent batteryStatus) {
            // Are we charging / charged?
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;

            // How are we charging?
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            // Determine the Current Battery Level
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float)scale;
            mBattery.setBatteryStatus(batteryPct, isCharging, usbCharge, acCharge);
            mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        }

        private void loadInfo() {
            // background
            mBackground = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
                    R.drawable.tile));
            mBackground.setTileModeXY(TileMode.MIRROR, TileMode.MIRROR);
        }

        private void registerReceiver() {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = mContext.registerReceiver(mBatteryReceiver, ifilter);
            processBatteryIntent(batteryStatus);
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);
        }

        private void unRegisterReceiver() {
            mContext.unregisterReceiver(mBatteryReceiver);
            mSensorManager.unregisterListener(this);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mIsVisible = visible;
            if (visible) {
                mInternalVa.setRepeatCount(ValueAnimator.INFINITE);
                mInternalVa.setRepeatMode(ValueAnimator.REVERSE);
                mInternalVa.start();
                registerReceiver();
            } else {
                unRegisterReceiver();
                mInternalVa.setRepeatCount(0);
                mInternalVa.end();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            mIsVisible = false;
            mInternalVa.setRepeatCount(0);
            mInternalVa.end();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, final int width,
                final int height) {
            mBackground.setBounds(0, 0, width, height);
            mBattery.setSize(width, height);
            mInternalVa = ValueAnimator.ofFloat(1, 4);
            mInternalVa.setDuration(4000);
            mInternalVa.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator va) {
                    draw();
                }
            });
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    if (mIsVisible) {
                        mBackground.draw(canvas);
                        if (mBattery != null) {
                            mBattery.draw(canvas);
                        }
                    }
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mBattery.setXYValue(event.values[1], event.values[0]);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}

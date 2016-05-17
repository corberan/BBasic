package com.liuzc.bbasic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class LayoutSettingActivity extends Activity {

    private SurfaceHolder sfh;

//    private Semaphore semaphore = new Semaphore(0);
    private boolean isOver = false;
    private boolean isChanged = false;

    private int screen_w, screen_h;
    private float center_x = -1, center_y = -1;

    private float oldrate = 1;
    private float rate = 1;
    private float oldLineDistance = 0;
    private boolean isFirstTouch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_setting);

        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.layout_setting_surfaceView);
        sfh = surfaceView.getHolder();
        sfh.addCallback(new displaySurfaceView());
    }

    class displaySurfaceView implements SurfaceHolder.Callback{
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            //Log.e("","surfaceCreated");
//            semaphore.release(1);
            new Thread(drawRunnable).start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            isOver = true;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isFirstTouch = true;
            oldrate = rate;
        } else {
            if (event.getPointerCount() > 1) {
                if (event.getPointerCount() == 2) {
                    if (isFirstTouch){ // 记录第一次触屏时的双指距离
                        oldLineDistance = (float) Math.sqrt(Math.pow(event.getX(1) - event.getX(0), 2) + Math.pow(event.getY(1) - event.getY(0), 2));
                        isFirstTouch = false;
                    }else { // 此后的双指滑动都会在此处理
                        float newLineDistance = (float) Math.sqrt(Math.pow(event.getX(1) - event.getX(0), 2) + Math.pow(event.getY(1) - event.getY(0), 2));
                        rate = oldrate * newLineDistance / oldLineDistance;
//                        semaphore.release(1);
                        isChanged = true;
                    }
                }
            } else if (event.getPointerCount() == 1) {
                float x = event.getX(0);
                float y = event.getY(0);
                if(x> center_x - 100 && x< center_x + 100 && y > center_y - 100 && y < center_y + 100) {
                    center_x = x;
                    center_y = y;
                    if(Math.abs(center_x - screen_w / 2) < 50) center_x = screen_w / 2;
                    if(Math.abs(center_y - screen_h / 2) < 50) center_y = screen_h / 2;
                    isChanged = true;
//                    semaphore.release(1);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            isOver = true;

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            preferences.edit().putFloat("rate",rate).apply();
            preferences.edit().putFloat("center_x",center_x).apply();
            preferences.edit().putFloat("center_y",center_y).apply();

//            Intent data = new Intent();
//            data.putExtra("rate",rate);
//            data.putExtra("center_x",center_x);
//            data.putExtra("center_y",center_y);
//            setResult(RESULT_OK, data);
        }
        return super.onKeyDown(keyCode, event);
    }

    Runnable drawRunnable = new Runnable() {
        @Override
        public void run() {
            //
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            screen_w = dm.widthPixels;
            screen_h = dm.heightPixels;
            //
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            oldrate = rate = preferences.getFloat("rate",1);
            center_x = preferences.getFloat("center_x",screen_w / 2);
            center_y = preferences.getFloat("center_y",screen_h / 2);
            //
            Bitmap vmScreen = Bitmap.createBitmap(240, 320, Bitmap.Config.RGB_565);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(12f);
            textPaint.setAntiAlias(true);

            Canvas canvas = new Canvas(vmScreen);
            canvas.drawColor(Color.BLACK);
            canvas.drawText("这代表虚拟机的屏幕(240x320)，你可以", 10, 12, textPaint);
            canvas.drawText("按住中心拖放到任意位置，也可以双指", 10, 24, textPaint);
            canvas.drawText("缩放到你满意的尺寸。显示的字体模拟", 10, 36, textPaint);
            canvas.drawText("的是小机上的12x12字体。", 10, 48, textPaint);

            isChanged = true;
//            semaphore.release(1);
            //
            while (!isOver){
                if (isChanged) {
                    try {
//                    boolean isDraw = semaphore.tryAcquire(1, 1, TimeUnit.SECONDS);
//                    if (isDraw && !isOver){
                        Canvas sfcanvas = sfh.lockCanvas();
                        if (sfcanvas != null) {
                            sfcanvas.drawColor(0xFFECECED);
                            sfcanvas.save();
                            sfcanvas.scale(rate, rate, 0, 0);
                            sfcanvas.drawBitmap(vmScreen, center_x / rate - 240 / 2.0f, center_y / rate - 320 / 2.0f, null);
                            sfcanvas.restore();
                            sfh.unlockCanvasAndPost(sfcanvas);
                            isChanged = false;
//                        }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
//                }
            }
            //Log.e("","绘图线程结束");
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}

package com.liuzc.bbasic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuzc on 16/5/3.
 */
public class VmSurface extends SurfaceView implements SurfaceHolder.Callback {
    final int SURFACEGOT = -1;
    final int FLIPPAGEGOT = 0;
    final int PRINTGOT = 1;

    private SurfaceHolder surfaceHolder;

    private float nextPrint_x = 0f, nextPrint_y = 0f;
    private float textSize = 12f, textAesent = 11f;
    private Paint textPaint, rectPaint;
    private boolean bkmode = false;
    private int vmScreenBackColor = Color.BLACK;

    public Bitmap vm_screen;
    private Canvas vm_screen_canvas;
    private float rate;
    private Rect vm_screen_rect;
    private int background_color;

    private boolean isOver = false;
    private Semaphore semaphore = new Semaphore(0);
    private int whichGet = SURFACEGOT;

    private int touchValue = 0;

    public VmSurface(Context context) {
        super(context);

        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);

        this.setKeepScreenOn(true);
    }

    public boolean init(float r, float center_x, float center_y, int c){
        vm_screen = Bitmap.createBitmap(240, 320, Bitmap.Config.RGB_565);
        vm_screen_canvas = new Canvas(vm_screen);
        rate = r;background_color = c + 0xff000000;

        vm_screen_rect = new Rect((int)(center_x - 240.0f * rate / 2.0f),
                (int)(center_y - 320.0f * rate / 2.0f),
                (int)(center_x + 240.0f * rate / 2.0f),
                (int)(center_y + 320.0f * rate / 2.0f));
        //Log.e("",""+vm_screen_rect);

        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(Color.BLACK);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);

        boolean isOK = false;
        try {
            if ((isOK =semaphore.tryAcquire(1, TimeUnit.SECONDS))) {
                new Thread(drawRunnable).start();
                semaphore.release();
            }
        }catch (InterruptedException ex){
            ex.printStackTrace();
        }
        return isOK;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            int action = event.getAction();
            int touch_x, touch_y;
            if (action == MotionEvent.ACTION_DOWN && touchValue == 0) {
                touch_x = (int) ((event.getX() - vm_screen_rect.left) / rate);
                touch_y = (int) ((event.getY() - vm_screen_rect.top) / rate);
                //Log.e("","("+touch_x+","+touch_y+")");
                if (touch_x > -1 && touch_x < 240 && touch_y > -1 && touch_y < 320) {
                    touchValue = ((touch_y * 65536) + touch_x) | 0x80000000;
                }else if (touch_y > 320){
                    touchValue = 1; // make keyboard visible
                }
            }else if (action == MotionEvent.ACTION_UP) {
                touchValue = 0;
            }
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        semaphore.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isOver = true;
    }

    public int getTouchValue() {
        int i = touchValue;
        touchValue = 0;
        return i;
    }

    public void setBkmode(boolean bkmode) {
        this.bkmode = bkmode;
    }

    public int getVmScreenBackColor() {
        return vmScreenBackColor;
    }

    public void setNextPrint_x(float nextPrint_x) {
        this.nextPrint_x = nextPrint_x;
    }

    public void setNextPrint_y(float nextPrint_y) {
        this.nextPrint_y = nextPrint_y;
    }

    public float getNextPrint_x() {
        return nextPrint_x;
    }

    public float getNextPrint_y() {
        return nextPrint_y;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setPrintColor(int text,int back,int frame){
        textPaint.setColor(0xff000000 + text);
        vmScreenBackColor = 0xff000000 + back;
        rectPaint.setColor(vmScreenBackColor);
    }

    public void setPrintFont(int size,Typeface typeface){
        textSize = size;
        textPaint.setTextSize(textSize);
        textAesent = (float)Math.ceil(-textPaint.ascent()-1);
    }

    public void flippage(@Nullable Bitmap page4Show, Boolean isClean){
        try {
            semaphore.acquire();
            if (isClean) vm_screen_canvas.drawColor(Color.BLACK);
            if (page4Show!=null) vm_screen_canvas.drawBitmap(page4Show,0,0,null);
            whichGet = FLIPPAGEGOT;
            semaphore.release();
        }catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

    public void print(byte[] s) {
        float currentX = nextPrint_x;
        float strRight = this.nextPrint_x + s.length * textSize / 2;

        String str = "print 转码出错";
        try {
            str = new String(s, "gb2312");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } finally {
            RectF backRect = new RectF(nextPrint_x, nextPrint_y, nextPrint_x + textPaint.measureText(str), nextPrint_y + textSize);
            nextPrint_x = backRect.right > strRight ? backRect.right : strRight;
            backRect.right = nextPrint_x;

            try {
                semaphore.acquire();
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }finally {
                if(!bkmode) vm_screen_canvas.drawRect(backRect, rectPaint);

                if (this.nextPrint_y > 320 - textSize) {
                    vm_screen_canvas.drawColor(vmScreenBackColor);
                    this.nextPrint_y = 0;
                }
                vm_screen_canvas.drawText(str, currentX, nextPrint_y + textAesent, textPaint);
                whichGet = PRINTGOT;
                semaphore.release();
            }
        }
    }

    public Runnable drawRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("","绘图线程启动");
            Canvas sfcancas;

            float x = vm_screen_rect.left / rate;
            float y = vm_screen_rect.top / rate;

            while (!isOver){
                if (whichGet == -1 || whichGet == SURFACEGOT) continue;
                try {
                    if((sfcancas = surfaceHolder.lockCanvas()) != null){

                        if (semaphore.tryAcquire(1, TimeUnit.SECONDS)){
                            sfcancas.drawColor(background_color);
                            sfcancas.save();
                            sfcancas.scale(rate, rate, 0, 0);
                            sfcancas.drawBitmap(vm_screen, x, y, null);
                            sfcancas.restore();
                            whichGet = SURFACEGOT;
                            semaphore.release();
                        }
                        surfaceHolder.unlockCanvasAndPost(sfcancas);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
            Log.e("", "绘图线程结束");
        }
    };


}

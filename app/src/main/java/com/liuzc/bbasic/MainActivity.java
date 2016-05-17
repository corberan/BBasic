package com.liuzc.bbasic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {

    final int SHOWMSG = 1;
    final int RTERROR = 2;
    final int SURETOEXIT = 3;
    final int BEFULLSCREEN = 4;
    final int ADDSURFACE = 5;
    final int ADDKEYBOARD = 6;
    final int GAMEOVER = 7;

    private int binVersion = 21;

    private VirtualMachine virtualMachine;
    private VmSurface surface;
    private KeyBoard keyBoard;
    private DisplayMetrics dm;
//    private Semaphore semaphore = new Semaphore(0);
    private float center_y, rate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surface = new VmSurface(this);
        keyBoard = new KeyBoard(this);
        new Thread(startVM).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            handler.obtainMessage(SURETOEXIT).sendToTarget();
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Log.e("",""+event.getAction());
        if(event.getAction() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
            Log.e("",""+event.getY()+" , "+ center_y + 160 * rate + " , " +keyBoard.getVisibility());
            if (event.getY() > center_y + 160 * rate && keyBoard.getVisibility() == View.INVISIBLE) keyBoard.setVisibility(View.VISIBLE);
        }
        return true;
    }

    Runnable startVM = new Runnable() {
        @Override
        public void run() {
            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            Intent data = getIntent();

            boolean isfullscreen = data.getBooleanExtra("isfullscreen", false);
            int msdelay_time = data.getIntExtra("msdelay_time",1000);
            int gettick_which = data.getIntExtra("gettick_which", 0);
            int flippage_pauseTime = data.getIntExtra("flippage_pauseTime",0);
            int background_color = data.getIntExtra("background_color", 0xECECED);
            rate = data.getFloatExtra("rate", 1f);
            float center_x = data.getFloatExtra("center_x", dm.widthPixels / 2);
            center_y = data.getFloatExtra("center_y", dm.heightPixels / 2);
            String filePath = data.getStringExtra("absolutePath");

            //Log.e("", "" + isfullscreen + "," + msdelay_time + "," + gettick_which + "," + flippage_pauseTime + "," + background_color + "," + rate + "," + center_x + "," + center_y + "," + filePath);
            if (isfullscreen){
                handler.obtainMessage(BEFULLSCREEN).sendToTarget();
            }
            handler.obtainMessage(ADDSURFACE).sendToTarget();
            handler.obtainMessage(ADDKEYBOARD).sendToTarget();

            if(surface.init(rate, center_x, center_y, background_color)){
                surface.flippage(null, false);
                int[] regs = new int[8];
                byte[] execBinBytes = GetBinFileIn(filePath);
                if(execBinBytes != null){
                    regs[2] = execBinBytes.length-8;//rs
                    regs[3] = execBinBytes.length-1008;//rb
                    try {
                        String baseFilePath = Environment.getExternalStorageDirectory().getCanonicalPath() + "/BBasic/";
                        File file = new File(baseFilePath);
                        if (!file.exists()) {
                            if(!file.mkdir()){
                                handler.obtainMessage(RTERROR,"建立BBasic文件夹失败").sendToTarget();
                            }
                        }
                        virtualMachine = new VirtualMachine(surface, keyBoard);
                        virtualMachine.initAndRun(binVersion, gettick_which, flippage_pauseTime, msdelay_time, baseFilePath, regs, execBinBytes);
                        handler.obtainMessage(GAMEOVER).sendToTarget();
                    }catch (IOException ex){
                        ex.printStackTrace();
                        handler.obtainMessage(RTERROR,"IO错误，无法获得路径").sendToTarget();
                    }
                }else{
                    handler.obtainMessage(RTERROR,"读取bin文件出错，请检查").sendToTarget();
                }
            }else{
                handler.obtainMessage(SHOWMSG, "抱歉，surfaceView初始化失败").sendToTarget();
                finish();
            }
        }
    };

    private Handler handler = new Handler(){
        public void handleMessage (Message msg){
            switch (msg.what){
                case SHOWMSG:{
                    Toast.makeText(MainActivity.this,(String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                }
                case RTERROR:{
                    Toast.makeText(MainActivity.this,(String)msg.obj, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }
                case SURETOEXIT:{
                    Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("确认退出")
                            .setMessage("你确定要退出吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    virtualMachine.stop();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                    break;
                }
                case BEFULLSCREEN:{
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    break;
                }
                case ADDSURFACE:{
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    addContentView(surface, layoutParams);
                    //semaphore.release(1);
                    break;
                }
                case ADDKEYBOARD:{
                    FrameLayout.LayoutParams kb_layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dm.widthPixels * 336 / 480);
                    kb_layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                    addContentView(keyBoard, kb_layoutParams);
                    //semaphore.release(1);
                    break;
                }
                case GAMEOVER:{
                    Toast.makeText(MainActivity.this,"程序运行结束", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }
            }
        }
    };


    public  byte[] GetBinFileIn(String FilePath){
        int fileSize;
        byte[] buf = null;
        RandomAccessFile raf = null;
        try{
            raf = new RandomAccessFile(FilePath, "r");
            fileSize=(int)raf.length();
            if (fileSize>0 && fileSize<2147483647){
                buf = new byte[16];
                if(raf.read(buf)==-1) {
                    handler.obtainMessage(RTERROR,"读文件到缓存buf出错").sendToTarget();
                }
                if (buf[0]=='B'&&buf[1]=='B'&&buf[2]=='E'){
                    if(buf[7]!=0x40) binVersion = 20;
                    fileSize -= 16;
                }else {
                    binVersion = 19;
                    raf.seek(0);
                }
                buf = new byte[fileSize + 1010];
                if (raf.read(buf)==-1){
                    handler.obtainMessage(RTERROR,"读文件到缓存buf出错").sendToTarget();
                }
            }
            else
            {
                handler.obtainMessage(RTERROR,"文件大小应在1B到2G之间").sendToTarget();
            }
        }
        catch (IOException e){
            handler.obtainMessage(RTERROR,"IO错误").sendToTarget();
            e.printStackTrace();
        }
        finally{
            try{
                if(raf!=null) raf.close();
            }
            catch (IOException e)
            { e.printStackTrace(); }
        }
        return buf;
    }

}

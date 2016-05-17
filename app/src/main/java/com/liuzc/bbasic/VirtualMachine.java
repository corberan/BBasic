package com.liuzc.bbasic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by 政材 on 2015/1/8.
 */
public class VirtualMachine {

    static {
        System.loadLibrary("VmBasic");
    }

    public native int runExecCode(int jVmsize,byte[] jcode,int[] jRegs,int jtime);
    public native int stopRunning();

    private VmSurface vmSurface;
    private KeyBoard keyBoard;

    private int vm_screen_w = 240,vm_screen_h = 320;

    private LinkedList<Bitmap> Pics = new LinkedList<>();
    private LinkedList<Bitmap> PicsDealed = new LinkedList<>();//经过透明色替换的，空间换时间

    private LinkedList<Bitmap> Pages = new LinkedList<>();
    private LinkedList<Paint> Paints = new LinkedList<>();
    private Map<String, int[]> func_lineto_lastPoint = new HashMap<>();

    private LinkedList<RandomAccessFile> openedfiles = new LinkedList<>();
    private Map<String, String> func_open_port2file = new HashMap<>();

    private boolean isOver = false;

    private int binVersion = 21;
    private String baseFilePath = "";

    private int gettickWhich = 0;
    private int gettick_tick = 0;

    private int flippagePauseMS = 0;
//    private long lastKeyPressedTime = 0;

    private final byte[] sync_bytes = new byte[1];

    public VirtualMachine(VmSurface v, KeyBoard k){
        this.vmSurface = v;
        this.keyBoard = k;
    }

    public void initAndRun(int ver, int which, int ms, int delay, String path, int[] r, byte[] b){
        this.binVersion = ver;
        this.gettickWhich = which;
        this.flippagePauseMS = ms;
        this.baseFilePath = path;

        Pics.clear();
        PicsDealed.clear();
        Pages.clear();
        Paints.clear();
        func_lineto_lastPoint.clear();
        func_open_port2file.clear();
        closeAllOpenedFiles();

        isOver = false;
        createpage(); //page -1
        Pages.set(0, vmSurface.vm_screen);
        int point = runExecCode(b.length, b, r, delay);
        Log.e("","here"+point);
        closeAllOpenedFiles();
        isOver = true;
        Log.e("","over!");
    }

    public int stop(){
        isOver = true;//对于正在waitkey的程序，需要这个变量来强制关闭
        stopRunning();
        return 1;
    }

    public void setlcd(int x,int y){
//        if(x > 0 && y > 0) {
//            this.vm_screen_w = x;
//            this.vm_screen_h = y;
//            vmSurface.setlcd(x,y);
//        }
    }

    public void flippage(int page){
//        long time = System.currentTimeMillis();
//        Log.e("BBASIC","flippage(page"+page+")");
        //waitkey();
        if(page>-1 && page+1<Pages.size()) {
            vmSurface.flippage(Pages.get(page+1),true);
            if (flippagePauseMS > 0) {
                synchronized (sync_bytes) {
                    try {
                        sync_bytes.wait(flippagePauseMS);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
//        Log.e("BBASIC", "cast " + (System.currentTimeMillis() - time));
    }

    public void freeres(int pic_id){
        //Log.e("","freeres("+pic_id+")");
        if(pic_id>-1 && pic_id<Pics.size()) {
            if(Pics.get(pic_id)!=null){
                Pics.set(pic_id,null);
                PicsDealed.set(pic_id,null);
            }
        }
    }

    public void showpic(int page,int pic,int screen_x,int screen_y,int w,int h,int cut_x,int cut_y,int mode){
//        long time = System.currentTimeMillis();
        if(page>-2 && pic>-1 && pic<Pics.size() && w>-1 && h>-1 && mode>-1) {

//            Log.e("BBASIC","showpic(page"+page+",pic"+pic+","+screen_x+","+screen_y+","+w+","+h+","+cut_x+","+cut_y+","+mode+")");
            //Log.e("","showpic x="+screen_x);
            if (Pics.get(pic) != null && page<Pages.size() && Pages.get(page+1)!=null) {
                showpic2page(page, pic, screen_x, screen_y, w, h, cut_x, cut_y, mode);
            }
        }
//        Log.e("BBASIC", "cast " + (System.currentTimeMillis() - time));
    }

    public void bitbltpage(int dest,int src){
//        Log.e("BBASIC","bitbitpage("+dest+","+src+")");
        //waitkey();
        if(dest>-2 && src>-2 && dest+1<Pages.size() && src+1<Pages.size()){
            Bitmap bitmapDest = Pages.get(dest+1);
            Bitmap bitmapSrc = Pages.get(src+1);
            if(bitmapDest!=null && bitmapSrc!=null) {
                Canvas canvas = new Canvas(bitmapDest);
                canvas.drawBitmap(bitmapSrc,0,0,new Paint());
                if(dest==-1) vmSurface.flippage(null,false);
            }
        }
    }

    public void stretchbltpageEx(int x,int y,int wid,int hgt,int cx,int cy,int dest,int src){

//        Log.e("BBASIC","stretchbltpageEx("+x+","+y+","+wid+","+hgt+","+cx+","+cy+","+dest+","+src+")");
        if(dest>-2 && src>-2 && dest+1<Pages.size() && src+1<Pages.size() && wid>0 && hgt>0){
            Bitmap bitmapDest ;
            Bitmap bitmapSrc ;
            bitmapDest = Pages.get(dest+1);
            if(bitmapDest==null) return;
            bitmapSrc = Pages.get(src+1);
            if(bitmapSrc==null) return;

            if(cx < 0){
                x -= cx;
                cx = 0;
            }
            if(cy < 0){
                y -= cy;
                cy = 0;
            }

            if(x < 0){
                cx -= x;
                wid += x;
                if(wid < 1) return;
                x = 0;
            }
            if(y < 0){
                cy -= y;
                hgt += y;
                if(hgt < 1) return;
                y = 0;
            }

            if(x >= vm_screen_w || y >= vm_screen_h || cx >= vm_screen_w || cy >= vm_screen_h) return;

            int realWid = bitmapSrc.getWidth() - cx;
            int realHgt = bitmapSrc.getHeight() - cy;

            wid = wid > realWid ? realWid : wid;
            hgt = hgt > realHgt ? realHgt : hgt;

//            Log.e("BBASIC", cx + "," + cy  + "," + wid  + "," + hgt  + "," + x  + "," + y);
//            long time = System.currentTimeMillis();

            Canvas bltcanvas = new Canvas(bitmapDest);

//            long timeNewCanvas = System.currentTimeMillis() - time;
//            time = System.currentTimeMillis();

//            Bitmap srcCutBmp = Bitmap.createBitmap(bitmapSrc,cx,cy,wid,hgt);

//            Log.e("BBASIC", "" + bitmapSrc.hasAlpha() + "" + bitmapSrc.isPremultiplied());

//            long timeCreateBMP = System.currentTimeMillis() - time;
//            time = System.currentTimeMillis();

            bltcanvas.drawBitmap(bitmapSrc, new Rect(cx,cy,cx+wid,cy+hgt), new Rect(x,y,x+wid,y+hgt), null);

//            bltcanvas.drawBitmap(srcCutBmp,x,y,new Paint());

//            long timeDrawBMP = System.currentTimeMillis() - time;
            //bltcanvas.drawBitmap(bitmapSrc,new Rect(cx,cy,cx+wid,cy+hgt),new Rect(x,y,x+wid,y+hgt), new Paint());

//            Log.e("BBASIC", "timeNewCanvas " + timeNewCanvas + ",timeDrawBMP " + timeDrawBMP);

            if(dest==-1) vmSurface.flippage(null,false);
        }
    }

    public void stretchbltpage(int x,int y,int wid,int hgt,int cx,int cy,int dest,int src){

        //waitkey();
        if(binVersion == 21){
            //Log.e("","stretchbltpage("+cx+","+cy+","+dest+","+src+")");
            stretchbltpageEx(cx,cy,vm_screen_w,vm_screen_h,0,0,dest,src);
        }else{
            //Log.e("","stretchbltpage("+x+","+y+","+wid+","+hgt+","+cx+","+cy+","+dest+","+src+")");
            stretchbltpageEx(x,y,wid,hgt,cx,cy,dest,src);
        }
    }

    public int deletepage(int page){
//        Log.e("BBASIC","deletepage: "+page);
        if(page>-1 && page+1<Pages.size()){// -1页不允许删除
            Pages.set(page + 1,null);//page的删除是填null,pic的删除则会remove,为的是和bb兼容
            Paints.set(page + 1,null);
            return page;
        }else{
            return -1;
        }
    }

    public void fillpage(int page,int x,int y,int wid,int hgt,int color){
//        Log.e("BBASIC"," fillpage("+page+","+x+","+y+","+wid+","+hgt+","+color+")");
        if(page>-2 && page+1<Pages.size()){
            fillPageWithColor(page, x, y, wid, hgt, color);
        }
    }

    public void pixel(int page,int x,int y,int color){
        if(page>-2 && page+1<Pages.size()) {
            Bitmap theTruePage;

            theTruePage = Pages.get(page+1);
            if(theTruePage==null) return;

            Paint paint = new Paint();
            paint.setColor(BGR2RGB(color));
            Canvas pixelcanvas = new Canvas(theTruePage);
            pixelcanvas.drawPoint(x, y, paint);
            if(page==-1){
                vmSurface.flippage(null,false);
            }
        }
    }

    public int readpixel(int page,int x,int y){
        if(page>-2) {
            Bitmap theTruePage = null;
            if(page+1<Pages.size()){
                theTruePage = Pages.get(page+1);
                if(theTruePage==null) return -1;
            }
            if(theTruePage!=null){
                return theTruePage.getPixel(x, y);
            }else{
                return -1;
            }
        }else{
            return -1;
        }
    }

    public int getpichgt(int pic_id){
        if(pic_id>-1 && pic_id<Pics.size()){
            Bitmap bitmap = Pics.get(pic_id);
            if(bitmap!=null){
                //Log.e("",bitmap.getHeight()+"="+"getpichgt(pic"+pic_id+")");
                return bitmap.getHeight();
            }else {
                return -1;
            }
        }else{
            return -2;
        }
    }

    public int getpicwid(int pic_id){
        if(pic_id>-1 && pic_id<Pics.size()){
            Bitmap bitmap = Pics.get(pic_id);
            if(bitmap!=null){
                //Log.e("",bitmap.getWidth()+"="+"getpicwid(pic"+pic_id+")");
                return bitmap.getWidth();
            }else {
                return -1;
            }
        }else{
            return -2;
        }
    }

    public void print(byte[] str4print) {
        if (str4print.length > 0) {
            vmSurface.print(str4print);
        }
    }

    public void out4(int ascii){
        //Log.e("out4",","+ascii);
        switch (ascii){
            case 9: {
                String str = "        ";
                print(str.getBytes());
                break;
            }
            case 10:
            case 13:{
                vmSurface.setNextPrint_y(vmSurface.getNextPrint_y() + vmSurface.getTextSize());
                vmSurface.setNextPrint_x(0);
                break;
            }
            case 32:{
                String str = " ";
                print(str.getBytes());
                break;
            }
            default:{
                byte[] bytes = new byte[1];
                bytes[0] = (byte)ascii;
                print(bytes);
            }
        }
    }

    public void print(int i){
        //Log.e("","print "+i);
        try {
            print(String.valueOf(i).getBytes("gb2312"));
        }catch (UnsupportedEncodingException ex){
            ex.printStackTrace();
        }
    }

    public void print(float f){
        try {
            print(String.valueOf(f).getBytes("gb2312"));
        }catch (UnsupportedEncodingException ex){
            ex.printStackTrace();
        }
    }

    public void cls() {
        //Log.e("","cls "+Integer.toHexString(vmSurface.getVmScreenBackColor()));
        fillpage(-1, 0, 0, vm_screen_w, vm_screen_h, vmSurface.getVmScreenBackColor());
        vmSurface.setNextPrint_x(0);
        vmSurface.setNextPrint_y(0);
    }

    public void color(int frontColor,int backColor,int frameColor){
        //Log.e("","color(&H"+Integer.toHexString(frontColor)+",&H"+Integer.toHexString(backColor)+",&H"+Integer.toHexString(frameColor)+")");
        vmSurface.setPrintColor(BGR2RGB(frontColor), BGR2RGB(backColor), BGR2RGB(frameColor));
    }

    public void pixlocate(int x,int y){
        vmSurface.setNextPrint_x(x);
        vmSurface.setNextPrint_y(y);
    }

    public void locate(int row,int line){
        //Log.e("","locate("+row+","+line+")");
        if(row < 1) row = 1;
        if(line < 1) line = 1;
        vmSurface.setNextPrint_x((line-1) * vmSurface.getTextSize() / 2);
        vmSurface.setNextPrint_y((row-1) * vmSurface.getTextSize());
    }

    public void font(int fontSize){
        //Log.e("","font("+fontSize+")");
        if(fontSize > -1 && fontSize < 3) fontSize = 12;
        else if(fontSize < 6) fontSize = 16;
        else if(fontSize < 9) fontSize = 24;
        vmSurface.setPrintFont(fontSize,null);
    }

    public void setbkmode(int mode){
        if(mode == 1){//TRANSPARENT = 1
            vmSurface.setBkmode(true);
        }else{
            vmSurface.setBkmode(false);
        }
    }

    public void setpen(int page,int style,int wid,int color){
        if(page>-2 && page+1<Pages.size()) {
            Paint paint = Paints.get(page+1);
            if(paint==null) return;
            if(style==0) paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color+0xff000000);
            paint.setStrokeWidth(wid);
        }
    }

    public void setbrush(int page,int style){
        if(page>-2  && page+1<Pages.size()) {
            Paint paint = Paints.get(page+1);
            if(paint==null) return;
            if(style!=0) paint.setStyle(Paint.Style.STROKE);//未完成
        }
    }

    public void moveto(int page,int x,int y) {
        if (page > -2  && page+1<Pages.size()) {
            int[] i = {x,y};
            func_lineto_lastPoint.put(String.valueOf(page),i);
        }
    }

    public void lineto(int page,int x,int y){
        if(page>-2 && page+1<Pages.size()){
            Bitmap bitmap;
            Paint paint = Paints.get(page + 1);
            if (paint == null) return;
            bitmap = Pages.get(page + 1);
            //Log.e("lineto","  "+page+"  "+x+"  "+y);
            if(bitmap!=null) {
                Canvas drawlinecanvas = new Canvas(bitmap);
                drawlinecanvas.drawLine(func_lineto_lastPoint.get(Integer.toString(page + 1))[0], func_lineto_lastPoint.get(Integer.toString(page + 1))[1], x, y, paint);
                int[] i = {x,y};
                func_lineto_lastPoint.put(Integer.toString(page + 1),i);
                if(page==-1) vmSurface.flippage(null,false);
            }
        }
    }

    public void rectangle(int page,int left,int top,int right,int bottom){
        if(page>-2 && page+1<Pages.size()){
            Bitmap bitmap ;
            Paint paint = Paints.get(page+1);
            bitmap = Pages.get(page+1);
            if (bitmap == null || paint == null) return;
            Canvas drawlinecanvas = new Canvas(bitmap);
            drawlinecanvas.drawRect(new Rect(left, top, right, bottom), paint);
            if(page==-1) vmSurface.flippage(null,false);
        }
    }

    public void circle(int page,int x,int y,int cr){
        if(page>-2 && page+1<Pages.size()){
            Bitmap bitmap;
            Paint paint = Paints.get(page+1);
            bitmap = Pages.get(page+1);
            if (bitmap == null || paint == null) return;
            Canvas drawlinecanvas = new Canvas(bitmap);
            drawlinecanvas.drawCircle(x,y,cr,paint);
            if(page==-1) vmSurface.flippage(null,false);
        }
    }

    public void openfile(byte[] filename,int port){
        if(port > 0) {
            try {
                String str = new String(filename, "gb2312");
                openfile(str,port);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }
    }

    public byte[] input_int(){
        int keycode = 0;
        String input = "";
        float oldX = vmSurface.getNextPrint_x();
        while(keycode != 13 && !isOver){
            keycode = waitkey();
            //Log.e("",""+keycode);
            if(keycode>47 && keycode<58){
                input += keycode-48;
            }else if(keycode == 8){
                input = input.substring(0, (input.length()-1 > 0 ? input.length()-1 : 0));
            }
            try {
                vmSurface.setNextPrint_x(oldX);
                vmSurface.print((input + " ").getBytes("gb2312"));
            }catch (UnsupportedEncodingException ex){
                ex.printStackTrace();
            }
        }
        out4(10);
        return input.getBytes();
    }

    public byte[] input_float(){
        int keycode = 0;
        String input = "";
        float oldX = vmSurface.getNextPrint_x();
        while(keycode != 13 && !isOver){
            keycode = waitkey();
            if(keycode>47 && keycode<58){
                input += keycode-48;
            }else if(keycode == 190) {
                input += '.';
            }else if(keycode == 8){
                input = input.substring(0, (input.length()-1 > 0 ? input.length()-1 : 0));
            }
            try {
                vmSurface.setNextPrint_x(oldX);
                vmSurface.print((input + " ").getBytes("gb2312"));
            }catch (UnsupportedEncodingException ex){
                ex.printStackTrace();
            }
        }
        out4(10);
        return input.getBytes();
    }

    public byte[] input_str(){
        int keycode = 0;
        String input = "";
        float oldX = vmSurface.getNextPrint_x();
        while(keycode != 13 && !isOver){
            keycode = waitkey();
            if(keycode == 8){
                input = input.substring(0, (input.length()-1 > 0 ? input.length()-1 : 0));
            }else if(keycode>47 && keycode<58){
                input += keycode - 48;
            }else if(keycode>64 && keycode<91){
                input += String.valueOf((char)keycode);
            }
            try {
                vmSurface.setNextPrint_x(oldX);
                vmSurface.print((input + " ").getBytes("gb2312"));
            }catch (UnsupportedEncodingException ex){
                ex.printStackTrace();
            }
        }
        out4(10);
        return input.getBytes();
    }

    public int waitkey(){
//        Log.e("BBASIC", "waiting key ...");
        int keycode;
        while(true) {
//            if (System.currentTimeMillis() - lastKeyPressedTime < 1000) continue;
            if (isOver) return 0;
            if ((keycode = keyBoard.getKeyValue()) != 0) break;
            keycode = vmSurface.getTouchValue();
            if (keycode < 0) break;
            else if (keycode == 1) {
                if (keyBoard.getVisibility() == View.INVISIBLE) {
                    keyBoard.post(new Runnable() {
                        @Override
                        public void run() {
                            keyBoard.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }
//        Log.e("BBASIC",keycode+"=waitkey()");
        keyBoard.setKeyValue(0);
//        lastKeyPressedTime = System.currentTimeMillis();
        return keycode;
    }

    public int KeyPress(int key){
//        Log.e("","keypressed,isOver:"+isOver);
        if(isOver) return 0;
        int isPressed = 0;
        if(key == keyBoard.getKeyValue()){
            isPressed = 1;
        }
        //keyBoard.setKeyValue(0);
        if (vmSurface.getTouchValue() == 1 && keyBoard.getVisibility() == View.INVISIBLE){
            keyBoard.post(new Runnable() {
                @Override
                public void run() {
                    keyBoard.setVisibility(View.VISIBLE);
                }
            });
        }

        return isPressed;
    }

    public int InKey(){
//        Log.e("","inkey");
        if(isOver) return 0;
        int value = keyBoard.getKeyValue();
        if(value == 0){
            value = vmSurface.getTouchValue();
            if (value == 1){
                value = 0;
                if (keyBoard.getVisibility() == View.INVISIBLE) {
                    keyBoard.post(new Runnable() {
                        @Override
                        public void run() {
                            keyBoard.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }
        return value;
    }

    public int loadres(byte[] filename,int id){
        try{
            String str = new String(filename,"gb2312");
            return loadres(str,id);
        }catch (UnsupportedEncodingException ex){
            ex.printStackTrace();
            return -1;
        }
    }

    /**
     * 默认使用到了成员变量pics
     * @param filename lib文件名
     * @param id 需要载入的图片编号，从1开始
     * @return 载入的pic在ArrayList中的编号
     * 返回错误代码：-1 lib不含图片或者文件损坏；-2 图片id不能小于0；-3 lib图片有误，size应大于0，请检查；-4 没有打开权限
     */
    public int loadres(String filename, int id) {
        int picSaveLoc = -1;
        try {
            filename = filename.replace("\\","/");
            if(filename.indexOf("/")==0) filename = filename.substring(1);
            FileInputStream fis = new FileInputStream(this.baseFilePath + filename);

            DataInputStream LibDataStream = new DataInputStream(fis);
            int maxid = big2little(LibDataStream.readInt());//得到图片数目
            if (maxid <= 0) return -1;
            if (id <= 0) return -2;

            id = id > maxid ? maxid : id;
            LibDataStream.skipBytes((id - 1) * 4);
            int offset = big2little(LibDataStream.readInt());
            LibDataStream.skipBytes(offset - id * 4 - 4);
            int size = big2little(LibDataStream.readInt()) - 12;
            if (size <= 0) return -3;

            short bfType = 0x424d;
            int biWidth = (big2little(LibDataStream.readShort() & 0x0000ffff) >> 16) & 0x0000ffff;//F0 00
            int biHeight = -((big2little(LibDataStream.readShort() & 0x0000ffff) >> 16) & 0x0000ffff);//40 01
            int lost4 = 0;
            if (biWidth % 2 != 0) {
                lost4 = 1;
                biWidth++;
                size = biWidth * (-biHeight) * 2;
                biWidth--;
            }

            //Log.e(" 载入lib：","图片宽高为："+biWidth+"x"+biHeight+",大小为："+size);

            int bfSize = size + 138;
            short bfReserved1 = 0;
            short bfReserved2 = 0;
            int bfOffBits = 138;
            int biSize = 124;
            short biPlanes = 1;
            short biBitcount = 16;
            int biCompression = 3;
            int biSizeImage = size;
            int biXPelsPerMeter = 2835;
            int biYPelsPerMeter = 2835;
            int biClrUsed = 0;
            int biClrImportant = 0;
            int ColorB = 0x00f80000;
            int ColorG = 0xe0070000;
            int ColorR = 0x1f000000;
            int unknowReserved = 0x00;
            int unknowType = 0x42475273;
            byte[] unknowarray = new byte[64];

            ByteArrayOutputStream baops = new ByteArrayOutputStream();
            DataOutputStream dops = new DataOutputStream(baops);
            dops.writeShort(bfType);
            dops.write(changeByte(bfSize), 0, 4);
            dops.write(changeByte(bfReserved1), 0, 2);
            dops.write(changeByte(bfReserved2), 0, 2);
            dops.write(changeByte(bfOffBits), 0, 4);
            dops.write(changeByte(biSize), 0, 4);
            dops.write(changeByte(biWidth), 0, 4);
            dops.write(changeByte(biHeight), 0, 4);
            dops.write(changeByte(biPlanes), 0, 2);
            dops.write(changeByte(biBitcount), 0, 2);
            dops.write(changeByte(biCompression), 0, 4);
            dops.write(changeByte(biSizeImage), 0, 4);
            dops.write(changeByte(biXPelsPerMeter), 0, 4);
            dops.write(changeByte(biYPelsPerMeter), 0, 4);
            dops.write(changeByte(biClrUsed), 0, 4);
            dops.write(changeByte(biClrImportant), 0, 4);
            dops.writeInt(ColorB);
            dops.writeInt(ColorG);
            dops.writeInt(ColorR);
            dops.write(changeByte(unknowReserved), 0, 4);
            dops.writeInt(unknowType);
            dops.write(unknowarray, 0, 64);

            byte[] bmpdata;//bmp数据
            LibDataStream.skipBytes(8);
            if (lost4 == 0) {
                bmpdata = new byte[size];
                LibDataStream.read(bmpdata);
                dops.write(bmpdata, 0, size);
            } else {
                int oldbw = biWidth * 2;
                bmpdata = new byte[oldbw];
                for (int count = 0; count < -biHeight; count++) {
                    LibDataStream.read(bmpdata);
                    dops.write(bmpdata, 0, oldbw);
                    dops.writeShort(0x0000);
                }
            }

            byte[] bmp = new byte[bfSize];
            bmp = baops.toByteArray();

            for (int i = 0; i < Pics.size(); i++) {
                if (Pics.get(i) == null) {
                    picSaveLoc = i;
                    break;
                }
            }

            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inSampleSize = 1;
            Bitmap bitmapfromByte = BitmapFactory.decodeByteArray(bmp, 0, bmp.length, bmpFactoryOptions);

            if (picSaveLoc >= 0 && picSaveLoc < Pics.size()) {
                Pics.set(picSaveLoc, bitmapfromByte);
                PicsDealed.set(picSaveLoc, removeOneColorAndReturn(bitmapfromByte, 0xfff800f8));
            } else {
                Pics.add(bitmapfromByte);
                PicsDealed.add(removeOneColorAndReturn(bitmapfromByte, 0xfff800f8));
                picSaveLoc = Pics.size() - 1;
            }

            //Log.e(" 载入lib：","Pics加入一张图片，图片大小为"+bmp.length);
            baops.reset();
            dops.flush();
            //if(fis!=null) fis.close();
            fis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //Log.e(" 载入的图片编号为为：","" + (Pics.size() - 1));
//        Log.e("BBASIC","pic"+picSaveLoc+"=loadres(\""+filename+"\","+id+")");
        return picSaveLoc;
    }

    public Bitmap removeOneColorAndReturn(Bitmap bitmapForDeal, int color){
        int wid = bitmapForDeal.getWidth();
        int hgt = bitmapForDeal.getHeight();
        Bitmap bitmap_pured = Bitmap.createBitmap(bitmapForDeal.getWidth(),bitmapForDeal.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap_pured);
        canvas.drawBitmap(bitmapForDeal,0,0,null);
        for(int x=0;x<wid;x++){
            for(int y=0;y<hgt;y++){
                if(bitmap_pured.getPixel(x,y) == color) bitmap_pured.setPixel(x,y,0);
            }
        }
        return bitmap_pured;
    }

    /**
     * @param page Pages里存在的编号
     * @param pic  Pics里存在的编号，从0开始
     * @param w    需要显示的宽带
     * @param h    需要显示的高度
     * @param x    以此坐标为起点截取
     * @param y    以此坐标为起点截取
     * @param mode 模式1：透明色为565的紫色，模式0，不透明
     * 下次把计算放到底层去完成
     */

    public void showpic2page(int page, int pic, int screen_x, int screen_y, int w, int h, int x, int y, int mode) {

        Bitmap bitmapfromByte;

        if(mode == 1){
            bitmapfromByte = PicsDealed.get(pic);
        }else{
            bitmapfromByte = Pics.get(pic);
        }
        if(bitmapfromByte == null) return;

        int picWidth = bitmapfromByte.getWidth();
        int picHeight = bitmapfromByte.getHeight();

        w = w > picWidth ? picWidth : w;
        h = h > picHeight ? picHeight : h;

        if (x < 0) {
            screen_x -= x;
            x = 0;
        }
        if (y < 0) {
            screen_y -= y;
            y = 0;
        }
        if (screen_x < 0) {
            x -= screen_x;
            w += screen_x;
            if(x > picWidth || w < 1) return;
            screen_x = 0;
        }
        if (screen_y < 0) {
            y -= screen_y;
            h += screen_y;
            if(y > picHeight || h < 1) return;
            screen_y = 0;
        }

        if(screen_x >= vm_screen_w) return;
        if(screen_y >= vm_screen_h) return;

        Bitmap bitmap4show = Pages.get(page + 1);
        //Log.e("page="+page,"时是不是和VMscreen相等 "+bitmap4show.equals(this.vmSurface.VMscreen));
        //Log.e("",",string: "+this.vmSurface.VMscreen.toString());
        int PageWidth = bitmap4show.getWidth();
        int PageHeight = bitmap4show.getHeight();

        if (x < picWidth && y < picHeight && screen_x < PageWidth && screen_y < PageHeight) {
            if(x + w > picWidth) w = picWidth - x;
            if(y + h > picHeight) h = picHeight - y;
//            Log.e("BBASIC","x:"+x+",y:"+y+",w"+w+",h"+h);
//            Log.e("BBASIC","screen_x:"+screen_x+",screen_y"+screen_y);
//            Bitmap cutBmp = Bitmap.createBitmap(bitmapfromByte,x,y,w,h);
            Canvas showpicCanvas = new Canvas(bitmap4show);
            //showpicCanvas.clipRect(screen_x, screen_y, screen_x + w, screen_y + h);
//            showpicCanvas.drawBitmap(cutBmp, screen_x, screen_y, null);
            showpicCanvas.drawBitmap(bitmapfromByte, new Rect(x,y,x+w,y+h), new Rect(screen_x,screen_y,screen_x+w,screen_y+h), null);

            if (page == -1) vmSurface.flippage(null,false);
        }
    }

//    public void showpic2page(int page, int pic, int screen_x, int screen_y, int w, int h, int x, int y, int mode) {
//
//        Bitmap bitmapfromByte = Pics.get(pic);
//
////        int oldPageWidth = Pages.get(page).getWidth();
////        int oldPageHeight = Pages.get(page).getHeight();
//        Bitmap bitmap4show;
//        if (x < 0) screen_x -= x;
//        if (y < 0) screen_y -= y;
//        if (screen_x < 0) {
//            x -= screen_x;
//            w += screen_x;
//            w = w > 0 ? w : 0;
//            screen_x = 0;
//        }
//        if (screen_y < 0) {
//            y -= screen_y;
//            h += screen_y;
//            h = h > 0 ? h : 0;
//            screen_y = 0;
//        }
//        int picWidth = bitmapfromByte.getWidth();
//        int picHeight = bitmapfromByte.getHeight();
//        int realShowWid = w < (picWidth - x) ? w : (picWidth - x);
//        int realShowHgt = h < (picHeight - y) ? h : (picHeight - y);
//        int maxWid = screen_x + realShowWid;
//        int maxHgt = screen_y + realShowHgt;
//
//        if (page == -1) {
//            bitmap4show = Bitmap.createBitmap(vm_screen_w, vm_screen_h, Bitmap.Config.ARGB_8888);
//            //Log.e(""," new bitmap color "+Integer.toHexString(bitmap4show.getPixel(0,0)));
//        } else {
//            bitmap4show = Pages.get(page + 1);
//        }
//        int PageWidth = bitmap4show.getWidth();
//        int PageHeight = bitmap4show.getHeight();
//
//        if (x < picWidth && y < picHeight && screen_x < PageWidth && screen_y < PageHeight) {
//            Bitmap bitmapOld = Bitmap.createBitmap(bitmap4show);
//            Canvas showpicCanvas = new Canvas(bitmap4show);
//            showpicCanvas.drawBitmap(bitmapfromByte, new Rect(x, y, x + w, y + h), new Rect(screen_x, screen_y, screen_x + w, screen_y + h), new Paint());
//            if (mode == 1) {
//                mode = 0xfff800f8;
//                int replace_wid = PageWidth < maxWid ? PageWidth : maxWid;
//                int replace_ght = PageHeight < maxHgt ? PageHeight : maxHgt;
//                //Log.e(""," screen_x: "+screen_x+" ,screen_y: "+screen_y);
//                //Log.e(""," replace_wid: "+replace_wid+" ,replace_ght: "+replace_ght);
//                for (int replace_x = screen_x; replace_x < replace_wid; replace_x++) {// && replace_x < PageWidth
//                    for (int replace_y = screen_y; replace_y < replace_ght; replace_y++) {// && replace_y < PageHeight
//                        int pixelColor = bitmap4show.getPixel(replace_x, replace_y);
//                        if (pixelColor == mode) {
//                            bitmap4show.setPixel(replace_x, replace_y, bitmapOld.getPixel(replace_x, replace_y));
//                        }
//                    }
//                }
//            }
//
//            if (page == -1) Pages.set(0, bitmap4show);
//        }
//    }

    public int createpage(){
        Bitmap bitmapshow = Bitmap.createBitmap(vm_screen_w,vm_screen_h, Bitmap.Config.RGB_565);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        Canvas canvas = new Canvas(bitmapshow);
        canvas.drawColor(Color.WHITE);

        Pages.add(bitmapshow);
        Paints.add(paint);
        //Log.e("",""+(Pages.size()-2)+"=createpage()");
        return Pages.size()-2;
    }

    public void fillPageWithColor(int page,int x,int y,int wid,int hgt,int color){
        Bitmap theTruePage ;
        theTruePage = Pages.get(page+1);
        if(theTruePage==null) return;
        //Log.e("",""+theTruePage.equals(this.vmSurface.VMscreen)+", str:"+this.vmSurface.VMscreen.toString());
        int pageWid = theTruePage.getWidth();
        int pageHgt = theTruePage.getHeight();
        if(x<pageWid && y<pageHgt) {
            if(x<0){
                wid += x;
                x=0;
            }
            if(y<0){
                hgt += y;
                y=0;
            }
            Paint paint = new Paint();
            paint.setColor(BGR2RGB(color+0xff000000));
            Canvas canvas = new Canvas(theTruePage);
            canvas.drawRect(x, y, x+wid, y+hgt, paint);
            if(page==-1) vmSurface.flippage(null,false);
        }
    }

    public int openfile(String filename,int port){
        //Log.e("原始文件名",filename+"长度"+filename.length());
        filename = filename.replace("\\","/");
        filename = filename.replace("//","/");
        if(filename.indexOf("/")==0) filename = filename.substring(1);
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.baseFilePath + filename, "rwd");
            //Log.e("","文件长度 "+randomAccessFile.length());
            openedfiles.add(randomAccessFile);
            func_open_port2file.put(Integer.toString(port),Integer.toString(openedfiles.size() - 1));
            //Log.e("","打开文件 "+filename+" 在端口"+port+"映射到"+(openedfiles.size()-1));
            return 0;
        } catch (IOException ex) {
            //Log.e("","打开文件出错"+filename);
            ex.printStackTrace();
            return -1;
        }
    }

    public void putBytes(int port,byte[] bytes){
        //Log.e("","putBytes(#"+port+")");
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile!=null){
                try {
                    randomAccessFile.write(bytes);
                    randomAccessFile.writeByte(0);
                }catch (IOException ex){
                    //Log.e(" error "," 写入文件错误-putbytes-port: "+port+" loc:"+index);
                    ex.printStackTrace();
                }
            }
        }
    }

    public byte[] getBytes(int port){
        //Log.e("","getBytes(#"+port+")");
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile!=null){
                try {
                    byte[] bytes = new byte[256];
                    for(int i=0;i<256;i++)
                        bytes[i] = 0;
                    byte singeleByte ;
                    for(int temp=0;temp<256 && randomAccessFile.getFilePointer() < randomAccessFile.length() && (singeleByte = randomAccessFile.readByte()) != 0;temp++){
                        bytes[temp] = singeleByte;
                    }
                    return bytes;
                }catch (IOException ex){
                    //Log.e(" error "," 读取文件错误-getBytes-port: "+port+" loc:"+index);
                    ex.printStackTrace();
                    return null;
                }
            }else{
                return null;
            }
        }else {
            return null;
        }
    }

    public void putInt(int port,int i){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile != null){
                try {
//                    byte[] bytes = changeByte(i);
//                    Log.e("!!!!!!!!","1: "+bytes[0]+" 2: "+bytes[1]+" 3: "+bytes[2]+" 4: "+bytes[3]);
                    randomAccessFile.write(changeByte(i), 0, 4);
                }catch (IOException ex){
                    //Log.e(" error "," 写入文件错误-putint-port: "+port+" loc:"+index);
                    ex.printStackTrace();
                }
            }
        }
    }

    public int getInt(int port){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile != null){
                try {
                    //Log.e("","getint "+",文件指针"+randomAccessFile.getFilePointer());
                    if(randomAccessFile.getFilePointer() < randomAccessFile.length() - 3) {
                        return big2little(randomAccessFile.readInt());
                    }else{
                        byte[] lastBytes = new byte[4];
                        int num = randomAccessFile.read(lastBytes);
                        int litterValue = 0;
                        for(int loop=num-1;loop>-1;loop--){
                            litterValue <<= 8;
                            litterValue += lastBytes[loop] & 0x000000ff;
                        }
                        //Log.e("","return"+litterValue);
                        return litterValue;
                    }
                }catch (IOException ex){
                    //Log.e(" error "," 读取文件错误-getInt  port:"+port+" loc:"+index);
                    ex.printStackTrace();
                    return -1;
                }
            }else{
                return -2;
            }
        }else {
            return -3;
        }
    }

    public void putFloat(int port,float f){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            try {
                randomAccessFile.write(changeByte(Float.floatToIntBits(f)), 0, 4);
            }catch (IOException ex){
                //Log.e(" error "," 写入文件错误-putfloat-port: "+port+" loc:"+index);
                ex.printStackTrace();
            }
        }
    }

    public float getFloat(int port){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile!=null){
                try {
                    return Float.intBitsToFloat(big2little(randomAccessFile.readInt()));
                }catch (IOException ex){
                    //Log.e(" error "," 读取文件错误-getFloat-port: "+port+" loc:"+index);
                    ex.printStackTrace();
                    return -1;
                }
            }else{
                return -2;
            }
        }else {
            return -3;
        }
    }

    public int eof(int port){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile!=null){
                try {
                    if(randomAccessFile.getFilePointer() < randomAccessFile.length()){
                        //Log.e("","eof("+port+") 0");
                        return 0;
                    }else{
                        //Log.e("","eof("+port+") 1");
                        return 1;
                    }
                }catch (IOException ex){
                    //Log.e(" error "," 读取文件错误-getInt-port: "+port+" loc:"+index);
                    ex.printStackTrace();
                    return -1;
                }
            }else{
                return -2;
            }
        }else {
            return -3;
        }
    }

    public int lof(int port){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile!=null){
                try {
                    return (int)randomAccessFile.length();
                }catch (IOException ex){
                    //Log.e(" error "," 读取文件错误-getInt-port: "+port+" loc:"+index);
                    ex.printStackTrace();
                    return -1;
                }
            }else{
                return -2;
            }
        }else {
            return -3;
        }
    }

    public int loc(int port){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile!=null){
                try {
                    return (int)randomAccessFile.getFilePointer();
                }catch (IOException ex){
                    //Log.e(" error "," 读取文件错误-getInt-port: "+port+" loc:"+index);
                    ex.printStackTrace();
                    return -1;
                }
            }else{
                return -2;
            }
        }else {
            return -3;
        }
    }

    public void seek(int port,int loc){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile!=null){
                try {
                    loc = loc >= randomAccessFile.length() ? (int)(randomAccessFile.length()-1) : loc;
                    loc = loc < 0 ? 0 : loc;
                    randomAccessFile.seek(loc);
                }catch (IOException ex){
                    //Log.e(" error "," 移动文件指针错误-seek-port: "+port+" loc:"+index+" seek: "+loc);
                    ex.printStackTrace();
                }
            }
        }
    }

    public void closefile(int port){
        String index = func_open_port2file.get(Integer.toString(port));
        if(index != null){
            RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
            if(randomAccessFile!=null){
                try {
                    randomAccessFile.close();
                }catch (IOException ex){
                    //Log.e(" error "," 关闭文件错误-closefile-port: "+port+" loc:"+index);
                    ex.printStackTrace();
                }finally {
                    openedfiles.set(Integer.valueOf(index),null);
                    func_open_port2file.remove(Integer.toString(port));
                    //Log.e("","关闭了一个文件，端口 "+port);
                }
            }
        }
    }

    public int gettick(){
        //Log.e("","gettick");
        if (gettickWhich == 1){
            return gettick_tick++;
        }else{
            return (int) (System.currentTimeMillis() % 1000000000l);
        }
    }

    public void closeAllOpenedFiles(){
        try {
            Iterator iterator = func_open_port2file.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry)iterator.next();
                String index = (String)entry.getValue();
                if(index!=null){
                    RandomAccessFile randomAccessFile = openedfiles.get(Integer.valueOf(index));
                    if(randomAccessFile!=null) randomAccessFile.close();
                }
            }
        }catch (IOException ex){
            //Log.e(" error "," 关闭文件失败-closeAllOpenedFiles ");
            ex.printStackTrace();
        }
    }

    public static int big2little(int big){
        int little = (int)(((big & 0xff000000)>>24)&0x000000ff)+((big & 0x00ff0000)>>8)+((big & 0x0000ff00)<<8)+((big & 0x00000000000000ff)<<24);
        return little;
    }

    public static byte[] changeByte(int data){
        byte b4 = (byte)((data)>>24);
        byte b3 = (byte)(((data)<<8)>>24);
        byte b2= (byte)(((data)<<16)>>24);
        byte b1 = (byte)(((data)<<24)>>24);
        byte[] bytes = {b1,b2,b3,b4};
        return bytes;
    }

    public static int BGR2RGB(int color){
        //Log.e(" color in ",""+Integer.toHexString(color));
        int blue = color&0x00ff0000;
        int green = color&0xff00ff00;
        int red = color&0xff0000ff;
        //Log.e("","blue: "+Integer.toHexString(blue)+" ,green: "+Integer.toHexString(green)+" ,red: "+Integer.toHexString(red));
        //Log.e(" color out ","red : "+Integer.toHexString((red<<16))+" green: "+Integer.toHexString(green)+" blue: "+Integer.toHexString((blue>>16)));
        int rgb = (red<<16)+(green)+(blue>>16);
        //Log.e(" rgb return ",""+Integer.toHexString(rgb));
        return rgb;
    }

}

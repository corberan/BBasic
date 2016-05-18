package com.liuzc.bbasic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by 政材 on 2015/1/19.
 */
public class KeyBoard extends ImageView {

    private float x,y;
    private Paint paint;
    private int KBwhich = -1;
    private int KB_wid,KB_hgt = 0;
    private int keyValue = 0;
//    private long lastKeyPressTime = 0;

    public KeyBoard(Context context) {
        super(context);

        this.setImageResource(R.drawable.playpad_gray_transparent);
        KBwhich = 0;

        paint = new Paint();
        paint.setColor((int)(Math.random() * 0xffffff) + 0xff000000);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        int softKey_x,softKey_y,softKey_w,softKey_h;
        switch (KBwhich){
            case 0:{ //上下左右
                if(x > 2 && y < 334){
                    if(y > 227){
                        softKey_w = 66;softKey_h = 78;softKey_y = 255;
                        if(x < 70){
                            softKey_x = 3;keyValue = 27;
                        }else if(x < 142){
                            x=y=0;KBwhich = 1;this.setImageResource(R.drawable.keyboard_letters_gray_transparent);break;
                        }else if(x > 410 && x < 480){
                            softKey_x = 411;keyValue = 13;
                        }else if(x > 201 && x < 280){
                            softKey_x = 202;softKey_w = 77;softKey_y = 228;keyValue = 40;
                        }else if(x > 338 && x < 406) {
                            x=y=0;this.setVisibility(INVISIBLE);break;
                        }else{
                            break;
                        }
                    }else if(y > 116){
                        softKey_w = 78;softKey_h = 78;softKey_y = 117;
                        if(x > 53 && x < 132){
                            softKey_x = 54;keyValue = 37;
                        }else if(x > 347 && x < 427){
                            softKey_x = 348;keyValue = 39;
                        }else {
                            break;
                        }
                    }else if(y > 5 && y < 85 && x > 201 && x < 281){
                        softKey_w = 77;softKey_h = 78;softKey_y = 6;softKey_x = 202;keyValue = 38;
                    }else {
                        break;
                    }
                    softKey_x = softKey_x * KB_wid / 480 ;
                    softKey_y = softKey_y * KB_hgt / 336 ;
                    softKey_w = softKey_w * KB_wid / 480 ;
                    softKey_h = softKey_h * KB_hgt / 336 ;
                    canvas.drawRect(softKey_x,softKey_y,softKey_x+softKey_w,softKey_y+softKey_h,paint);
                }
                break;
            }
            case 1:{ //英文
                if(y > 3) {
                    if (y > 252) {
                        softKey_y = 255;
                        softKey_h = 78;
                        if (x < 75) {
                            x=y=0;KBwhich = 2;this.setImageResource(R.drawable.keyboard_symbols_gray_transparent);break;
                        } else if (x < 166) {
                            x=y=0;KBwhich = 0;this.setImageResource(R.drawable.playpad_gray_transparent);break;
                        } else if (x < 310) {
                            softKey_x = 171;
                            softKey_w = 138;
                            keyValue = 32;
                        } else if (x < 358) {
                            softKey_x = 315;
                            softKey_w = 42;
                            keyValue = 188;
                        } else if (x < 406) {
                            softKey_x = 363;
                            softKey_w = 42;
                            keyValue = 190;
                        } else {
                            softKey_x = 411;
                            softKey_w = 66;
                            keyValue = 13;
                        }
                    } else if (y > 168) {
                        softKey_y = 172;
                        softKey_h = 78;
                        softKey_w = 42;
                        if (x > 74) {
                            if (x < 124) {
                                softKey_x = 75;keyValue = 90;
                            } else if (x < 172) {
                                softKey_x = 123;keyValue = 88;
                            } else if (x < 220) {
                                softKey_x = 171;keyValue = 67;
                            } else if (x < 268) {
                                softKey_x = 219;keyValue = 86;
                            } else if (x < 316) {
                                softKey_x = 267;keyValue = 66;
                            } else if (x < 364) {
                                softKey_x = 315;keyValue = 78;
                            } else if (x < 412) {
                                softKey_x = 363;keyValue = 77;
                            } else {
                                softKey_x = 411;
                                softKey_w = 66;
                                keyValue = 8;
                            }
                        }else{
                            x=y=0;break;
                        }
                    } else if (y > 84) {
                        softKey_y = 88;
                        softKey_h = 78;
                        softKey_w = 42;
                        if (x > 26 && x < 453) {
                            if (x < 75) {
                                softKey_x = 27;keyValue = 65;
                            } else if (x < 123) {
                                softKey_x = 75;keyValue = 83;
                            } else if (x < 171) {
                                softKey_x = 123;keyValue = 68;
                            } else if (x < 219) {
                                softKey_x = 171;keyValue = 70;
                            } else if (x < 267) {
                                softKey_x = 219;keyValue = 71;
                            } else if (x < 315) {
                                softKey_x = 267;keyValue = 72;
                            } else if (x < 363) {
                                softKey_x = 315;keyValue = 74;
                            } else if (x < 411){
                                softKey_x = 363;keyValue = 75;
                            } else {
                                softKey_x = 411;keyValue = 76;
                            }
                        }else{
                            x=y=0;break;
                        }
                    } else {
                        softKey_y = 4;
                        softKey_h = 78;
                        softKey_w = 42;
                        if (x < 51) {
                            softKey_x = 3;keyValue = 81;
                        } else if (x < 99) {
                            softKey_x = 51;keyValue = 87;
                        } else if (x < 147) {
                            softKey_x = 99;keyValue = 69;
                        } else if (x < 195) {
                            softKey_x = 147;keyValue = 82;
                        } else if (x < 243) {
                            softKey_x = 195;keyValue = 84;
                        } else if (x < 291) {
                            softKey_x = 243;keyValue = 89;
                        } else if (x < 339) {
                            softKey_x = 291;keyValue = 85;
                        } else if (x < 387) {
                            softKey_x = 339;keyValue = 73;
                        } else if (x < 435) {
                            softKey_x = 387;keyValue = 79;
                        } else {
                            softKey_x = 435;keyValue = 80;
                        }
                    }
                    softKey_x = softKey_x * KB_wid / 480;
                    softKey_y = softKey_y * KB_hgt / 336;
                    softKey_w = softKey_w * KB_wid / 480;
                    softKey_h = softKey_h * KB_hgt / 336;
                    canvas.drawRect(softKey_x, softKey_y, softKey_x + softKey_w, softKey_y + softKey_h, paint);
                    break;
                }
            }
            case 2:{ //符号
                if(y > 3) {
                    if (y > 252) {
                        softKey_y = 255;
                        softKey_h = 78;
                        if (x < 75) {
                            x=y=0;KBwhich = 1;this.setImageResource(R.drawable.keyboard_letters_gray_transparent);break;
                        } else if (x < 166) {
                            x=y=0;break;
                        } else if (x < 310) {
                            softKey_x = 171;
                            softKey_w = 138;
                            keyValue = 32;
                        } else if (x < 358) {
                            softKey_x = 315;
                            softKey_w = 42;
                            keyValue = 188;
                        } else if (x < 406) {
                            softKey_x = 363;
                            softKey_w = 42;
                            keyValue = 190;
                        } else {
                            softKey_x = 411;
                            softKey_w = 66;
                            keyValue = 13;
                        }
                    } else if (y > 168) {
                        softKey_y = 172;
                        softKey_h = 78;
                        softKey_w = 42;
                        if (x > 74) {
                            if (x < 124) {
                                softKey_x = 75;
                            } else if (x < 172) {
                                softKey_x = 123;
                            } else if (x < 220) {
                                softKey_x = 171;
                            } else if (x < 268) {
                                softKey_x = 219;
                            } else if (x < 316) {
                                softKey_x = 267;
                            } else if (x < 364) {
                                softKey_x = 315;
                            } else if (x < 412) {
                                softKey_x = 363;
                            } else {
                                softKey_x = 411;
                                softKey_w = 66;
                                keyValue = 8;
                            }
                        }else{
                            x=y=0;break;
                        }
                    } else if (y > 84) {
                        softKey_y = 88;
                        softKey_h = 78;
                        softKey_w = 42;
                        if (x < 51) {
                            softKey_x = 3;
                        } else if (x < 99) {
                            softKey_x = 51;
                        } else if (x < 147) {
                            softKey_x = 99;
                        } else if (x < 195) {
                            softKey_x = 147;
                        } else if (x < 243) {
                            softKey_x = 195;
                        } else if (x < 291) {
                            softKey_x = 243;
                        } else if (x < 339) {
                            softKey_x = 291;
                        } else if (x < 387) {
                            softKey_x = 339;
                        } else if (x < 435) {
                            softKey_x = 387;
                        } else {
                            softKey_x = 435;
                        }
                    } else {
                        softKey_y = 4;
                        softKey_h = 78;
                        softKey_w = 42;
                        if (x < 51) {
                            softKey_x = 3;keyValue = 49;
                        } else if (x < 99) {
                            softKey_x = 51;keyValue = 50;
                        } else if (x < 147) {
                            softKey_x = 99;keyValue = 51;
                        } else if (x < 195) {
                            softKey_x = 147;keyValue = 52;
                        } else if (x < 243) {
                            softKey_x = 195;keyValue = 53;
                        } else if (x < 291) {
                            softKey_x = 243;keyValue = 54;
                        } else if (x < 339) {
                            softKey_x = 291;keyValue = 55;
                        } else if (x < 387) {
                            softKey_x = 339;keyValue = 56;
                        } else if (x < 435) {
                            softKey_x = 387;keyValue = 57;
                        } else {
                            softKey_x = 435;keyValue = 48;
                        }
                    }
                    softKey_x = softKey_x * KB_wid / 480;
                    softKey_y = softKey_y * KB_hgt / 336;
                    softKey_w = softKey_w * KB_wid / 480;
                    softKey_h = softKey_h * KB_hgt / 336;
                    canvas.drawRect(softKey_x, softKey_y, softKey_x + softKey_w, softKey_y + softKey_h, paint);
                    break;
                }
                break;
            }
        }
    }

    public int getKeyValue() {
//        int value = keyValue;
//        lastKeyPressTime = System.currentTimeMillis();
        return keyValue;
//        int i = keyValue;
        //keyValue = 0;
//        return i;
    }

    public void setKeyValue(int keyValue) {
//        Log.e("","set0 "+keyValue);
        this.keyValue = keyValue;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getPointerCount() == 1){
            if (KB_hgt == 0) {
                KB_wid = getWidth();
                KB_hgt = getHeight();
            }
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN) {
                x = event.getX() * 480 / KB_wid;
                y = event.getY() * 336 / KB_hgt;
                this.invalidate();
//                lastKeyPressTime = System.currentTimeMillis();
            }else if(action == MotionEvent.ACTION_UP) {
                x = y = 0;
                keyValue = 0;
                this.invalidate();
            }
//            else if (action == MotionEvent.ACTION_MOVE){ // !=0是为了不在keyboard刚被贴上时触发事件，阻塞ui
//                keyValue = 0;
//                if (System.currentTimeMillis() - lastKeyPressTime > 30){
//                    lastKeyPressTime = System.currentTimeMillis();
//                    x = event.getX() * 480 / KB_wid;
//                    y = event.getY() * 336 / KB_hgt;
//                    this.invalidate();
//                }
//            } else{
//                keyValue = 0;
//            }
//            Log.e("",""+event);
//            Log.e("BBASIC","keyValue "+keyValue);
        }
        return true;
    }

}

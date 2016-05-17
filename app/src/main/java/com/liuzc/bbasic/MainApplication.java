package com.liuzc.bbasic;

import android.app.Application;

/**
 * Created by liuzc on 16/5/3.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(getApplicationContext());
    }
}

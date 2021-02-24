package com.dspread.demoui;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory;

import xcrash.XCrash;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            // Skip app initialization.
            //detect if download the apk from google play
            return;
        }
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //  默认初始化
        XCrash.init(this);
        MultiDex.install(this);
    }

}

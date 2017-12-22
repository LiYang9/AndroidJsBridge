package com.github.liyang.androidjsbridge;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * author : LiYang
 * email  : yang.li@meiauto.cn
 * time   : 2017/12/18
 */
public class App extends Application {

    private static RefWatcher sRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        sRefWatcher = LeakCanary.install(this);
    }


    public static RefWatcher getRefWatcher() {
        return sRefWatcher;
    }

}

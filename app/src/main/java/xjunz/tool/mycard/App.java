/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class App extends Application {
    private static Settings sSettings;
    private static Context sApplicationContext;
    private static boolean sIsYGOMobileInstalled;

    public static Context getContext() {
        return sApplicationContext;
    }

    public static boolean isYGOMobileInstalled() {
        return sIsYGOMobileInstalled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sSettings = new Settings(getApplicationContext().getSharedPreferences("config", MODE_PRIVATE));
        sApplicationContext = getApplicationContext();
        try {
            getPackageManager().getApplicationInfo("cn.garymb.ygomobile", PackageManager.GET_UNINSTALLED_PACKAGES);
            sIsYGOMobileInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            sIsYGOMobileInstalled = false;
        }
    }

    @NonNull
    public static String getStringOf(@StringRes int strRes) {
        return sApplicationContext.getString(strRes);
    }

    @NonNull
    public static String getStringOf(@StringRes int strRes, Object... args) {
        return sApplicationContext.getString(strRes, args);
    }

    public static Settings config() {
        return sSettings;
    }


}

/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xjunz.tool.mycard.api.CheckUpdateService;
import xjunz.tool.mycard.api.Constants;
import xjunz.tool.mycard.api.bean.UpdateInfo;
import xjunz.tool.mycard.util.TokenGenerator;
import xjunz.tool.mycard.util.Utils;

public class App extends Application {
    private static Settings sSettings;
    private static Context sApplicationContext;
    private static boolean sIsYGOMobileInstalled;
    private static int sVersionCode;
    private static boolean sHasUpdate;
    private static String sVersionName;
    private static String sWatchToken;

    public static Context getContext() {
        return sApplicationContext;
    }

    public static boolean isYGOMobileInstalled() {
        return sIsYGOMobileInstalled;
    }

    public static String getVersionName() {
        return sVersionName;
    }

    public static int getVersionCode() {
        return sVersionCode;
    }

    public static void setHasUpdate(boolean sHasUpdate) {
        App.sHasUpdate = sHasUpdate;
    }

    public static boolean hasUpdate() {
        return sHasUpdate;
    }


    public static String getWatchToken() {
        return sWatchToken;
    }

    public static void setWatchToken(String watchToken) {
        App.sWatchToken = watchToken;
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
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            sVersionName = info.versionName;
            sVersionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (config().id.getValue() != -1) {
            setWatchToken(TokenGenerator.generate(config().id.getValue()));
        }
        OkHttpClient client = new OkHttpClient.Builder().build();
        CheckUpdateService checkUpdateService = new Retrofit.Builder()
                .baseUrl(Constants.CHECK_UPDATE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(CheckUpdateService.class);
        checkUpdateService.checkUpdate(Constants.CHECK_UPDATE_FIR_API_TOKEN).enqueue(new Utils.CallbackAdapter<UpdateInfo>() {
            @Override
            public void onResponse(@NonNull Call<UpdateInfo> call, @NonNull Response<UpdateInfo> response) {
                super.onResponse(call, response);
                UpdateInfo info = response.body();
                if (info != null) {
                    if (sVersionCode < info.getBuild()) {
                        sHasUpdate = true;
                    }
                }
            }
        });
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

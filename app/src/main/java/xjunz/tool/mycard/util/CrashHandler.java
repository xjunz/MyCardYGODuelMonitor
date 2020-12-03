/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.util;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.ui.MasterToast;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        String stacktrace = Utils.readExceptionStackTrace(e);
        ClipboardManager cm = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(stacktrace);
        new Thread(() -> {
            Looper.prepare();
            MasterToast.shortToast("正在收集错误信息，即将退出...");
            Looper.loop();
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        System.exit(1);
    }
}


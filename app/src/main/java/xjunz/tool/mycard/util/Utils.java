/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.util;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.transition.Transition;
import android.view.View;
import android.view.animation.BounceInterpolator;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.api.Constants;
import xjunz.tool.mycard.ui.MasterToast;

public class Utils {


    public static void launchWatch(@NonNull Context context, String roomId) {
        if (App.isYGOMobileInstalled()) {
            context.startActivity(buildLaunchWatchIntent(App.config().username.getValue(), App.getWatchToken(), roomId));
        } else {
            MasterToast.shortToast(R.string.start_watch_failed);
        }
    }


    @NonNull
    public static Intent buildLaunchWatchIntent(String watchAs, String token, String roomId) {
        Intent intent = new Intent("ygomobile.intent.action.GAME");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("host", "tiramisu.mycard.moe");
        intent.putExtra("port", 8911);
        intent.putExtra("user", watchAs);
        intent.putExtra("room", token + roomId);
        return intent;
    }

    public static int getAttrColor(@NonNull Context context, @AttrRes int attr) {
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{attr});
        int color = typedArray.getColor(0, -1);
        typedArray.recycle();
        return color;
    }


    @NonNull
    public static String formatDate(Locale locale, long timestamp) {
        Date date = new Date(timestamp);
        return String.format(locale, "%tF %tT", date, date);
    }


    @NonNull
    public static Retrofit createRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_API_ARENA)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    @NonNull
    public static Retrofit createRetrofit(int timeoutSecs) {
        OkHttpClient client = new OkHttpClient.Builder().callTimeout(timeoutSecs, TimeUnit.SECONDS).readTimeout(timeoutSecs, TimeUnit.SECONDS).build();
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_API_ARENA)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public static void swing(@NonNull View view) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -20F, 20F, 0F);
        objectAnimator.setInterpolator(new BounceInterpolator());
        objectAnimator.start();
    }

    public static class CallbackAdapter<T> implements Callback<T> {

        @Override
        public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
            onWhatever();
        }

        @Override
        public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
            onWhatever();
        }

        public void onWhatever() {

        }
    }

    public static class TransitionListenerAdapter implements Transition.TransitionListener {
        @Override
        public void onTransitionStart(Transition transition) {

        }

        @Override
        public void onTransitionEnd(Transition transition) {

        }

        @Override
        public void onTransitionCancel(Transition transition) {

        }

        @Override
        public void onTransitionPause(Transition transition) {

        }

        @Override
        public void onTransitionResume(Transition transition) {

        }
    }
}

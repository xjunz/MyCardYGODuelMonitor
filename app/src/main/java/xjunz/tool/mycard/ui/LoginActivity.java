/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.api.Constants;
import xjunz.tool.mycard.api.LoginService;
import xjunz.tool.mycard.api.bean.UserInfo;
import xjunz.tool.mycard.databinding.ActivityLoginBinding;
import xjunz.tool.mycard.util.TokenGenerator;
import xjunz.tool.mycard.util.Utils;

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_DUEL_ID = "WatchSetupActivity.extra.duelId";
    private String duelId;
    private final ObservableBoolean mLoading = new ObservableBoolean(false);
    private final ObservableInt mRetryTimeLimit = new ObservableInt(0);
    private ActivityLoginBinding mBinding;
    private LoginService mLoginService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果未安装YGO Mobile，直接提示退出
        if (!App.isYGOMobileInstalled()) {
            MasterToast.shortToast(R.string.ygo_mobile_not_installed);
            finish();
            return;
        }
        duelId = getIntent().getStringExtra(EXTRA_DUEL_ID);
        if (App.config().hasLoggedIn()) {
            if (duelId != null) {
                Utils.launchWatch(this, duelId);
            } else {
                MasterToast.shortToast("Unexpected null duel id.");
            }
            finish();
            return;
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mBinding.setHost(this);
        mBinding.setLoading(mLoading);
        mBinding.setWait(mRetryTimeLimit);
        OkHttpClient client = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
        mLoginService = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.BASE_API_ACCOUNTS)
                .client(client)
                .build().create(LoginService.class);
    }

    private void transitToLoad(@NonNull Runnable endAction) {
        AutoTransition transition = new AutoTransition();
        transition.setInterpolator(AnimationUtils.loadInterpolator(LoginActivity.this, android.R.interpolator.fast_out_slow_in));
        transition.addListener(new Utils.TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                if (endAction != null) {
                    endAction.run();
                }
            }
        });
        TransitionManager.beginDelayedTransition((ViewGroup) mBinding.getRoot(), transition);
        mLoading.set(true);
        mBinding.executePendingBindings();
    }

    private void transitToLogin() {
        mLoading.set(false);
        AutoTransition transition = new AutoTransition();
        transition.setInterpolator(AnimationUtils.loadInterpolator(LoginActivity.this, android.R.interpolator.fast_out_slow_in));
        TransitionManager.beginDelayedTransition((ViewGroup) mBinding.getRoot(), transition);
    }

    public void login(String username, String pwd) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
            MasterToast.shortToast(R.string.msg_input_uncompletely);
            return;
        }
        transitToLoad(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> userInfo = new HashMap<>();
                userInfo.put("account", username);
                userInfo.put("password", pwd);
                mLoginService.login(userInfo).enqueue(new Utils.CallbackAdapter<UserInfo>() {
                    @Override
                    public void onResponse(@NonNull Call<UserInfo> call, @NonNull Response<UserInfo> response) {
                        super.onResponse(call, response);
                        UserInfo info = response.body();
                        if (info != null) {
                            App.config().id.setValue(info.getUser().getId());
                            App.setWatchToken(TokenGenerator.generate(info.getUser().getId()));
                            App.config().username.setValue(info.getUser().getUsername());
                            MasterToast.shortToast(R.string.login_successfully);
                            mBinding.getRoot().animate().alpha(0).scaleY(1.2f).scaleX(1.2f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    finish();
                                }
                            }).start();
                            if (duelId != null) {
                                Utils.launchWatch(LoginActivity.this, duelId);
                            }
                        } else {
                            MasterToast.shortToast(R.string.login_failed);
                            transitToLogin();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
                        super.onFailure(call, t);
                        MasterToast.shortToast(getString(R.string.login_error) + ": " + t.getMessage());
                        transitToLogin();
                    }
                });
            }
        });
    }

    public void cancel(View view) {
        finishAfterTransition();
    }

}

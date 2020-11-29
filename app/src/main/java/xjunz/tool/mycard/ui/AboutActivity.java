/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.api.CheckUpdateService;
import xjunz.tool.mycard.api.Constants;
import xjunz.tool.mycard.api.bean.UpdateInfo;
import xjunz.tool.mycard.databinding.ActivityAboutBinding;
import xjunz.tool.mycard.util.Utils;

public class AboutActivity extends AppCompatActivity {
    private ActivityAboutBinding mBinding;
    private final String[] OS_PROJECTS_NAME = new String[]{"Android Jetpack", "Java WebSocket", "RxJava", "RxAndroid", "Retrofit", "Material design icons"};
    private final String[] OS_PROJECTS_URL = new String[]{"https://github.com/androidx/androidx", "https://github.com/TooTallNate/Java-WebSocket", "https://github.com/ReactiveX/RxJava", "https://github.com/ReactiveX/RxAndroid", "https://github.com/square/retrofit", "https://github.com/google/material-design-icons"};
    private final String[] OS_PROJECTS_LICENSE = new String[]{"Apache License 2.0", "MIT License", "Apache License 2.0", "Apache License 2.0", "Apache License 2.0", "Apache License 2.0"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        mBinding.rvOs.setAdapter(new OsAdapter());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        OkHttpClient client = new OkHttpClient.Builder().build();
        mCheckUpdateService = new Retrofit.Builder()
                .baseUrl(Constants.CHECK_UPDATE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(CheckUpdateService.class);
    }

    private void viewURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(Intent.createChooser(intent, getString(R.string.browser_chooser_title)));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDeveloper(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://mycard.moe/ygopro/arena/#/userinfo?username=xjunz"));
        startActivity(Intent.createChooser(intent, getString(R.string.browser_chooser_title)));
    }

    private CheckUpdateService mCheckUpdateService;
    private long mLastCheckTimestamp;

    public void checkUpdate(View view) {
        if (mLastCheckTimestamp != 0 && System.currentTimeMillis() - mLastCheckTimestamp <= 5 * 1000) {
            MasterToast.shortToast(R.string.try_later);
            return;
        }
        MasterToast.shortToast(R.string.checking_update);
        mLastCheckTimestamp = System.currentTimeMillis();
        mCheckUpdateService.checkUpdate(Constants.CHECK_UPDATE_FIR_API_TOKEN).enqueue(new Utils.CallbackAdapter<UpdateInfo>() {
            @Override
            public void onResponse(@NonNull Call<UpdateInfo> call, @NonNull Response<UpdateInfo> response) {
                super.onResponse(call, response);
                if (AboutActivity.this.isDestroyed()) {
                    return;
                }
                UpdateInfo info = response.body();
                if (info == null) {
                    MasterToast.shortToast(R.string.check_update_failed);
                    return;
                }
                if (!App.getVersionName().equals(info.getVersionShort())) {
                    mBinding.updateBadge.setVisibility(View.VISIBLE);
                    App.setHasUpdate(true);
                    new AlertDialog.Builder(AboutActivity.this)
                            .setTitle(R.string.new_version_available)
                            .setMessage(getString(R.string.new_version_msg,
                                    info.getVersionShort(),
                                    info.getChangelog(),
                                    Formatter.formatFileSize(AboutActivity.this, info.getBinary().getFsize())))
                            .setPositiveButton(getString(R.string.download), (dialog, which) -> {
                                viewURL(info.getInstallUrl());
                                MasterToast.shortToast(R.string.what_if_download_fail);
                            }).setNegativeButton(android.R.string.cancel, null).show();
                } else {
                    MasterToast.shortToast(R.string.no_new_version);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateInfo> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                MasterToast.shortToast(R.string.check_update_failed);
            }

            @Override
            public void onWhatever() {
            }
        });

    }

    public void viewSource(View view) {
        viewURL("https://github.com/xjunz/MyCardYGODuelMonitor");
    }

    private static final String QQ_GROUP_NUM = "684446998";

    public void feedback(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=" + QQ_GROUP_NUM + "&card_type=group&source=qrcode"));
        try {
            startActivity(intent);
        } catch (Exception e) {
            MasterToast.shortToast(R.string.join_group_failed);
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(new ClipData("q_group_num", new String[]{"text/plain"}, new ClipData.Item(QQ_GROUP_NUM)));
        }
    }

    public void donate(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/fkx154567xmljmwmkmchc65?t=1606376518084"));
        try {
            startActivity(intent);
        } catch (Exception e) {
            MasterToast.shortToast(R.string.donate_failed);
        }
    }

    private class OsAdapter extends RecyclerView.Adapter<OsViewHolder> {
        @NonNull
        @Override
        public OsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OsViewHolder(getLayoutInflater().inflate(R.layout.item_os_project, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull OsViewHolder holder, int position) {
            holder.name.setText(OS_PROJECTS_NAME[position]);
            holder.license.setText(OS_PROJECTS_LICENSE[position]);
        }

        @Override
        public int getItemCount() {
            return OS_PROJECTS_NAME.length;
        }
    }

    private class OsViewHolder extends RecyclerView.ViewHolder {
        TextView name, license;

        public OsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            license = itemView.findViewById(R.id.tv_license);
            itemView.setOnClickListener(v -> viewURL(OS_PROJECTS_URL[getAdapterPosition()]));
        }
    }
}

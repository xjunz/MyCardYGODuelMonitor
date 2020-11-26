/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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

import xjunz.coolapkcheckupdatezerosdk.ApkInfo;
import xjunz.coolapkcheckupdatezerosdk.UpdateChecker;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {
    private ActivityAboutBinding mBinding;
    private final String[] OS_PROJECTS_NAME = new String[]{"Android Jetpack", "Java WebSocket", "RxJava", "RxAndroid", "Retrofit", "Material design icons"};
    private final String[] OS_PROJECTS_URL = new String[]{"https://github.com/androidx/androidx", "https://github.com/TooTallNate/Java-WebSocket", "https://github.com/ReactiveX/RxJava", "https://github.com/ReactiveX/RxAndroid", "https://github.com/square/retrofit", "https://github.com/google/material-design-icons"};
    private final String[] OS_PROJECTS_LICENSE = new String[]{"Apache License 2.0", "MIT License", "Apache License 2.0", "Apache License 2.0", "Apache License 2.0", "Apache License 2.0"};
    private String mVersionName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        mBinding.rvOs.setAdapter(new OsAdapter());
        try {
            mVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            mBinding.setVersionName(mVersionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void viewURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(Intent.createChooser(intent, getString(R.string.broswer_chooser_title)));
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
        intent.setData(Uri.parse("http://www.coolapk.com/u/639020"));
        startActivity(Intent.createChooser(intent, getString(R.string.broswer_chooser_title)));
    }

    public void checkUpdate(View view) {
        new UpdateChecker(getPackageName(), new UpdateChecker.UpdateCheckCallback() {
            @Override
            public void onStart() {
                MasterToast.shortToast(R.string.checking_update);
            }

            @Override
            public void onSuccess(ApkInfo info) {
                if (AboutActivity.this.isDestroyed()) {
                    return;
                }
                if (!mVersionName.equals(info.getVersionName())) {
                    new AlertDialog.Builder(AboutActivity.this)
                            .setTitle(R.string.new_version_available)
                            .setMessage(getString(R.string.new_version_msg, info.getVersionName(), info.getChangelog()))
                            .setPositiveButton(getString(R.string.download), (dialog, which) -> {
                                viewURL(info.getDownloadURL());
                            }).setPositiveButton(android.R.string.cancel, null).show();
                } else {
                    MasterToast.shortToast(R.string.no_new_version);
                }
            }

            @Override
            public void onFail() {
                MasterToast.shortToast(R.string.chek_update_failed);
            }
        }).start();
    }

    public void viewSource(View view) {
        viewURL("https://github.com/xjunz/MyCardYGODuelMonitor");
    }

    public void feedback(View view) {
    }

    public void donate(View view) {
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
            itemView.setOnClickListener(v -> {
                viewURL(OS_PROJECTS_URL[getAdapterPosition()]);
            });
        }
    }
}

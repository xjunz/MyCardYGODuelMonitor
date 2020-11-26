/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.util.Utils;

public class WatchSetupActivity extends AppCompatActivity {
    private EditText mEtUsername, mEtToken;
    public static final String EXTRA_DUEL_ID = "WatchSetupActivity.extra.duelId";
    private String duelId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        duelId = getIntent().getStringExtra(EXTRA_DUEL_ID);
        if (App.config().hasCompleteWatchConfig()) {
            if (duelId != null) {
                startActivity(Utils.buildLaunchWatchIntent(duelId));
            } else {
                MasterToast.shortToast("Unexpected null duel id.");
            }
            finishAndRemoveTask();
            return;
        }
        setContentView(R.layout.activity_watch_setup);
        mEtUsername = findViewById(R.id.et_username);
        mEtToken = findViewById(R.id.et_token);
        mEtUsername.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

    }

    public void confirm(View view) {
        String username = mEtUsername.getText().toString();
        String token = mEtToken.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(token)) {
            MasterToast.shortToast(R.string.msg_input_uncompletely);
            return;
        }
        if (token.length() > 8) {
            MasterToast.shortToast(R.string.token_over_length);
            return;
        }
        App.config().username.setValue(username);
        App.config().token.setValue(token);
        MasterToast.longToast(R.string.msg_reset_watch_config_in_settings);
        if (duelId != null) {
            startActivity(Utils.buildLaunchWatchIntent(duelId));
        }
        finishAfterTransition();
    }

    public void ignoreUsername(View view) {
        if (duelId != null) {
            startActivity(Utils.buildLaunchWatchIntent(getString(R.string.def_username), getString(R.string.def_token), duelId));
        }
    }

    public void cancel(View view) {
        finishAfterTransition();
    }

    public void help(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.help)
                .setMessage(R.string.help_token_and_username)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}

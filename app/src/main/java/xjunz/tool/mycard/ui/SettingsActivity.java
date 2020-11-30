/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.util.Objects;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.databinding.ActivitySettingsBinding;
import xjunz.tool.mycard.ui.fragment.ConditionFragment;
import xjunz.tool.mycard.ui.fragment.InputFragment;
import xjunz.tool.mycard.ui.fragment.WhitelistFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        binding.setActivity(this);
        binding.setSettings(App.config());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    public void helpWhiteHot() {
        new AlertDialog.Builder(this).setTitle(R.string.help).setMessage(R.string.help_whitehot).show();
    }

    public void helpReconnect() {
        new AlertDialog.Builder(this).setTitle(R.string.help).setMessage(R.string.help_connect).show();
    }

    public void configPushCondition(View view) {
        new ConditionFragment().show(getSupportFragmentManager(), "condition");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void configPushTime(View view) {
        new InputFragment().setCallback(new InputFragment.Callback() {
            @Override
            public void onConfirmed(String inputText) {
                if (TextUtils.isEmpty(inputText)) {
                    App.config().pushDelayMin.setValue(0);
                } else {
                    App.config().pushDelayMin.setValue(Integer.valueOf(inputText));
                }
            }

            @Override
            public void configEditText(EditText editText) {
                editText.setText(String.valueOf(App.config().pushDelayMin.getValue()));
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            @Override
            public String getIllegalInputToastText() {
                return getString(R.string.token_over_length);
            }
        }).setTitle(getString(R.string.push_minutes_after_duel))
                .setCaption(getText(R.string.caption_push))
                .show(getSupportFragmentManager(), "push_delay");
    }

    public void configRankListLoadTimeout(View view) {
        new InputFragment().setCallback(new InputFragment.Callback() {
            @Override
            public void onConfirmed(String inputText) {
                if (TextUtils.isEmpty(inputText)) {
                    App.config().rankingListLoadTimeout.restoreDefault();
                } else {
                    int timeout = Integer.parseInt(inputText);
                    if (timeout == 0) {
                        App.config().rankingListLoadTimeout.restoreDefault();
                    } else {
                        App.config().rankingListLoadTimeout.setValue(timeout);
                    }
                }
            }

            @Override
            public void configEditText(EditText editText) {
                editText.setText(String.valueOf(App.config().rankingListLoadTimeout.getValue()));
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

        }).setTitle(getString(R.string.ranking_list_load_timeout)).show(getSupportFragmentManager(), "ranking_list_load_timeout");

    }

    public void configDuelRankLoadTimeout(View view) {
        new InputFragment().setCallback(new InputFragment.Callback() {
            @Override
            public void onConfirmed(String inputText) {
                if (TextUtils.isEmpty(inputText)) {
                    App.config().duelRankLoadTimeout.restoreDefault();
                } else {
                    int timeout = Integer.parseInt(inputText);
                    if (timeout == 0) {
                        App.config().duelRankLoadTimeout.restoreDefault();
                    } else {
                        App.config().duelRankLoadTimeout.setValue(timeout);
                    }
                }
            }

            @Override
            public void configEditText(EditText editText) {
                editText.setText(String.valueOf(App.config().duelRankLoadTimeout.getValue()));
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

        }).setTitle(getString(R.string.duel_rank_load_timeout)).setCaption(getString(R.string.caption_duel_rank_load_timeout)).show(getSupportFragmentManager(), "rank_load_timeout");
    }

    public void configDuelRankTimeoutReloadTimes(View view) {
        new InputFragment().setCallback(new InputFragment.Callback() {
            @Override
            public void onConfirmed(String inputText) {
                if (TextUtils.isEmpty(inputText)) {
                    App.config().duelRankLoadRetryTimes.restoreDefault();
                } else {
                    App.config().duelRankLoadRetryTimes.setValue(Integer.parseInt(inputText));
                }
            }

            @Override
            public void configEditText(EditText editText) {
                editText.setText(String.valueOf(App.config().duelRankLoadRetryTimes.getValue()));
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

        }).setTitle(getString(R.string.duel_rank_auto_try_times)).show(getSupportFragmentManager(), "dr_retry_timeout");
    }

    public void configWhitelist(View view) {
        new WhitelistFragment().show(getSupportFragmentManager(), "whitelist");
    }

    public void logInOrOut(View view) {
        if (App.config().hasLoggedIn()) {
            App.config().logOut();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    public void logInOrOutConfirm(View view) {
        if (App.config().hasLoggedIn()) {
            new AlertDialog.Builder(this).setTitle(R.string.alert).setMessage(R.string.alert_log_out)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> App.config().logOut()).setNegativeButton(android.R.string.cancel, null).show();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}

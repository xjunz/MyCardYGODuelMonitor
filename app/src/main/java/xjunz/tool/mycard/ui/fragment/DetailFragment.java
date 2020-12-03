/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.fragment.app.DialogFragment;

import java.util.HashSet;
import java.util.Set;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.api.bean.Duel;
import xjunz.tool.mycard.databinding.FragmentDetailBinding;
import xjunz.tool.mycard.ui.LoginActivity;
import xjunz.tool.mycard.ui.MainActivity;
import xjunz.tool.mycard.ui.MasterToast;
import xjunz.tool.mycard.util.Utils;

public class DetailFragment extends DialogFragment {
    private final ObservableInt mWhitelisted = new ObservableInt(0);
    private final ObservableBoolean mIsFinished = new ObservableBoolean(false);
    private Duel mDuel;
    private FragmentDetailBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Base_Dialog);
    }

    public Duel getDuel() {
        return mDuel;
    }

    public DetailFragment setDuel(@NonNull Duel duel) {
        mDuel = duel;
        return this;
    }

    public void addToOrRemoveFromWhitelist(int which) {
        Set<String> set = App.config().pushWhiteList.getValue();
        Set<String> whitelist = set == null ? new HashSet<>() : new HashSet<>(set);
        String name = mDuel.getPlayerName(which);
        if (mWhitelisted.get() == which || mWhitelisted.get() == 3) {
            MasterToast.shortToast(getString(R.string.msg_un_whitelisted, name));
            whitelist.remove(name);
            mWhitelisted.set(mWhitelisted.get() - which);
        } else {
            MasterToast.shortToast(getString(R.string.msg_whitelisted, name));
            whitelist.add(name);
            mWhitelisted.set(mWhitelisted.get() + which);
        }
        App.config().pushWhiteList.setValue(whitelist);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!mDuel.isSuccess()) {
            mDuel.playerLoadState.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (mDuel.playerLoadState.get() == Duel.LOAD_STATE_SUCCESS) {
                        mBinding.setP1(mDuel.getPlayer1());
                        mBinding.setP2(mDuel.getPlayer2());
                        sender.removeOnPropertyChangedCallback(this);
                    }
                }
            });
        } else {
            mBinding.setP1(mDuel.getPlayer1());
            mBinding.setP2(mDuel.getPlayer2());
        }
        mWhitelisted.set(App.config().isWhitelisted(mDuel));
        mBinding.setWhitelisted(mWhitelisted);
        mBinding.setDuel(mDuel);
        mBinding.setFinished(mIsFinished);
        mBinding.setHost(this);
    }

    public void reload() {
        MainActivity main = (MainActivity) requireActivity();
        main.getWatchService().loadPlayerRank(mDuel, false);
    }

    public void launchWatch() {
        if (!App.isYGOMobileInstalled()) {
            MasterToast.shortToast(R.string.ygo_mobile_not_installed);
            return;
        }
        if (App.config().hasLoggedIn()) {
            Utils.launchWatch(requireContext(), mDuel.getId());
        } else {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.putExtra(LoginActivity.EXTRA_DUEL_ID, mDuel.getId());
            startActivity(intent);
        }
    }

    public void notifyDuelFinished() {
        mIsFinished.set(true);
    }
}

/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.databinding.FragmentPushConditionBinding;

public class ConditionFragment extends DialogFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Base_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentPushConditionBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_push_condition, container, false);
        binding.setHost(this);
        binding.setSettings(App.config());
        return binding.getRoot();
    }


    private Object check(@NonNull String src) {
        return src.length() == 0 ? 0 : src;
    }

    public void confirm(String p1r1, String p1r2, String p2r1, String p2r2, int checkedId) {
        App.config().player1RankLimit.setValue(check(p1r1) + "," + check(p1r2));
        App.config().player2RankLimit.setValue(check(p2r1) + "," + check(p2r2));
        App.config().isConditionAnd.setValue(checkedId == R.id.rb_and);
        dismiss();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}

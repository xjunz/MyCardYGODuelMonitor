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
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.fragment.app.DialogFragment;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.databinding.FragmentSortBinding;

public class SortFragment extends DialogFragment {
    private final ObservableBoolean mAscending = new ObservableBoolean(true);
    private final ObservableInt mSortBy = new ObservableInt(0);
    private Runnable mCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Base_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentSortBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sort, container, false);
        binding.setHost(this);
        mAscending.set(App.config().watchListAscending.get());
        mSortBy.set(App.config().watchListSortBy.get());
        binding.setAscending(mAscending);
        binding.setSortBy(mSortBy);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public SortFragment setCallback(Runnable callback) {
        this.mCallback = callback;
        return this;
    }

    public void apply() {
        if (App.config().watchListAscending.get() != mAscending.get() || App.config().watchListSortBy.get() != mSortBy.get()) {
            App.config().watchListAscending.set(mAscending.get());
            App.config().watchListSortBy.set(mSortBy.get());
            if (mCallback != null) {
                mCallback.run();
            }
        }
        dismiss();
    }
}

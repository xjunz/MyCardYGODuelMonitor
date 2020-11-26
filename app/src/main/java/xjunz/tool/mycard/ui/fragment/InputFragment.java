/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import xjunz.tool.mycard.R;
import xjunz.tool.mycard.databinding.FragmentInputBinding;
import xjunz.tool.mycard.ui.MasterToast;
import xjunz.tool.mycard.util.Utils;

public class InputFragment extends DialogFragment {
    private Callback mCallback;

    public static class Callback {
        public void onCancelled() {
        }

        public void onConfirmed(String inputText) {
        }

        public boolean illegalInput(String input) {
            return false;
        }

        public String getIllegalInputToastText() {
            return null;
        }

        public void configEditText(EditText editText) {

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Base_Dialog);
    }

    public InputFragment setCallback(Callback callback) {
        this.mCallback = callback;
        return this;
    }

    private String mTitleStr;
    private CharSequence mCaptionStr;

    public String getTitle() {
        return mTitleStr;
    }

    public InputFragment setTitle(String title) {
        this.mTitleStr = title;
        return this;
    }

    public CharSequence getCaption() {
        return mCaptionStr;
    }

    public InputFragment setCaption(CharSequence caption) {
        this.mCaptionStr = caption;
        return this;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCallback = null;
    }

    private FragmentInputBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_input, container, false);
        mBinding.setHost(this);
        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText etInput = mBinding.etInput;
        if (mCallback != null) {
            mCallback.configEditText(etInput);
        }
        mBinding.btnOk.setOnClickListener(v -> {
            if (mCallback != null) {
                if (mCallback.illegalInput(etInput.getText().toString())) {
                    if (mCallback.getIllegalInputToastText() != null) {
                        MasterToast.shortToast(mCallback.getIllegalInputToastText());
                    }
                    Utils.swing(etInput);
                } else {
                    mCallback.onConfirmed(etInput.getText().toString());
                    dismiss();
                }
            } else {
                dismiss();
            }
        });
        mBinding.btnCancel.setOnClickListener(v -> {
            dismiss();
            if (mCallback != null) {
                mCallback.onCancelled();
            }
        });
    }


}

/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.databinding.FragmentWhitelistBinding;
import xjunz.tool.mycard.databinding.ItemChipBinding;
import xjunz.tool.mycard.ui.MasterToast;

public class WhitelistFragment extends DialogFragment {
    private FragmentWhitelistBinding mBinding;
    private List<String> mWhitelist;
    private WhitelistAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Base_Dialog);
        Set<String> set = App.config().pushWhiteList.getValue();
        if (set == null) {
            mWhitelist = new ArrayList<>();
        } else {
            mWhitelist = new ArrayList<>(set);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_whitelist, container, false);
        mBinding.setHost(this);
        mBinding.setEnable(App.config().enableWhitelist);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new WhitelistAdapter();
        mBinding.rvWhitelist.setAdapter(mAdapter);
    }

    public void addToWhitelist(String name) {
        if (!TextUtils.isEmpty(name)) {
            if (mWhitelist.contains(name)) {
                MasterToast.shortToast(R.string.whitelist_duplication);
                return;
            }
            mWhitelist.add(name);
            mAdapter.notifyItemInserted(mWhitelist.size() - 1);
            mBinding.etInput.getText().clear();
        }
    }

    public void confirm() {
        App.config().pushWhiteList.setValue(new HashSet<>(mWhitelist));
        dismiss();
    }

    public void importFromRankList() {
        new ImportFragment().setPreSelected(mWhitelist).setCallback(selected -> {
            if (selected.size() != 0) {
                mWhitelist.addAll(selected);
                mAdapter.notifyDataSetChanged();
            }
        }).show(requireFragmentManager(), "import");
    }

    private class WhitelistAdapter extends RecyclerView.Adapter<WhitelistAdapter.WhitelistViewHolder> {

        @NonNull
        @Override
        public WhitelistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new WhitelistViewHolder(DataBindingUtil.inflate(getLayoutInflater(), R.layout.item_chip, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull WhitelistViewHolder holder, int position) {
            holder.binding.setText(mWhitelist.get(position));
        }

        @Override
        public int getItemCount() {
            return mWhitelist.size();
        }

        private class WhitelistViewHolder extends RecyclerView.ViewHolder {
            ItemChipBinding binding;

            public WhitelistViewHolder(@NonNull ItemChipBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                this.binding.ibRemove.setOnClickListener(v -> {
                    mWhitelist.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                });
            }
        }
    }
}

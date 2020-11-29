/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.api.LoadPlayerListService;
import xjunz.tool.mycard.api.bean.Player;
import xjunz.tool.mycard.databinding.FragmentImportBinding;
import xjunz.tool.mycard.ui.MasterToast;
import xjunz.tool.mycard.util.Utils;

public class ImportFragment extends DialogFragment {
    private final List<String> mSelected = new ArrayList<>();
    private final ObservableBoolean mLoading = new ObservableBoolean(true);
    private final ObservableBoolean mSuccess = new ObservableBoolean(false);
    private List<Player> mRankingList;
    private List<String> mPreSelected;
    private FragmentImportBinding mBinding;
    private Callback mCallback;
    private Call<List<Player>> mLoadListCall;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Base_Dialog);
        mLoading.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                TransitionManager.beginDelayedTransition((ViewGroup) mBinding.getRoot());
            }
        });
        mSuccess.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mSuccess.get()) {
                    mBinding.rvImport.setAdapter(new ImportAdapter());
                }
            }
        });
    }

    public ImportFragment setCallback(Callback callback) {
        this.mCallback = callback;
        return this;
    }

    public ImportFragment setPreSelected(List<String> selected) {
        mPreSelected = selected;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_import, container, false);
        mBinding.setLoading(mLoading);
        mBinding.setSuccess(mSuccess);
        mBinding.setHost(this);
        return mBinding.getRoot();
    }

    public void confirm() {
        if (mCallback != null) {
            mCallback.onReturn(mSelected);
        }
        dismiss();
    }

    public void loadList() {
        mLoadListCall = Utils.createRetrofit(App.config().duelRankLoadTimeout.getValue()).create(LoadPlayerListService.class).loadPlayerList();
        mLoading.set(true);
        mLoadListCall.enqueue(new Utils.CallbackAdapter<List<Player>>() {
            @Override
            public void onResponse(@NonNull Call<List<Player>> call, @NonNull Response<List<Player>> response) {
                super.onResponse(call, response);
                mRankingList = response.body();
                mSuccess.set(mRankingList != null);
            }

            @Override
            public void onFailure(@NonNull Call<List<Player>> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                mSuccess.set(false);
                MasterToast.shortToast(getString(R.string.load_failed, t.getMessage()));
            }

            @Override
            public void onWhatever() {
                super.onWhatever();
                mLoading.set(false);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mLoadListCall != null) {
            mLoadListCall.cancel();
        }
    }

    public interface Callback {
        void onReturn(@NonNull List<String> selected);
    }

    private class ImportAdapter extends RecyclerView.Adapter<ImportAdapter.ImportViewHolder> {
        @NonNull
        @Override
        public ImportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImportViewHolder(getLayoutInflater().inflate(R.layout.item_import, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ImportViewHolder holder, int position) {
            String name = mRankingList.get(position).getUsername();
            holder.name.setText(name);
            boolean isPreSelected = mPreSelected.contains(name);
            holder.itemView.setEnabled(!isPreSelected);
            holder.ordinal.setText(String.valueOf(position + 1));
            holder.selected.setChecked(isPreSelected || mSelected.contains(name));
        }

        @Override
        public int getItemCount() {
            return mRankingList.size();
        }

        private class ImportViewHolder extends RecyclerView.ViewHolder {
            TextView name, ordinal;
            CheckBox selected;

            public ImportViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tv_name);
                ordinal = itemView.findViewById(R.id.tv_ordinal);
                selected = itemView.findViewById(R.id.cb_selected);
                itemView.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    String curName = mRankingList.get(getAdapterPosition()).getUsername();
                    if (mSelected.contains(curName)) {
                        mSelected.remove(curName);
                    } else {
                        mSelected.add(curName);
                    }
                    notifyItemChanged(pos);
                });
            }
        }
    }


}

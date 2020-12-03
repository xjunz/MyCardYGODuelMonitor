/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.WatchService;
import xjunz.tool.mycard.api.bean.Duel;
import xjunz.tool.mycard.databinding.ItemDuelBinding;
import xjunz.tool.mycard.util.Utils;

public class WatchFragment extends Fragment implements WatchService.DuelCallback {
    private RecyclerView mRvDuel;
    private DuelAdapter mDuelAdapter;
    private List<Duel> mDuels;
    private final Observable.OnPropertyChangedCallback mSettingsChangeCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            if (mDuelAdapter != null) {
                mDuelAdapter.notifyDataSetChanged();
            }
        }
    };
    private ProgressBar mProgress;
    private List<Duel> mOldDuels;
    private DetailFragment mCurDetail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        App.config().pushWhiteList.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().enableWhitelist.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().player1RankLimit.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().player2RankLimit.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().isConditionAnd.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().enableConditionedPush.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().highlightPro.addOnPropertyChangedCallback(mSettingsChangeCallback);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_watch, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvDuel = view.findViewById(R.id.rv_duel);
        mProgress = view.findViewById(R.id.progress);
        mRvDuel.setHasFixedSize(true);
        if (mDuels != null) {
            mDuelAdapter = new DuelAdapter();
            mRvDuel.setAdapter(mDuelAdapter);
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.watch_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_sort) {
            new SortFragment().setCallback(() -> {
                Collections.sort(mDuels);
                mDuelAdapter.notifyDataSetChanged();
            }).show(requireFragmentManager(), "sort");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInitList(List<Duel> initialDuels) {
        mDuels = new ArrayList<>(initialDuels);
        Collections.sort(mDuels);
        if (mRvDuel != null) {
            mDuelAdapter = new DuelAdapter();
            mRvDuel.setAdapter(mDuelAdapter);
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDuelCreated(Duel duel) {
        if (mDuelAdapter != null) {
            mDuels.add(duel);
            mDuelAdapter.notifyItemInserted(mDuels.size() - 1);
        }
    }

    @Override
    public void onDuelDeleted(String id) {
        if (mDuelAdapter != null) {
            if (mCurDetail != null && mCurDetail.isAdded()) {
                if (mCurDetail.getDuel().getId().equals(id)) {
                    mCurDetail.notifyDuelFinished();
                }
            }
            for (int i = 0; i < mDuels.size(); i++) {
                if (mDuels.get(i).getId().equals(id)) {
                    mDuels.remove(i);
                    mDuelAdapter.notifyItemRemoved(i);
                    return;
                }
            }
        }
    }

    @Override
    public void onPlayerInfoGot(Duel duel) {
        if (mDuelAdapter != null) {
            if (App.config().watchListSortBy.get() == 0) {
                int index = mDuels.indexOf(duel);
                if (index >= 0) {
                    mDuelAdapter.notifyItemChanged(index);
                }
                return;
            }
            mOldDuels = new ArrayList<>(mDuels);
            Collections.sort(mDuels);
            DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mOldDuels.size();
                }

                @Override
                public int getNewListSize() {
                    return mDuels.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mOldDuels.get(oldItemPosition).getId().equals(mDuels.get(newItemPosition).getId());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return !mOldDuels.get(oldItemPosition).getId().equals(duel.getId());
                }
            }, true).dispatchUpdatesTo(mDuelAdapter);
        }
    }

    @Override
    public void onPlayerInfoLoadFailed(Duel duel) {
        int index = mDuels.indexOf(duel);
        if (index >= 0) {
            mDuelAdapter.notifyItemChanged(index);
        }
    }


    private class DuelAdapter extends RecyclerView.Adapter<PlayerViewHolder> {

        private final int colorHighlight;
        private final int colorText;
        private final int colorFocusedHighlight;

        private DuelAdapter() {
            colorHighlight = Utils.getAttrColor(requireContext(), R.attr.colorAccent);
            colorText = Utils.getAttrColor(requireContext(), android.R.attr.textColorPrimary);
            colorFocusedHighlight = getResources().getColor(R.color.red_200, requireActivity().getTheme());
        }

        @NonNull
        @Override
        public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PlayerViewHolder(DataBindingUtil.inflate(getLayoutInflater(), R.layout.item_duel, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final PlayerViewHolder holder, int position) {
            Duel duel = mDuels.get(position);
            int whitelisted = App.config().isWhitelisted(duel);
            holder.binding.setDuel(duel);
            holder.binding.setWhitelisted(whitelisted);
            holder.binding.setFocused(App.config().isDuelInPushCondition(duel));
            holder.binding.executePendingBindings();
            if (duel.isLoadFailed()) {
                holder.binding.tvRank1.setText(R.string.load_failed);
                holder.binding.tvRank2.setText(R.string.load_failed);
            } else if (duel.isLoading()) {
                holder.binding.tvRank1.setText(R.string.loading);
                holder.binding.tvRank2.setText(R.string.loading);
            } else {
                holder.binding.tvRank1.setText(String.valueOf(duel.getPlayer1Rank()));
                holder.binding.tvRank2.setText(String.valueOf(duel.getPlayer2Rank()));
            }
            if (App.config().highlightPro.getValue()) {
                if (duel.isPlayer1Pro()) {
                    if (whitelisted != 0) {
                        holder.binding.tvPlayer1.setTextColor(colorFocusedHighlight);
                    } else {
                        holder.binding.tvPlayer1.setTextColor(colorHighlight);
                    }
                } else {
                    holder.binding.tvPlayer1.setTextColor(colorText);
                }
                if (duel.isPlayer2Pro()) {
                    if (whitelisted != 0) {
                        holder.binding.tvPlayer2.setTextColor(colorFocusedHighlight);
                    } else {
                        holder.binding.tvPlayer2.setTextColor(colorHighlight);
                    }
                } else {
                    holder.binding.tvPlayer2.setTextColor(colorText);
                }
            } else {
                holder.binding.tvPlayer1.setTextColor(colorText);
                holder.binding.tvPlayer2.setTextColor(colorText);
            }
        }

        @Override
        public int getItemCount() {
            return mDuels.size();
        }
    }

    private class PlayerViewHolder extends RecyclerView.ViewHolder {
        ItemDuelBinding binding;

        public PlayerViewHolder(@NonNull ItemDuelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(v -> {
                mCurDetail = new DetailFragment().setDuel(mDuels.get(getAdapterPosition()));
                mCurDetail.show(requireFragmentManager(), "detail");
            });
        }
    }
}

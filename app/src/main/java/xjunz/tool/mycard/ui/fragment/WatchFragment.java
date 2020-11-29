/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
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
import xjunz.tool.mycard.ui.MasterToast;
import xjunz.tool.mycard.ui.WatchSetupActivity;
import xjunz.tool.mycard.util.Utils;

public class WatchFragment extends Fragment implements WatchService.DuelCallback {
    private RecyclerView mRvDuel;
    private DuelAdapter mDuelAdapter;
    private List<Duel> mDuels;
    private final Observable.OnPropertyChangedCallback mSettingsChangeCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            mDuelAdapter.notifyDataSetChanged();
        }
    };
    private ProgressBar mProgress;
    private boolean mPendingLoad;
    private List<Duel> mOldDuels;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        App.config().pushWhiteList.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().enableWhitelist.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().player1RankLimit.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().player2RankLimit.addOnPropertyChangedCallback(mSettingsChangeCallback);
        App.config().isConditionAnd.addOnPropertyChangedCallback(mSettingsChangeCallback);
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
        if (mPendingLoad) {
            mDuelAdapter = new DuelAdapter();
            mRvDuel.setAdapter(mDuelAdapter);
            mProgress.setVisibility(View.GONE);
            mPendingLoad = false;
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
        if (mRvDuel == null) {
            mPendingLoad = true;
        } else {
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
    public void onPlayerRankGot(Duel duel) {
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


    private class DuelAdapter extends RecyclerView.Adapter<PlayerViewHolder> {
        private final int accentColor;
        private final int textColor;

        private DuelAdapter() {
            accentColor = Utils.getAttrColor(requireContext(), R.attr.colorAccent);
            textColor = Utils.getAttrColor(requireContext(), android.R.attr.textColorPrimary);
        }

        @NonNull
        @Override
        public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = getLayoutInflater().inflate(R.layout.item_duel, parent, false);
            return new PlayerViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull final PlayerViewHolder holder, int position) {
            Duel duel = mDuels.get(position);
            int whitelisted = App.config().isWhitelisted(duel);
            if (duel.getPlayer1Rank() < 0) {
                holder.rank1.setText(R.string.loading);
            } else {
                if (duel.getPlayer1Rank() == 0) {
                    holder.rank1.setText(R.string.unknown);
                } else {
                    holder.rank1.setText(String.valueOf(duel.getPlayer1Rank()));
                }
            }
            holder.name1.setTextColor(duel.isPlayer1Pro() ? accentColor : textColor);
            if (duel.getPlayer2Rank() < 0) {
                holder.rank2.setText(R.string.loading);
            } else {
                if (duel.getPlayer2Rank() == 0) {
                    holder.rank2.setText(R.string.unknown);
                } else {
                    holder.rank2.setText(String.valueOf(duel.getPlayer2Rank()));
                }
            }
            if (whitelisted == 1) {
                holder.rank1.append("|" + getString(R.string.whitelisted));
            } else if (whitelisted == 2) {
                holder.rank2.append("|" + getString(R.string.whitelisted));
            }
            holder.name2.setTextColor(duel.isPlayer2Pro() ? accentColor : textColor);
            holder.name1.setText(String.valueOf(duel.getPlayer1Name()));
            holder.name2.setText(String.valueOf(duel.getPlayer2Name()));
            if (App.config().isDuelInPushCondition(duel) || whitelisted > 0) {
                holder.itemView.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_focused, requireActivity().getTheme()));
            } else {
                holder.itemView.setForeground(null);
            }
        }

        @Override
        public int getItemCount() {
            return mDuels.size();
        }
    }

    private class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView rank1, rank2, name1, name2;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            rank1 = itemView.findViewById(R.id.tv_rank_1);
            rank2 = itemView.findViewById(R.id.tv_rank_2);
            name1 = itemView.findViewById(R.id.tv_player_1);
            name2 = itemView.findViewById(R.id.tv_player_2);
            itemView.setOnClickListener(v -> {
                if (!App.isYGOMobileInstalled()) {
                    MasterToast.shortToast(R.string.ygo_mobile_not_installed);
                    return;
                }
                if (App.config().hasCompleteWatchConfig()) {
                    Utils.launchWatch(requireContext(), mDuels.get(getAdapterPosition()).getId());
                } else {
                    Intent intent = new Intent(requireActivity(), WatchSetupActivity.class);
                    intent.putExtra(WatchSetupActivity.EXTRA_DUEL_ID, mDuels.get(getAdapterPosition()).getId());
                    startActivity(intent);
                }
            });
        }
    }
}

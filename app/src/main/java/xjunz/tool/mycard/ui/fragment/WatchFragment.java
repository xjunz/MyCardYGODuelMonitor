/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.WatchService;
import xjunz.tool.mycard.api.bean.Duel;
import xjunz.tool.mycard.ui.WatchSetupActivity;
import xjunz.tool.mycard.util.Utils;

public class WatchFragment extends Fragment implements WatchService.DuelCallback {
    private RecyclerView mRvDuel;
    private DuelAdapter mDuelAdapter;
    private List<Duel> mDuels;
    private ProgressBar mProgress;
    private boolean mPendingLoad;

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
    public void onInitList(List<Duel> initialDuels) {
        mDuels = initialDuels;
        if (mRvDuel == null) {
            mPendingLoad = true;
        } else {
            mDuelAdapter = new DuelAdapter();
            mRvDuel.setAdapter(mDuelAdapter);
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDuelCreated(int index) {
        if (mDuelAdapter != null) {
            LinearLayoutManager llm = (LinearLayoutManager) mRvDuel.getLayoutManager();
            int last = llm.findLastVisibleItemPosition();
            if (last == index - 1) {
                mDuelAdapter.notifyItemInserted(index);
                mRvDuel.smoothScrollToPosition(index);
            } else {
                mDuelAdapter.notifyItemInserted(index);
            }
        }
    }

    @Override
    public void onDuelDeleted(int index) {
        if (mDuelAdapter != null) {
            mDuelAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    public void onPlayerRankGot(int index) {
        if (mDuelAdapter != null) {
            mDuelAdapter.notifyItemChanged(index);
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
            holder.name2.setTextColor(duel.isPlayer2Pro() ? accentColor : textColor);
            holder.name1.setText(String.valueOf(duel.getPlayer1Name()));
            holder.name2.setText(String.valueOf(duel.getPlayer2Name()));
            if (App.config().isDuelInPushCondition(duel)) {
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
                try {
                    if (App.config().hasCompleteWatchConfig()) {
                        startActivity(Utils.buildLaunchWatchIntent(mDuels.get(getAdapterPosition()).getId()));
                    } else {
                        Intent intent = new Intent(requireActivity(), WatchSetupActivity.class);
                        intent.putExtra(WatchSetupActivity.EXTRA_DUEL_ID, mDuels.get(getAdapterPosition()).getId());
                        startActivity(intent);
                    }
                } catch (ActivityNotFoundException e) {
                    //没安装YGO Mobile
                    //TODO
                }
            });
        }
    }
}

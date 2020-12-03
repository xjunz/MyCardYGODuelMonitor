/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import xjunz.tool.mycard.App;
import xjunz.tool.mycard.R;
import xjunz.tool.mycard.api.LoadHistoryService;
import xjunz.tool.mycard.api.LoadPlayerListService;
import xjunz.tool.mycard.api.bean.HistorySet;
import xjunz.tool.mycard.api.bean.Player;
import xjunz.tool.mycard.databinding.FragmentRankBinding;
import xjunz.tool.mycard.ui.MasterToast;
import xjunz.tool.mycard.util.Utils;

public class RankFragment extends Fragment {
    private List<Player> mRankList;
    private RankAdapter mAdapter;
    private LoadHistoryService mLoadHistoryService;
    private final ObservableBoolean mLoading = new ObservableBoolean(true);
    private FragmentRankBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_rank, container, false);
        mBinding.setLoading(mLoading);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.rvRank.setHasFixedSize(true);
        mBinding.ibRefresh.setOnClickListener(v -> loadRankList());
        loadRankList();
    }

    private Call<List<Player>> mLoadListCall;

    private final List<Call<HistorySet>> mLoadTrendCall = new ArrayList<>();

    private void loadRankList() {
        if (mLoadListCall != null) {
            mLoadListCall.cancel();
        }
        for (Call<HistorySet> call : mLoadTrendCall) {
            call.cancel();
        }
        mLoadListCall = Utils.createRetrofit(App.config().rankingListLoadTimeout.getValue()).create(LoadPlayerListService.class).loadPlayerList();
        mLoading.set(true);
        mLoadListCall.enqueue(new Utils.CallbackAdapter<List<Player>>() {
            @Override
            public void onSuccess(List<Player> players) {
                super.onSuccess(players);
                mRankList = players;
                mAdapter = new RankAdapter();
                mBinding.rvRank.setAdapter(mAdapter);
                for (Player player : mRankList) {
                    loadTrendOf(player);
                }
            }

            @Override
            public void onNull(@NonNull Throwable t) {
                MasterToast.shortToast(getString(R.string.msg_load_failed, t.getMessage()));
            }

            @Override
            public void onWhatever() {
                mLoading.set(false);
                mLoadListCall = null;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Call<HistorySet> call : mLoadTrendCall) {
            call.cancel();
        }
        if (mLoadListCall != null) {
            mLoadListCall.cancel();
        }
    }

    private void loadTrendOf(@NonNull Player player) {
        String playerName = player.getUsername();
        if (mLoadHistoryService == null) {
            mLoadHistoryService = Utils.createRetrofit().create(LoadHistoryService.class);
        }
        Call<HistorySet> call = mLoadHistoryService.loadHistoryOf(playerName);
        mLoadTrendCall.add(call);
        call.enqueue(new Utils.CallbackAdapter<HistorySet>() {
            @Override
            public void onResponse(@NonNull Call<HistorySet> call, @NonNull Response<HistorySet> response) {
                super.onResponse(call, response);
                HistorySet set = response.body();
                if (set != null) {
                    player.setHistorySet(set);
                }
                int index = mRankList.indexOf(player);
                if (index >= 0) {
                    mAdapter.notifyItemChanged(index, true);
                }
            }

            @Override
            public void onWhatever() {
                mLoadTrendCall.remove(call);
            }
        });
    }

    private class RankAdapter extends RecyclerView.Adapter<PlayerViewHolder> {
        private final int colorAccent;
        private final int colorTextSecondary;

        private RankAdapter() {
            colorAccent = Utils.getAttrColor(requireContext(), R.attr.colorAccent);
            colorTextSecondary = Utils.getAttrColor(requireContext(), android.R.attr.textColorSecondary);
        }

        @Override
        public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.size() > 0) {
                Player player = mRankList.get(position);
                if (player.getPtTrendIndex() != null) {
                    holder.trend.setVisibility(View.VISIBLE);
                    if (player.getPtTrendIndex() > 5) {
                        holder.trend.setText("↑");
                        holder.trend.setTextColor(colorAccent);
                    } else if (player.getPtTrendIndex() < -5) {
                        holder.trend.setText("↓");
                        holder.trend.setTextColor(colorTextSecondary);
                    } else {
                        holder.trend.setText("—");
                        holder.trend.setTextColor(colorTextSecondary);
                    }
                } else {
                    holder.trend.setVisibility(View.GONE);
                }
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }
        }

        @NonNull
        @Override
        public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = getLayoutInflater().inflate(R.layout.item_player, parent, false);
            return new PlayerViewHolder(root);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onBindViewHolder(@NonNull final PlayerViewHolder holder, int position) {
            Player player = mRankList.get(position);
            holder.tags.setText(getString(R.string.win_rate_separator, player.getFormattedWinRatio()));
            holder.ordinal.setText(String.valueOf(position + 1));
            holder.name.setText(player.getUsername());
            holder.score.setText(String.format("%.2f", player.getPt()));
            if (player.getPtTrendIndex() != null) {
                holder.trend.setVisibility(View.VISIBLE);
                if (player.getPtTrendIndex() > 5) {
                    holder.trend.setText("↑");
                    holder.trend.setTextColor(colorAccent);
                } else if (player.getPtTrendIndex() < -5) {
                    holder.trend.setText("↓");
                    holder.trend.setTextColor(colorTextSecondary);
                } else {
                    holder.trend.setText("—");
                    holder.trend.setTextColor(colorTextSecondary);
                }
            } else {
                holder.trend.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mRankList.size();
        }
    }

    private class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView name, ordinal, tags, score, trend;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            ordinal = itemView.findViewById(R.id.tv_ordinal);
            tags = itemView.findViewById(R.id.tv_tags);
            score = itemView.findViewById(R.id.tv_score);
            trend = itemView.findViewById(R.id.tv_trend);
            itemView.setOnClickListener(v -> Utils.viewURL(requireActivity(), "https://mycard.moe/ygopro/arena/#/userinfo?username=" + mRankList.get(getAdapterPosition()).getUsername()));
        }
    }
}

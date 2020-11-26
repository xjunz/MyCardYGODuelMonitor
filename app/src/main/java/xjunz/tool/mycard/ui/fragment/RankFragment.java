/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import xjunz.tool.mycard.api.bean.FrequencyDeck;
import xjunz.tool.mycard.api.bean.HistorySet;
import xjunz.tool.mycard.api.bean.Player;
import xjunz.tool.mycard.ui.MasterToast;
import xjunz.tool.mycard.util.Utils;

public class RankFragment extends Fragment {
    private RecyclerView mRvRank;
    private List<Player> mRankList;
    private RankAdapter mAdapter;
    private ProgressBar mProgress;
    private ImageButton mIbRefresh;
    private View mMask;
    private LoadHistoryService mLoadHistoryService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_rank, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvRank = view.findViewById(R.id.rv_rank);
        mRvRank.setHasFixedSize(true);
        mProgress = view.findViewById(R.id.progress);
        mIbRefresh = view.findViewById(R.id.ib_refresh);
        mMask = view.findViewById(R.id.mask);
        mIbRefresh.setOnClickListener(v -> {
            animateHideFAB();
            mMask.setAlpha(0);
            mIbRefresh.setEnabled(false);
            mMask.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            mMask.animate().alpha(0.8f).setListener(null).start();
            loadRankList();
        });
        loadRankList();
    }

    private Call<List<Player>> mLoadListCall;

    private void loadRankList() {
        if (mLoadListCall != null) {
            mLoadListCall.cancel();
        }
        for (Call<HistorySet> call : mLoadDeckCalls) {
            call.cancel();
        }
        mLoadListCall = Utils.createRetrofit(App.config().rankingListLoadTimeout.getValue()).create(LoadPlayerListService.class).loadPlayerList();
        mLoadListCall.enqueue(new Utils.CallbackAdapter<List<Player>>() {
            @Override
            public void onResponse(@NonNull Call<List<Player>> call, @NonNull Response<List<Player>> response) {
                super.onResponse(call, response);
                mRankList = response.body();
                if (mRankList != null) {
                    mAdapter = new RankAdapter();
                    mRvRank.setAdapter(mAdapter);
                    for (Player player : mRankList) {
                        loadDecksOf(player);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Player>> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                MasterToast.shortToast(getString(R.string.load_failed, t.getMessage()));
            }

            @Override
            public void onWhatever() {
                animateShowFAB();
                mProgress.setVisibility(View.GONE);
                if (mMask.getVisibility() == View.VISIBLE) {
                    mMask.animate().setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mMask.setVisibility(View.GONE);
                            mIbRefresh.setEnabled(true);
                        }
                    }).alpha(0).start();
                }
                mLoadListCall = null;
            }
        });
    }

    private void animateShowFAB() {
        mIbRefresh.setScaleX(0);
        mIbRefresh.setScaleY(0);
        mIbRefresh.setVisibility(View.VISIBLE);
        mIbRefresh.animate().scaleX(1f).scaleY(1f).setListener(null).setInterpolator(new OvershootInterpolator()).start();
    }

    private void animateHideFAB() {
        mIbRefresh.animate().scaleX(0).scaleY(0).setInterpolator(new AnticipateInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIbRefresh.setVisibility(View.GONE);
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Call<HistorySet> call : mLoadDeckCalls) {
            call.cancel();
        }
        if (mLoadListCall != null) {
            mLoadListCall.cancel();
        }
    }

    private final List<Call<HistorySet>> mLoadDeckCalls = new ArrayList<>();

    private void loadDecksOf(@NonNull Player player) {
        String playerName = player.getUsername();
        if (mLoadHistoryService == null) {
            mLoadHistoryService = Utils.createRetrofit().create(LoadHistoryService.class);
        }
        Call<HistorySet> call = mLoadHistoryService.loadHistoryOf(playerName);
        mLoadDeckCalls.add(call);
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
                    mAdapter.notifyItemChanged(index);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistorySet> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                t.printStackTrace();
            }

            @Override
            public void onWhatever() {
                mLoadDeckCalls.remove(call);
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
            holder.ordinal.setText(String.valueOf(position + 1));
            holder.name.setText(player.getUsername());
            holder.score.setText(String.format("%.2f", player.getPt()));
            List<FrequencyDeck> decks = player.getUsedDecks();
            if (decks == null || decks.size() == 0) {
                holder.decks.setVisibility(View.GONE);
            } else {
                holder.decks.setVisibility(View.VISIBLE);
                StringBuilder deckSerial = new StringBuilder();
                int showCount = Math.min(3, decks.size());
                for (int i = 0; i < showCount; i++) {
                    if (i == showCount - 1) {
                        deckSerial.append(decks.get(i).getName());
                    } else {
                        deckSerial.append(decks.get(i).getName()).append("|");
                    }
                }
                holder.decks.setText(deckSerial);
            }
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

    private static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView name, ordinal, decks, score, trend;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            ordinal = itemView.findViewById(R.id.tv_ordinal);
            decks = itemView.findViewById(R.id.tv_decks);
            score = itemView.findViewById(R.id.tv_score);
            trend = itemView.findViewById(R.id.tv_trend);
        }
    }
}

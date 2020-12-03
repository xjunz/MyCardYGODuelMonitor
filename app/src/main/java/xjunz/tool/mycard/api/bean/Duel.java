/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableInt;

import java.util.Objects;

import xjunz.tool.mycard.App;

public class Duel implements Comparable<Duel> {

    private String player1Name;
    private String player2Name;
    private Player player1;
    private Player player2;
    private String id;
    public static final int SORT_BY_ORDINAL = 0;
    private long startTimestamp;
    public static final int SORT_BY_RANK_SUM = 1;
    public static final int SORT_BY_RANK_MINOR = 2;
    private int ordinal;
    public static final int LOAD_STATE_FAILED = -1;
    public static final int LOAD_STATE_LOADING = 0;
    public static final int LOAD_STATE_SUCCESS = 1;
    public ObservableInt playerLoadState = new ObservableInt(0);

    public boolean isLoadFailed() {
        return playerLoadState.get() == LOAD_STATE_FAILED;
    }

    public boolean isLoading() {
        return playerLoadState.get() == LOAD_STATE_LOADING;
    }

    public boolean isSuccess() {
        return playerLoadState.get() == LOAD_STATE_SUCCESS;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayerName(int which) {
        return which == 1 ? getPlayer1Name() : which == 2 ? getPlayer2Name() : null;
    }

    public int getPlayerRank(int which) {
        return which == 1 ? getPlayer1Rank() : which == 2 ? getPlayer2Rank() : -1;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean isPlayer1Pro() {
        if (player1 == null) {
            return false;
        }
        return player1.getArenaRank() >= 1 && player1.getArenaRank() <= 100;
    }

    public boolean isPlayer2Pro() {
        if (player2 == null) {
            return false;
        }
        return player2.getArenaRank() >= 1 && player2.getArenaRank() <= 100;
    }

    public int getPlayer1Rank() {
        if (player1 == null) {
            return -1;
        }
        return player1.getArenaRank();
    }

    public int getPlayer2Rank() {
        if (player2 == null) {
            return -1;
        }
        return player2.getArenaRank();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Duel) {
            return ((Duel) obj).id.equals(id);
        } else if (obj instanceof String) {
            return obj.equals(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    private int getCompareRank(@IntRange(from = 1, to = 2) int whichPlayer) {
        int rank = whichPlayer == 1 ? getPlayer1Rank() : getPlayer2Rank();
        if (rank == -1) {
            return 1000 * 10000 + ordinal;
        } else if (rank == 0) {
            return 100 * 10000 + ordinal;
        }
        return rank;
    }

    @Override
    public int compareTo(Duel o) {
        int multiplier = App.config().watchListAscending.get() ? 1 : -1;
        switch (App.config().watchListSortBy.get()) {
            case SORT_BY_ORDINAL:
                return multiplier * Integer.compare(ordinal, o.getOrdinal());
            case SORT_BY_RANK_MINOR:
                return multiplier * Integer.compare(Math.min(getCompareRank(1), getCompareRank(2)), Math.min(o.getCompareRank(1), o.getCompareRank(2)));
            case SORT_BY_RANK_SUM:
                return multiplier * Integer.compare(getCompareRank(1) + getCompareRank(2), o.getCompareRank(1) + o.getCompareRank(2));
        }
        return 0;
    }

}

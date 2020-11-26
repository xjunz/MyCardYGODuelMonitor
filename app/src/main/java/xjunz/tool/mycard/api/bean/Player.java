
/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@SuppressWarnings("unused")
public class Player {

    @SerializedName("athletic_all")
    private Long mAthleticAll;
    @SerializedName("athletic_draw")
    private Long mAthleticDraw;
    @SerializedName("athletic_lose")
    private Long mAthleticLose;
    @SerializedName("athletic_win")
    private Long mAthleticWin;
    @SerializedName("entertain_all")
    private Long mEntertainAll;
    @SerializedName("entertain_draw")
    private Long mEntertainDraw;
    @SerializedName("entertain_lose")
    private Long mEntertainLose;
    @SerializedName("entertain_win")
    private Long mEntertainWin;
    @SerializedName("exp")
    private Double mExp;
    @SerializedName("id")
    private Long mId;
    @SerializedName("pt")
    private Double mPt;
    @SerializedName("username")
    private String mUsername;
    @SerializedName("arena_rank")
    private Integer mArenaRank;
    @SerializedName("athletic_wl_ratio")
    private Double mAthleticWlRatio;
    @SerializedName("entertain_wl_ratio")
    private Double mEntertainWlRatio;
    @SerializedName("exp_rank")
    private Integer mExpRank;

    private Double mLatestPt;
    private Double mEarliestPt;
    private HistorySet mHistorySet;

    private List<FrequencyDeck> mDecks;

    public void setHistorySet(HistorySet set) {
        this.mHistorySet = set;
        readDeckFromHistory(set);
    }

    @Nullable
    public Double getPtTrendIndex() {
        if (mLatestPt == null || mEarliestPt == null) {
            return null;
        }
        return mLatestPt - mEarliestPt;
    }

    public HistorySet getHistorySet() {
        return mHistorySet;
    }

    public List<FrequencyDeck> getUsedDecks() {
        return mDecks;
    }

    private void readDeckFromHistory(@NonNull HistorySet set) {
        mDecks = new ArrayList<>();
        int size = set.getData().size();
        for (int i = 0; i < size; i++) {
            History history = set.getData().get(i);
            String deckName = null;
            if (mUsername.equals(history.getUsernamea())) {
                deckName = history.getDecka();
                if (i == 0) {
                    mLatestPt = history.getPta();
                }
                if (i == size - 1) {
                    mEarliestPt = history.getPtaEx();
                }
            } else if (mUsername.equals(history.getUsernameb())) {
                deckName = history.getDeckb();
                if (i == 0) {
                    mLatestPt = history.getPtb();
                }
                if (i == size - 1) {
                    mEarliestPt = history.getPtbEx();
                }
            }
            if (!TextUtils.isEmpty(deckName) && !deckName.equals("no deck")) {
                int index = -1;
                for (int a = 0; a < mDecks.size(); a++) {
                    if (deckName.equals(mDecks.get(a).getName())) {
                        index = a;
                        break;
                    }
                }
                if (index >= 0) {
                    mDecks.get(index).increase();
                } else {
                    if (mDecks.size() == 0) {
                        mDecks.add(new FrequencyDeck(deckName, true));
                    } else {
                        mDecks.add(new FrequencyDeck(deckName));
                    }
                }
            }
        }
        Collections.sort(mDecks);
    }

    public Long getAthleticAll() {
        return mAthleticAll;
    }

    public void setAthleticAll(Long athleticAll) {
        mAthleticAll = athleticAll;
    }

    public Long getAthleticDraw() {
        return mAthleticDraw;
    }

    public void setAthleticDraw(Long athleticDraw) {
        mAthleticDraw = athleticDraw;
    }

    public Long getAthleticLose() {
        return mAthleticLose;
    }

    public void setAthleticLose(Long athleticLose) {
        mAthleticLose = athleticLose;
    }

    public Long getAthleticWin() {
        return mAthleticWin;
    }

    public void setAthleticWin(Long athleticWin) {
        mAthleticWin = athleticWin;
    }

    public Long getEntertainAll() {
        return mEntertainAll;
    }

    public void setEntertainAll(Long entertainAll) {
        mEntertainAll = entertainAll;
    }

    public Long getEntertainDraw() {
        return mEntertainDraw;
    }

    public void setEntertainDraw(Long entertainDraw) {
        mEntertainDraw = entertainDraw;
    }

    public Long getEntertainLose() {
        return mEntertainLose;
    }

    public void setEntertainLose(Long entertainLose) {
        mEntertainLose = entertainLose;
    }

    public Long getEntertainWin() {
        return mEntertainWin;
    }

    public void setEntertainWin(Long entertainWin) {
        mEntertainWin = entertainWin;
    }

    public Double getExp() {
        return mExp;
    }

    public void setExp(Double exp) {
        mExp = exp;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public Double getPt() {
        return mPt;
    }

    public void setPt(Double pt) {
        mPt = pt;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public Integer getArenaRank() {
        return mArenaRank;
    }

    public void setArenaRank(Integer arenaRank) {
        mArenaRank = arenaRank;
    }

    public Integer getExpRank() {
        return mExpRank;
    }

    public void setExpRank(Integer expRank) {
        mExpRank = expRank;
    }

    public Double getEntertainWlRatio() {
        return mEntertainWlRatio;
    }

    public void setEntertainWlRatio(Double entertainWlRatio) {
        mEntertainWlRatio = entertainWlRatio;
    }

    public Double getAthleticWlRatio() {
        return mAthleticWlRatio;
    }

    public void setAthleticWlRatio(Double athleticWlRatio) {
        mAthleticWlRatio = athleticWlRatio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return mUsername.equals(player.mUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUsername);
    }
}

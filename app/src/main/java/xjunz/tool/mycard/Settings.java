/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import xjunz.tool.mycard.api.bean.Duel;

import static xjunz.tool.mycard.App.getStringOf;

@SuppressLint("ApplySharedPref")
public class Settings {
    private final SharedPreferences mSP;
    public StringSetting username = new StringSetting("username", null);
    public StringSetting token = new StringSetting("token", null);
    public StringSetting player1RankLimit = new StringSetting("player1_rank_limit", "0,100");
    public StringSetting player2RankLimit = new StringSetting("player2_rank_limit", "0,100");
    public BooleanSetting isConditionAnd = new BooleanSetting("is_and", false);
    public IntegerSetting pushDelayMin = new IntegerSetting("push_delay_min", 0);
    public BooleanSetting pushWhiteHot = new BooleanSetting("push_wh", false);
    public IntegerSetting duelRankLoadTimeout = new IntegerSetting("duel_rank_load_timeout", 5);
    public IntegerSetting duelRankLoadRetryTimes = new IntegerSetting("duel_rank_load_retry_times", 3);
    public IntegerSetting rankingListLoadTimeout = new IntegerSetting("ranking_list_load_timeout", 10);
    public BooleanSetting notifyInSingleId = new BooleanSetting("notify_in_single_id", true);
    public BooleanSetting reconnectWhenClosedRemotely = new BooleanSetting("reconnect_when_closed_remotely", false);
    private final ObservableField<String> conditionText = new ObservableField<>(player1RankLimit, player2RankLimit, isConditionAnd);

    protected Settings(SharedPreferences sp) {
        this.mSP = sp;
    }


    private String getConditionString(int playerNum, @NonNull int[] condition) {
        StringBuilder sb = new StringBuilder();
        if (condition[0] > 0 && condition[1] > 0) {
            sb.append(getStringOf(R.string.player_rank_range, playerNum, condition[0], condition[1]));
        } else if (condition[0] == 0 && condition[1] > 0) {
            sb.append(getStringOf(R.string.player_rank_less_than, playerNum, condition[1]));
        } else if (condition[0] > 0 && condition[1] == 0) {
            sb.append(getStringOf(R.string.player_rank_larger_than, playerNum, condition[0]));
        } else {
            return null;
        }
        return sb.toString();
    }

    public ObservableField<String> getPushConditionText() {
        String player1 = getConditionString(1, getPlayer1RankRange());
        String player2 = getConditionString(2, getPlayer2RankRange());
        String logic = isConditionAnd.getValue() ? getStringOf(R.string.and) : getStringOf(R.string.or);
        StringBuilder sb = new StringBuilder();
        if (player1 == null) {
            if (player2 == null) {
                sb.append(getStringOf(R.string.push_all));
            } else {
                sb.append(player2);
            }
        } else {
            sb.append(player1);
            if (player2 != null) {
                sb.append(logic).append(player2);
            }
        }
        conditionText.set(sb.toString());
        return conditionText;
    }

    public boolean isDuelInPushCondition(@NonNull Duel duel) {
        int[] limit1 = getPlayer1RankRange();
        int[] limit2 = getPlayer2RankRange();
        boolean a = (limit1[0] == 0 && limit1[1] == 0) || (duel.getPlayer1Rank() > 0 && (limit1[0] == 0 || duel.getPlayer1Rank() >= limit1[0]) && (limit1[1] == 0 || duel.getPlayer1Rank() <= limit1[1]));
        boolean b = (limit2[0] == 0 && limit2[1] == 0) || (duel.getPlayer2Rank() > 0 && (limit2[0] == 0 || duel.getPlayer2Rank() >= limit2[0]) && (limit2[1] == 0 || duel.getPlayer2Rank() <= limit2[1]));
        return isConditionAnd.getValue() ? a && b : a || b;
    }

    public int[] getPlayer1RankRange() {
        String[] condition = player1RankLimit.getValue().split(",");
        return new int[]{Integer.parseInt(condition[0]), Integer.parseInt(condition[1])};
    }

    public int[] getPlayer2RankRange() {
        String[] condition = player2RankLimit.getValue().split(",");
        return new int[]{Integer.parseInt(condition[0]), Integer.parseInt(condition[1])};
    }

    public boolean hasCompleteWatchConfig() {
        return username.getValue() != null && token.getValue() != null;
    }

    public static abstract class Setting<T> extends BaseObservable {
        String key;
        T defValue;

        Setting(String key, T defValue) {
            this.key = key;
            this.defValue = defValue;
        }

        public void restoreDefault() {
            setValue(defValue);
        }

        @Bindable
        abstract T getValue();

        abstract void setValueInternal(T value);

        public void setValue(T value) {
            setValueInternal(value);
            notifyPropertyChanged(BR.value);
        }
    }

    public class IntegerSetting extends Setting<Integer> {

        IntegerSetting(String key, Integer defValue) {
            super(key, defValue);
        }

        @Bindable
        public Integer getValue() {
            return mSP.getInt(key, defValue);
        }

        @Override
        void setValueInternal(Integer value) {
            mSP.edit().putInt(key, value).apply();
        }
    }

    public class StringSetting extends Setting<String> {

        StringSetting(String key, String defValue) {
            super(key, defValue);
        }

        @Bindable
        public String getValue() {
            return mSP.getString(key, defValue);
        }

        @Override
        void setValueInternal(String value) {
            mSP.edit().putString(key, value).apply();
        }

    }

    public class BooleanSetting extends Setting<Boolean> {

        BooleanSetting(String key, Boolean defValue) {
            super(key, defValue);
        }

        @Bindable
        public Boolean getValue() {
            return mSP.getBoolean(key, defValue);
        }

        @Override
        void setValueInternal(Boolean value) {
            mSP.edit().putBoolean(key, value).apply();
        }

        @Override
        public void setValue(Boolean value) {
            super.setValue(value);
        }
    }
}
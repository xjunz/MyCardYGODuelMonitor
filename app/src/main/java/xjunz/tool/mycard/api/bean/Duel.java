/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import androidx.annotation.Nullable;

import java.util.Objects;

public class Duel {

    private String player1Name;
    private String player2Name;
    private Player player1;
    private Player player2;
    private String id;

    public String getPlayer1Name() {
        return player1Name;
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
}

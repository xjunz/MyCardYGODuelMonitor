/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import androidx.annotation.NonNull;

public class FrequencyDeck implements Comparable<FrequencyDeck> {
    int freq;
    String name;
    boolean recentUsed;

    @NonNull
    @Override
    public String toString() {
        return "FrequencyDeck{" +
                "freq=" + freq +
                ", name='" + name + '\'' +
                ", recentUsed=" + recentUsed +
                '}';
    }

    @Override
    public int compareTo(@NonNull FrequencyDeck o) {
        int comparison = -Integer.compare(freq, o.freq);
        if (comparison == 0) {
            if (recentUsed) {
                return -1;
            } else if (o.recentUsed) {
                return 1;
            }
        }
        return comparison;
    }

    public void increase() {
        freq += 1;
    }

    public FrequencyDeck(String name) {
        this.name = name;
        this.freq = 1;
    }

    public FrequencyDeck(String name, boolean recentUsed) {
        this.name = name;
        this.freq = 1;
        this.recentUsed = recentUsed;
    }

    public String getName() {
        return name;
    }

}

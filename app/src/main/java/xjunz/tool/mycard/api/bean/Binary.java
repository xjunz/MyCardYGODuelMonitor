
/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;


import com.google.gson.annotations.SerializedName;


@SuppressWarnings("unused")
public class Binary {

    @SerializedName("fsize")
    private Long mFsize;

    public Long getFsize() {
        return mFsize;
    }

    public void setFsize(Long fsize) {
        mFsize = fsize;
    }

}

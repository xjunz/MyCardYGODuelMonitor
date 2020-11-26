
/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import java.util.List;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class HistorySet {

    @SerializedName("data")
    private List<History> mData;
    @SerializedName("total")
    private Long mTotal;

    public List<History> getData() {
        return mData;
    }

    public void setData(List<History> data) {
        mData = data;
    }

    public Long getTotal() {
        return mTotal;
    }

    public void setTotal(Long total) {
        mTotal = total;
    }

}

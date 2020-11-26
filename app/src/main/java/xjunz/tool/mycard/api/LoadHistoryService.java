/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import xjunz.tool.mycard.api.bean.HistorySet;

public interface LoadHistoryService {
    @GET(Constants.API_PLAYER_HISTORY)
    Call<HistorySet> loadHistoryOf(@Query("username") String name);
}

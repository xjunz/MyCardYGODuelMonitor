/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import xjunz.tool.mycard.api.Constants;
import xjunz.tool.mycard.api.bean.Player;

public interface LoadPlayerInfoService {
    @GET(Constants.API_PLAYER_INFO)
    Call<Player> loadPlayerInfo(@Query("username") String name);
}

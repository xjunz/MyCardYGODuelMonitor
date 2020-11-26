/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import xjunz.tool.mycard.api.bean.Player;

public interface LoadPlayerListService {
    @GET(Constants.API_TOP_PLAYER)
    Call<List<Player>> loadPlayerList();
}

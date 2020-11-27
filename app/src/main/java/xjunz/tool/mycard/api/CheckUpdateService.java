/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import xjunz.tool.mycard.api.bean.UpdateInfo;

public interface CheckUpdateService {
    @GET(Constants.CHECK_UPDATE_FIR_APP_ID)
    Call<UpdateInfo> checkUpdate(@Query("api_token") String api_token);
}

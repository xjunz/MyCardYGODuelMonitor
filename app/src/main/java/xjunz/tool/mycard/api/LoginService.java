/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import xjunz.tool.mycard.api.bean.UserInfo;

public interface LoginService {
    @POST(Constants.API_LOGIN)
    Call<UserInfo> login(@Body HashMap<String, String> userInfo);
}

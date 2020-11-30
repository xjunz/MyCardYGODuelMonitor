
/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class UserInfo {

    @SerializedName("token")
    private String mToken;
    @SerializedName("user")
    private User mUser;

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

}

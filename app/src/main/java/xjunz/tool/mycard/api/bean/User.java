
/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class User {

    @SerializedName("active")
    private Boolean mActive;
    @SerializedName("admin")
    private Boolean mAdmin;
    @SerializedName("avatar")
    private String mAvatar;
    @SerializedName("created_at")
    private String mCreatedAt;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("id")
    private Integer mId;
    @SerializedName("ip_address")
    private String mIpAddress;
    @SerializedName("locale")
    private String mLocale;
    @SerializedName("name")
    private String mName;
    @SerializedName("password_hash")
    private String mPasswordHash;
    @SerializedName("registration_ip_address")
    private String mRegistrationIpAddress;
    @SerializedName("salt")
    private String mSalt;
    @SerializedName("updated_at")
    private String mUpdatedAt;
    @SerializedName("username")
    private String mUsername;

    public Boolean getActive() {
        return mActive;
    }

    public void setActive(Boolean active) {
        mActive = active;
    }

    public Boolean getAdmin() {
        return mAdmin;
    }

    public void setAdmin(Boolean admin) {
        mAdmin = admin;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        mCreatedAt = createdAt;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public int getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public void setIpAddress(String ipAddress) {
        mIpAddress = ipAddress;
    }

    public String getLocale() {
        return mLocale;
    }

    public void setLocale(String locale) {
        mLocale = locale;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPasswordHash() {
        return mPasswordHash;
    }

    public void setPasswordHash(String passwordHash) {
        mPasswordHash = passwordHash;
    }

    public String getRegistrationIpAddress() {
        return mRegistrationIpAddress;
    }

    public void setRegistrationIpAddress(String registrationIpAddress) {
        mRegistrationIpAddress = registrationIpAddress;
    }

    public String getSalt() {
        return mSalt;
    }

    public void setSalt(String salt) {
        mSalt = salt;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        mUpdatedAt = updatedAt;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

}

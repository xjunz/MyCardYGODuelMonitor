
/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class UpdateInfo {

    @SerializedName("binary")
    private Binary mBinary;
    @SerializedName("build")
    private int mBuild;
    @SerializedName("changelog")
    private String mChangelog;
    @SerializedName("installUrl")
    private String mInstallUrl;
    @SerializedName("name")
    private String mName;
    @SerializedName("update_url")
    private String mUpdateUrl;
    @SerializedName("version")
    private String mVersion;
    @SerializedName("versionShort")
    private String mVersionShort;

    public Binary getBinary() {
        return mBinary;
    }

    public void setBinary(Binary binary) {
        mBinary = binary;
    }

    public int getBuild() {
        return mBuild;
    }

    public void setBuild(int build) {
        mBuild = build;
    }

    public String getChangelog() {
        return mChangelog;
    }

    public void setChangelog(String changelog) {
        mChangelog = changelog;
    }

    public String getInstallUrl() {
        return mInstallUrl;
    }

    public void setInstallUrl(String installUrl) {
        mInstallUrl = installUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUpdateUrl() {
        return mUpdateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        mUpdateUrl = updateUrl;
    }

    public String getVersion() {
        return mVersion;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    public String getVersionShort() {
        return mVersionShort;
    }

    public void setVersionShort(String versionShort) {
        mVersionShort = versionShort;
    }

    @NonNull
    @Override
    public String toString() {
        return "UpdateInfo{" +
                "mBinary=" + mBinary +
                ", mBuild='" + mBuild + '\'' +
                ", mChangelog='" + mChangelog + '\'' +
                ", mInstallUrl='" + mInstallUrl + '\'' +
                ", mName='" + mName + '\'' +
                ", mUpdateUrl='" + mUpdateUrl + '\'' +
                ", mVersion='" + mVersion + '\'' +
                ", mVersionShort='" + mVersionShort + '\'' +
                '}';
    }
}

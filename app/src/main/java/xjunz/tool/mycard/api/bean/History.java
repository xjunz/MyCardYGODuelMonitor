
/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.api.bean;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class History {

    @SerializedName("decka")
    private String mDecka;
    @SerializedName("deckb")
    private String mDeckb;
    @SerializedName("end_time")
    private String mEndTime;
    @SerializedName("expa")
    private Double mExpa;
    @SerializedName("expa_ex")
    private Double mExpaEx;
    @SerializedName("expb")
    private Double mExpb;
    @SerializedName("expb_ex")
    private Double mExpbEx;
    @SerializedName("isfirstwin")
    private Boolean mIsfirstwin;
    @SerializedName("pta")
    private Double mPta;
    @SerializedName("pta_ex")
    private Double mPtaEx;
    @SerializedName("ptb")
    private Double mPtb;
    @SerializedName("ptb_ex")
    private Double mPtbEx;
    @SerializedName("start_time")
    private String mStartTime;
    @SerializedName("type")
    private String mType;
    @SerializedName("usernamea")
    private String mUsernamea;
    @SerializedName("usernameb")
    private String mUsernameb;
    @SerializedName("userscorea")
    private Long mUserscorea;
    @SerializedName("userscoreb")
    private Long mUserscoreb;
    @SerializedName("winner")
    private String mWinner;

    public String getDecka() {
        return mDecka;
    }

    public void setDecka(String decka) {
        mDecka = decka;
    }

    public String getDeckb() {
        return mDeckb;
    }

    public void setDeckb(String deckb) {
        mDeckb = deckb;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    public Double getExpa() {
        return mExpa;
    }

    public void setExpa(Double expa) {
        mExpa = expa;
    }

    public Double getExpaEx() {
        return mExpaEx;
    }

    public void setExpaEx(Double expaEx) {
        mExpaEx = expaEx;
    }

    public Double getExpb() {
        return mExpb;
    }

    public void setExpb(Double expb) {
        mExpb = expb;
    }

    public Double getExpbEx() {
        return mExpbEx;
    }

    public void setExpbEx(Double expbEx) {
        mExpbEx = expbEx;
    }

    public Boolean getIsfirstwin() {
        return mIsfirstwin;
    }

    public void setIsfirstwin(Boolean isfirstwin) {
        mIsfirstwin = isfirstwin;
    }

    public Double getPta() {
        return mPta;
    }

    public void setPta(Double pta) {
        mPta = pta;
    }

    public Double getPtaEx() {
        return mPtaEx;
    }

    public void setPtaEx(Double ptaEx) {
        mPtaEx = ptaEx;
    }

    public Double getPtb() {
        return mPtb;
    }

    public void setPtb(Double ptb) {
        mPtb = ptb;
    }

    public Double getPtbEx() {
        return mPtbEx;
    }

    public void setPtbEx(Double ptbEx) {
        mPtbEx = ptbEx;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getUsernamea() {
        return mUsernamea;
    }

    public void setUsernamea(String usernamea) {
        mUsernamea = usernamea;
    }

    public String getUsernameb() {
        return mUsernameb;
    }

    public void setUsernameb(String usernameb) {
        mUsernameb = usernameb;
    }

    public Long getUserscorea() {
        return mUserscorea;
    }

    public void setUserscorea(Long userscorea) {
        mUserscorea = userscorea;
    }

    public Long getUserscoreb() {
        return mUserscoreb;
    }

    public void setUserscoreb(Long userscoreb) {
        mUserscoreb = userscoreb;
    }

    public String getWinner() {
        return mWinner;
    }

    public void setWinner(String winner) {
        mWinner = winner;
    }

}

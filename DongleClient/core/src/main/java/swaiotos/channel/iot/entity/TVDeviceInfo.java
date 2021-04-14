package swaiotos.channel.iot.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class TVDeviceInfo {
    public String activeId;
    public String deviceName;
    public String mMovieSource;
    public String mChip, mModel, mSize;
    public String cHomepageVersion;
    public String MAC;
    public String cFMode;
    public String cTcVersion;
    public String cPattern;
    public String Resolution;
    public String aSdk;
    public String cEmmcCID;
    public String cBrand;
    public String mNickName;        //电视的昵称

    public TVDeviceInfo(String in) {
        try {
            JSONObject object = new JSONObject(in);
            String activeId = object.optString("activeId");
            String deviceName = object.optString("deviceName");
            String mMovieSource = object.optString("mMovieSource");
            String mChip = object.optString("mChip");
            String mModel = object.optString("mModel");
            String mSize = object.optString("mSize");
            String cHomepageVersion = object.optString("cHomepageVersion");
            String MAC = object.optString("MAC");
            String cFMode = object.optString("cFMode");
            String cTcVersion = object.optString("cTcVersion");
            String cPattern = object.optString("cPattern");
            String Resolution = object.optString("Resolution");
            String aSdk = object.optString("aSdk");
            String cEmmcCID = object.optString("cEmmcCID");
            String cBrand = object.optString("cBrand");
            String mNickName = object.optString("mNickName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public String getActiveId() {
        return activeId;
    }

    public void setActiveId(String activeId) {
        this.activeId = activeId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMovieSource() {
        return mMovieSource;
    }

    public void setMovieSource(String movieSource) {
        this.mMovieSource = movieSource;
    }

    public String getChip() {
        return mChip;
    }

    public void setChip(String chip) {
        this.mChip = chip;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        this.mModel = model;
    }

    public String getSize() {
        return mSize;
    }

    public void setSize(String size) {
        this.mSize = size;
    }

    public String getHomepageVersion() {
        return cHomepageVersion;
    }

    public void setHomepageVersion(String cHomepageVersion) {
        this.cHomepageVersion = cHomepageVersion;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public String getFMode() {
        return cFMode;
    }

    public void setFMode(String cFMode) {
        this.cFMode = cFMode;
    }

    public String getTcVersion() {
        return cTcVersion;
    }

    public void setTcVersion(String cTcVersion) {
        this.cTcVersion = cTcVersion;
    }

    public String getPattern() {
        return cPattern;
    }

    public void setPattern(String cPattern) {
        this.cPattern = cPattern;
    }

    public String getResolution() {
        return Resolution;
    }

    public void setResolution(String resolution) {
        Resolution = resolution;
    }

    public String getSdk() {
        return aSdk;
    }

    public void setSdk(String aSdk) {
        this.aSdk = aSdk;
    }

    public String getEmmcCID() {
        return cEmmcCID;
    }

    public void setEmmcCID(String emmCID) {
        this.cEmmcCID = emmCID;
    }

    public String getBrand() {
        return cBrand;
    }

    public void setBrand(String cBrand) {
        this.cBrand = cBrand;
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        this.mNickName = nickName;
    }
}

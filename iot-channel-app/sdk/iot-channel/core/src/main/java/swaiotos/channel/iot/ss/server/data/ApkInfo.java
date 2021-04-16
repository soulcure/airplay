package swaiotos.channel.iot.ss.server.data;


public class ApkInfo {

    private String ssClientId;
    private String ssClientKey;
    private String ssClientVersion;
    private String appPkgName;
    private String versionName;
    private String versionCode;

    public String getSsClientId() {
        return ssClientId;
    }

    public void setSsClientId(String ssClientId) {
        this.ssClientId = ssClientId;
    }

    public String getSsClientKey() {
        return ssClientKey;
    }

    public void setSsClientKey(String ssClientKey) {
        this.ssClientKey = ssClientKey;
    }

    public String getSsClientVersion() {
        return ssClientVersion;
    }

    public void setSsClientVersion(String ssClientVersion) {
        this.ssClientVersion = ssClientVersion;
    }

    public String getAppPkgName() {
        return appPkgName;
    }

    public void setAppPkgName(String appPkgName) {
        this.appPkgName = appPkgName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        int verCode;
        try {
            verCode = Integer.parseInt(versionCode);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            verCode = 0;
        }
        return verCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }
}

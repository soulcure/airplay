package swaiotos.channel.iot.ss.server.data;


public class AppItem {
    public int appId;
    public String icon;
    public String appName;
    public float grade;
    public float fileSize;
    public int downloads;
    public String appVersion;//versionName
    public String versioncode;
    public String desc;
    public String pkg;
    public String webAppLink;
    public String browser;
    //更新信息
    public String newVersionCode;
    public String newVersionName;
    //更新说明
    public String updateInfo;

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public float getGrade() {
        return grade;
    }

    public void setGrade(float grade) {
        this.grade = grade;
    }

    public float getFileSize() {
        return fileSize;
    }

    public void setFileSize(float fileSize) {
        this.fileSize = fileSize;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getVersioncode() {
        int vc = 0;
        try {
            vc = Integer.parseInt(versioncode);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            vc = 0;
        }

        return vc;
    }

    public void setVersioncode(String versioncode) {
        this.versioncode = versioncode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getWebAppLink() {
        return webAppLink;
    }

    public void setWebAppLink(String webAppLink) {
        this.webAppLink = webAppLink;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getNewVersionCode() {
        return newVersionCode;
    }

    public void setNewVersionCode(String newVersionCode) {
        this.newVersionCode = newVersionCode;
    }

    public String getNewVersionName() {
        return newVersionName;
    }

    public void setNewVersionName(String newVersionName) {
        this.newVersionName = newVersionName;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }


    @Override
    public String toString() {
        return "AppItem{" +
                "appId=" + appId +
                ", icon='" + icon + '\'' +
                ", appName='" + appName + '\'' +
                ", grade=" + grade +
                ", fileSize=" + fileSize +
                ", downloads=" + downloads +
                ", appVersion='" + appVersion + '\'' +
                ", versioncode='" + versioncode + '\'' +
                ", desc='" + desc + '\'' +
                ", pkg='" + pkg + '\'' +
                ", webAppLink='" + webAppLink + '\'' +
                ", browser='" + browser + '\'' +
                ", newVersionCode='" + newVersionCode + '\'' +
                ", newVersionName='" + newVersionName + '\'' +
                ", updateInfo='" + updateInfo + '\'' +
                '}';
    }
}

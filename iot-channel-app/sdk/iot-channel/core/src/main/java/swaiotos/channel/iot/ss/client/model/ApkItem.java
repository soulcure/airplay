package swaiotos.channel.iot.ss.client.model;


public class ApkItem {
    private String packageName;
    private int versionCode;
    private String clientId;


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof ApkItem) {
            ApkItem target = (ApkItem) obj;
            return this.clientId.equals(target.clientId);
        }
        return false;
    }
}

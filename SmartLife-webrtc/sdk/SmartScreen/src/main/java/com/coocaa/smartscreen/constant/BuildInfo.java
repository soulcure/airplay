package com.coocaa.smartscreen.constant;

import android.util.Log;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class BuildInfo implements Serializable {
    public final int versionCode;
    public final String versionName;
    public final String buildDate; //yyyy-MM-dd HH:mm:ss
    public final long buildTimestamp;
    public final String buildChannel;
    public final boolean debug;
    public final boolean publishMode;

    private BuildInfo(int vc, String vn, String bd, long bt, String bc, boolean debug, boolean publishMode) {
        this.versionCode = vc;
        this.versionName = vn;
        this.buildDate = bd;
        this.buildTimestamp = bt;
        this.buildChannel = bc;
        this.debug = debug;
        this.publishMode = publishMode;
        Log.d("Tvpi", "versionCode=" + vc + ", versionName=" + vn + ", buildDate=" + bd + ", buildChannel=" + bc + ", debug=" + debug + ", publishMode=" + publishMode);
    }

    public static class BuildInfoBuilder {
        private int versionCode = 0;
        private String versionName = "";
        private String buildDate = ""; //yyyy-MM-dd HH:mm:ss
        private long buildTimestamp = 1L;
        private String buildChannel = "";
        private boolean debug = false;
        private boolean publishMode = false;

        public static BuildInfoBuilder builder() {
            return new BuildInfoBuilder();
        }

        public BuildInfoBuilder setVersionCode(int vc) {
            this.versionCode = vc;
            return this;
        }

        public BuildInfoBuilder setVersionname(String vn) {
            this.versionName = vn;
            return this;
        }

        public BuildInfoBuilder setBuildDate(String bd) {
            this.buildDate = bd;
            return this;
        }

        public BuildInfoBuilder setBuildTimestamp(long bt) {
            this.buildTimestamp = bt;
            return this;
        }

        public BuildInfoBuilder setBuildChannel(String buildChannel) {
            this.buildChannel = buildChannel;
            return this;
        }

        public BuildInfoBuilder setDebugMode(boolean debug) {
            this.debug = debug;
            return this;
        }

        public BuildInfoBuilder setPublishMode(boolean b) {
            this.publishMode = b;
            return this;
        }

        public BuildInfo build() {
            return new BuildInfo(versionCode, versionName, buildDate, buildTimestamp, buildChannel, debug, publishMode);
        }
    }
}

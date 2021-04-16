package com.coocaa.smartscreen.constant;

import dalvik.system.PathClassLoader;

/**
 * @Author: yuzhan
 */
public class BusinessInfo {
    public final String APPID_WECHAT;
    public final String APPID_QQ;
    public final String APPID_ALI;
    public final String APP_KEY;
    public final String APP_SECRET;

    public BusinessInfo(String wxId, String qqId, String aliId, String key, String secret) {
        APPID_WECHAT = wxId;
        APPID_QQ = qqId;
        APPID_ALI = aliId;
        APP_KEY = key;
        APP_SECRET = secret;
    }

    public static class BusinessInfoBuilder {
        private String wxId;
        private String qqId;
        private String aliId;
        private String key;
        private String secret;

        private BusinessInfoBuilder(){}

        public static BusinessInfoBuilder builder() {
            return new BusinessInfoBuilder();
        }

        public BusinessInfoBuilder setWeChatId(String id) {
            wxId = id;
            return this;
        }

        public BusinessInfoBuilder setQQId(String id) {
            qqId = id;
            return this;
        }

        public BusinessInfoBuilder setAliId(String id) {
            aliId = id;
            return this;
        }

        public BusinessInfoBuilder setAppKey(String key) {
            this.key = key;
            return this;
        }

        public BusinessInfoBuilder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public BusinessInfo build() {
            return new BusinessInfo(wxId, qqId, aliId, key, secret);
        }
    }
}

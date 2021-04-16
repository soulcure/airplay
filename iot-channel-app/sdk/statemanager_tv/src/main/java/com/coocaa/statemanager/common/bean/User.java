package com.coocaa.statemanager.common.bean;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * Describe:用户信息
 * Created by AwenZeng on 2020/12/18
 */
public class User implements Serializable {

    /**
     * 用户userID（必填信息）
     */
    public String userID;

    /**
     * 用户token（必填信息）
     */
    public String token;

    /**
     * 用户手机号（必填信息）
     */
    public String mobile;

    /**
     * 用户的昵称
     */
    public String nickName;

    /**
     * 用户的头像
     */
    public String avatar;


    public User() {
    }

    private User(String userID, String token, String mobile, String nickName, String avatar) {
        this.userID = userID;
        this.token = token;
        this.mobile = mobile;
        this.nickName = nickName;
        this.avatar = avatar;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String userID;
        private String token;
        private String mobile;
        private String nickName;
        private String avatar;

        public Builder userID(final String userID) {
            this.userID = userID;
            return this;
        }

        public Builder token(final String token) {
            this.token = token;
            return this;
        }

        public Builder mobile(final String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder nickName(final String nickName) {
            this.nickName = nickName;
            return this;
        }

        public Builder avatar(final String avatar) {
            this.avatar = avatar;
            return this;
        }

        public User build() {
            return new User(userID, token, mobile, nickName,avatar);
        }
    }

    public static User decode(String json) {
        try {
            return JSONObject.parseObject(json, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String encode(User user) {
        try {
            return JSONObject.toJSONString(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

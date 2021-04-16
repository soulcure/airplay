package com.coocaa.smartmall.data.mobile.data;

import java.io.Serializable;

public class LoginResult extends BaseResult  {

    /**
     * state : true
     * msg : 登录成功
     * code : 200
     * data : {"username":"用户_tjns5tn","nick_name":"13058161342@dianshipai","active":1}
     */

    private DataBean data;


    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * username : 用户_tjns5tn
         * nick_name : 13058161342@dianshipai
         * active : 1
         */

        private String username;
        private String nick_name;
        private int active;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNick_name() {
            return nick_name;
        }

        public void setNick_name(String nick_name) {
            this.nick_name = nick_name;
        }

        public int getActive() {
            return active;
        }

        public void setActive(int active) {
            this.active = active;
        }
    }
}

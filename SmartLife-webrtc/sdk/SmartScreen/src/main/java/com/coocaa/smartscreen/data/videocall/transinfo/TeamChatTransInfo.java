package com.coocaa.smartscreen.data.videocall.transinfo;

import java.io.Serializable;

/**
 * @author kangwen
 * @date 2020/7/29.
 */
public class TeamChatTransInfo implements Serializable {

    private String account;
    private String nickName;

    public TeamChatTransInfo(String account, String nickName) {
        this.account = account;
        this.nickName = nickName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}

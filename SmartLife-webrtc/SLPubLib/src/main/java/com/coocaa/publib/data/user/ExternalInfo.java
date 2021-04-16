package com.coocaa.publib.data.user;

/**
 * Created by guxiaolong on 2020/4/24.
 * 用户的第三方信息
 */
public class ExternalInfo {
    private String vuserid;
    private String external_avatar;
    private String external_id;
    private String external_nickname;
    private String unionid;
    private Long bind_time;
    private String vusession;
    private Boolean login;
    private String external_flag;

    public String getVuserid() {
        return vuserid;
    }

    public void setVuserid(String vuserid) {
        this.vuserid = vuserid;
    }

    public String getExternal_avatar() {
        return external_avatar;
    }

    public void setExternal_avatar(String external_avatar) {
        this.external_avatar = external_avatar;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
    }

    public String getExternal_nickname() {
        return external_nickname;
    }

    public void setExternal_nickname(String external_nickname) {
        this.external_nickname = external_nickname;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public Long getBind_time() {
        return bind_time;
    }

    public void setBind_time(Long bind_time) {
        this.bind_time = bind_time;
    }

    public String getVusession() {
        return vusession;
    }

    public void setVusession(String vusession) {
        this.vusession = vusession;
    }

    public Boolean getLogin() {
        return login;
    }

    public void setLogin(Boolean login) {
        this.login = login;
    }

    public String getExternal_flag() {
        return external_flag;
    }

    public void setExternal_flag(String external_flag) {
        this.external_flag = external_flag;
    }

    @Override
    public String toString() {
        return "ExternalInfo{" +
                "vuserid='" + vuserid + '\'' +
                ", external_avatar='" + external_avatar + '\'' +
                ", external_id='" + external_id + '\'' +
                ", external_nickname='" + external_nickname + '\'' +
                ", unionid='" + unionid + '\'' +
                ", bind_time=" + bind_time +
                ", vusession='" + vusession + '\'' +
                ", login=" + login +
                ", external_flag='" + external_flag + '\'' +
                '}';
    }
}

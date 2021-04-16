package com.coocaa.smartscreen.businessstate.object;


import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * Describe: 业务状态
 * Created by AwenZeng on 2020/12/18
 */
public class BusinessState implements Serializable {
    /**
     * 描述：业务ID，具体业务的唯一标识
     * 规则：包名$唯一类型（业务ID需唯一，不能与其他业务ID相同）
     *  例：swaiotos.channel.iot$IMAGE
     */
    public String id;

    /**
     * 业务拥有者，发起业务的用户信息
     */
    public User owner;

    /**
     * 业务参与者List，参与业务的相关用户信息
     */
    public List<User> userList;

    /**
     * 业务具体相关的json信息
     * 特殊业务展示例子：正在播放130****8899分享的电视剧：奥特曼
     * 公共参数：
     * {
     *     "title": "奥特曼",
     *     "category": "电视剧",
     *     ....其他业务参数
     * }
     */
    public String values;

    /**
     * 额外参数
     * 描述：为了扩展，参数为移动传给业务端extra,可以原样回传给移动端
     */
    public String extra;

    /**
     * 业务类型,兼容老业务(例：DEFAULT、IMAGE、DOC、VIDEO、AUDIO、LIVE...)
     */
    public String type;

    public String version;

    public BusinessState(){
    }

    private BusinessState(String id,User owner, List<User> userList, String values,String extra) {
        this.id = id;
        this.owner = owner;
        this.userList = userList;
        this.values = values;
        this.extra = extra;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private User owner;
        private List<User> userList;
        private String values;
        private String extra;

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder owner(final User owner) {
            this.owner = owner;
            return this;
        }

        public Builder userList(final List<User> userList) {
            this.userList = userList;
            return this;
        }

        public Builder values(final String values) {
            this.values = values;
            return this;
        }

        public Builder extra(final String extra) {
            this.extra = extra;
            return this;
        }


        public BusinessState build() {
            return new BusinessState(id, owner, userList, values,extra);
        }
    }


    public static BusinessState decode(String json) {
        try {
            return JSONObject.parseObject(json,BusinessState.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String encode(BusinessState state) {
        try {
            return JSONObject.toJSONString(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

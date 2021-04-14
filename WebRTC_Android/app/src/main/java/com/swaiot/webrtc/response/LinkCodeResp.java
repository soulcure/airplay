package com.swaiot.webrtc.response;

import com.swaiot.webrtc.entity.LinkCodeBean;

import java.util.List;


public class LinkCodeResp extends BaseResp {

    private LinkCodeBean data;

    public LinkCodeBean getData() {
        return data;
    }

    public void setData(LinkCodeBean data) {
        this.data = data;
    }
}


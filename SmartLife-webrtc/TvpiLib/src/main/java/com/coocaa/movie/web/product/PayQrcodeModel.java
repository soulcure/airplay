package com.coocaa.movie.web.product;

import java.io.Serializable;

public class PayQrcodeModel implements Serializable {

    /**
     * 是否是正确的视频源
     */
    private boolean is_right_source;

    /**
     * 二维码id，用于轮询二维码状态
     */
    private String qrcode_id;

    /**
     * 获取产品包ID
     */
    public String product_id;

    /**
     * 二维码地址
     */
    private String url;

    public boolean isIs_right_source() {
        return is_right_source;
    }

    public void setIs_right_source(boolean is_right_source) {
        this.is_right_source = is_right_source;
    }

    public String getQrcode_id() {
        return qrcode_id;
    }

    public void setQrcode_id(String qrcode_id) {
        this.qrcode_id = qrcode_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}

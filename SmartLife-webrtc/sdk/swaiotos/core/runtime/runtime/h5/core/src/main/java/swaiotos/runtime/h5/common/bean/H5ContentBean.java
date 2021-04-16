package swaiotos.runtime.h5.common.bean;

import com.alibaba.fastjson.JSON;

public class H5ContentBean {
    private String h5SenderUrl;
    private String h5ReceivedUrl;
    private String h5Content;
    private String h5LogType;

    public String getH5SenderUrl() {
        return h5SenderUrl;
    }

    public void setH5SenderUrl(String h5SenderUrl) {
        this.h5SenderUrl = h5SenderUrl;
    }

    public String getH5ReceivedUrl() {
        return h5ReceivedUrl;
    }

    public void setH5ReceivedUrl(String h5ReceivedUrl) {
        this.h5ReceivedUrl = h5ReceivedUrl;
    }

    public String getH5Content() {
        return h5Content;
    }

    public void setH5Content(String h5Content) {
        this.h5Content = h5Content;
    }

    public String getH5LogType() {
        return h5LogType;
    }

    public void setH5LogType(String h5LogType) {
        this.h5LogType = h5LogType;
    }

    public H5ContentBean(){

    }

    public H5ContentBean(String senderUrl,String receivedUrl,String content){
        h5SenderUrl = senderUrl;
        h5ReceivedUrl = receivedUrl;
        h5Content = content;
    }

    public H5ContentBean(String senderUrl,String receivedUrl,String content,String logType){
        h5SenderUrl = senderUrl;
        h5ReceivedUrl = receivedUrl;
        h5Content = content;
        h5LogType = logType;
    }

    @Override
    public String toString() {
        return "H5ContentBean{" +
                "h5SenderUrl='" + h5SenderUrl + '\'' +
                ", h5ReceivedUrl='" + h5ReceivedUrl + '\'' +
                ", h5Content='" + h5Content + '\'' +
                ", h5LogType='" + h5LogType + '\'' +
                '}';
    }

    public static String toJSONString(H5ContentBean sseUrlBean){
        return JSON.toJSONString(sseUrlBean);
    }

    public static H5ContentBean fromJSONString(String content){
        H5ContentBean newBean = JSON.parseObject(content,H5ContentBean.class);
        return newBean;
    }
}

package com.threesoft.webrtc.webrtcroom.webrtcmodule;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * SSL全部信任
 * Created by zengjinlong 2021-01-20
 */


public class TrustAllCerts implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
}
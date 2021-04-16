package com.coocaa.smartscreen.data.clientconfig;

import java.io.Serializable;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 1/11/21
 */
public class ClientConfigHttpData implements Serializable {

    public int code;
    public String msg;
    public ClientConfigData data;

    public static class ClientConfigData implements Serializable {
        public VideoResourceData video;
    }
}

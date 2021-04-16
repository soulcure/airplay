package com.coocaa.publib.data.local;

import java.sql.Date;

/**
 * Created by dvlee1 on 3/17/15.
 */
public class LocalImageBean {
    public long id = 0;
    public String title;
    public String data;
    public String thumb;
    public long size = 0;
    public Date takeTime;
    public String bucketName;

    public String getURI(String addr, int port)
    {
        StringBuffer uri = new StringBuffer();
        uri.append("http://").append(addr).append(":").append(port).append("/r?id=")
                .append(String.valueOf(id)).append("&type=image");
        return uri.toString();
    }
}

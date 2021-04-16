package com.coocaa.smartscreen.data.device;

import java.io.Serializable;

/**
 * @ClassName ConnectRecord
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/12/25
 * @Version TODO (write something)
 */
public class ConnectRecord implements Comparable<ConnectRecord>, Serializable {

    public String lsid;
    public long connectTime;

    public ConnectRecord(String lsid, long connectTime) {
        this.lsid = lsid;
        this.connectTime = connectTime;
    }

    @Override
    public int compareTo(ConnectRecord o) {
        return Long.compare(o.connectTime, this.connectTime);
    }
}

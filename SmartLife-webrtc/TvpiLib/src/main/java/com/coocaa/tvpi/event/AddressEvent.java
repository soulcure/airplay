package com.coocaa.tvpi.event;

import com.coocaa.smartmall.data.mobile.data.AddressResult;

/**
 * Created by IceStorm on 2017/12/13.
 */

public class AddressEvent {
    public static final int SELECT=1;
    public static final int DELETE=2;
    public static final int UPDATE=3;
    public static final int ADD=4;
    public int type=SELECT;
    public AddressResult.GetAddressBean bean;
    public AddressEvent(AddressResult.GetAddressBean addr,int type) {
        this.bean = addr;
        this.type=type;
    }
}

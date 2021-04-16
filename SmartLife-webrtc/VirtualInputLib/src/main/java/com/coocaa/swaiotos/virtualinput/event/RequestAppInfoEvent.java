package com.coocaa.swaiotos.virtualinput.event;

import com.coocaa.smartscreen.data.channel.AppInfo;

import java.io.Serializable;
import java.util.List;

public class RequestAppInfoEvent implements Serializable {
    public List<AppInfo> appInfoList;
}

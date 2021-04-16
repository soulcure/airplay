package com.coocaa.smartscreen.data.device;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IceStorm on 2018/1/15.
 */

public class TVSourceResp implements Serializable {
    public int code;
    public String msg;
    public List<TVSourceModel> data;
}

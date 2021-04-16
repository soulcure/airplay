package com.coocaa.swaiotos.virtualinput.data;

import java.io.Serializable;
import java.util.List;

import swaiotos.sensor.data.AccountInfo;

/**
 * @ClassName WhiteboardUser
 * @Description TODO (write something)
 * @User heni
 * @Date 3/30/21
 */
public class WhiteboardUser implements Serializable {
    public AccountInfo owner;
    public List<AccountInfo> userList;
}

package com.coocaa.statemanager.common.bean;

import java.io.Serializable;

/**
 * @ClassName: BusinessAppInfo
 * @Author: AwenZeng
 * @CreateDate: 2021/3/26 19:02
 * @Description:
 */
public class MiracastAppInfo implements Serializable {
    /**
     * name : 爱投屏
     * pkgName : com.tianci.de
     * claseName : com.tianci.de.Activity
     * isDisable : false：不禁止 true:禁止
     */
    public String name;
    public String pkgName;
    public String claseName;
    public Boolean isDisable;
}

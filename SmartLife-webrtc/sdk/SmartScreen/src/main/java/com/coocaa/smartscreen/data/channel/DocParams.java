package com.coocaa.smartscreen.data.channel;

import java.io.Serializable;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/26
 */
public class DocParams implements Serializable {
    public String platform;// 平台：android/ios
    public double scale;//显示文档的view的宽高比
}

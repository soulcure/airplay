package com.coocaa.smartscreen.data.clientconfig;

import java.io.Serializable;

/**
 * @Description: 手机端后台配置的视频资源数据结构 (Android文档教程视频目前只有微信和QQ--2021.01.11)
 * @Author: wzh
 * @CreateDate: 1/11/21
 */
public class VideoResourceData implements Serializable {
    public String qq;//qq文档教程视频
    public String wechat;//微信文档教程视频
    public String dingding;//钉钉文档教程视频
    public String enterpriseWeChat;//企业微信文档教程视频
    public String bleAnimation;//蓝牙配网教程视频
}

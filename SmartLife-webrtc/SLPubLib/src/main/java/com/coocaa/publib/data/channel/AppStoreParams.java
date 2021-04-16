package com.coocaa.publib.data.channel;

import com.google.gson.Gson;

/**
 * @ClassName DeviceParams
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/8
 * @Version TODO (write something)
 */
public class AppStoreParams {
    /**
     * 用于给CmdData cmd字段赋值
     */
    public enum CMD {
        /**
         * 获取协议版本（SKY_COMMAND_APPSTORE_MOBILE_GET_VERSION）：
         *         请求：无
         *         返回：AppResultBean<VersionBean>
         */
        SKY_COMMAND_APPSTORE_MOBILE_GET_VERSION,

        /**
         * 启动应用（SKY_COMMAND_APPSTORE_MOBILE_START_APP）：
         *         请求：packageName，mainActivity
         *         返回：AppResultBean<AppResultBean.StartApp>
         */
        SKY_COMMAND_APPSTORE_MOBILE_START_APP,

        /**
         * 获取安装的应用列表（SKY_COMMAND_APPSTORE_MOBILE_GET_INSTALLED_APPS）：
         *         请求：无
         *         返回：AppResultBean<List<AppResultBean.GetInstalled>>
         */
        SKY_COMMAND_APPSTORE_MOBILE_GET_INSTALLED_APPS,

        /**
         * 下载应用（SKY_COMMAND_APPSTORE_MOBILE_DOWNLOAD_SKYAPP）：
         *         请求：appid
         *         返回：无
         */
        SKY_COMMAND_APPSTORE_MOBILE_DOWNLOAD_SKYAPP,

        /**
         * 暂停下载（SKY_COMMAND_APPSTORE_MOBILE_PAUSEDOWNLOAD_SKYAPP）：
         *         请求：appid
         *         返回：无
         */
        SKY_COMMAND_APPSTORE_MOBILE_PAUSEDOWNLOAD_SKYAPP,

        /**
         * 恢复下载（SKY_COMMAND_APPSTORE_MOBILE_STARTDOWNLOAD_SKYAPP）：
         *         请求：appid
         *         返回：无
         */
        SKY_COMMAND_APPSTORE_MOBILE_STARTDOWNLOAD_SKYAPP,

        /**
         * 取消下载（SKY_COMMAND_APPSTORE_MOBILE_CANCELDOWNLOAD_SKYAPP）：
         *         请求：appid
         *         返回：无
         */
        SKY_COMMAND_APPSTORE_MOBILE_CANCELDOWNLOAD_SKYAPP,

        /**
         * 卸载应用（SKY_COMMAND_APPSTORE_MOBILE_UNINSTALL_APP）：
         *         请求：packageName
         *         返回：无
         */
        SKY_COMMAND_APPSTORE_MOBILE_UNINSTALL_APP,

        /**
         * 获取APKINFO（SKY_COMMAND_APPSTORE_MOBILE_GET_APKINFO）：
         *         请求：appid
         *         返回：AppResultBean<AppMessage.MobileApkInfo>
         */
        SKY_COMMAND_APPSTORE_MOBILE_GET_APKINFO,

        /**
         * 获取应用状态（SKY_COMMAND_APPSTORE_MOBILE_GET_APPSTATUS）：
         *         请求：List<packageName>
         *         返回：AppResultBean<List<AppMessage.MobileAppStatus>>
         */
        SKY_COMMAND_APPSTORE_MOBILE_GET_APPSTATUS,

        /**
         * 安装应用（SKY_COMMAND_APPSTORE_MOBILE_INSTALL_APP）：
         *         请求：path
         *         返回：无
         */
        SKY_COMMAND_APPSTORE_MOBILE_INSTALL_APP,

        /**
         * 获取已下载应用（SKY_COMMAND_APPSTORE_MOBILE_GET_DOWNLOADED_SKYAPPS）：
         *         请求：无
         *         返回：AppResultBean<List<AppResultBean.GetDownLoaded>>
         */
        SKY_COMMAND_APPSTORE_MOBILE_GET_DOWNLOADED_SKYAPPS,

        /**
         * 安装开始监听（SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONINSTALLSTART）：
         *         返回：AppResultBean<AppResultBean.OnInstallstart>
         */
        SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONINSTALLSTART,

        /**
         * 安装结束监听（SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONINSTALLED）：
         *         返回：AppResultBean<AppResultBean.OnInstalled>
         */
        SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONINSTALLED,

        /**
         * 卸载开始监听（SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONUNINSTALLSTART）：
         *         返回：AppResultBean<AppResultBean.OnUnInstalled>
         */
        SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONUNINSTALLSTART,

        /**
         * 卸载完成监听（SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONUNINSTALLED）：
         *         返回：AppResultBean<AppResultBean.OnUnInstalled>
         */
        SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONUNINSTALLED,

        /**
         * 下载开始监听（SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONPROCESS）：
         *         返回：AppResultBean<AppResultBean.OnProcessInfo>
         */
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONPROCESS,

        /**
         * 下载入队监听（SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONREADY）：
         *         返回：AppResultBean<AppResultBean.DownTask>
         */
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONREADY,

        /**
         * 下载开始监听（SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONPREPARE）：
         *         返回：AppResultBean<AppResultBean.DownTask>
         */
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONPREPARE,

        /**
         * 下载进度监听（SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONSTART）：
         *         返回：AppResultBean<AppResultBean.DownTask>
         */
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONSTART,

        /**
         * 下载暂停监听（SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONSTOP）：
         *         返回：AppResultBean<AppResultBean.DownTask>
         */
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONSTOP,

        /**
         * 下载完成监听（SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONFINISH）：
         *         返回：AppResultBean<AppResultBean.DownTask>
         */
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONFINISH,

        /**
         * 下载删除监听（SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONDELETE）：
         *         返回：AppResultBean<AppResultBean.DownTask>
         */
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONDELETE,

//        START_APP,
//        INSTALL,
//        UNINSTALL,
//        GET_APP_LIST,
//        DOWNLOAD
    }

    public String pkgName;
    public String mainACtivity;
    public String appId;
    public String toJson() {
        return new Gson().toJson(this);
    }

}

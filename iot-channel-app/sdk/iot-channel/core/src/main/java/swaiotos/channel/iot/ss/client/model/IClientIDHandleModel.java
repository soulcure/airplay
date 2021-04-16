package swaiotos.channel.iot.ss.client.model;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

import swaiotos.channel.iot.ss.client.ClientManager;

/**
 * @ClassName: IClientIdHandleModel
 * @Author: AwenZeng
 * @CreateDate: 2020/3/25 21:16
 * @Description:
 */
public interface IClientIDHandleModel {
    /**
     * 查找手机中符合action的client
     *
     * @param intent
     */
    void findClients(Intent intent);


    /**
     * 从activity列表中添加client
     *
     * @param activities
     * @param isNeedNotify
     */
    void addActivities(List<ResolveInfo> activities, boolean isNeedNotify);

    /**
     * 从servie列表中添加client
     *
     * @param services
     * @param isNeedNotify
     */
    void addServices(List<ResolveInfo> services, boolean isNeedNotify);

    /**
     * 删除client
     *
     * @param pkgName
     * @return
     */
    void removeClient(String pkgName);

    /**
     * 添加client
     *
     * @return
     */
    void addClient(String pkgName);

    /**
     * 更新client
     *
     * @param pkgName
     * @return
     */
    void updateClient(String pkgName);

    /**
     * 根据包名添加activity_clientID
     *
     * @param packageName
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    void addActivityClient(String packageName);

    /**
     * 根据包名添加service_clientID
     *
     * @param packageName
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    void addServiceClient(String packageName);

    /**
     * 根据包名更新ActivityClient
     *
     * @param packageName
     */
    void updateActivityClient(String packageName);

    /**
     * 根据包名更新ServiceClient
     *
     * @param packageName
     */
    void updateServiceClient(String packageName);

    /**
     * clientID验证
     *
     * @param cn
     * @return
     */
    String verify(ComponentName cn, ComponentInfo componentInfo) throws VerifyError;

    /**
     * 注册应用安装变化广播
     */
    void registerAppStatusReceiver();

    /**
     * 解注册应用安装变化广播
     */
    void unRegisterAppStatusReceiver();

    /**
     * 添加Client变化回调
     *
     * @param listener
     */
    void addOnClientChangeListener(ClientManager.OnClientChangeListener listener);
}

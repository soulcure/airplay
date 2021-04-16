package swaiotos.channel.iot.ss.client.model;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.client.ClientManager;
import swaiotos.channel.iot.ss.client.ClientManagerImpl;
import swaiotos.channel.iot.ss.client.Clients;
import swaiotos.channel.iot.utils.EmptyUtils;
import swaiotos.channel.iot.utils.LogUtil;

import static android.content.ContentValues.TAG;
import static swaiotos.channel.iot.ss.client.ClientManager.OnClientChangeListener.VERSION_REMOVE;

/**
 * @ClassName: ClientIdHandleModel
 * @Author: AwenZeng
 * @CreateDate: 2020/3/25 21:18
 * @Description: ClientID处理Model
 */
public class ClientIDHandleModel implements IClientIDHandleModel {

    private Context mContext;
    private PackageManager pm;
    private ClientManager.OnClientChangeListener mClientChangeListener;
    private static final String SUFFIX_ACTIVITY = "_activity";
    private static final String SUFFIX_SERVICE = "_service";
    private BroadcastReceiver appStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.androidLog("应用状态变化：" + intent.getAction());
            String packName = intent.getData().getSchemeSpecificPart();
            //是否是覆盖安装
            boolean isCoverInstall = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            switch (intent.getAction()) {
                case Intent.ACTION_PACKAGE_ADDED:
                    if (!isCoverInstall) {
                        addClient(packName);
                    }
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    if (!isCoverInstall) {
                        removeClient(packName);
                    }
                    break;
                case Intent.ACTION_PACKAGE_REPLACED:
                    updateClient(packName);
                    break;
            }
        }
    };
    private SSContext mSSContext;

    public ClientIDHandleModel(Context context, SSContext ssContext) {
        mSSContext = ssContext;
        mContext = context;
        pm = mContext.getPackageManager();
        registerAppStatusReceiver();
    }

    @Override
    public void registerAppStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(appStatusReceiver, intentFilter);
    }

    @Override
    public void unRegisterAppStatusReceiver() {
        try {
            mContext.unregisterReceiver(appStatusReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void findClients(Intent intent) {
        if (intent == null) {
            return;
        }
        Clients.getInstance().setIntent(intent);
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServices(intent, PackageManager.GET_META_DATA);
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        addServices(services, false);
        addActivities(activities, false);
    }

    @Override
    public void addServices(List<ResolveInfo> services, boolean isNeedNotify) {
        if (services != null) {
            for (ResolveInfo service : services) {
                String packageName = service.serviceInfo.packageName;
                String className = service.serviceInfo.name;
                ComponentName cn = new ComponentName(packageName, className);
                String id = null;
                try {
                    id = verify(cn, service.serviceInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(id)) {
                    int version = service.serviceInfo.metaData.getInt(SSChannelClient.META_VERSION, 0);
                    ClientManagerImpl.Client client = new ClientManagerImpl.Client(ClientManagerImpl.Client.TYPE_SERVICE, cn, version);
                    Clients.getInstance().getClients().put(id, client);
                    Clients.getInstance().getPackageNames().put(packageName + SUFFIX_SERVICE, id);
                    Clients.getInstance().getVersions().put(packageName + SUFFIX_SERVICE, version);
                    Log.d(TAG, "find client " + cn + "@" + id);
                    if (isNeedNotify && EmptyUtils.isNotEmpty(mClientChangeListener)) {
                        mClientChangeListener.onClientChange(id, version);
                    }
                }
            }
        }
    }

    @Override
    public void addActivities(List<ResolveInfo> activities, boolean isNeedNotify) {
        if (activities != null) {
            for (ResolveInfo activity : activities) {
                String packageName = activity.activityInfo.packageName;
                String className = activity.activityInfo.name;
                ComponentName cn = new ComponentName(packageName, className);
                String id = null;
                try {
                    id = verify(cn, activity.activityInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(id)) {
                    int version = activity.activityInfo.metaData.getInt(SSChannelClient.META_VERSION, 0);
                    ClientManagerImpl.Client client = new ClientManagerImpl.Client(ClientManagerImpl.Client.TYPE_ACTIVITY, cn, version);
                    Clients.getInstance().getClients().put(id, client);
                    Clients.getInstance().getPackageNames().put(packageName + SUFFIX_ACTIVITY, id);
                    Clients.getInstance().getVersions().put(packageName + SUFFIX_ACTIVITY, version);
                    Log.d(TAG, "find client " + cn + "@" + id);
                    if (isNeedNotify && EmptyUtils.isNotEmpty(mClientChangeListener)) {
                        mClientChangeListener.onClientChange(id, version);
                    }
                }
            }
        }
    }


    @Override
    public synchronized void removeClient(String pkgName) {
        try {
            Map<String, String> pkgNameMap = Clients.getInstance().getPackageNames();
            Map<String, Integer> versionMap = Clients.getInstance().getVersions();
            Map<String, ClientManagerImpl.Client> clientMap = Clients.getInstance().getClients();
            String activityPkgName = pkgName + SUFFIX_ACTIVITY;
            String servciePkgName = pkgName + SUFFIX_SERVICE;
            String activityClientID = pkgNameMap.get(activityPkgName);
            String serviceClientID = pkgNameMap.get(servciePkgName);
            if (EmptyUtils.isNotEmpty(activityClientID) || EmptyUtils.isNotEmpty(serviceClientID)) {
                for (Map.Entry<String, ClientManagerImpl.Client> item : clientMap.entrySet()) {
                    if (EmptyUtils.isNotEmpty(activityClientID) && activityClientID.equals(item.getKey())
                            || EmptyUtils.isNotEmpty(serviceClientID) && serviceClientID.equals(item.getKey())) {
                        clientMap.remove(item.getKey());
                        if (EmptyUtils.isNotEmpty(mClientChangeListener)) {
                            if (EmptyUtils.isNotEmpty(activityClientID)) {
                                mClientChangeListener.onClientChange(activityClientID, VERSION_REMOVE);
                            }
                            if (EmptyUtils.isNotEmpty(serviceClientID)) {
                                mClientChangeListener.onClientChange(serviceClientID, VERSION_REMOVE);
                            }
                        }
                        break;
                    }
                }
                for (Map.Entry<String, String> item : pkgNameMap.entrySet()) {
                    if (activityPkgName.equals(item.getKey()) || servciePkgName.equals(item.getKey())) {
                        pkgNameMap.remove(item.getKey());
                        break;
                    }
                }
                for (Map.Entry<String, Integer> item : versionMap.entrySet()) {
                    if (activityPkgName.equals(item.getKey()) || servciePkgName.equals(item.getKey())) {
                        versionMap.remove(item.getKey());
                        break;
                    }
                }
                Clients.getInstance().setClients(clientMap);
                Clients.getInstance().setVersions(versionMap);
                Clients.getInstance().setPackageNames(pkgNameMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateClient(String pkgName) {
        try {
            updateActivityClient(pkgName);
            updateServiceClient(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String verify(ComponentName cn, ComponentInfo componentInfo) throws VerifyError {
        String id = componentInfo.metaData.getString(SSChannelClient.META_ID);
        String key = componentInfo.metaData.getString(SSChannelClient.META_KEY);

        if (mSSContext.getSmartScreenManager().performClientVerify(mContext, cn, id, key)) {
            return id;
        } else {
            throw new VerifyError(cn + " verify failed! Checker is null");
        }
    }

    @Override
    public void addActivityClient(String packageName) {
        Intent resolveIntent = Clients.getInstance().getIntent();
        if (EmptyUtils.isNotEmpty(resolveIntent)) {
            resolveIntent.setPackage(packageName);
            try {
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(resolveIntent, PackageManager.GET_META_DATA);
                addActivities(resolveInfos, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addServiceClient(String packageName) {
        Intent resolveIntent = Clients.getInstance().getIntent();
        if (EmptyUtils.isNotEmpty(resolveIntent)) {
            resolveIntent.setPackage(packageName);
            try {
                List<ResolveInfo> resolveInfos = pm.queryIntentServices(resolveIntent, PackageManager.GET_META_DATA);
                addServices(resolveInfos, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void updateActivityClient(String pkgName) {
        Intent resolveIntent = Clients.getInstance().getIntent();
        if (EmptyUtils.isNotEmpty(resolveIntent)) {
            resolveIntent.setPackage(pkgName);
            try {
                Map<String, ClientManagerImpl.Client> clientMap = Clients.getInstance().getClients();
                Map<String, String> pkgNameMap = Clients.getInstance().getPackageNames();
                Map<String, Integer> versionMap = Clients.getInstance().getVersions();
                String activityPkgName = pkgName + SUFFIX_ACTIVITY;
                String orginId = pkgNameMap.get(activityPkgName);
                Integer orginVersion = versionMap.get(activityPkgName);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(resolveIntent, PackageManager.GET_META_DATA);
                if (EmptyUtils.isNotEmpty(resolveInfos)) {
                    for (ResolveInfo activity : resolveInfos) {
                        String className = activity.activityInfo.name;
                        ComponentName cn = new ComponentName(pkgName, className);
                        String id = null;
                        int version = 0;
                        try {
                            id = verify(cn, activity.activityInfo);
                        } catch (Exception e) {
                            //ClentID配置清除
                            deleteClientID(pkgName, false);
                            e.printStackTrace();
                            return;
                        }

                        try {
                            //防止version没有配置，但也可以接收
                            version = activity.activityInfo.metaData.getInt(SSChannelClient.META_VERSION, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (EmptyUtils.isNotEmpty(id)) {
                            if (!clientMap.containsKey(id)) {//新增或修改ClientID
                                if (EmptyUtils.isNotEmpty(orginId)) {//原来有ClientID,修改ClientID
                                    //删除原来的clientID
                                    for (Map.Entry<String, ClientManagerImpl.Client> item : clientMap.entrySet()) {
                                        if (orginId.equals(item.getKey())) {
                                            clientMap.remove(item.getKey());
                                            break;
                                        }
                                    }
                                    Clients.getInstance().setClients(clientMap);
                                }
                                //添加或修改ClientID
                                ClientManagerImpl.Client client = new ClientManagerImpl.Client(ClientManagerImpl.Client.TYPE_ACTIVITY, cn, version);
                                Clients.getInstance().getClients().put(id, client);
                                Clients.getInstance().getPackageNames().put(activityPkgName, id);
                                Clients.getInstance().getVersions().put(activityPkgName, version);
                                Log.d(TAG, "udpate client " + cn + "@" + id);
                                if (EmptyUtils.isNotEmpty(mClientChangeListener)) {
                                    mClientChangeListener.onClientChange(id, version);
                                }
                            } else if (EmptyUtils.isNotEmpty(orginVersion) && (version > orginVersion || version < orginVersion)) {
                                //Version号变化
                                if (EmptyUtils.isNotEmpty(mClientChangeListener)) {
                                    mClientChangeListener.onClientChange(id, version);
                                }
                            }
                        } else if (EmptyUtils.isNotEmpty(orginId)) {
                            //配置ClientID清楚
                            deleteClientID(pkgName, false);
                        } else if (EmptyUtils.isNotEmpty(orginVersion) && (version > orginVersion || version < orginVersion)) {
                            //Version号变化
                            if (EmptyUtils.isNotEmpty(mClientChangeListener)) {
                                mClientChangeListener.onClientChange(id, version);
                            }
                        }
                    }
                } else {//接收Activity删除
                    deleteClientID(pkgName, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateServiceClient(String pkgName) {
        Intent resolveIntent = Clients.getInstance().getIntent();
        if (EmptyUtils.isNotEmpty(resolveIntent)) {
            resolveIntent.setPackage(pkgName);
            try {
                Map<String, ClientManagerImpl.Client> clientMap = Clients.getInstance().getClients();
                Map<String, String> pkgNameMap = Clients.getInstance().getPackageNames();
                Map<String, Integer> versionMap = Clients.getInstance().getVersions();
                String servciePkgName = pkgName + SUFFIX_SERVICE;
                String orginId = pkgNameMap.get(servciePkgName);
                Integer orginVersion = versionMap.get(servciePkgName);
                List<ResolveInfo> services = pm.queryIntentServices(resolveIntent, PackageManager.GET_META_DATA);
                if (EmptyUtils.isNotEmpty(services)) {
                    for (ResolveInfo service : services) {
                        String className = service.serviceInfo.name;
                        ComponentName cn = new ComponentName(pkgName, className);
                        String id = null;
                        int version = 0;
                        try {
                            id = verify(cn, service.serviceInfo);
                        } catch (Exception e) {
                            //删除Meta配置
                            deleteClientID(pkgName, true);
                            e.printStackTrace();
                            return;
                        }

                        try {
                            //防止version没有配置，但也可以接收
                            version = service.serviceInfo.metaData.getInt(SSChannelClient.META_VERSION, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (EmptyUtils.isNotEmpty(id)) {
                            if (!clientMap.containsKey(id) || true) {//新增或修改ClientID
                                if (EmptyUtils.isNotEmpty(orginId)) {//原来有ClientID,修改ClientID
                                    //删除原来的clientID
                                    for (Map.Entry<String, ClientManagerImpl.Client> item : clientMap.entrySet()) {
                                        if (orginId.equals(item.getKey())) {
                                            clientMap.remove(item.getKey());
                                            break;
                                        }
                                    }
                                    Clients.getInstance().setClients(clientMap);
                                }
                                //添加ClientID
                                ClientManagerImpl.Client client = new ClientManagerImpl.Client(ClientManagerImpl.Client.TYPE_SERVICE, cn, version);
                                Clients.getInstance().getClients().put(id, client);
                                Log.e("yao", "Clients put id---" + id + " version---" + client.version);

                                Clients.getInstance().getPackageNames().put(servciePkgName, id);
                                Clients.getInstance().getVersions().put(servciePkgName, version);
                                Log.d(TAG, "update client " + cn + "@" + id);
                                if (EmptyUtils.isNotEmpty(mClientChangeListener)) {
                                    mClientChangeListener.onClientChange(id, version);
                                }
                            } else if (EmptyUtils.isNotEmpty(orginVersion) && (version > orginVersion || version < orginVersion)) {//version号变化
                                if (EmptyUtils.isNotEmpty(mClientChangeListener)) {
                                    mClientChangeListener.onClientChange(id, version);
                                }
                            }
                        } else if (EmptyUtils.isNotEmpty(orginId)) {
                            //clientID配置清楚
                            deleteClientID(pkgName, true);
                        } else if (EmptyUtils.isNotEmpty(orginVersion) && (version > orginVersion || version < orginVersion)) {//version号变化
                            if (EmptyUtils.isNotEmpty(mClientChangeListener)) {
                                mClientChangeListener.onClientChange(id, version);
                            }
                        }
                    }
                } else {//接收Service删除
                    deleteClientID(pkgName, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新ClientID的时候删除
     *
     * @param pkgName
     * @param isService
     */
    private void deleteClientID(String pkgName, boolean isService) {
        Map<String, ClientManagerImpl.Client> clientMap = Clients.getInstance().getClients();
        Map<String, String> pkgNameMap = Clients.getInstance().getPackageNames();
        Map<String, Integer> versionMap = Clients.getInstance().getVersions();
        String suffixPkgName = "";
        if (isService) {
            suffixPkgName = pkgName + SUFFIX_SERVICE;
        } else {
            suffixPkgName = pkgName + SUFFIX_ACTIVITY;
        }
        String orginId = pkgNameMap.get(suffixPkgName);
        if (EmptyUtils.isNotEmpty(orginId)) {
            for (Map.Entry<String, ClientManagerImpl.Client> item : clientMap.entrySet()) {
                if (orginId.equals(item.getKey())) {
                    clientMap.remove(item.getKey());
                    break;
                }
            }
            for (Map.Entry<String, String> item : pkgNameMap.entrySet()) {
                if (suffixPkgName.equals(item.getKey())) {
                    pkgNameMap.remove(item.getKey());
                    break;
                }
            }
            for (Map.Entry<String, Integer> item : versionMap.entrySet()) {
                if (suffixPkgName.equals(item.getKey())) {
                    versionMap.remove(item.getKey());
                    break;
                }
            }
            Log.d(TAG, "delete clientId " + null + "@" + orginId);
            Clients.getInstance().setClients(clientMap);
            Clients.getInstance().setVersions(versionMap);
            Clients.getInstance().setPackageNames(pkgNameMap);
            if (EmptyUtils.isNotEmpty(mClientChangeListener)) {
                mClientChangeListener.onClientChange(orginId, VERSION_REMOVE);
            }
        }
    }

    @Override
    public void addClient(String pkgName) {
        try {
            addActivityClient(pkgName);
            addServiceClient(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addOnClientChangeListener(ClientManager.OnClientChangeListener listener) {
        mClientChangeListener = listener;
    }
}

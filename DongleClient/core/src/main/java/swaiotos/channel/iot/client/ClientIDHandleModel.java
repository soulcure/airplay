package swaiotos.channel.iot.client;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.sdk.SSChannelClient;
import com.coocaa.sdk.entity.IMMessage;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ClientIDHandleModel {
    private static final String TAG = ClientIDHandleModel.class.getSimpleName();

    private final Context mContext;
    private final PackageManager pm;
    private static final String SUFFIX_ACTIVITY = "_activity";
    private static final String SUFFIX_SERVICE = "_service";


    private final BroadcastReceiver appStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packName = Objects.requireNonNull(intent.getData()).getSchemeSpecificPart();
            //是否是覆盖安装
            boolean isCoverInstall = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            switch (Objects.requireNonNull(intent.getAction())) {
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

    public ClientIDHandleModel(Context context) {
        mContext = context;
        pm = mContext.getPackageManager();

        Intent filter = new Intent(SSChannelClient.DEFAULT_ACTION);
        findClients(filter);
    }


    public boolean start(String clientID, IMMessage message) {
        Map<String, Client> clients = Clients.getInstance().getClients();
        Client client = clients.get(clientID);
        if (client != null) {
            Intent intent = new Intent();
            intent.setComponent(client.cn);
            intent.putExtra("message", message);
            try {
                if (client.type == Client.TYPE_SERVICE) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mContext.startForegroundService(intent);
                    } else {
                        mContext.startService(intent);
                    }
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
                Log.d(TAG, "send " + message + " to " + client);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public void registerAppStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(appStatusReceiver, intentFilter);
    }

    public void unRegisterAppStatusReceiver() {
        try {
            mContext.unregisterReceiver(appStatusReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void findClients(Intent intent) {
        if (intent == null) {
            return;
        }
        Clients.getInstance().setIntent(intent);
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServices(intent, PackageManager.GET_META_DATA);
        addServices(services);
    }

    public void addServices(List<ResolveInfo> services) {
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
                    Client client = new Client(Client.TYPE_SERVICE, cn, version);
                    Clients.getInstance().getClients().put(id, client);
                    Clients.getInstance().getPackageNames().put(packageName + SUFFIX_SERVICE, id);
                    Clients.getInstance().getVersions().put(packageName + SUFFIX_SERVICE, version);
                    Log.d(TAG, "find client " + cn + "@" + id);
                }
            }
        }
    }


    public synchronized void removeClient(String pkgName) {
        try {
            Map<String, String> pkgNameMap = Clients.getInstance().getPackageNames();
            Map<String, Integer> versionMap = Clients.getInstance().getVersions();
            Map<String, Client> clientMap = Clients.getInstance().getClients();
            String activityPkgName = pkgName + SUFFIX_ACTIVITY;
            String servicePkgName = pkgName + SUFFIX_SERVICE;
            String activityClientID = pkgNameMap.get(activityPkgName);
            String serviceClientID = pkgNameMap.get(servicePkgName);
            if (!TextUtils.isEmpty(activityClientID) || !TextUtils.isEmpty(serviceClientID)) {
                for (Map.Entry<String, Client> item : clientMap.entrySet()) {
                    if (!TextUtils.isEmpty(activityClientID) && activityClientID.equals(item.getKey())
                            || !TextUtils.isEmpty(serviceClientID) && serviceClientID.equals(item.getKey())) {
                        clientMap.remove(item.getKey());
                        break;
                    }
                }
                for (Map.Entry<String, String> item : pkgNameMap.entrySet()) {
                    if (activityPkgName.equals(item.getKey()) || servicePkgName.equals(item.getKey())) {
                        pkgNameMap.remove(item.getKey());
                        break;
                    }
                }
                for (Map.Entry<String, Integer> item : versionMap.entrySet()) {
                    if (activityPkgName.equals(item.getKey()) || servicePkgName.equals(item.getKey())) {
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

    public void updateClient(String pkgName) {
        try {
            updateActivityClient(pkgName);
            updateServiceClient(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String verify(ComponentName cn, ComponentInfo componentInfo) throws VerifyError {
        String id = componentInfo.metaData.getString(SSChannelClient.META_ID);
        //String key = componentInfo.metaData.getString(SSChannelClient.META_KEY);
        return id;
    }

    public void addServiceClient(String packageName) {
        Intent resolveIntent = Clients.getInstance().getIntent();
        if (resolveIntent != null) {
            resolveIntent.setPackage(packageName);
            try {
                List<ResolveInfo> resolveInfos = pm.queryIntentServices(resolveIntent, PackageManager.GET_META_DATA);
                addServices(resolveInfos);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void updateActivityClient(String pkgName) {
        Intent resolveIntent = Clients.getInstance().getIntent();
        if (resolveIntent != null) {
            resolveIntent.setPackage(pkgName);
            try {
                Map<String, Client> clientMap = Clients.getInstance().getClients();
                Map<String, String> pkgNameMap = Clients.getInstance().getPackageNames();
                Map<String, Integer> versionMap = Clients.getInstance().getVersions();
                String activityPkgName = pkgName + SUFFIX_ACTIVITY;
                String orginId = pkgNameMap.get(activityPkgName);
                Integer orginVersion = versionMap.get(activityPkgName);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(resolveIntent, PackageManager.GET_META_DATA);
                if (resolveInfos.size() > 0) {
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

                        if (!TextUtils.isEmpty(id)) {
                            if (!clientMap.containsKey(id)) {//新增或修改ClientID
                                if (!TextUtils.isEmpty(orginId)) {//原来有ClientID,修改ClientID
                                    //删除原来的clientID
                                    for (Map.Entry<String, Client> item : clientMap.entrySet()) {
                                        if (orginId.equals(item.getKey())) {
                                            clientMap.remove(item.getKey());
                                            break;
                                        }
                                    }
                                    Clients.getInstance().setClients(clientMap);
                                }
                                //添加或修改ClientID
                                Client client = new Client(Client.TYPE_ACTIVITY, cn, version);
                                Clients.getInstance().getClients().put(id, client);
                                Clients.getInstance().getPackageNames().put(activityPkgName, id);
                                Clients.getInstance().getVersions().put(activityPkgName, version);
                                Log.d(TAG, "udpate client " + cn + "@" + id);
                            } else if (orginVersion != null && (version > orginVersion || version < orginVersion)) {
                                //Version号变化
                            }
                        } else if (!TextUtils.isEmpty(orginId)) {
                            //配置ClientID清楚
                            deleteClientID(pkgName, false);
                        } else if (orginVersion != null && (version > orginVersion || version < orginVersion)) {
                            //Version号变化
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

    public void updateServiceClient(String pkgName) {
        Intent resolveIntent = Clients.getInstance().getIntent();
        if (resolveIntent != null) {
            resolveIntent.setPackage(pkgName);
            try {
                Map<String, Client> clientMap = Clients.getInstance().getClients();
                Map<String, String> pkgNameMap = Clients.getInstance().getPackageNames();
                Map<String, Integer> versionMap = Clients.getInstance().getVersions();
                String servicePkgName = pkgName + SUFFIX_SERVICE;
                String orginId = pkgNameMap.get(servicePkgName);
                Integer orginVersion = versionMap.get(servicePkgName);
                List<ResolveInfo> services = pm.queryIntentServices(resolveIntent, PackageManager.GET_META_DATA);
                if (services.size() > 0) {
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

                        if (!TextUtils.isEmpty(id)) {
                            if (!clientMap.containsKey(id)) {//新增或修改ClientID
                                if (!TextUtils.isEmpty(orginId)) {//原来有ClientID,修改ClientID
                                    //删除原来的clientID
                                    for (Map.Entry<String, Client> item : clientMap.entrySet()) {
                                        if (orginId.equals(item.getKey())) {
                                            clientMap.remove(item.getKey());
                                            break;
                                        }
                                    }
                                    Clients.getInstance().setClients(clientMap);
                                }
                                //添加ClientID
                                Client client = new Client(Client.TYPE_SERVICE, cn, version);
                                Clients.getInstance().getClients().put(id, client);
                                Clients.getInstance().getPackageNames().put(servicePkgName, id);
                                Clients.getInstance().getVersions().put(servicePkgName, version);
                                Log.d(TAG, "update client " + cn + "@" + id);

                            } else if (orginVersion != null && (version > orginVersion || version < orginVersion)) {//version号变化

                            }
                        } else if (!TextUtils.isEmpty(orginId)) {
                            //clientID配置清楚
                            deleteClientID(pkgName, true);
                        } else if (orginVersion != null && (version > orginVersion || version < orginVersion)) {//version号变化

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
        Map<String, Client> clientMap = Clients.getInstance().getClients();
        Map<String, String> pkgNameMap = Clients.getInstance().getPackageNames();
        Map<String, Integer> versionMap = Clients.getInstance().getVersions();
        String suffixPkgName = "";
        if (isService) {
            suffixPkgName = pkgName + SUFFIX_SERVICE;
        } else {
            suffixPkgName = pkgName + SUFFIX_ACTIVITY;
        }
        String orginId = pkgNameMap.get(suffixPkgName);
        if (!TextUtils.isEmpty(orginId)) {
            for (Map.Entry<String, Client> item : clientMap.entrySet()) {
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
        }
    }

    public void addClient(String pkgName) {
        try {
            addServiceClient(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

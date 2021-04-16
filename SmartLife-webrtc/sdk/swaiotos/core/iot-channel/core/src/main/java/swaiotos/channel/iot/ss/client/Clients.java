package swaiotos.channel.iot.ss.client;


import android.content.Intent;

import java.util.HashMap;
import java.util.Map;


/**
 * Describe:clients类
 * Created by AwenZeng on 2019/1/9
 */
public class Clients {
    private static Clients instance = null;
    private Intent intent;
    private Map<String,String> packageNames = new HashMap<>();
    private Map<String,Integer> versions = new HashMap<>();
    private Map<String, ClientManagerImpl.Client> clients = new HashMap<>();
    public static Clients getInstance() {
        if (instance == null) {
            synchronized (Clients.class) {
                if (instance == null) {
                    instance = new Clients();
                }
            }
        }
        return instance;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Map<String, ClientManagerImpl.Client> getClients() {
        return clients;
    }

    public void setClients(Map<String, ClientManagerImpl.Client> clients) {
        this.clients = clients;
    }

    public Map<String, String> getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(Map<String, String> packageNames) {
        this.packageNames = packageNames;
    }

    public Map<String, Integer> getVersions() {
        return versions;
    }

    public void setVersions(Map<String, Integer> versions) {
        this.versions = versions;
    }
}

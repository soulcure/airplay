package swaiotos.channel.iot.client;


import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class Clients {
    private static Clients instance = null;
    private Intent intent;
    private Map<String, String> packageNames = new HashMap<>();
    private Map<String, Integer> versions = new HashMap<>();
    private Map<String, Client> clients = new HashMap<>();

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

    public Map<String, Client> getClients() {
        return clients;
    }

    public void setClients(Map<String, Client> clients) {
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

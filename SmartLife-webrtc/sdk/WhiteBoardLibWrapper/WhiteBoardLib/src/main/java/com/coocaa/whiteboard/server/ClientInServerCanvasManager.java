package com.coocaa.whiteboard.server;

import java.util.HashMap;
import java.util.Map;

public class ClientInServerCanvasManager {

    private Map<String, ClientInServerCanvasInfo> map = new HashMap<>();

    public void addClientCanvas(String cid) {
        if(map.containsKey(cid))
            return ;
        map.put(cid, defaultInfo());
    }

    public ClientInServerCanvasInfo getClientCanvas(String cid) {
        if(!map.containsKey(cid))
            addClientCanvas(cid);
        return map.get(cid);
    }

    public void removeClientCanvas(String cid) {
        map.remove(cid);
    }

    private ClientInServerCanvasInfo defaultInfo() {
        ClientInServerCanvasInfo info = new ClientInServerCanvasInfo();
        //...
        return info;
    }
}

package com.swaiot.webrtcc;

import java.util.List;

public class ModelUser {

    private String action;
    private String type;
    private List<Payload> payload;

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setPayload(List<Payload> payload) {
        this.payload = payload;
    }

    public List<Payload> getPayload() {
        return payload;
    }


    public static class Payload {
        private String userName;

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int size = payload.size();
        for (int i = 0; i < size; i++) {
            Payload item = payload.get(i);
            sb.append(item.userName);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
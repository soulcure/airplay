package com.coocaa.tvpi.module.log;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.tvpi.module.service.api.SmartDeviceConnectHelper;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class ConnectDeviceEvent {

    public static void submit(String connectType, boolean connectSuccess, long connectTime) {
        PayloadEvent.submit("iotchannel.link_events", connectSuccess ? "connectTime" : "connectError", new ConnectData(connectType, connectTime));
    }

    static class ConnectData implements Serializable {
        public String type;
        public long time;
        public String deviceId;

        public ConnectData(String type, long time) {
            this.type = type;
            this.time = time;
            this.deviceId = SmartDeviceConnectHelper.getDeviceActiveId(SSConnectManager.getInstance().getDevice());
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ConnectData{");
            sb.append("type='").append(type).append('\'');
            sb.append(", time=").append(time);
            sb.append(", deviceId='").append(deviceId).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}

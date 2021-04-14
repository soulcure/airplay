package com.swaiot.webrtc.entity;


import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class Model {
    private String type;
    private PayLoad payload;

    public static class PayLoad {
        private IceCandidate candidate;
        private SessionDescription sdp;

        public IceCandidate getIceCandidate() {
            return candidate;
        }

        public void setIceCandidate(IceCandidate iceCandidate) {
            this.candidate = iceCandidate;
        }

        public SessionDescription getSdp() {
            return sdp;
        }

        public void setSdp(SessionDescription sdp) {
            this.sdp = sdp;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PayLoad getPayload() {
        return payload;
    }

    public void setPayload(PayLoad payload) {
        this.payload = payload;
    }


    public Model() {

    }

    public Model(String in, boolean isWeb) {

        try {
            JSONObject json = new JSONObject(in);
            type = json.optString("type", "");
            JSONObject p = json.optJSONObject("payload");

            payload = new PayLoad();
            JSONObject s = p.optJSONObject("sdp");
            if (s != null) {
                String type = s.optString("type", "");

                String sdp;
                if (isWeb) {
                    sdp = s.optString("sdp", "");
                } else {
                    sdp = s.optString("description", "");
                }

                SessionDescription.Type t = SessionDescription.Type.fromCanonicalForm(type);

                SessionDescription sdpTemp = new SessionDescription(t, sdp);

                payload.setSdp(sdpTemp);
            }

            JSONObject c = p.optJSONObject("candidate");
            if (c != null) {
                String sdpMid = c.optString("sdpMid", "");
                int sdpMLineIndex = c.optInt("sdpMLineIndex", 0);

                String candidate;
                if (isWeb) {
                    candidate = c.optString("candidate", "");
                } else {
                    candidate = c.optString("sdp", "");
                }

                IceCandidate can = new IceCandidate(sdpMid, sdpMLineIndex, candidate);

                payload.setIceCandidate(can);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public String toJson(boolean isWeb) {
        String text = new Gson().toJson(this);

        if (isWeb) {
            text = text.replace("description", "sdp");

            if (text.contains("\"OFFER\"")) {
                text = text.replace("\"OFFER\"", "\"offer\"");
            } else if (text.contains("\"ANSWER\"")) {
                text = text.replace("\"ANSWER\"", "\"answer\"");
            }
        }
        return text;
    }
}

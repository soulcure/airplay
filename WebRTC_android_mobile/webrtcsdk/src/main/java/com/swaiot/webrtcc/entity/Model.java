package com.swaiot.webrtcc.entity;



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

    public Model(String in) {

        try {
            JSONObject json = new JSONObject(in);
            type = json.optString("type", "");
            JSONObject p = json.optJSONObject("payload");

            payload = new PayLoad();
            JSONObject s = p.optJSONObject("sdp");
            if (s != null) {
                String type = s.optString("type", "");

                String sdp = s.optString("description", "");
                SessionDescription.Type t = SessionDescription.Type.fromCanonicalForm(type);

                SessionDescription sdpTemp = new SessionDescription(t, sdp);

                payload.setSdp(sdpTemp);
            }

            JSONObject c = p.optJSONObject("candidate");
            if (c != null) {
                String sdpMid = c.optString("sdpMid", "");
                int sdpMLineIndex = c.optInt("sdpMLineIndex", 0);

                String candidate = c.optString("sdp", "");

                IceCandidate can = new IceCandidate(sdpMid, sdpMLineIndex, candidate);

                payload.setIceCandidate(can);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}

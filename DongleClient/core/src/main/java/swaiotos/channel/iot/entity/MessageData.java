package swaiotos.channel.iot.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class MessageData {
    private String id;
    @SerializedName("client-source")
    private String client_source;
    @SerializedName("client-target")
    private String client_target;
    private String type;
    private String content;
    private Map<String, String> extra;
    private boolean reply;

    public void setId(String id) {
        this.id = id;
    }

    public void setClient_source(String client_source) {
        this.client_source = client_source;
    }

    public void setClient_target(String client_target) {
        this.client_target = client_target;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }

    public void setReply(boolean reply) {
        this.reply = reply;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

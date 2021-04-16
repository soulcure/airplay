package com.coocaa.publib.data.channel;

import com.coocaa.publib.data.def.SkyMediaType;
import com.google.gson.Gson;

/**
 * @ClassName LongVideoParams
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-12-27
 * @Version TODO (write something)
 */
public class LongVideoParams {
    public String mediaID;
    public String childId;
    public String content_providers;
    public String mediaType;
    public String name;

    public LongVideoParams(String mediaID, String childId, String content_providers, String name) {
        this.mediaID = mediaID;
        this.childId = childId;
        this.content_providers = content_providers;
        this.mediaType = SkyMediaType.MEDIA_VIDEO.toString();
        this.name = name;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}

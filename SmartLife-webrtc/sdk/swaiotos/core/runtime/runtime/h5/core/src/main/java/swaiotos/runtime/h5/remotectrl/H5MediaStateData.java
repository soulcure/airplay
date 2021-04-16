package swaiotos.runtime.h5.remotectrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * @ClassName: VideoStateData
 * @Author: XuZeXiao
 * @CreateDate: 2020/11/9 21:58
 * @Description:
 */
public class H5MediaStateData implements Serializable {
    public String playCmd;
    public String mediaTitle;

    public H5MediaStateData(){

    }

    public H5MediaStateData(String playCmd,String mediaTitle){
        this.playCmd = playCmd;
        this.mediaTitle = mediaTitle;
    }

    public static String toJsonString(H5MediaStateData h5State){
        return JSON.toJSONString(h5State);
    }

    public static H5MediaStateData fromJson(String jsonString){
        return JSONObject.parseObject(jsonString,H5MediaStateData.class);
    }
}

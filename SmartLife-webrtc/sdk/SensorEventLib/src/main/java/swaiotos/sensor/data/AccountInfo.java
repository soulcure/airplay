package swaiotos.sensor.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class AccountInfo implements Serializable {
    public String nickName;
    public String mobile;
    public String avatar;
    public String open_id;
    public String accessToken;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}

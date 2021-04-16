package swaiotos.sensor.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class DisplayInfo implements Serializable {

    public int x;
    public int y;

    public int width;
    public int height;

    public int screenWidth;
    public int screenHeight;

    public DisplayInfo() {

    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}

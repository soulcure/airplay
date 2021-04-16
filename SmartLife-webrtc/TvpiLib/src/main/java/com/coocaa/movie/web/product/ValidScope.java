package com.coocaa.movie.web.product;

import java.io.Serializable;

/**
 * Created by forever on 18-3-15.
 */
public class ValidScope implements Serializable{
    public long start;//起始有效时间，UTC时间戳，单位：秒。
    public String start_readable;//易读的起始有效时间字符串，比如:"2015-10-14 16:45:34"
    public long end;//截止有效时间，UTC时间戳，单位：秒。
    public String end_readable;//易读的截止有效时间字符串，比如:"2015-11-14 16:45:34"

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ValidScope=[, start=");
        sb.append(start);
        sb.append(", end=");
        sb.append(end);
        sb.append(", end_readable=");
        sb.append(end_readable);
        sb.append("] ");
        return sb.toString();
    }
}

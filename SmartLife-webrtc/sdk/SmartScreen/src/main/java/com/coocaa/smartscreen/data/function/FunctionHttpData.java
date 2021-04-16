package com.coocaa.smartscreen.data.function;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: yuzhan
 */
public class FunctionHttpData implements Serializable {
    public int code;
    public String msg;
    public FunctionContent data;

    public static class FunctionContent  implements Serializable{
        public List<FunctionBean> content;
    }
}

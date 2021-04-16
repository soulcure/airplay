package swaiotos.runtime.h5.core.os;

import java.util.HashMap;
import java.util.Map;

public class H5RunType {

    public enum RunType{
        TV_RUNTYPE_ENUM(H5RunType.TV_RUNTYPE),MOBILE_RUNTYPE_ENUM(H5RunType.MOBILE_RUNTYPE);
        String mType;
        private RunType(String type){
            mType = type;
        }

        public String toString() {
            return this.mType;
        }

        private static final Map<String, RunType> values = new HashMap<String, RunType>();
        static {
            for (RunType g : RunType.values()) {
                if (values.put(g.mType, g) != null) {
                    throw new IllegalArgumentException("duplicate value: " + g.mType);
                }
            }
        }
        public static RunType fromString(String type){
            return values.get(type);
        }
    }
    public static String RUNTIME_KEY = "H5RUNTIME_KEY";
    public static String TV_RUNTYPE = "TV-RUNTIME";
    public static String MOBILE_RUNTYPE = "MOBILE-RUNTIME";

    public static String RUNTIME_NAV_KEY = "RUNTIME_NAV_KEY";
    public static String RUNTIME_NAV_FLOAT = "RUNTIME_NAV_FLOAT";
    public static String RUNTIME_NAV_FLOAT_NP = "RUNTIME_NAV_FLOAT_NP";
    public static String RUNTIME_NAV_TOP = "RUNTIME_NAV_TOP";

    public static String RUNTIME_TRANSITION_KEY = "RUNTIME_TRANSITION_KEY"; //启动动画效果
    public static String RUNTIME_TRANSITION_FROM_BOTTOM = "RUNTIME_TRANSITION_FROM_BOTTOM"; //从底部弹出
    public static String RUNTIME_TRANSITION_FROM_RIGHT = "RUNTIME_TRANSITION_FROM_RIGHT"; //从右边弹出


    public static String RUNTIME_NETWORK_FORCE_KEY = "RUNTIME_NETWORK_FORCE_KEY"; //是否配置了在哪种类型网络可用
    public static String RUNTIME_NETWORK_NORMAL = "NORMAL"; // 局域网/广域网都可用，默认是这个
    public static String RUNTIME_NETWORK_FORCE_LAN = "FORCE_LAN"; // 必须判断在局域网可用
    public static String RUNTIME_NETWORK_FORCE_WAN = "FORCE_WAN";  // 只能走广域网可用

    public static String RUNTIME_EXT_JS_URL = "RUNTIME_EXT_JS_URL";

    public static String RUNTIME_ORIENTATION_KEY = "orientation"; //web请求横竖屏，默认竖屏
    public static String RUNTIME_ORIENTATION_LANDSCAPE = "0"; //横屏
}

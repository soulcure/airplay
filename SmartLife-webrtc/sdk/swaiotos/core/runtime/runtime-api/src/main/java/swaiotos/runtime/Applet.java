package swaiotos.runtime;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: Applet
 * @Author: lu
 * @CreateDate: 2020/10/27 3:29 PM
 * @Description:
 */
public class Applet implements Serializable {
    public static class Builder {
        public static final String APPLET_NAME = "name";
        public static final String APPLET_ICON = "icon";
        public static final String APPLET_VERSION = "version";
        public static final String APPLET_RUNTIME = "runtime";
        public static final String APPLET_PARAMS = "params";

        public static String decode(Applet applet) {
            Uri uri = parse(applet);
            return Uri.decode(uri.toString());
        }

        public static Uri parse(Applet applet) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(applet.type);
            builder.authority(applet.id);
            builder.encodedPath(applet.target);
            if(!TextUtils.isEmpty(applet.name)) {
                builder.appendQueryParameter(APPLET_NAME, applet.name);
            }
            if(!TextUtils.isEmpty(applet.icon)) {
                builder.appendQueryParameter(APPLET_ICON, applet.icon);
            }
            builder.appendQueryParameter(APPLET_VERSION, String.valueOf(applet.version));

            if (applet.runtime != null) {
                String value = JSONObject.toJSONString(applet.runtime);
                builder.appendQueryParameter(APPLET_RUNTIME, value);
            }

            if (applet.params != null) {
                String value = JSONObject.toJSONString(applet.params);
                builder.appendQueryParameter(APPLET_PARAMS, value);
            }
            return builder.build();
        }

        public static Applet parse(Uri uri) {
            String type = uri.getScheme();
            String host = uri.getAuthority();
            String target = uri.getEncodedPath();
            String name = uri.getQueryParameter(APPLET_NAME);
            String icon = uri.getQueryParameter(APPLET_ICON);
            int version = 1;
            if(!TextUtils.isEmpty(APPLET_VERSION)) {
                try {
                    version = Integer.valueOf(uri.getQueryParameter(APPLET_VERSION));
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
            Map<String, String> runtime = null;
            {
                String _runtime = uri.getQueryParameter(APPLET_RUNTIME);
                Map<String, String> t = JSONObject.parseObject(_runtime, new TypeReference<Map<String, String>>() {
                });
                if (t != null) {
                    runtime = new HashMap<>(t);
                }
            }
            Map<String, String> params = null;
            {
                String _params = uri.getQueryParameter(APPLET_PARAMS);
                Map<String, String> t = JSONObject.parseObject(_params, new TypeReference<Map<String, String>>() {
                });
                if (t != null) {
                    params = new HashMap<>(t);
                }
            }

            Map<String, String> map = null;
            try {
                Set<String> keySet = uri.getQueryParameterNames();
                String value = null;
                for(String s : keySet) {
                    if(!TextUtils.equals(s, APPLET_NAME) && !TextUtils.equals(s, APPLET_ICON)
                            && !TextUtils.equals(s, APPLET_VERSION) && !TextUtils.equals(s, APPLET_RUNTIME)  && !TextUtils.equals(s, APPLET_PARAMS)) {
                        //其他额外参数
                        if(map == null) {
                            map = new HashMap<>();
                        }
                        value = uri.getQueryParameter(s);
                        if(!TextUtils.isEmpty(value)) {
                            map.put(s, value);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return new Applet(type, host, target, TextUtils.isEmpty(uri.getFragment()) ? null : uri.getFragment(), name, icon, version, runtime, params, map);
        }
    }

    public static final String APPLET_MP = "mp";
    public static final String APPLET_NP = "np";
    public static final String APPLET_H5 = "h5";
    public static final String APPLET_HTTP = "http";
    public static final String APPLET_HTTPS = "https";
    public static final List<String> APPLET_H = Arrays.asList(APPLET_H5, APPLET_HTTP, APPLET_HTTPS);

    public static final String RUNTIME_APPLET_FROM = "applet.from";

    private String type;
    private String id;
    private String target;
    private String name;
    private String fragment;
    private String icon;
    private int version;
    private Map<String, String> runtime;
    private Map<String, String> params;
    private Map<String, String> extMap = null;

    public Applet() {
    }

    public Applet(String type,
                  String id,
                  String target,
                  String name,
                  String icon,
                  int version,
                  Map<String, String> runtime,
                  Map<String, String> params) {
        this(type, id, target, null, name, icon, version, runtime, params, null);
    }

    public Applet(String type,
                  String id,
                  String target,
                  String fragment,
                  String name,
                  String icon,
                  int version,
                  Map<String, String> runtime,
                  Map<String, String> params,
                  Map<String, String> extMap) {
        setType(type);
        setId(id);
        setTarget(target);
        this.fragment = fragment;
        setName(name);
        setIcon(icon);
        setVersion(version);
        setRuntime(runtime);
        setParams(params);
        if(extMap != null) {
            this.extMap = new HashMap<>(extMap);
        }
    }

    public final void setFromApplet(String appletId) {
        if (this.runtime == null) {
            this.runtime = new HashMap<>();
        }
        this.runtime.put(RUNTIME_APPLET_FROM, appletId);
    }

    public final String getType() {
        return type;
    }

    public final String getId() {
        return id;
    }

    public final String getTarget() {
        return target;
    }

    public final String getName() {
        return name;
    }

    public final String getIcon() {
        return icon;
    }

    public final int getVersion() {
        return version;
    }

    public final String getRuntime(String key) {
        return runtime != null ? runtime.get(key) : null;
    }

    public final String getParams(String key) {
        return params != null ? params.get(key) : null;
    }

    public final Map<String, String> getAllRuntime() {
        return runtime != null ? new HashMap<>(runtime) : null;
    }

    public final Map<String, String> getAllParams() {
        return params != null ? new HashMap<>(params) : null;
    }

    public final Map<String, String> getExtMap() {
        return extMap != null ? new HashMap<>(extMap) : null;
    }

    public final void setType(String type) {
        this.type = type;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final void setTarget(String target) {
        this.target = target;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final void setIcon(String icon) {
        this.icon = icon;
    }

    public final void setVersion(int version) {
        this.version = version;
    }

    public String getFragment() {
        return fragment;
    }

    public final void setRuntime(Map<String, String> runtime) {
        this.runtime = runtime != null ? new HashMap<>(runtime) : null;
    }

    public final void setParams(Map<String, String> params) {
        this.params = params != null ? new HashMap<>(params) : null;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Applet{");
        sb.append("type='").append(type).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", target='").append(target).append('\'');
        sb.append(", fragment='").append(fragment).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", icon='").append(icon).append('\'');
        sb.append(", version=").append(version);
        sb.append(", runtime=").append(runtime);
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}

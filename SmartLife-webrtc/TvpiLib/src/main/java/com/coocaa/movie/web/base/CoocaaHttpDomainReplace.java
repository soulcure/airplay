package com.coocaa.movie.web.base;

import android.text.TextUtils;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: yuzhan
 */
public class CoocaaHttpDomainReplace {
    private static CoocaaHttpDomainReplace instance;
    private Map<String, String> domainBaseUrlMap;
    private Map<String, String> defaultBaseUrlMap;

    private CoocaaHttpDomainReplace(){
        domainBaseUrlMap = new ConcurrentHashMap<>();
        defaultBaseUrlMap = new ConcurrentHashMap<>();
    }

    public static CoocaaHttpDomainReplace getInstance() {
        if(instance == null) {
            synchronized (CoocaaHttpDomainReplace.class) {
                if(instance == null) {
                    instance = new CoocaaHttpDomainReplace();
                }
            }
        }
        return instance;
    }

    public void addBaseUrl(String domainName, String baseUrl) {
        Log.d("CoocaaHttp", "++ addBaseUrl, name=" + domainName + ", baseUrl=" + baseUrl);
        if(!TextUtils.isEmpty(domainName) || baseUrl != null) {
            domainBaseUrlMap.put(domainName, baseUrl);
        }
    }

    public void addDefaultBaseUrl(String domainName, String baseUrl) {
        Log.d("CoocaaHttp", "++ addDefaultBaseUrl, name=" + domainName + ", baseUrl=" + baseUrl);
        if(!TextUtils.isEmpty(domainName) || baseUrl != null) {
            defaultBaseUrlMap.put(domainName, baseUrl);
        }
    }

    public void addBaseUrl(Map<String, String> map) {
        if(map != null && !map.isEmpty()) {
            domainBaseUrlMap.putAll(map);
        }
    }

    public void addDefaultBaseUrl(Map<String, String> map) {
        if(map != null && !map.isEmpty()) {
            defaultBaseUrlMap.putAll(map);
        }
    }

    public String getBaseUrl(String domainName) {
        return domainBaseUrlMap.get(domainName);
    }

    public String getDefaultBaseUrl(String domainName) {
        return defaultBaseUrlMap.get(domainName);
    }
}

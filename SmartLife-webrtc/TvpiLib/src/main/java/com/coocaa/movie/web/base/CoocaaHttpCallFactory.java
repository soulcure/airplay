package com.coocaa.movie.web.base;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 为了实现一个OkHttpClient使用多个baseUrl
 * @Author: yuzhan
 */
public class CoocaaHttpCallFactory implements Call.Factory{

    private final Call.Factory delegate;
    private final String domainName;

    public CoocaaHttpCallFactory(Call.Factory delegate, String domainName) {
        this.delegate = delegate;
        this.domainName = domainName;
    }

    @Override
    public Call newCall(Request request) {
        Log.d("CooHttp", "old request=" + request.url());
        Headers headers = request.headers();
        int headersSize = headers.size();
        if (headersSize > 0) {
            String replaceDomainName = headers.get("replace_domain");
            String newUrlStr = getNewUrlStr(replaceDomainName, request.url().toString());
            if(!TextUtils.isEmpty(newUrlStr)) {
                Log.d("CooHttp", "newUrlStr=" + newUrlStr);
                HttpUrl newHttpUrl = HttpUrl.get(newUrlStr);
                Headers createNewHeaders = createNewHeader(request.headers());
                Field urlField;
                Field headerField;
                try {
                    urlField = request.getClass().getDeclaredField("url");
                    urlField.setAccessible(true);
                    urlField.set(request, newHttpUrl);

                    headerField = request.getClass().getDeclaredField("headers");
                    headerField.setAccessible(true);
                    headerField.set(request,createNewHeaders);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    Log.d("CooHttp", "change request error : " + e.toString());
                    e.printStackTrace();
                }
                Log.d("CooHttp", "new request=" + request.url());
            }
        }
        return delegate.newCall(request);
    }

    private String getNewUrlStr(String replaceDomainName, String requestUrl) {
        String newBaseUrl = CoocaaHttpDomainReplace.getInstance().getBaseUrl(replaceDomainName);
        String oldBaseUrl = CoocaaHttpDomainReplace.getInstance().getBaseUrl(domainName);
        Log.d("CooHttp", "domainName=" + domainName + ", replaceDomainName=" + replaceDomainName + ", oldBaseUrl=" + oldBaseUrl + ", newBaseUrl=" + newBaseUrl);
        String newUrlStr = null;
        if(!TextUtils.isEmpty(newBaseUrl) && !TextUtils.isEmpty(oldBaseUrl) && !TextUtils.equals(newBaseUrl, oldBaseUrl)) {
            //没有配置oldBaseUrl，使用default
            if(requestUrl.startsWith(oldBaseUrl)) {
                newUrlStr = requestUrl.replace(oldBaseUrl, newBaseUrl);
            }
        }
        if(newUrlStr == null) {
            oldBaseUrl = CoocaaHttpDomainReplace.getInstance().getDefaultBaseUrl(domainName);
            Log.d("CooHttp", "defaultBaseUrl=" + oldBaseUrl);
            if(!TextUtils.isEmpty(newBaseUrl) && !TextUtils.isEmpty(oldBaseUrl) && !TextUtils.equals(newBaseUrl, oldBaseUrl)) {
                //没有配置oldBaseUrl，使用default
                if(requestUrl.startsWith(oldBaseUrl)) {
                    newUrlStr = requestUrl.replace(oldBaseUrl, newBaseUrl);
                }
            }
        }
        return newUrlStr;
    }

    //刚才自己的对应的URL地址清除掉
    private Headers createNewHeader(Headers headers) {
        if (null == headers) return null;
        int headerSize = headers.size();

        Headers.Builder builder = new Headers.Builder();
        for (int i = 0; i < headerSize; i++) {
            if ("replace_domain".equals(headers.name(i))) continue;
            builder.add(headers.name(i), headers.value(i));
        }
        return builder.build();
    }
}

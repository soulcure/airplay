package com.coocaa.smartmall.data.tv. http;


import com.coocaa.smartmall.data.api.HttpApi;
import com.coocaa.smartmall.data.api.HttpManager;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.tv.data.DetailResult;
import com.coocaa.smartmall.data.tv.data.RecommandResult;
import com.coocaa.smartmall.data.tv.data.SmartMallRequestConfig;

import java.util.Map;

import retrofit2.Call;

/**
 */
public class SmartMallRequestService extends HttpManager<com.coocaa.smartmall.data.tv.http.ISmartMallRequestMethod> {
    private static final String REQUEST_CHANNEL = "tv";
    private static final String REQUEST_OUTPUT_FORMAT = "JSON";

    public  static  final SmartMallRequestService SERVICE = new SmartMallRequestService();
    @Override
    protected Class<com.coocaa.smartmall.data.tv.http.ISmartMallRequestMethod> getServiceClass() {
        return com.coocaa.smartmall.data.tv.http.ISmartMallRequestMethod.class;
    }

    @Override
    protected Map<String, String> getHeaders() {
        return SmartMallRequestConfig.getInstance().mAllDefaultHeaders;
    }

    @Override
    protected String getBaseUrl() {
        return SmartMallRequestConfig.TAB_PRODUCT_BASE_URL;
    }

    public void getRecommand(HttpSubscribe<RecommandResult> subscribe){
        Call<RecommandResult> allRecommandCall = getHttpService().getRecommend();
        HttpApi.getInstance().request(allRecommandCall , subscribe);
    }

    public void getDetail(String product_id,HttpSubscribe<DetailResult> subscribe){
        Call<DetailResult> allRecommandCall = getHttpService().getDetail(product_id);
        HttpApi.getInstance().request(allRecommandCall , subscribe);
    }
}

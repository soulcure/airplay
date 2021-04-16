package swaiotos.channel.iot.common.http.net;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import swaiotos.channel.iot.common.http.exception.UniteThrowable;
import swaiotos.channel.iot.common.response.CooCaaResponse;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.ss.server.utils.Constants;


public class CooCaaRestApiImpl implements CooCaaRestApi {
    protected static final String TAG = CooCaaRestApiImpl.class.getSimpleName();

    private final Context mContext;
    private final RestApi mRestApi;
    private final RestApi mRestApiWithLog;
    private Map<String,String> mHeaders;

    public CooCaaRestApiImpl(Context context, RestApiFactory restApiFactory, Map<String,String> headers) {
        mContext = context.getApplicationContext();
        mRestApi = restApiFactory.create(false);
        mRestApiWithLog = restApiFactory.create(true);
        mHeaders = headers;
    }

    @Override
    public void reflush() {
        Map<String,String> DEFAULT_HEADERS = new HashMap<>();
        DEFAULT_HEADERS.put(Constants.COOCAA_MAC, PublicParametersUtils.getMac(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CCHIP, PublicParametersUtils.getcChip(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CUDID, PublicParametersUtils.getcUDID(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CMODEL, PublicParametersUtils.getcModel(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CSIZE, PublicParametersUtils.getcSize(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_DEVICENAME, PublicParametersUtils.getdeviceName(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CVERSION,""+PublicParametersUtils.getVersionCode(mContext));

        mHeaders = DEFAULT_HEADERS;
    }

    @Override
    public <T extends CooCaaResponse> void queryAIotCooCaaResponse(String url, Map<String, String> querys, final Class<T> cls, final RequestListener<T> requestListener) {
        mRestApiWithLog.getLSIDInfo(url,mHeaders,querys).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String string = response.body().string();
                    requestListener.requestSuccess((T)com.alibaba.fastjson.JSON.parseObject(string,cls));
                } catch (Exception e) {
                    UniteThrowable throwable = UniteThrowable.handleException(e);
                    requestListener.requestError(throwable.errorType, throwable.errorCode, throwable.errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                UniteThrowable throwable = UniteThrowable.handleException(t);
                requestListener.requestError(throwable.errorType, throwable.errorCode, throwable.errorMsg);
            }
        });

    }

    @Override
    public <T extends CooCaaResponse> void querySynAIotCooCaaResponse(String url, Map<String, String> querys, Class<T> cls, RequestListener<T> requestListener) {
        try {
            Response<ResponseBody> response = mRestApiWithLog.getLSIDInfo(url,mHeaders,querys).execute();

            String string = response.body().string();
            requestListener.requestSuccess((T)com.alibaba.fastjson.JSON.parseObject(string,cls));

        } catch (Exception e) {
            UniteThrowable throwable = UniteThrowable.handleException(e);
            requestListener.requestError(throwable.errorType, throwable.errorCode, throwable.errorMsg);
        }
    }

}

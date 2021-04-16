package com.coocaa.movie.web.product;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.movie.product.m.entity.ProductEntity;
import com.coocaa.movie.util.MovieConstant;
import com.coocaa.movie.web.base.HttpExecption;
import com.coocaa.movie.web.base.HttpMethod;
import com.coocaa.movie.web.base.HttpResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PayHttpMethods extends HttpMethod<PayHttpService> {

    private String TAG = "Sea-pay";

    @Override
    public String getBaseUrl() {
        return MovieConstant.PAY_DOMAIN;
    }

    @Override
    public int getTimeOut() {
        return 10;
    }

    @Override
    public Class<PayHttpService> getServiceClazz() {
        return PayHttpService.class;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        //iqiyi
        headers.put("MAC", "fca38639900d");
        headers.put("cModel", "G7200");
        headers.put("cChip", "8H87");

        //qq
//        headers.put("MAC", "4e020b0fc50c");
//        headers.put("cModel", "Q8");
//        headers.put("cChip", "8H91");

        return headers;
    }

    /**
     *
     * @param callback
     * @param user_id  用户身份标志 非必填项；字符型数据； 该字段含义与userFlag关联
     * @param user_flag  用户身份标志含义 必填项；整型数据；0表示用户未登录酷开账号，user_id的值为空值；1表示user_id是酷开账号中心向客户端下发的token值；2表示user_id是酷开账号中心向客户端下发的openId值
     * @param third_user_id  第三方用户身份标志 非必填项；字符型数据；该字段含义不与userFlag关联；比如是腾讯绑定的账号下的订单需要将腾讯的openId放在这里一起提交
     * @param source_id  指定的产品源ID非必填项；整型数据；当该值存在时，后台会返回指定产品源的产品包信息；此值存在时会优先拿该值下的产品源信息。
     * @param client_type  客户端设备类型；不同的设备类型，将会获取到不一样的产品源列表，且不提交该值时默认按TV设备处理； 非必填项；整型数据 1表示TV，2表示天赐派，3表示微信，4表示web版TV客户端影视会员中心；5表示电视派；6表示web版TV客户端iptv会员中心
     * @param movie_id  单点影片ID，例如：“_yinhe_370885800”，带有前缀  非必填项；字符型数据 当该值存在时，后台会返回该影片命中的产品源信息
     * @param node_type  影片类型；res:影片（默认），live:直播节目；iptv:网络电视 非必填项；字符型数据 movie_id有值时需要传此值。
     * @param auth_type  鉴权方式，非必填项，默认0；该字段从影视详情接口获取；movie_id有值时需要传此值。
     * @param business_type  业务线，非必填项，-1获取全部,0获取影视，1获取教育,2iptv，默认0
     * @param company_list  腾讯产品源:tencent;奇艺产品源:yinhe；优朋混奇艺产品源:yinhe_mix_voole;优朋产品源:voole;iptv产品源:coocaa_iptv；教育产品源:coocaa,聚体育：jutiyu
     * @param extend_info  扩展参数，非必填项，字符型数据，默认空；影视中心3.19之后版本需要上传的值目前有login_type:0表示手机登陆，1表示QQ登陆，2表示微信登陆；wx_vu_id：微信帐号对应的vuserid，login_type为2时需要传此值；格式为json，如{"login_type":1,"wx_vu_id":"wxvuuserid"}
     */
    public void getSourceList(PayHttpCallback<PaySourceModel> callback, String user_id, String user_flag, String third_user_id, String source_id,
                              String client_type, String movie_id, String node_type, String auth_type, String business_type, String company_list, String extend_info) {
        Log.i(TAG, "getSourceList "+user_id+"  "+user_flag+"  "+third_user_id+"  "+client_type+"  "+business_type+"  "+company_list+"  "+auth_type+"  "+callback);
        HashMap<String, Object> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("source_id", source_id);
        params.put("user_flag", user_flag);
        params.put("third_user_id", third_user_id);
        params.put("movie_id", movie_id);
        params.put("node_type", node_type);
        params.put("client_type", "1");
        params.put("auth_type", auth_type);
        params.put("business_type", business_type);
        String data = JSON.toJSONString(params);
        HttpResult<String> result = getService().getSourceList(data);
        if(callback == null)
            return;
        String s = map(result);
        if(!TextUtils.isEmpty(s)) {
            try {
                PaySourceModel paySourceModel = JSONObject.parseObject(s, PaySourceModel.class);
                if(paySourceModel != null) {
                    callback.callback(paySourceModel);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        callback.error(getExecption(result));
    }


    /**
     * 获取产品包列表
     * @param callback
     * @param user_id   非必填项 该字段含义与userFlag关联
     * @param user_flag  必填项 0表示用户未登录酷开账号，user_id的值为空值；1表示user_id是酷开账号中心向客户端下发的token值；2表示user_id是酷开账号中心向客户端下发的openId值
     * @param third_user_id  非必填项 第三方用户身份标志 该字段含义不与userFlag关联；比如是腾讯绑定的账号下的订单需要将腾讯的openId放在这里一起提交
     * @param movie_id  非必填项（与source_id必填一个）；单点影片ID 当该值存在时，后台会返回含有单点产品的产品列表；否则只返回包年、包月等产品包
     * @param node_type  非必填项 影片类型；res:影片（默认），live:直播节目；字符型数据；
     * @param source_id  非必填项（与movie_id必填一个）产品源ID；产品源的唯一标识
     * @param client_type  必填项 客户端设备类型； 1表示TV，2表示天赐派，3表示微信，4表示web版TV客户端；
     * @param crowd_id  非必填项 客户端传上来的人群标识，用于做abtest方案，，默认传空
     * @param auth_type  鉴权类型，0第三方，1自有,该字段影视详情接口取
     * @param come_from  渠道,字符型，非必填，区分从哪个块块点击进产品包。
     * @param v_id
     * @param is_support_movie  非必填  是否要出单点产品包 "true"表示出单点包，"false"表示出会员包
     * @param third_id  非必填 剧集ID/视频ID；影片剧集ID/视频ID,超前点播时需要传此值上来
     */
    public void getProductList(PayHttpCallback<ProductEntity> callback, String user_id, String user_flag, String third_user_id,
                               String movie_id, String node_type, String source_id, String client_type, String crowd_id, String auth_type,
                               String come_from, String v_id, String is_support_movie, String third_id) {
        Log.i(TAG, "getProductList "+user_id+"  "+user_flag+"  "+third_user_id+"  "+movie_id+"  "+node_type+"  "+source_id+"  "+client_type+"  "+crowd_id+"  "+auth_type+"  "+come_from+"  "+v_id+"  "+callback);
        HashMap<String, Object> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("source_id", source_id);
        params.put("user_flag", user_flag);
        params.put("third_user_id", third_user_id);
        params.put("movie_id", movie_id);
        params.put("node_type", node_type);
        params.put("client_type", "1");
        params.put("auth_type", auth_type);
        params.put("crowd_id", crowd_id);
        params.put("third_id", third_id);
        params.put("come_from", come_from);
        params.put("is_support_movie", is_support_movie);
        params.put("v_id", v_id);
        String data = JSON.toJSONString(params);
        HttpResult<String> result = getService().getProductList(data);
        String s = map(result);

        if(callback == null)
            return;
        if(!TextUtils.isEmpty(s)) {
            try {
                ProductEntity payProductModel = JSONObject.parseObject(s, ProductEntity.class);
                if(payProductModel != null) {
                    callback.callback(payProductModel);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        callback.error(getExecption(result));
    }

//    public void getOrderUrl(PayHttpCallback<String> callback, String cSID, String user_id, String user_flag, String third_user_id,
//                            String movie_id, String node_type, String source_id, String client_type, String crowd_id,
//                            String come_from, String union_code, String shopCartProducts, String scheme_id, String b_id, String dmp_code,
//                            String policy_id, String extend_info) {
//        Log.i(TAG, "getOrderUrl ");
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("cSID", cSID);
//        params.put("user_id", user_id);
//        params.put("source_id", source_id);
//        params.put("user_flag", user_flag);
//        params.put("third_user_id", third_user_id);
//        params.put("movie_id", movie_id);
//        params.put("node_type", node_type);
//        params.put("client_type", "1");
//        params.put("crowd_id", crowd_id);
//        params.put("come_from", come_from);
//        params.put("union_code", union_code);
//        params.put("shopCartProducts", shopCartProducts);
//        params.put("scheme_id", scheme_id);
//        params.put("b_id", b_id);
//        params.put("dmp_code", dmp_code);
//        params.put("policy_id", policy_id);
//        params.put("path", "/normal/paysuccess");
//        params.put("extend_info", extend_info);
//        String data = JSON.toJSONString(params);
//        HttpResult<String> result = getService().getOrderUrl(data);
//        String s = map(result);
//
//        if(callback == null)
//            return;
//        if(!TextUtils.isEmpty(s)) {
//            callback.callback(s);
//            return;
//        } else {
//            callback.error(getExecption(result));
//        }
//    }

    public void getOrderUrl(PayHttpCallback<PayQrcodeModel> callback, String user_id, String user_flag, String third_user_id, String extend_info, String license, String come_from, String dmp_code, String policy_id, String scheme_id,
                              String option, List shopCartProducts, String source, String is_support_wx, int product_id) {
        Log.i(TAG, "getQrcode "+user_id+"  "+user_flag+"  "+third_user_id+"  " + source + "  " + is_support_wx
                +"  "+extend_info+"  "+license+"  "+come_from+"  "+dmp_code+"  "+policy_id+"  "+scheme_id+"  "+option + "  " + shopCartProducts);
        HashMap<String, Object> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("user_flag", user_flag);
        params.put("third_user_id", third_user_id);
        params.put("shop_cart_products", shopCartProducts);
        params.put("extend_info", extend_info);
        params.put("license", license);
        params.put("client_type", "1");
        params.put("come_from", come_from);
        params.put("dmp_code", dmp_code);
        params.put("policy_id", policy_id);
        params.put("scheme_id", scheme_id);
        params.put("option", option);
        params.put("source", source);
        params.put("is_support_wx", is_support_wx);
        String data = JSON.toJSONString(params);
        HttpResult<String> result = getService().getQrcode(data, product_id);
        String s = map(result);
        if(s == null) {
            if(callback != null)
                callback.error(new HttpExecption("net error", result.code));
        } else {
            PayQrcodeModel payQrcodeModel = null;
            try {
                payQrcodeModel = JSONObject.parseObject(s, PayQrcodeModel.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(callback != null) {
                if(payQrcodeModel == null) {
                    callback.error(new HttpExecption("parse error", result.code));
                }else {
                    callback.callback(payQrcodeModel);
                }
            }
        }
    }

    private HttpExecption getExecption(HttpResult<String> result) {
        if(result == null)
            return new HttpExecption("网络异常", 700);
        if(result.data != null)
            return new HttpExecption("解析错误", 800);
        return new HttpExecption(result.msg, result.code);
    }
}

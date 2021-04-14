package com.swaiot.webrtc.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.utils
 * @ClassName: SignCore
 * @Description: 对请求参数进行加解密操作
 * @Author: wangyuehui
 * @CreateDate: 2020/4/9 12:35
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/9 12:35
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SignCore {
    /**
     * 除去数组中的空值和签名参数
     *
     * @param sArray
     *            签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")|| key.equalsIgnoreCase("sign_code")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params
     *            需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }

    /**
     * 生成签名结果
     *
     * @param sParaTemp
     *            要签名的数组
     * @return 签名结果字符串
     */
    public static String buildRequestMysign(Map<String, String> sParaTemp, String key) {

        Map<String, String> sPara = paraFilter(sParaTemp);

        String prestr = createLinkString(sPara); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";

        mysign = MD5.sign(prestr, key, "utf-8");

        return mysign;
    }

//    // 验证签名
//    public static boolean getSignVeryfy(Map<String, String> Params, String sign, String key) {
//        // 过滤空值、sign与sign_type参数
//        Map<String, String> sParaNew = paraFilter(Params);
//        // 获取待签名字符串
//        String preSignStr = createLinkString(sParaNew);
//        // 获得签名验证结果
//        boolean isSign = false;
//
//        isSign = MD5.verify(preSignStr, sign, key, "utf-8");
//
//        return isSign;
//    }
}

package com.coocaa.smartscreen.data.error;

import java.util.HashMap;
import java.util.Map;

/**
 * 编号规则： 1 - 01 - 01 - 1:系统错误，2：业务错误 - 01：业务系统 - 01：编号
 * Created by neo on 14-4-30.
 */
public enum ErrorCodes {
    /*LOGIN_ERROR*/
    LOGIN_ERROR(1000, "Token失效,请重新登录"),

    /*SYSTEM_ERROR*/
    SYSTEM_ERROR(1001, "系统错误"),

    /*INVALID_INPUT_PARAMS*/
    INVALID_INPUT_PARAMS(1002, "参数错误：%s"),

    /*NO_SUCH_ENTITY*/
    NO_SUCH_ENTITY(1003, "没有找到对象"),

    /*NO_SUCH_ENTITY_ERROR*/
    NO_SUCH_ENTITY_ERROR(1004, "%s"),

    /*NETWORK_ERROR*/
    NETWORK_ERROR(1005, "网络错误,%s"),

    /*PARAMETER_REQUIRED_WHAT*/
    PARAMETER_REQUIRED_WHAT(10006, "缺少参数: %s"),

    /*SYSTEM_CONFIG_ERROR*/
    SYSTEM_CONFIG_ERROR(1006, "系统配置错误：%s"),

    /*ILLEGAL_REQUEST*/
    ILLEGAL_REQUEST(10012, "非法请求"),

    /*QUERY_THIRD_INT_ERROR*/
    QUERY_THIRD_INT_ERROR(5001, "请第三方接口失败：%s"),

    /*API_INTERFACE_SHUTDOWN*/
    API_INTERFACE_SHUTDOWN(9001, "接口已经停止服务"),

    /*API_INTERFACE_SHUTDOWN_CUSTOMER_INFO*/
    API_INTERFACE_SHUTDOWN_CUSTOMER_INFO(9002, "%s"),

    /*ACCOUNT_MOBILE_FORMAT_WRONG*/
    ACCOUNT_MOBILE_FORMAT_WRONG(20298, "手机号码格式错误"),

    /*ACCOUNT_CAPTCHA_TOO_MANY*/
    ACCOUNT_CAPTCHA_TOO_MANY(20220, "验证码发送过于频繁,请于24小时后重试！"),

    /*ACCOUNT_CAPTCHA_DENIED*/
    ACCOUNT_CAPTCHA_DENIED(20221, "请在%s秒后重新获取"),

    /*IP_EXCEED_LIMIT*/
    IP_EXCEED_LIMIT(20223, "设备访问超限，请输入图片验证码"),

    /*CODEKEY_AND_CAPCHA_ERROR*/
    CODEKEY_AND_CAPCHA_ERROR(20225, "图片验证码错误"),

    /*IP_CANNOT_ACCESS*/
    IP_CANNOT_ACCESS(20227, "IP访问受限"),

    /*ACCOUNT_PASSWORD_LIMIT*/
    ACCOUNT_PASSWORD_LIMIT(202223, "密码错误次数超限，请24小时后重试"),

    /*ACCOUNT_NICK_NAME_DUPLICATE*/
    ACCOUNT_NICK_NAME_DUPLICATE(202091, "已经被%s占用"),

    /*ACCOUNT_CAPTCHA_ERROR*/
    ACCOUNT_CAPTCHA_ERROR(20204, "验证码错误,还有%s次验证机会"),

    /*ACCOUNT_CAPTCHA_VALIDATE_TO_MANY*/
    ACCOUNT_CAPTCHA_VALIDATE_TO_MANY(20205, "验证码验证过于频繁，请1分钟后重试！"),

    /*ACCOUNT_CAPTCHA_VALIDATE_TO_RETURN*/
    ACCOUNT_CAPTCHA_VALIDATE_TO_RETURN(20226, "验证码错误，请重新发送验证码！"),

    /*ACCOUNT_VALIDATE_CODE_EXPIRED*/
    ACCOUNT_VALIDATE_CODE_EXPIRED(20205, "验证码失效"),

    /*ACCOUNT_USER_FORMAT_WRONG*/
    ACCOUNT_USER_FORMAT_WRONG(20299, "账户名格式错误"),

    /*SKYAPI_AES_DECRYPT_FAILURE*/
    SKYAPI_AES_DECRYPT_FAILURE(30001, "AES解密失败"),

    /*USER_ADDRESS_SIGN_INVALID*/
    USER_ADDRESS_SIGN_INVALID(20611, "验签失败"),

    /*AUTH_ERROR*/
    AUTH_ERROR(300402, "%s"),

    /*ERROR_WEIXIN_USER_INFO*/
    ERROR_WEIXIN_USER_INFO(900001, "获取微信用户的信息失败"),

    /*ACCOUNT_FAIL_GET_THIRD_INFO*/
    ACCOUNT_FAIL_GET_THIRD_INFO(20564, "获取第三方账号信息失败，请返回重新登录"),

    /*ACCOUNT_USER_NOT_FOUND*/
    ACCOUNT_USER_NOT_FOUND(20202, "用户未找到"),

    /*EXTERNAL_ID_ALREADY_BIND*/
    EXTERNAL_ID_ALREADY_BIND(20566, "此绑定账号已经与其他账号绑定"),

    /*AUTH_TYPE_UNSUPPORT*/
    AUTH_TYPE_UNSUPPORT(20104, "认证类型不支持"),

    /*REFRESH_TOKEN_EXPIRED*/
    REFRESH_TOKEN_EXPIRED(20124, "refresh_token 已经失效"),

    /*AUTH_TOKEN_ILLEGAL*/
    AUTH_TOKEN_ILLEGAL(20122, "非法 token"),

    /*ACCOUNT_BIND_EXTERNAL_ID*/
    ACCOUNT_BIND_EXTERNAL_ID(20562, "此账号已经与其他账号绑定"),

    /*ACCOUNT_MOBILE_DUPLICATE_2*/
    ACCOUNT_MOBILE_DUPLICATE_2(20210, "该账号已注册"),

    /*ACCOUNT_ALREADY_BINDING_DEVICE*/
    ACCOUNT_ALREADY_BINDING_DEVICE(20001, "此账号已经与该设备绑定，请勿重复绑定"),

    ACCOUNT_BINDINGCODE_VALIDATE_TO_RETURN(20002, "绑定码错误，请重新输入"),

    ACCOUNT_BINDINGCODE_EXPIRED(20003, "绑定码已失效"),

    ACCOUNT_BINDINGCODE_BINDING(20004, "该二维码已被绑定");

    Integer code;
    private String msg;

    ErrorCodes(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return this.msg;
    }

    public static ErrorCodes fromString(String code) {
        return KEYS_MAP.get(Integer.valueOf(code));
    }

    private static Map<Integer, ErrorCodes> KEYS_MAP = new HashMap<>();

    static {
        for (ErrorCodes e : ErrorCodes.values()) {
            KEYS_MAP.put(e.code, e);
        }
    }
}

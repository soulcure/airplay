package com.coocaa.smartmall.data.mobile.data;

/**
 * @ClassName PaymentInfo
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/8/28
 * @Version TODO (write something)
 */
public class PaymentInfo {
    private String message;
    private String errorCode;
    private boolean success;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public CreateOrderResult.CreateOrderBean.PayInfoBean getData() {
        return data;
    }

    public void setData(CreateOrderResult.CreateOrderBean.PayInfoBean data) {
        this.data = data;
    }

    private CreateOrderResult.CreateOrderBean.PayInfoBean data;
}

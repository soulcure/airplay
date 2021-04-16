package swaiotos.channel.iot.common.entity;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.entity
 * @ClassName: ValidCode
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/23 18:59
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/23 18:59
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ValidCode {
    private String verificationCode;
    private String retryNumber;
    private String retryIntervalTime;

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getRetryNumber() {
        return retryNumber;
    }

    public void setRetryNumber(String retryNumber) {
        this.retryNumber = retryNumber;
    }

    public String getRetryIntervalTime() {
        return retryIntervalTime;
    }

    public void setRetryIntervalTime(String retryIntervalTime) {
        this.retryIntervalTime = retryIntervalTime;
    }
}

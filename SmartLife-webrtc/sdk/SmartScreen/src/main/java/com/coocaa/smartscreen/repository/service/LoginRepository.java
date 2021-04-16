package com.coocaa.smartscreen.repository.service;

import android.graphics.Bitmap;

import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.AccountLoginInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.data.account.UpdateAvatarBean;
import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.data.device.RegisterLogin;
import com.coocaa.smartscreen.data.device.TVSourceModel;
import com.coocaa.smartscreen.repository.future.InvocateFuture;

import java.util.List;

/**
 * 登录相关接口仓库
 * Created by songxing on 2020/6/5
 */
public interface LoginRepository {

    //远程
    /**
     * 获取图片验证码
     * @param width 长度
     * @param height 宽度
     * @return
     */
    InvocateFuture<Bitmap> getImageCaptcha(int width, int height);

    /**
     * 获取短信验证码
     * @param phoneNumber 手机号码
     * @return
     */
    InvocateFuture<Boolean> getSmsCaptcha(String phoneNumber);

    /**
     * 获取短信验证码
     * @param phoneNumber 手机号码
     * @param imageCaptcha 图片验证码
     * @return
     */
    InvocateFuture<Boolean> getSmsCaptcha(String phoneNumber, String imageCaptcha);

    /**
     * 短信验证码登录
     * @param phoneNumber 手机号码
     * @param smsCaptcha 短信验证码
     * @return
     */
    InvocateFuture<AccountLoginInfo> smsCaptchaLogin(String phoneNumber, String smsCaptcha);

    /**
     * 获取一键登录信息
     * @param sanYanToken token
     * @return
     */
    InvocateFuture<AccountLoginInfo> oneKeyLogin(String sanYanToken);

    /**
     * 获取酷开用户信息
     * @param accessToken token
     * @return
     */
    InvocateFuture<CoocaaUserInfo> getCoocaaUserInfo(String accessToken);

    /**
     * 获取酷开用户信息
     * @param accessToken token
     * @return
     */
    InvocateFuture<String> updateCoocaaUserInfo(String accessToken, String nickName,String gender,String birthday);

    /**
     * 修改头像
     * @param base64Avatar 头像base64数据
     * @param type 类型 如jpg
     * @return
     */
    InvocateFuture<List<UpdateAvatarBean>> updateAvatar(String accessToken,String base64Avatar, String type);

    /**
     * 获取酷开用户信息
     * @param accessToken token
     * @return
     */
    InvocateFuture<TpTokenInfo> getTpToken(String accessToken);

    /**
     * 注册设备
     * @param accessToken token
     * @return
     */
    InvocateFuture<RegisterLogin> registerDevice(String accessToken,final String nickname,final String openId);

    /**
     * 更新设备信息
     * @param accessToken token
     * @return
     */
    InvocateFuture<Integer> updateDeviceInfo(String accessToken,final String nickname,final String openId);



    InvocateFuture<YunXinUserInfo> getYunXinUserInfo(String accessToken);

    /**
     * 获取电视源
     * @param activeId
     * @return
     */
    InvocateFuture<TVSourceModel> getTVSource(String activeId);

    /**
     * 获取电视源
     * @param mac
     * @param model
     * @param version
     * @param tv_name
     * @return
     */
    InvocateFuture<TVSourceModel> getTVSource(String mac, String model, String version, String tv_name);




    //本地

    /**
     * 保存token
     * @param token
     */
    void saveToken(String token);

    /**
     * 保存注册设备信息
     * @param registerLogin
     */
    void saveDeviceRegisterLoginInfo(RegisterLogin registerLogin);

    /**
     * 保存酷开用户信息
     * @param info
     */
    void saveCoocaaUserInfo(CoocaaUserInfo info);

    /**
     * 保存云信用户信息
     * @param info
     */
    void saveYunXinUserInfo(YunXinUserInfo info);

    /**
     * 保存tp_token
     * @param token
     */
    void saveTpToken(TpTokenInfo info);

    /**
     * 查询token
     * @return 返回token
     */
    String queryToken();

    /**
     * 查询设备注册信息
     * @return {@link RegisterLogin}
     */
    RegisterLogin queryDeviceRegisterLoginInfo();

    /**
     * 查询酷开用户信息
     * @return {@link CoocaaUserInfo}
     */
    CoocaaUserInfo queryCoocaaUserInfo();

    /**
     * 查询云信用户信息
     * @return {@link YunXinUserInfo}
     */
    YunXinUserInfo queryYunXinUserInfo();

    /**
     * 查询tp_token
     * @return 返回token
     */
    TpTokenInfo queryTpTokenInfo();
}

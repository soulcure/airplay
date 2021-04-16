package com.coocaa.smartscreen.repository.service;

import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.data.videocall.ContactsResp;
import com.coocaa.smartscreen.repository.future.InvocateFuture;

import java.util.List;

/**
 * 视频通话模块仓库接口
 * Created by songxing on 2020/6/5
 */
public interface VideoCallRepository {

    //远程
    /**
     * 添加联系人
     * @param accessToken token
     * @param isForceAdd 是否强制添加
     * @param yxRegisterCode 注册号
     * @param friendRemark 昵称
     * @return
     */
    InvocateFuture<YunXinUserInfo> addContacts(String accessToken, boolean isForceAdd, String yxRegisterCode, String friendRemark);

    /**
     * 删除联系人
     * @param accessToken token
     * @param friendYxAccountId 云信id
     * @return
     */
    InvocateFuture<Void> deleteContacts(String accessToken, String friendYxAccountId);

    /**
     * 更新联系人信息
     * @param accessToken token
     * @param friendYxAccountId 云信id
     * @param friendRemark 昵称
     * @return
     */
    InvocateFuture<Void> updateContacts(String accessToken, String friendYxAccountId, String friendRemark);

    /**
     * 同意或拒绝添加联系人
     * @param accessToken token
     * @param isAgree 是否同意
     * @param friendYxAccountId 云信id
     * @return
     */
    InvocateFuture<Void> agreeOrRefundAddContacts(String accessToken, boolean isAgree, String friendYxAccountId);

    /**
     * 是否已添加联系人
     * @param accessToken token
     * @param friendYxAccountId 云信id
     * @return 结果
     */
    InvocateFuture<Boolean> isFriend(String accessToken, String friendYxAccountId);

    /**
     * 获取联系人列表
     * @param accessToken token
     * @return
     */
    InvocateFuture<List<ContactsResp>> getContactsList(String accessToken);

    /**
     * 推送到电视（拨打视频电话时调用）
     * @param registerId
     * @return
     */
    InvocateFuture<Boolean> pushToTv(String registerId);


    //本地
    /**
     * 保存联系人列表到本地
     * @param contactsRespList
     */
    void saveContactsList(List<ContactsResp> contactsRespList);

    /**
     * 读取本地联系人列表
     * @return
     */
    List<ContactsResp> queryContactsList();

    /**
     * 获取本地昵称列表
     * @return
     */
    List<String> getLocalNicknameList();

}

package com.coocaa.tvpi.module.homepager.adapter.holder;

/**
 * 智屏留言班界面布局
 * Created by songxing on 2020/3/25
 */
/*public class MessageBoardHolder extends RecyclerView.ViewHolder {

    public MessageBoardHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void onBind(final MessageBoardBean beans) {

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity();
            }
        });
    }

    private void startActivity() {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(itemView.getContext());
            return;
        }
//        SkyUserApi skyUserApi = new SkyUserApi(itemView.getContext());
//        Map<String, Object> accountInfo = skyUserApi.getAccoutInfo();
//        String userId = AccountUtils.getAccountValue(accountInfo, UserCmdDefine.UserKeyDefine.KEY_OPEN_ID);

        String user_id = null;
        String avatar = null;
        String nick_name = null;
        String token = null;
        String activeid = null;
        if (UserInfoCenter.getInstance().getCoocaaUserInfo() != null
                && !TextUtils.isEmpty(UserInfoCenter.getInstance().getCoocaaUserInfo().open_id)) {
            user_id = UserInfoCenter.getInstance().getCoocaaUserInfo().open_id;
            avatar = UserInfoCenter.getInstance().getCoocaaUserInfo().avatar;
            nick_name = UserInfoCenter.getInstance().getCoocaaUserInfo().nick_name;
            token = UserInfoCenter.getInstance().getCoocaaUserInfo().accessToken;
        } else {
            ToastUtils.getInstance().showGlobalShort("未登录");
            return;
        }

        if (SSConnectManager.getInstance().isConnected()) {
            Device device = SSConnectManager.getInstance().getDevice();
            if (null != device) {
                DeviceInfo deviceInfo = device.getInfo();
                if (null != deviceInfo) {
                    switch (deviceInfo.type()) {
                        case TV:
                            TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                            activeid = tvDeviceInfo.activeId;
                            break;
                    }
                }
            }
        } else {
            ToastUtils.getInstance().showGlobalShort(R.string.tip_connected_tv);
            new ConnectDialogFragment2().with((AppCompatActivity) itemView.getContext()).show();
            return;
        }

        ComponentName cn = new ComponentName(itemView.getContext().getPackageName(),"com.skyworth.smartsystem.vhome.intent") ;
        Intent intent = new Intent() ;
//        intent.setComponent(cn) ;
        intent.setAction("com.skyworth.smartsystem.vhome.intent");
        intent.putExtra("intent_action", "message_list");
        Log.d("wuhaiyuan", "user_id: " + user_id + "  activeid: " + activeid);
        intent.putExtra("user_id", user_id);
        intent.putExtra("avatar", avatar);
        intent.putExtra("nick_name", nick_name);
        intent.putExtra("token", token);
        intent.putExtra("activeid", activeid);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        itemView.getContext().startActivity(intent) ;
    }


}*/

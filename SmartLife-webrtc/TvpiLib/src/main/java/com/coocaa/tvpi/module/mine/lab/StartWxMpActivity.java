package com.coocaa.tvpi.module.mine.lab;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.tvpi.module.openapi.StartAppStore;
import com.coocaa.tvpilib.R;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * @Author: yuzhan
 */
public class StartWxMpActivity extends BaseAppletActivity {

    EditText idText;
    EditText pathText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getIntent().getStringExtra("id");
        String path = getIntent().getStringExtra("path");

        if(!TextUtils.isEmpty(id)) {
            startMp(id, path);
            finish();
        } else {
            setContentView(R.layout.test_wx_mp_layout);
            idText = findViewById(R.id.mp_id);
            pathText = findViewById(R.id.mp_path);
            button = findViewById(R.id.start_mp);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(idText.getText())) {
                        ToastUtils.getInstance().showGlobalShort("小程序id不能为空");
                        return ;
                    }
                    startMp(idText.getText().toString(), pathText.getText().toString());
                }
            });
        }
    }

    private void startMp(String id, String path) {
        IWXAPI api = WXAPIFactory.createWXAPI(this, SmartConstans.getBusinessInfo().APPID_WECHAT);
        if(api.isWXAppInstalled()) {
            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            req.userName = id; //"gh_d43f693ca31f"; // 小程序原始id
            req.path = path;   //拉起小程序页面的可带参路径，不填默认拉起小程序首页
            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;
            api.sendReq(req);
        } else {
            ToastUtils.getInstance().showGlobalShort("您的手机未安装微信，无法打开");
        }
    }
}

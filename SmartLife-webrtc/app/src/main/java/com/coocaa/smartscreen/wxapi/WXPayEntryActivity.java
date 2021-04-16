package com.coocaa.smartscreen.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.coocaa.smartsdk.pay.PayResultEvent;
import com.coocaa.tvpi.module.pay.PayCenterActivity;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	private static String TAG = "MicroMsg.WXEntryActivity";

    private IWXAPI api;
    private MyHandler handler;

	private static class MyHandler extends Handler {
		private final WeakReference<WXPayEntryActivity> wxEntryActivityWeakReference;

		public MyHandler(WXPayEntryActivity wxEntryActivity){
			wxEntryActivityWeakReference = new WeakReference<WXPayEntryActivity>(wxEntryActivity);
		}

		@Override
		public void handleMessage(Message msg) {
//			int tag = msg.what;
//			switch (tag) {
//				case NetworkUtil.GET_TOKEN: {
//					Bundle data = msg.getData();
//					JSONObject json = null;
//					try {
//						json = new JSONObject(data.getString("result"));
//						String openId, accessToken, refreshToken, scope;
//						openId = json.getString("openid");
//						accessToken = json.getString("access_token");
//						refreshToken = json.getString("refresh_token");
//						scope = json.getString("scope");
//						Intent intent = new Intent(wxEntryActivityWeakReference.get(), SendToWXActivity.class);
//						intent.putExtra("openId", openId);
//						intent.putExtra("accessToken", accessToken);
//						intent.putExtra("refreshToken", refreshToken);
//						intent.putExtra("scope", scope);
//						wxEntryActivityWeakReference.get().startActivity(intent);
//					} catch (JSONException e) {
//						Log.e(TAG, e.getMessage());
//					}
//				}
//			}
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	api = WXAPIFactory.createWXAPI(this,"wx51bbf061ea14abb0", false);
		handler = new MyHandler(this);

        try {
            Intent intent = getIntent();
        	api.handleIntent(intent, this);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		Log.d("LXMLXM_PAY","onReq:"+req);
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			goToGetMsg();
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			goToShowMsg((ShowMessageFromWX.Req) req);
			break;
		default:
			break;
		}
        finish();
	}

	private void onPayResult(String status,String payId) {
		EventBus.getDefault().post(new PayResultEvent(status, payId));
	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d("LXMLXM_PAY","onResp:"+resp);
		String result="unknow_failed";

//		int result = 0;
//
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = "success";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = "cancel";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = "fail_auth_denied";
			break;
		}
		onPayResult(result,PayCenterActivity.instance.id);
		PayCenterActivity.instance.finish();
//		Toast.makeText(this, getString(result) + ", type=" + resp.getType(), Toast.LENGTH_SHORT).show();


		if (resp.getType() == ConstantsAPI.COMMAND_SUBSCRIBE_MESSAGE) {
			SubscribeMessage.Resp subscribeMsgResp = (SubscribeMessage.Resp) resp;
			String text = String.format("openid=%s\ntemplate_id=%s\nscene=%d\naction=%s\nreserved=%s",
					subscribeMsgResp.openId, subscribeMsgResp.templateID, subscribeMsgResp.scene, subscribeMsgResp.action, subscribeMsgResp.reserved);

			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}

        if (resp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
            WXLaunchMiniProgram.Resp launchMiniProgramResp = (WXLaunchMiniProgram.Resp) resp;
            String text = String.format("openid=%s\nextMsg=%s\nerrStr=%s",
                    launchMiniProgramResp.openId, launchMiniProgramResp.extMsg,launchMiniProgramResp.errStr);

            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        if (resp.getType() == ConstantsAPI.COMMAND_OPEN_BUSINESS_VIEW) {
            WXOpenBusinessView.Resp launchMiniProgramResp = (WXOpenBusinessView.Resp) resp;
            String text = String.format("openid=%s\nextMsg=%s\nerrStr=%s\nbusinessType=%s",
                    launchMiniProgramResp.openId, launchMiniProgramResp.extMsg,launchMiniProgramResp.errStr,launchMiniProgramResp.businessType);

            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        if (resp.getType() == ConstantsAPI.COMMAND_OPEN_BUSINESS_WEBVIEW) {
            WXOpenBusinessWebview.Resp response = (WXOpenBusinessWebview.Resp) resp;
            String text = String.format("businessType=%d\nresultInfo=%s\nret=%d",response.businessType,response.resultInfo,response.errCode);

            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

//		if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
//			SendAuth.Resp authResp = (SendAuth.Resp)resp;
//			final String code = authResp.code;
//			NetworkUtil.sendWxAPI(handler, String.format("https://api.weixin.qq.com/sns/oauth2/access_token?" +
//							"appid=%s&secret=%s&code=%s&grant_type=authorization_code", "wxd930ea5d5a258f4f",
//					"1d6d1d57a3dd063b36d917bc0b44d964", code), NetworkUtil.GET_TOKEN);
//		}
        finish();
	}

	private void goToGetMsg() {
//		Intent intent = new Intent(this, GetFromWXActivity.class);
//		intent.putExtras(getIntent());
//		startActivity(intent);
//		finish();
	}

	private void goToShowMsg(ShowMessageFromWX.Req showReq) {
		WXMediaMessage wxMsg = showReq.message;
		WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

		StringBuffer msg = new StringBuffer();
		msg.append("description: ");
		msg.append(wxMsg.description);
		msg.append("\n");
		msg.append("extInfo: ");
		msg.append(obj.extInfo);
		msg.append("\n");
		msg.append("filePath: ");
		msg.append(obj.filePath);

//		Intent intent = new Intent(this, ShowFromWXActivity.class);
//		intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
//		intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
//		intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
//		startActivity(intent);
//		finish();
	}
}
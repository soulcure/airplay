package swaiotos.runtime.h5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.coocaa.businessstate.BusinessStateTvReport;
import com.coocaa.businessstate.IInitResultCallback;
import com.coocaa.businessstate.common.listener.IBusinessStateReportListener;
import com.coocaa.businessstate.object.BusinessState;
import com.coocaa.businessstate.object.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import swaiotos.runtime.h5.common.event.OnQrCodeCBData;
import swaiotos.runtime.h5.common.event.OnReportRC;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.H5RunType;

/**
 * @Author: yuzhan
 */
public class H5TV2AppletActivity extends BaseH5AppletActivity {

    private Handler mDismissQrCodeHandler;

    private QrCodeUpdateBroadcastReceiver mBroadcastReceiver;

    private boolean ifGameEngine = false;

    class H5TVAppletLayoutBuilder implements LayoutBuilder {
        @Override
        public View build(View content) {
            return content;
        }
    }

    @Override
    protected LayoutBuilder createLayoutBuilder() {
        return new H5TVAppletLayoutBuilder();
    }

    @Override
    protected H5RunType.RunType runType() {
        return H5RunType.RunType.TV_RUNTYPE_ENUM;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDismissQrCodeHandler = new Handler(getMainLooper());

        String gameEngineString = getIntent().getStringExtra("game_engine");
        if (gameEngineString != null) {
            if (mBroadcastReceiver == null) {
                mBroadcastReceiver = new QrCodeUpdateBroadcastReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("swaiotos.channel.iot.action.qrupdate");
                try {
                    registerReceiver(mBroadcastReceiver, intentFilter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ifGameEngine = true;
        } else {
            ifGameEngine = false;
        }

        String rState = getIntent().getStringExtra("r_state");
        if(rState!=null){
            remoteState = BusinessState.decode(rState);
            LogUtil.d("Tv2 onCreate remoteState = " + remoteState +" string = " + rState);
        }

        BusinessStateTvReport.getDefault().setInitCallBack(new IInitResultCallback() {
            @Override
            public void onInitSucess() {
                reportBusinessState();
            }

            @Override
            public void onInitFailed() {

            }
        });
        BusinessStateTvReport.getDefault().init(this, new IBusinessStateReportListener() {
            @Override
            public void onNoticeReportBusinessState() {
                LogUtil.d("onNoticeReportBusinessState() called");
                //再次上报一下业务当前状态
                reportBusinessState();
            }
        });
    }

    private void reportBusinessState() {
        LogUtil.d("reportBusinessState called " + remoteState);
        if (remoteState != null && remoteState.id != null && remoteState.owner != null) {
            BusinessStateTvReport.getDefault().updateBusinessState(remoteState);
        } else {
            LogUtil.d("reportBusinessState failure. remoteState.user ==null. ");
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReportRC(OnReportRC event) {
        LogUtil.d(" onReportRC event");
        if (event == null || event.getState() == null || remoteState == null) {
            return;
        }
        LogUtil.d(" onReportRC event " + event.getId() + " " + id);
        if (!TextUtils.isEmpty(event.getId()) && TextUtils.equals(id, event.getId())) {

            BusinessState businessState  = BusinessState.decode(event.getState());
            if (businessState != null) {
                if (businessState.owner == null && H5SSClientService.owner != null) {
                    businessState.owner = User.decode(H5SSClientService.owner);
                }
                if (businessState.owner == null && remoteState.owner != null) {
                    businessState.owner = remoteState.owner;
                }
//                if (businessState.owner != null && businessState.id != null) {
//                    businessState.id = getPackageName() + "$" + businessState.id;
//                }
                if (businessState.owner != null) {
                    remoteState = businessState;
                    reportBusinessState();
                }
            }
        }
    }

    private void dissmissSmallQrCode() {
        if (ifGameEngine && mDismissQrCodeHandler != null) {
            mDismissQrCodeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //不出现全局二维码
                    Intent qrBroadCastIntent = new Intent();
                    qrBroadCastIntent.setAction("swaiotos.channel.iot.action.qrshow");
                    qrBroadCastIntent.setPackage("swaiotos.channel.iot");
                    qrBroadCastIntent.putExtra("show", false);
                    H5TV2AppletActivity.this.sendBroadcast(qrBroadCastIntent);
                }
            }, 1000);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String gameEngineString = getIntent().getStringExtra("game_engine");
        if (gameEngineString != null) {
            ifGameEngine = true;
//            dissmissSmallQrCode();
        } else {
            ifGameEngine = false;
        }

        String rState = getIntent().getStringExtra("r_state");
        if(rState!=null && remoteState!=null){
            BusinessState newRemoteState = BusinessState.decode(rState);

            LogUtil.d("Tv2 onCreate newRemoteState = " + newRemoteState +" string = " + rState + " " + remoteState);
            if(newRemoteState!=null&&rState!=null){
                if(newRemoteState.id!=null){
                    LogUtil.d("newremoteState.id = " + newRemoteState.id +" remoteState.id " + remoteState.id);
                    if(!newRemoteState.id.equals(remoteState.id)){
                        BusinessStateTvReport.getDefault().exitBusiness();

                        BusinessStateTvReport.getDefault().init(this, new IBusinessStateReportListener() {
                            @Override
                            public void onNoticeReportBusinessState() {
                                //再次上报一下业务当前状态
                                reportBusinessState();
                            }
                        });
                        reportBusinessState();
                    }
                    remoteState = newRemoteState;
                }
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d("onPause() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d("onResume() called");
        reportBusinessState();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBroadcastReceiver != null) {
            try {
                unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mBroadcastReceiver = null;
        }
        remoteState = new BusinessState();
        BusinessStateTvReport.getDefault().exitBusiness();
    }

    public class QrCodeUpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("swaiotos.channel.iot.action.qrupdate".equals(intent.getAction())) {
                //接收到二维码变化的广播消息
                String bindCode = intent.getStringExtra("bind_code");
                String qrCode = intent.getStringExtra("screenQR");

                EventBus.getDefault().post(new OnQrCodeCBData("onGetQRCode", qrCode, bindCode));
            }
        }
    }
}

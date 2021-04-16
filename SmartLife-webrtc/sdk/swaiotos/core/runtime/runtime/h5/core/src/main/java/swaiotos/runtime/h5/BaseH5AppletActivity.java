package swaiotos.runtime.h5;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.businessstate.object.BusinessState;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.pay.SubmitPayEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import swaiotos.runtime.Applet;
import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.base.AppletThread;
import swaiotos.runtime.base.WebMetaData;
import swaiotos.runtime.base.style.IControlBar;
import swaiotos.runtime.base.style.IControlBarable;
import swaiotos.runtime.h5.common.bean.SsePushBean;
import swaiotos.runtime.h5.common.event.ControlBarEvent;
import swaiotos.runtime.h5.common.event.OnGameEngineInfo;
import swaiotos.runtime.h5.common.event.SetHeaderColorEvent;
import swaiotos.runtime.h5.common.event.SetLeftBtnEvent;
import swaiotos.runtime.h5.common.event.SetNativeUI;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.H5CoreOS;
import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.runtime.h5.core.os.MobileH5CoreOS;
import swaiotos.runtime.h5.core.os.exts.SW;
import swaiotos.runtime.h5.core.os.exts.device.RequestScanQrCodeEvent;
import swaiotos.runtime.h5.core.os.exts.device.ScanQrCodeEvent;
import swaiotos.share.api.ShareApi;
import swaiotos.share.api.define.ShareObject;
import swaiotos.share.api.define.ShareType;

public abstract class BaseH5AppletActivity extends AppletActivity implements H5Style, H5VirtualInputable, IControlBarable {

    private static final String TAG = "CCApplet";
    protected H5Core h5core;
    private static boolean isWebViewInit = false;
    protected boolean hasOverwriteBackAction = false;

    String id;

    protected BusinessState remoteState;

    @RequiresApi(api = 28)
    public void webviewSetPath(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName(context);
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {//判断不等于默认进程名称
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetNativeUI(SetNativeUI event) {
        LogUtil.d(" onSetNativeUI data: " + event.getData().toString());
        if (mHeaderHandler == null) {
            LogUtil.d("mHeaderHandler == null is on tv?");
            return;
        }
        try {
            if(!TextUtils.isEmpty(event.getId()) && !TextUtils.equals(id, event.getId())) {
                LogUtil.d("onSetNativeUI not same id, event.id: " + event.getId() + ", h5CoreOS id=" + id);
                return ;
            }
            JSONObject obj = (JSONObject) event.getData();
            Object tmp = obj.get("titleText");
            if (tmp != null) {
                mHeaderHandler.setTitle((CharSequence) tmp);
            }
            tmp = obj.get("headerBgColor");
            if (tmp != null) {
                mHeaderHandler.setBackgroundColor(Color.parseColor((String) tmp));
            }
            tmp = obj.get("leftBtnVisible");
            if (tmp != null) {
                mHeaderHandler.setBackButtonVisible((Boolean) tmp);
            }
            tmp =  obj.get("darkMode");
            if (tmp != null) {
                mHeaderHandler.setDarkMode((Boolean) tmp);
            }
            tmp =  obj.get("leftBtnType");
            if (tmp != null) {
                setOverwriteBackAction("back".equalsIgnoreCase(event.getTypeString()),true);
                if ("search".equalsIgnoreCase((String) tmp)) {
                    LogUtil.d(" onSetLeftBtnEvent type set search");
                    mHeaderHandler.setBackButtonIcon(getResources().getDrawable(R.drawable.search));
                    mHeaderHandler.setBackButtonVisible(true);
                } else if ("back".equalsIgnoreCase((String) tmp)) {
                    mHeaderHandler.setBackButtonIcon(null);
                    mHeaderHandler.setBackButtonVisible(true);
                } else {
                    mHeaderHandler.setBackButtonVisible(false);
                }
            }
            tmp =  obj.get("overwriteBack");
            if (tmp != null) {
                setOverwriteBackAction((Boolean) tmp);
            }
            tmp = obj.get("overwriteShareInfo");
            if (tmp != null) {
                if ((Boolean) tmp) {
                    mHeaderHandler.setShareButtonOnClickListener(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.d("run() called");
                            h5core.onShareBtnClick();
                        }
                    });
                } else {
                    mHeaderHandler.setShareButtonOnClickListener(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setOverwriteBackAction(boolean isOverwrite) {
        Log.d(TAG, "setOverwriteBackAction() called with: isOverwriteBack = [" + isOverwrite);
        hasOverwriteBackAction = isOverwrite;
        if (isOverwrite) {
            mHeaderHandler.setBackButtonOnClickListener(new Runnable() {
                @Override
                public void run() {
                    h5core.onLeftBtnClick();
                }
            });
        } else {
            mHeaderHandler.setBackButtonOnClickListener(null);
        }
    }
    private void setOverwriteBackAction(boolean isOverwriteBack, boolean isSetBackAction) {
        Log.d(TAG, "setOverwriteBackAction() called with: isOverwriteBack = [" + isOverwriteBack + "], isSetBackAction = [" + isSetBackAction + "]");
        hasOverwriteBackAction = isOverwriteBack;
        if (isSetBackAction) {
            mHeaderHandler.setBackButtonOnClickListener(new Runnable() {
                @Override
                public void run() {
                    h5core.onLeftBtnClick();
                }
            });
        } else {
            mHeaderHandler.setBackButtonOnClickListener(null);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetLeftBtnEvent(SetLeftBtnEvent event) {
        LogUtil.d(" onSetLeftBtnEvent type: " + event.getTypeString());
        if (mHeaderHandler == null) {
            LogUtil.d("mHeaderHandler == null is on tv?");
            return;
        }
        if(!TextUtils.isEmpty(event.getId()) && !TextUtils.equals(id, event.getId())) {
            LogUtil.d("onSetLeftBtnEvent not same id, event.id: " + event.getId() + ", h5CoreOS id=" + id);
            return ;
        }
        setOverwriteBackAction("back".equalsIgnoreCase(event.getTypeString()), true);
        if ("search".equalsIgnoreCase(event.getTypeString())) {
            LogUtil.d(" onSetLeftBtnEvent type set search");
            mHeaderHandler.setBackButtonIcon(getResources().getDrawable(R.drawable.search));
            mHeaderHandler.setBackButtonVisible(true);
        } else if ("back".equalsIgnoreCase(event.getTypeString())) {
            mHeaderHandler.setBackButtonIcon(null);
            mHeaderHandler.setBackButtonVisible(true);
        } else {
            mHeaderHandler.setBackButtonVisible(false);
        }
    }

    @Override
    public void share(Map<String, String> params, Map<String, String> runtime) {
        final String url = h5core.curUrl();
        AppletThread.IO(new Runnable() {
            @Override
            public void run() {
                ShareObject shareObject = new ShareObject();
                shareObject.type = ShareType.WEB.toString();
                if(mApplet != null) {
                    shareObject.from = Applet.Builder.decode(mApplet);
                    if(mApplet.getIcon() != null) {
                        shareObject.thumb = mApplet.getIcon();
                    }
                    Log.d("H5", "cur url=" + url);
                    if(!TextUtils.isEmpty(WebMetaData.getTitle(url))) {
                        shareObject.title = WebMetaData.getTitle(url);
                    }
                    if(!TextUtils.isEmpty(WebMetaData.getDescription(url))) {
                        shareObject.text = WebMetaData.getDescription(url);
                        shareObject.description = WebMetaData.getDescription(url);
                    }
                    if(mApplet.getName() == null) {
                        if(shareObject.title == null)
                            shareObject.title = mApplet.getName();
                        if(shareObject.text == null)
                            shareObject.text = mApplet.getName();
                    }
                    shareObject.version = String.valueOf(mApplet.getVersion());
                }
                appendShareObject(shareObject);

                ShareApi.share(BaseH5AppletActivity.this, shareObject, params);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetHeaderColorEvent(SetHeaderColorEvent event) {
        LogUtil.d(" SetHeaderColorEvent type: " + event.getTypeString());
        if (mHeaderHandler == null) {
            LogUtil.d("mHeaderHandler == null is on tv?");
            return;
        }
        if(!TextUtils.isEmpty(event.getId()) && !TextUtils.equals(id, event.getId())) {
            LogUtil.d("onSetHeaderColorEvent not same id, event.id: " + event.getId() + ", h5CoreOS id=" + id);
            return ;
        }
        String color = event.getTypeString();
        if (color == null) {
            LogUtil.d("color == null?");
            return;
        }
        mHeaderHandler.setBackgroundColor(Color.parseColor(color));
    }

    private String getProcessName(Context context) {
        if (context == null) return null;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }

    protected View view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!SmartApi.isMobileRuntime())//TV端锁定屏保
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 21) {
//            getWindow().setStatusBarColor(Color.parseColor("#33000000"));
        }
        String eventString = getIntent().getStringExtra("event");
        SsePushBean pushBean = null;
        if (eventString != null) {
            pushBean = JSON.parseObject(eventString, SsePushBean.class);
        }
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!isWebViewInit) {
                webviewSetPath(this);
                isWebViewInit = true;
            }
        }

        //提前初始化open
        H5ChannelInstance.getSingleton().getSSChannel(getApplicationContext());

        id = String.valueOf(System.currentTimeMillis());
        LogUtil.d("create h5CoreOs id = " + id);
        if(SmartApi.isMobileRuntime()) {
            h5core = new MobileH5CoreOS(runType(), pushBean, id);
        } else {
            h5core = new H5CoreOS(runType(), pushBean, id);
        }

        h5core.setH5Style(this);
        if(mApplet != null) {
            h5core.setExtJS(mApplet.getRuntime(H5RunType.RUNTIME_EXT_JS_URL));
        }

        Map<String, H5CoreExt> extension = new HashMap<>();
        SW sw = SW.get(getApplicationContext());
        sw.setHeaderHandler(mHeaderHandler).setRuntime(mApplet != null ? mApplet.getAllRuntime() : null);
        sw.setH5Style(this);
        extension.put(SW.NAME, sw);

        view = h5core.create(this, extension);
        setContentView(view);
        load();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.d(" onNewIntent h5 runtime");
        super.onNewIntent(intent);
        setIntent(intent);
        load();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(h5core != null) {
            h5core.onPause();
        }

    }

    protected void callOnStop() {
        if (h5core != null) {
            h5core.destroy();
            h5core = null;
        }
        if (!isFinishing()) {
            finish();
        }
    }

    public void evaluateJavascript(String data) {
        if (h5core != null) {
            h5core.evaluateJavascript(data);
        } else {
            Log.d(TAG, "evaluateJavascript() called h5core == null");
        }
    }

    @Override
    protected void back() {
        Log.d(TAG, "back()");
        onBackPressed();
    }

    @Override
    protected void exit() {
        finish();
        overridePendingTransition(0, swaiotos.runtime.base.R.anim.applet_exit);
    }

    private void load() {
        Intent intent = getIntent();
        String url = null;
        if (intent != null) {
            url = intent.getStringExtra("url");
        }
        LogUtil.d("url = " + url);
        String eventString = getIntent().getStringExtra("event");

        String wifiForce = getIntent().getStringExtra(H5RunType.RUNTIME_NETWORK_FORCE_KEY);

        if (eventString != null) {
            SsePushBean pushBean = JSON.parseObject(eventString, SsePushBean.class);
            ((H5CoreOS) h5core).updateSsePushBean(pushBean);
        }
        if (wifiForce != null) {
            ((H5CoreOS) h5core).updateAppletNetType(wifiForce);
        }

        String gameEngineString = getIntent().getStringExtra("game_engine");
        if(gameEngineString!=null){
            OnGameEngineInfo info = JSON.parseObject(gameEngineString,OnGameEngineInfo.class);
            ((H5CoreOS) h5core).updateGameEngine(info);
        }

        h5core.load(url);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed() called hasOverwriteBackAction: " + hasOverwriteBackAction);
        if (hasOverwriteBackAction) {
            h5core.onLeftBtnClick();
        } else if(!h5core.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(h5core != null) {
            h5core.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(h5core != null) {
            h5core.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (h5core != null) {
            h5core.destroy();
            h5core = null;
        }
        super.onDestroy();
    }

    @Override
    protected boolean needFitsSystemWindows() {
        return true;
    }

    protected abstract H5RunType.RunType runType();

    private final static int REQUEST_SCAN_QRCODE = 100;
    private final static int REQUEST_SCAN_QRCODE_RESULT = 1001;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestScanQrCode(RequestScanQrCodeEvent event) {
        LogUtil.d(" start requestScanQrCode");
        Intent intent = new Intent();
        intent.setData(Uri.parse("np://com.coocaa.smart.scan_api/index?from=h5"));
        intent.setPackage(getPackageName());
        try {
            this.startActivityForResult(intent, REQUEST_SCAN_QRCODE);
        } catch (Exception e) {
            LogUtil.d("requestScanQrCode fail : " + e.toString());
            e.printStackTrace();
        }
    }

    private final int REQUEST_SELECT_FILE = 1;
    private final int FILECHOOSER_RESULTCODE = 2;
    private ValueCallback uploadMessage = null;
    private ValueCallback mUploadMessage = null;
    private final int REQUEST_EXTERN_STORAGE_PERMISSION = 99;
    private boolean readExStoragePermission = true;
    private PermissionListener permissionListener;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], intent = [" + intent + "]");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                Uri[] uris = WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
                Log.d(TAG, "onActivityResult FileChooserParams uris: " + uris);
                uploadMessage.onReceiveValue(uris);
                uploadMessage = null;
            } else if(requestCode == REQUEST_SCAN_QRCODE && resultCode == REQUEST_SCAN_QRCODE_RESULT) {
                String result = intent == null ? null : intent.getStringExtra("result");
                Log.d(TAG, "scan qrcode result = " + result);
                EventBus.getDefault().post(new ScanQrCodeEvent().setResult(result));
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != BaseH5AppletActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if(requestCode == REQUEST_SCAN_QRCODE && resultCode == REQUEST_SCAN_QRCODE_RESULT) {
            String result = intent == null ? null : intent.getStringExtra("result");
            Log.d(TAG, "scan qrcode result = " + result);
            EventBus.getDefault().post(new ScanQrCodeEvent().setResult(result));
        }
        else {
            Toast.makeText(getApplicationContext(), "选择文件失败", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private interface PermissionListener{
        void onPermissionAllow();
        void onPermissionDeny();
    }

    private void checkPermission(PermissionListener listener){
        permissionListener = listener;
        readExStoragePermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERN_STORAGE_PERMISSION);
        }else{
            readExStoragePermission = true;
        }
    }

    /**
     *   启动当前应用设置页面
     * */
    private static void startAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    private static void showTipsDialog(final Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            new AlertDialog.Builder(context)
                    .setTitle("提示信息")
                    .setMessage("当前应用缺少存储权限，该功能暂时无法使用。如若需要，请单击【确定】按钮前往设置中心进行权限授权。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startAppSettings(context);
                        }
                    }).show();
        }
    }

    // For 3.0+ Devices (Start)
    // onActivityResult attached before constructor
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        Log.d(TAG, "openFileChooser() called with: uploadMsg = [" + uploadMsg + "], acceptType = [" + acceptType + "]");
        checkPermission(new PermissionListener() {
            @Override
            public void onPermissionAllow() {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            @Override
            public void onPermissionDeny() {
                showTipsDialog(BaseH5AppletActivity.this);
            }
        });

        if(readExStoragePermission){
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
        }
    }

    // For Lollipop 5.0+ Devices
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        Log.d(TAG, "onShowFileChooser() called with: mWebView = [" + mWebView + "], filePathCallback = [" + filePathCallback + "], fileChooserParams = [" + fileChooserParams + "]");
        checkPermission(new PermissionListener() {
            @Override
            public void onPermissionAllow() {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPermissionDeny() {
                showTipsDialog(BaseH5AppletActivity.this);
            }
        });

        if(readExStoragePermission){
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }

    //For Android 4.1 only
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        Log.d(TAG, "openFileChooser() called with: uploadMsg = [" + uploadMsg + "], acceptType = [" + acceptType + "], capture = [" + capture + "]");
        checkPermission(new PermissionListener() {
            @Override
            public void onPermissionAllow() {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            @Override
            public void onPermissionDeny() {
                showTipsDialog(BaseH5AppletActivity.this);
            }
        });
        if(readExStoragePermission){
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
        }

    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        Log.d(TAG, "openFileChooser() called with: uploadMsg = [" + uploadMsg + "]");
        checkPermission(new PermissionListener() {
            @Override
            public void onPermissionAllow() {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

            @Override
            public void onPermissionDeny() {
                showTipsDialog(BaseH5AppletActivity.this);
            }
        });
        if(readExStoragePermission){
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = [" + requestCode + "], permissions = [" + permissions + "], grantResults = [" + grantResults + "]");
        if (requestCode == REQUEST_EXTERN_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                readExStoragePermission = true;
                if(permissionListener!=null){
                    permissionListener.onPermissionAllow();
                }

            } else {
                // User refused to grant permission.
                readExStoragePermission = false;
                if(permissionListener!=null){
                    permissionListener.onPermissionDeny();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Subscribe(threadMode=ThreadMode.MAIN)
    public void onPayRequestEvent(SubmitPayEvent event) {
        Log.d("PaymentExt","onPayResultEvent..................event:"+event);
        SmartApi.startPay(this, event.id, event.json);
    }

    @Override
    public int getSafeDistanceTop() {
        return 0;
    }

    @Override
    public int getSafeDistanceBottom() {
        if(isSupportVirtualInput()) {
            return (int) getResources().getDimension(R.dimen.virtual_input_height);
        }
        return 0;
    }

    @Override
    public boolean isSupportVirtualInput() {
        if(getIntent() != null) {
            if(TextUtils.equals("true", getIntent().getStringExtra("hideVirtualInput"))) {
                //需要隐藏
                return false;
            }
        }
        return true;
    }

    @Subscribe(threadMode=ThreadMode.MAIN)
    public void onControlBarEvent(ControlBarEvent event) {
        Log.d(TAG, "onControlBarEvent : " + event);
        if(controlBar != null && event != null) {
            controlBar.setControlBarVisible(event.visible);
        }
    }

    protected IControlBar controlBar;
    @Override
    public void setIControlBar(IControlBar controlBar) {
        this.controlBar = controlBar;
    }

    @Override
    public void onControlBarVisibleChanged(boolean b) {
        Log.d(TAG, "onControlBarVisibleChanged : " + b);
        if(h5core instanceof MobileH5CoreOS) {
            ((MobileH5CoreOS) h5core).onControlBarVisibleChanged(b);
        }
    }
}
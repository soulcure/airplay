package com.coocaa.tvpi.module.local.document.page;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.service.MainSSClientService;
import com.coocaa.smartscreen.data.channel.CmdData;
import com.coocaa.smartscreen.data.channel.LocalMediaParams;
import com.coocaa.smartscreen.data.channel.events.ProgressEvent;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.event.StartPushEvent;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.util.WifiUtil;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.local.document.DocLogSubmit;
import com.coocaa.tvpi.module.local.document.DocumentBrowser;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FormatEnum;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.log.PayloadEvent;
import com.coocaa.tvpi.util.FileCalculatorUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_MEDIA_PLAYER;

/**
 * @Description: 文档播放页面
 * @Author: wzh
 * @CreateDate: 2020/10/23
 */
public class DocumentPlayerActivity extends BaseActivity {

    private DocumentReaderView mDocumentReaderView;
    private String mFilePath = "";
    private String mFileSize = "";
    private String mSourceApp = "";//文档来源APP
    private String mSourcePage = "";//文档来源页面
    private Handler mHandler;
    private static long startTime;
    private static long endTime;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openFile(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        TAG = DocumentPlayerActivity.class.getSimpleName();
        EventBus.getDefault().register(this);
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_document_player, null);
        setContentView(contentView);
        if (!DocumentBrowser.isInited()) {
            DocumentBrowser.init(getApplicationContext(), "DocumentPlayerActivity onCreate.");
        }
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mDocumentReaderView = findViewById(R.id.doc_reader_view);
        initButton();
        openFile(getIntent());
    }

    private void openFile(Intent intent) {
        mFilePath = intent.getStringExtra(DocumentUtil.KEY_FILE_PATH);
        mFileSize = intent.getStringExtra(DocumentUtil.KEY_FILE_SIZE);
        mSourcePage = intent.getStringExtra(DocumentUtil.KEY_SOURCE_PAGE);
        mSourceApp = intent.getStringExtra(DocumentUtil.KEY_SOURCE_APP);
        if (TextUtils.isEmpty(mFilePath)) {
            Log.e(TAG, "openFile: filePath is null !!!");
        }
        savePlayRecord();
        getPermission();
    }

    private void savePlayRecord() {
        if (TextUtils.isEmpty(mFilePath)) {
            return;
        }
        List<DocumentData> docs = SpUtil.getList(this, DocumentUtil.SP_KEY_RECORD);
        if (docs == null) {
            docs = new ArrayList<>();
        }
        for (DocumentData d : docs) {
            if (d.url.equals(mFilePath)) {
                docs.remove(d);
                break;
            }
        }
        File file = new File(mFilePath);
        if (file.exists() && file.isFile()) {
            DocumentData data = createDocData(file);
            if (data != null) {
                docs.add(data);
                SpUtil.putList(this, DocumentUtil.SP_KEY_RECORD, docs);
                submitOpenDocLog(data);
            }
        }
    }

    private DocumentData createDocData(File file) {
        String filePath = file.getAbsolutePath();
        String suffix = DocumentUtil.getFileType(filePath);
        long size = file.length();
        if (size <= 0) {
            return null;
        }
        int pos = filePath.lastIndexOf(File.separator);
        if (pos == -1) return null;
        Log.i(TAG, "createDocData--> path:" + filePath);
        String displayName = filePath.substring(pos + 1);
        DocumentData data = new DocumentData();
        data.tittle = displayName;
        data.url = filePath;
        data.size = size;
        data.lastModifiedTime = System.currentTimeMillis();//此处赋值当前打开文档的时间
        data.suffix = suffix;
        data.format = FormatEnum.getFormat(suffix).type;
        return data;
    }

    private void submitOpenDocLog(DocumentData data) {
        LogParams params = LogParams.newParams().append("file_type", data.suffix).append("file_size", FileCalculatorUtil.getFileSize(data.size));
        String eventId = "";
        switch (mSourcePage) {
            case DocumentUtil.SOURCE_PAGE_OTHER_APP:
                params.append(DocumentUtil.KEY_SOURCE_APP, mSourceApp);
                eventId = DocLogSubmit.EVENTID_OPEN_DOC_BY_OTHER_APP;
                break;
            case DocumentUtil.SOURCE_PAGE_DOC_SCAN:
                eventId = DocLogSubmit.EVENTID_ADD_CLICKED_DOC;
                break;
            case DocumentUtil.SOURCE_PAGE_DOC_MAIN:
                break;
        }
        if (!TextUtils.isEmpty(eventId)) {
            DocLogSubmit.submit(eventId, params.getParams());
        }
    }

    private void getPermission() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                Log.d(TAG, "permissionGranted");
                DocumentBrowser.setInitListener(new DocumentBrowser.OnInitListener() {
                    @Override
                    public void onInitFinish(boolean ret) {
                        Log.i(TAG, "onInitFinish: ret:" + ret);
                        mDocumentReaderView.openFile(mFilePath);
                        if (ret) {
                            DocumentBrowser.clearListener();
                        } else {
                            //强制初始化
                            DocumentBrowser.init(DocumentPlayerActivity.this, "DocumentPlayerActivity onInitFinish false.");
                        }
                    }
                });
            }

            @Override
            public void permissionDenied(String[] permission) {
                Log.d(TAG, "permissionDenied");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.getInstance().showGlobalShort("需要打开文件读取权限");
                    }
                });
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initButton() {
        mHandler = new Handler();
        LinearLayout btnForScreen = findViewById(R.id.btn_for_screen);
        btnForScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeIOThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        pushDoc(DocumentPlayerActivity.this, mFilePath);
                    }
                });
                submitLocalPushUMData();
            }
        });
        btnForScreen.bringToFront();

    }

    public static void pushDoc(Context context, String mFilePath) {
        playVibrate(context);
        startTime = System.currentTimeMillis();

        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if (connectState == CONNECT_NOTHING || deviceInfo == null) {
            ConnectDialogActivity.start(context);
            return;
        }
        //本地连接不通
        if (!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)) {
            WifiConnectActivity.start(context);
            return;
        }

        EventBus.getDefault().post(new StartPushEvent("document"));

        IMMessageCallback callback = new IMMessageCallback() {
            @Override
            public void onStart(IMMessage message) {

                                /*if (!deviceInfo.isTempDevice) {
                                    ToastUtils.getInstance().showGlobalShort(R.string.push_screen_success_tips);
                                    return;
                                }*/
            }

            @Override
            public void onProgress(IMMessage message, int progress) {

            }

            @Override
            public void onEnd(IMMessage message, int code, String info) {
                Log.d(TAG, "onEnd: code=" + code + "\n info:" + info);
            }
        };
        FormatEnum format = FormatEnum.getFormat(DocumentUtil.getFileType(mFilePath));
        String fileName = DocumentUtil.getFileNameFromPath(mFilePath);
        Map<String, String> message = new HashMap<>();
        message.put("log_castType", format.type);
        message.put("yozo_version", format.equals(FormatEnum.PPT) ? "205011" : "205023");
//                        message.put("yozo_version", "205011");
        int protoVersion = 4;
        if (format.equals(FormatEnum.PPT)) {//ppt功能不变，可以兼容旧版本，不需要拉平
            protoVersion = 1;
        }
        sendDocMessage(fileName, new File(mFilePath), message, protoVersion, TARGET_CLIENT_MEDIA_PLAYER, callback);
    }

    public static void sendDocMessage(String name, File content, Map<String, String> map, int protoVersion, String targetClient, IMMessageCallback imMessageCallback) {
        IMMessage message = IMMessage.Builder.createDocMessage(
                SSConnectManager.getInstance().getMy(),
                SSConnectManager.getInstance().getTarget(),
                MainSSClientService.AUTH,
                targetClient,
                content);
        message.setReqProtoVersion(protoVersion);
        CmdData cmdData = new CmdData(LocalMediaParams.CMD.PLAY.toString(),
                CmdData.CMD_TYPE.LOCAL_MEDIA.toString(), new LocalMediaParams(name).toJson());
        message.putExtra("response", cmdData.toJson());
        message.putExtra("showtips", "true");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            message.putExtra(entry.getKey(), entry.getValue());
        }
        SSConnectManager.getInstance().sendMessage(message, imMessageCallback);
    }

    Runnable runnablePushError = new Runnable() {
        @Override
        public void run() {
            submitPushDuration(-2, "timeout");
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ProgressEvent progressEvent) {
        Log.d(TAG, "onEvent: " + progressEvent.isResultSuccess());
        if (progressEvent.getType() == IMMessage.TYPE.PROGRESS) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(runnablePushError, 9000);
            return;
        }
        if (progressEvent.getType() != IMMessage.TYPE.RESULT) {
            return;
        }
        Log.d(TAG, "onEvent: " + progressEvent.getInfo());
        mHandler.removeCallbacksAndMessages(null);
        submitPushDuration(progressEvent.isResultSuccess() ? 0 : -1, progressEvent.getInfo());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG); // 统计页面
//        disableScreenSaver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    @Override
    protected void onStop() {
        super.onStop();
//        releaseScreenSaver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        EventBus.getDefault().unregister(this);
        DocumentBrowser.clearListener();
        mDocumentReaderView.destroy();
    }

    private void submitLocalPushUMData() {
        DecimalFormat df = new DecimalFormat("#0.0");
        String size = "unknown";
        try {
            size = String.valueOf(df.format(Double.valueOf(mFileSize) / 1024 / 1024));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String suffix = mFilePath.substring(mFilePath.lastIndexOf('.') + 1);
        LogParams params = LogParams.newParams().append("applet_id", "com.coocaa.smart.localdoc_guide")
                .append("applet_name", "文档投电视")
                .append("file_size", size)
                .append("file_format", suffix);
        LogSubmit.event("file_cast_btn_clicked", params.getParams());

        params = LogParams.newParams().append("file_type", suffix)
                .append("file_size", FileCalculatorUtil.getFileSize(Long.parseLong(mFileSize == null ? "0" : mFileSize)));
        DocLogSubmit.submit(DocLogSubmit.EVENTID_CLICK_CAST_DOC_BTN, params.getParams());
    }

    private void submitPushDuration(int retCode, String retMsg) {
        endTime = System.currentTimeMillis();
        long size = 0;
        String duration = "0";
        try {
            size = Long.parseLong(mFileSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (startTime < endTime) {
            duration = String.valueOf(endTime - startTime);
        }
        try {
            LogParams params = LogParams.newParams().append("applet_id", "com.coocaa.smart.localdoc_guide")
                    .append("applet_name", "文档投电视")
                    .append("duration", duration)
                    .append("file_size", FileCalculatorUtil.getFileSize(size))
                    .append("file_format", mFilePath.substring(mFilePath.lastIndexOf('.') + 1));
            LogSubmit.event("cast_load_duration", params.getParams());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File file = new File(mFilePath);
            String fileSize = "";
            if (file.exists()) {
                fileSize = Formatter.formatFileSize(this, file.length());
            }
            String wifiSsid = WifiUtil.getConnectWifiSsid(this);
            Map<String, Object> data = new HashMap<>();
            data.put("fileUri", mFilePath);//文件链接地址
            data.put("fileType", DocumentUtil.getFileType(mFilePath));//文件类型；
            data.put("fileSize", fileSize);
            data.put("time", Long.parseLong(duration));//从点击开始共享到传输完成，或者出错/超时；
            data.put("wifiSSID", wifiSsid);
            if (retCode == 0) {
                PayloadEvent.submit("iotchannel.link_events", "FileServerMsg", data);
            } else {
                data.put("errorCode", String.valueOf(retCode));//返回的错误码，或者异常，正常则为0；
                data.put("errorDsc", retMsg);//错误描述（选填），若太大，则不建议上报；
                PayloadEvent.submit("iotchannel.link_events", "FileServerMsgError", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

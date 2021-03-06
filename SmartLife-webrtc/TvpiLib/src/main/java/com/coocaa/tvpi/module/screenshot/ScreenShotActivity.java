package com.coocaa.tvpi.module.screenshot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ScreenshotEvent;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpi.util.CommonUtil;
import com.coocaa.tvpi.util.RotateTransformation;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.retrofiturlmanager.RetrofitUrlManager;
import me.jessyan.retrofiturlmanager.onUrlChangeListener;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.coocaa.tvpi.common.UMengEventId.SCREEN_SHOT_AGAIN;
import static com.coocaa.tvpi.common.UMengEventId.SCREEN_SHOT_SAVE;
import static com.coocaa.tvpi.common.UMengEventId.SCREEN_SHOT_SHARE;

/**
 * @ClassName ScreenShotActivity
 * @Description TODO (write something)
 * @User heni
 * @Date 2020-05-14
 */
public class ScreenShotActivity extends BaseActivity {
    private static final String TAG = ScreenShotActivity.class.getSimpleName();
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 0x01;
    private static final String SD_PATH = Environment
            .getExternalStorageDirectory().getPath() + "/ScreenShots/";

    private Context mContext;
    private ImageView leftTopImg;
    private TextView rightTopTv;
    private ImageView screenShotIV;
    private ImageView screenShotIVPortrait;
    private TextView screenShotFailedTv;
    private View loadingLayout;
    private TextView saveBtn;
    private View shareWechat, shareWechatGroup, shareQQ, shareQzone;

    private MyHandler mMyHandler;
    private static final int MSG_SCREENSHOT_FAILED = 0x0001;
    private static final int MSG_SCREENSHOT_SUCC = 0x0002;

    private boolean isLandscape;
    private boolean isGetScreenShoting;
    private Bitmap bitmap;
    SHARE_MEDIA share_media = null;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: ?????????????????? ?????????SD Card");
                    saveFileToStorage();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_shot_view);
        mContext = this;
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarDarkMode(this);
        mMyHandler = new MyHandler(this);
        EventBus.getDefault().register(this);
        initView();
        initListener();
        startScreenShot(true);
//         bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_film_banner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mMyHandler != null) {
            mMyHandler.removeCallbacksAndMessages(null);
            mMyHandler = null;
        }
    }

    @Subscribe
    public void onEvent(ScreenshotEvent screenshotEvent) {
        Log.d(TAG, "ScreenshotEvent: " + screenshotEvent.url + "\n"
                + screenshotEvent.msg + "\n");
        if (isGetScreenShoting) {
            isGetScreenShoting = false;
            downloadPic(screenshotEvent.url);
        }
    }

    private void initView() {
        leftTopImg = findViewById(R.id.screen_shot_left_top_iv);
        rightTopTv = findViewById(R.id.screen_shot_right_top_tv);
        screenShotIV = findViewById(R.id.screen_shot_iv);
        screenShotIVPortrait = findViewById(R.id.screen_shot_iv_portrait);
        screenShotFailedTv = findViewById(R.id.screen_shot_failed_tips);
        loadingLayout = findViewById(R.id.screen_shot_loading_layout);
        shareWechat = findViewById(R.id.share_ll_wechat);
        shareWechatGroup = findViewById(R.id.share_ll_wechat_group);
        shareQQ = findViewById(R.id.share_ll_qq);
        shareQzone = findViewById(R.id.share_ll_qzone);

        saveBtn = findViewById(R.id.screen_shot_save_to_sdcard);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ???????????????????????????????????????????????????????????????
                boolean writeSDCardPermisson = checkSDCardPermisson();
                Log.d(TAG, "writeSDCardPermisson:" + writeSDCardPermisson);
                if (writeSDCardPermisson) {
                    boolean saveResult = saveFileToStorage();
                    if (!saveResult) {
                        ToastUtils.getInstance().showGlobalShort("??????????????????????????????...");
                    }else{
                        ToastUtils.getInstance().showGlobalShort("???????????????????????????");
                    }
                } else {
                    //????????????????????????????????????????????????????????????
                    //showPermissionLayout();
                }
                MobclickAgent.onEvent(ScreenShotActivity.this, SCREEN_SHOT_SAVE);
            }
        });
        RetrofitUrlManager.getInstance().registerUrlChangeListener(new ChangeListener());
    }

    private void initListener() {
        leftTopImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rightTopTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????????????????????
                startScreenShot(true);
                MobclickAgent.onEvent(ScreenShotActivity.this, SCREEN_SHOT_AGAIN);
            }
        });
        shareWechat.setOnClickListener(shareBtnClickLis);
        shareWechatGroup.setOnClickListener(shareBtnClickLis);
        shareQQ.setOnClickListener(shareBtnClickLis);
        shareQzone.setOnClickListener(shareBtnClickLis);
    }

    public void startScreenShot(boolean isLandscape) {
        Log.d(TAG, "startScreenShot: isLandscape: " + isLandscape);
        this.isLandscape = isLandscape;
        startLoadingAnimation();

        screenShotIV.setVisibility(isLandscape ? VISIBLE : GONE);
        screenShotIVPortrait.setVisibility(isLandscape ? GONE : VISIBLE);
        screenShotFailedTv.setVisibility(GONE);

        CmdUtil.sendScreenshot();
        isGetScreenShoting = true;
    }

    public void downloadPic(final String url) {
        Log.d(TAG, "downloadPic: " +url);
        final String savePath = Environment.getExternalStorageDirectory().getPath() + "/tvpi" +
                "/tvshot/";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        final String fileName = "tvshot-" + simpleDateFormat.format(new Date()) + ".jpg";

        NetWorkManager
                .getInstance()
                .getApiService()
                .downPic(url)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (ScreenShotActivity.this == null
                                || ScreenShotActivity.this.isFinishing()) {
                            return;
                        }
                        //??????activity???????????????????????????????????????????????????
                        try {
                            byte[] bys;
                            bys = responseBody.bytes();
                            bitmap = BitmapFactory.decodeByteArray(bys, 0, bys.length);

                            //??????????????????
                            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + savePath + fileName)));
                            Message.obtain(mMyHandler, MSG_SCREENSHOT_SUCC, bitmap).sendToTarget();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        if (ScreenShotActivity.this == null
                                || ScreenShotActivity.this.isFinishing()) {
                            return;
                        }
                        //??????activity???????????????????????????????????????????????????
                        Message.obtain(mMyHandler, MSG_SCREENSHOT_FAILED, bitmap).sendToTarget();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void startLoadingAnimation() {
        saveBtn.setEnabled(false);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    private void stopLoadingAnimation() {
        saveBtn.setEnabled(true);
        loadingLayout.setVisibility(View.GONE);
    }

    private class MyHandler extends Handler {

        //???Activity????????????
        private final WeakReference<ScreenShotActivity> mActivity;

        public MyHandler(ScreenShotActivity activity) {
            mActivity = new WeakReference<ScreenShotActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                Log.d(TAG, "handleMessage: mActivity.get() == null");
                return;
            }
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SCREENSHOT_FAILED:
                    screenShotFailed();
                    break;
                case MSG_SCREENSHOT_SUCC:
                    screenShotSucess();
                    break;
                default:
                    break;
            }
        }
    }

    private void screenShotFailed() {
        stopLoadingAnimation();
        screenShotFailedTv.setVisibility(VISIBLE);
    }

    private void screenShotSucess() {
        stopLoadingAnimation();
        if (isLandscape) {
            Log.d(TAG, "1111111111handleMessage: isLandscape:" + isLandscape);
            screenShotIV.setVisibility(isLandscape ? VISIBLE : GONE);

            RequestOptions options = new RequestOptions()
                    .signature(new ObjectKey(UUID.randomUUID().toString()));  //??????????????????
            if (!((AppCompatActivity) mContext).isDestroyed()) {
                Glide.with(mContext)
                        .load(bitmap)
                        .apply(options)
                        .into(screenShotIV);
            }
        } else {
            screenShotIVPortrait.setVisibility(isLandscape ? GONE : VISIBLE);
            Log.d(TAG, "22222222handleMessage: isLandscape:" + isLandscape);
            RequestOptions options = new RequestOptions()
                    .signature(new ObjectKey(UUID.randomUUID().toString()));  //??????????????????
            GlideApp.with(mContext)
                    .load(bitmap)
                    .transform(new RotateTransformation(mContext, 90))
                    .apply(options)
                    .into(screenShotIVPortrait);
        }
    }

    private boolean checkSDCardPermisson() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(ScreenShotActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ScreenShotActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                //?????????????????????????????????
                ToastUtils.getInstance().showGlobalShort("SD???????????????????????????????????????????????????");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean saveFileToStorage() {
        String savePath = Environment.getExternalStorageDirectory().getPath() + "/tvpi/tvshot/";

        if (!createDirectory(savePath)) {
            Log.e(TAG, "create directory failed!");
            return false;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "tvshot-" + simpleDateFormat.format(new Date()) + ".jpg";

        try {
            File dirFile = new File(SD_PATH);
            //?????????????????????????????????????????????
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File file = new File(SD_PATH, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            // ????????????????????????????????????
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ????????????????????????
        String dstFilename = savePath + fileName;
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file" +
                "://" + dstFilename)));
        return true;
    }

    private boolean createDirectory(final String path) {
        boolean result = false;

        try {
            File file = new File(path);
            boolean isDir = false;
            if (!file.isDirectory()) {
                file.delete();
                isDir = file.mkdirs();
            } else {
                isDir = true;
            }

            if (isDir)
                Runtime.getRuntime().exec("chmod 777 " + path);

            result = isDir;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean copyFile(File srcFile, File dstFile) {
        boolean result = false;
        FileInputStream input = null;
        BufferedInputStream inBuff = null;
        FileOutputStream output = null;
        BufferedOutputStream outBuff = null;

        try {
            // ??????????????????????????????????????????
            input = new FileInputStream(srcFile);
            inBuff = new BufferedInputStream(input);

            // ??????????????????????????????????????????
            if (!dstFile.exists()) {
                dstFile.createNewFile();
                output = new FileOutputStream(dstFile);
            } else {
                /*if (dstFile.isFile()) {
                    FileOperator.deleteFile(dstFile);
                } else {
                    FileOperator.deleteDir(dstFile);
                }*/
                CommonUtil.deleteFile(dstFile.getAbsolutePath());
                dstFile.createNewFile();
                output = new FileOutputStream(dstFile);
            }
            outBuff = new BufferedOutputStream(output);


            // ????????????
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // ???????????????????????????
            outBuff.flush();

            result = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //?????????
                if (outBuff != null)
                    outBuff.close();
                if (output != null)
                    output.close();
                if (inBuff != null)
                    inBuff.close();
                if (input != null)
                    input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private class ChangeListener implements onUrlChangeListener {

        @Override
        public void onUrlChangeBefore(HttpUrl oldUrl, String domainName) {
            Log.d(TAG, String.format("The oldUrl is <%s>, ready fetch <%s> from DomainNameHub",
                    oldUrl.toString(),
                    domainName));
        }

        @Override
        public void onUrlChanged(final HttpUrl newUrl, HttpUrl oldUrl) {
            Observable.just(1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            Log.d(TAG, "The newUrl is { " + newUrl.toString() + " }");
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
        }
    }

    View.OnClickListener shareBtnClickLis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(loadingLayout.getVisibility() == VISIBLE){
                return;
            }
            if (v.getId() == R.id.share_ll_wechat) {
                share_media = SHARE_MEDIA.WEIXIN;
                submitShare("wechat");
            } else if (v.getId() == R.id.share_ll_wechat_group) {
                share_media = SHARE_MEDIA.WEIXIN_CIRCLE;
                submitShare("wechat_circle");
            } else if (v.getId() == R.id.share_ll_qq) {
                share_media = SHARE_MEDIA.QQ;
                submitShare("QQ");
            } else if (v.getId() == R.id.share_ll_qzone) {
                share_media = SHARE_MEDIA.QZONE;
                submitShare("QZone");
            }
            Log.d(TAG, "onClick: ....." + share_media);
            if (share_media != null) {
                umengShare(bitmap);
            }
        }
    };

    private void umengShare(Bitmap screenBitmap) {
        if (screenBitmap == null) return;
        Log.d(TAG, "onUmengShare: start compress..." + screenBitmap);
//        ????????????????????????????????????????????????????????????????????????????????????bitmap??????UMImage???????????????
//        Bitmap bitmapScreen = screenBitmap;
//        boolean watermark = SpUtil.getBoolean(this, SpUtil.Keys.SCREENSHOT_WATERMARK, true);
//        Bitmap bitmapQR = null;
//        if (watermark) {
//            bitmapQR = BitmapFactory.decodeResource(getResources(), R.drawable.bg_tvpi_watermark);//????????????
//        }
//        Bitmap bitmap = ScreenShotUtil.newBitmap2(bitmapScreen, bitmapQR);
//        Bitmap bitmapThumb = ScreenShotUtil.newBitmap2(bitmapScreen, bitmapQR);
//        bitmapThumb = ScreenShotUtil.getNewSizeBitmap(bitmapThumb, bitmapThumb.getWidth() / 10,
//                bitmapThumb.getHeight() / 10);
//        UMImage image = new UMImage(this, bitmap);
//        UMImage thumb = new UMImage(this, bitmapThumb);
//        image.setThumb(thumb);
//        Log.d(TAG, "onUmengShare: finish compress");


        UMImage image = new UMImage(this, bitmap);

        new ShareAction(this)
                .setPlatform(share_media)//????????????
                .withText("hello")//????????????
                .withMedia(image)
                .setCallback(shareListener)//???????????????
                .share();
    }

    private UMShareListener shareListener = new UMShareListener() {
        /**
         * @descrption ?????????????????????
         * @param platform ????????????
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        /**
         * @descrption ?????????????????????
         * @param platform ????????????
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtils.getInstance().showGlobalShort("???????????????");
        }

        /**
         * @descrption ?????????????????????
         * @param platform ????????????
         * @param t ????????????
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.getInstance().showGlobalShort("???????????????");
        }

        /**
         * @descrption ?????????????????????
         * @param platform ????????????
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            ToastUtils.getInstance().showGlobalShort("???????????????");
        }
    };

    private void submitShare(String platform) {
        Map<String, String> map = new HashMap<>();
        map.put("platform", platform);
        MobclickAgent.onEvent(this, SCREEN_SHOT_SHARE, map);
    }

}

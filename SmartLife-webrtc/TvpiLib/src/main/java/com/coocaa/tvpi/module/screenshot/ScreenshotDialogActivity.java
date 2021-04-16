package com.coocaa.tvpi.module.screenshot;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.coocaa.publib.base.DialogActivity;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ScreenshotEvent;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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
import static com.airbnb.lottie.LottieDrawable.INFINITE;

/**
 * @Author: wuhaiyuan
 */
public class ScreenshotDialogActivity extends DialogActivity {

    private static final String TAG = ScreenshotDialogActivity.class.getSimpleName();

    private View mLayout;
    private ImageView screenshotIV;
    private ImageView screenshotTipsIV;
    private LottieAnimationView screenshotAnim;
    private TextView screenshotBtn;

    private static final String savePath = Environment.getExternalStorageDirectory().getPath() + "/tvpi" + "/tvshot/";

    private MyHandler mMyHandler;
    private static final int MSG_SCREENSHOT_FAILED = 0x0001;
    private static final int MSG_SCREENSHOT_SUCC = 0x0002;
    private static final int MSG_SCREENSHOT_TIMEOUT = 0x0003;
    private static final int MSG_SCREENSHOT_NO_PERMISSION = 0x0004;

    private Bitmap bitmap;

    private boolean isGetScreenShoting;

    public static void start(Context context) {
        Intent starter = new Intent(context, ScreenshotDialogActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarHelper.translucent(this);
        mMyHandler = new MyHandler(this);
        EventBus.getDefault().register(this);
        initView();
        startScreenShot();
        RetrofitUrlManager.getInstance().registerUrlChangeListener(new ChangeListener());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMyHandler != null) {
            mMyHandler.removeCallbacksAndMessages(null);
            mMyHandler = null;
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScreenshotEvent(ScreenshotEvent screenshotEvent) {
        Log.d(TAG, "ScreenshotEvent: " + screenshotEvent.url + "\n"
                + screenshotEvent.msg + "\n");
        if (isGetScreenShoting) {
            isGetScreenShoting = false;
            downloadPic(screenshotEvent.url);
        }
    }

    private void initView() {
        mLayout = LayoutInflater.from(this).inflate(R.layout.activity_screenshot_dialog, null);
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        contentRl.addView(mLayout, params);

        mLayout.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mLayout.findViewById(R.id.content_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mLayout.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        screenshotIV = mLayout.findViewById(R.id.screenshot_iv);
        ViewGroup.LayoutParams layoutParams = screenshotIV.getLayoutParams();
        int screenW = DimensUtils.getDeviceWidth(this);
        int width = screenW - DimensUtils.dp2Px(this, 20*2);
        int height = width * 9 / 16;
        layoutParams.width = width;
        layoutParams.height = height;
        screenshotIV.setLayoutParams(layoutParams);

        screenshotTipsIV = mLayout.findViewById(R.id.screenshot_tips_iv);

        screenshotAnim = mLayout.findViewById(R.id.screenshot_anim);
        screenshotAnim.setImageAssetsFolder("images/");
        screenshotAnim.setAnimation("screenshot_loading.json");
        screenshotAnim.setRepeatCount(INFINITE);

        screenshotBtn = mLayout.findViewById(R.id.screenshot_btn);
        screenshotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScreenShot();
                playStartAnim();
            }
        });
    }

    private void startScreenShot() {
        Log.d(TAG, "startScreenShot: isLandscape: ");

        if (!SSConnectManager.getInstance().isSameWifi()) {
            WifiConnectActivity.start(this);
            return;
        }

        isGetScreenShoting = true;

        screenshotIV.setImageDrawable(null);

        screenshotAnim.playAnimation();
        screenshotAnim.setVisibility(VISIBLE);

        screenshotBtn.setEnabled(false);
        screenshotBtn.setTextColor(getResources().getColor(R.color.color_111111_a20));
        screenshotBtn.setText(R.string.screenshotting);

        CmdUtil.sendScreenshot();
        startTimer();
    }

    private void screenShotFailed() {
        screenshotAnim.cancelAnimation();
        screenshotAnim.setVisibility(GONE);

        screenshotBtn.setEnabled(true);
        screenshotBtn.setTextColor(getResources().getColor(R.color.color_111111));
        screenshotBtn.setText(R.string.screenshot_fail);

        playResultAnim(MSG_SCREENSHOT_FAILED);
    }

    private void screenShotSucess() {
        screenshotAnim.cancelAnimation();
        screenshotAnim.setVisibility(GONE);

        screenshotBtn.setEnabled(true);
        screenshotBtn.setTextColor(getResources().getColor(R.color.color_111111));
        screenshotBtn.setText(R.string.screenshot_success);

        RequestOptions options = new RequestOptions()
                .signature(new ObjectKey(UUID.randomUUID().toString()));  //给缓存加签名

        if (!isDestroyed()) {
            Glide.with(this)
                    .load(bitmap)
                    .apply(options)
                    .into(screenshotIV);
        }
    }

    private void screenShotTimeout() {
        Log.d(TAG, "screenShotTimeout: ");
        screenshotAnim.cancelAnimation();
        screenshotAnim.setVisibility(GONE);

        screenshotBtn.setEnabled(true);
        screenshotBtn.setTextColor(getResources().getColor(R.color.color_111111));
        screenshotBtn.setText(R.string.screenshot_fail);

        playResultAnim(MSG_SCREENSHOT_TIMEOUT);
        isGetScreenShoting = false;
        ToastUtils.getInstance().showGlobalShort("截屏超时");
    }

    private void playStartAnim() {
        Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                screenshotTipsIV.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        screenshotTipsIV.startAnimation(animationOut);
    }

    private void playResultAnim(int result) {
        switch (result) {
            case MSG_SCREENSHOT_FAILED:
                screenshotTipsIV.setBackgroundResource(R.drawable.bg_screenshot_fail);
                break;
            case MSG_SCREENSHOT_SUCC:
                screenshotTipsIV.setBackgroundResource(R.drawable.bg_screenshot_success);
                break;
            case MSG_SCREENSHOT_TIMEOUT:
                screenshotTipsIV.setBackgroundResource(R.drawable.bg_screenshot_fail);
                break;
            case MSG_SCREENSHOT_NO_PERMISSION:
                screenshotTipsIV.setBackgroundResource(R.drawable.bg_screenshot_no_permission);
                break;
            default:
                break;
        }
        screenshotTipsIV.setVisibility(VISIBLE);

        Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
        animationIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                screenshotTipsIV.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        screenshotTipsIV.startAnimation(animationIn);
    }

    private void downloadPic(final String url) {
        Log.d(TAG, "downloadPic: " +url);

        NetWorkManager
                .getInstance()
                .getApiService()
                .downPic(url)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (ScreenshotDialogActivity.this == null
                                || ScreenshotDialogActivity.this.isFinishing()) {
                            return;
                        }
                        //防止activity退出后还发送消息，会导致空指针异常
                        try {
                            byte[] bys;
                            bys = responseBody.bytes();
                            bitmap = BitmapFactory.decodeByteArray(bys, 0, bys.length);

                            Message.obtain(mMyHandler, MSG_SCREENSHOT_SUCC, bitmap).sendToTarget();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        if (ScreenshotDialogActivity.this == null
                                || ScreenshotDialogActivity.this.isFinishing()) {
                            return;
                        }
                        //防止activity退出后还发送消息，会导致空指针异常
                        Message.obtain(mMyHandler, MSG_SCREENSHOT_FAILED, bitmap).sendToTarget();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private class MyHandler extends Handler {

        //对Activity的弱引用
        private final WeakReference<ScreenshotDialogActivity> mActivity;

        public MyHandler(ScreenshotDialogActivity activity) {
            mActivity = new WeakReference<ScreenshotDialogActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                Log.d(TAG, "handleMessage: mActivity.get() == null");
                return;
            }
            super.handleMessage(msg);

            removeTimer();

            switch (msg.what) {
                case MSG_SCREENSHOT_FAILED:
                    screenShotFailed();
                    break;
                case MSG_SCREENSHOT_SUCC:
                    screenShotSucess();
                    checkSDCardPermisson();
                    break;
                case MSG_SCREENSHOT_TIMEOUT:
                    screenShotTimeout();
                    break;
                default:
                    break;
            }
        }
    }

    private Runnable timeRunable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "timeRunable");
            Message.obtain(mMyHandler, MSG_SCREENSHOT_TIMEOUT).sendToTarget();
        }
    };

    private static final int SCREENSHOT_TIME = 10 * 1000;
    private void startTimer() {
        Log.d(TAG, "startTimer: ");
        mMyHandler.postDelayed(timeRunable, SCREENSHOT_TIME);
    }

    private void removeTimer() {
        mMyHandler.removeCallbacks(timeRunable);
    }

    //检查权限保存图片
    private void checkSDCardPermisson() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                boolean saveResult = saveFileToStorage();
                if (saveResult) {
                    ToastUtils.getInstance().showGlobalShort("成功保存截图至相册");
                    playResultAnim(MSG_SCREENSHOT_SUCC);
                }else{
                    ToastUtils.getInstance().showGlobalShort("保存截图至相册失败了...");
                    playResultAnim(MSG_SCREENSHOT_FAILED);
                }
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalShort("SD卡读写权限被禁，请前往手机设置打开");
                playResultAnim(MSG_SCREENSHOT_NO_PERMISSION);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public boolean saveFileToStorage() {
        if (!createDirectory(savePath)) {
            Log.e(TAG, "create directory failed!");
            return false;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "tvshot-" + simpleDateFormat.format(new Date()) + ".jpg";

        try {
            File dirFile = new File(savePath);
            //如果不存在，那就建立这个文件夹
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File file = new File(savePath, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            // 其次把文件插入到系统图库
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // 最后通知图库更新
            String dstFilename = savePath + fileName;
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file" +
                    "://" + dstFilename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

}

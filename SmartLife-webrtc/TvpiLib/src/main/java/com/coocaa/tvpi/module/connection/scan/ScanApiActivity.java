package com.coocaa.tvpi.module.connection.scan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.UriUtils;
import com.coocaa.tvpilib.R;
import com.huawei.hms.hmsscankit.OnResultCallback;
import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.ml.scan.HmsScan;
import com.king.zxing.util.CodeUtils;

import static android.view.animation.Animation.INFINITE;

/**
 * 对外提供接口，返回扫描结果的activity
 * @Author: yuzhan
 */
public class ScanApiActivity extends BaseActivity implements UnVirtualInputable {
    private static final String TAG = "ScanApi";
    public static final int REQUEST_CODE_PHOTO = 0X02;

    private static final int MODE_SCAN = 1;
    private int curMode = MODE_SCAN;


    private RemoteView remoteView;
    private Bundle savedInstanceState;

    private ImageView ivFlash;
    private TextView tvFlash;
    private boolean isFlashOpen = false;
    private ImageView imgScan;


    public static void start(Context context) {
        Intent starter = new Intent(context, ScanApiActivity.class);
        if(!(context instanceof Activity)) {
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        StatusBarHelper.setStatusBarLightMode(this);
        StatusBarHelper.translucent(this);
        //让全面屏手机底部滑两次才能退出；
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        });
        setContentView(R.layout.activity_scan_api);
        this.savedInstanceState = savedInstanceState;
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != remoteView) {
            remoteView.onResume();
        }
        startScan();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (null != remoteView) {
            remoteView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != remoteView) {
            remoteView.onStop();
        }
    }

    private void startScan() {
        Log.d(TAG, "startScan: ");
        if (curMode == MODE_SCAN) {
            if (remoteView != null) {
                remoteView.resumeContinuouslyScan();
                startScanAnimation();
            } else {
                initScan();
            }
        }
    }

    private void initScan() {
        Rect rect = new Rect();
        rect.left = 0;
        rect.right = DimensUtils.getDeviceWidth(this);
        rect.top = 0;
        rect.bottom = DimensUtils.getDeviceHeight(this);

        //initialize RemoteView instance, and set calling back for scanning result
        remoteView = new RemoteView.Builder().setContext(this).setBoundingBox(rect).setFormat(HmsScan.ALL_SCAN_TYPE).build();
        remoteView.onCreate(savedInstanceState);
        remoteView.setOnResultCallback(onResultCallback);

        //add remoteView to framelayout
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = findViewById(R.id.rim);
        frameLayout.addView(remoteView, params);

        remoteView.onStart();
        remoteView.onResume();
    }

    private OnResultCallback onResultCallback = new OnResultCallback() {
        @Override
        public void onResult(HmsScan[] result) {
            if (result != null && result.length > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {
//                ToastUtils.getInstance().showGlobalLong(result[0].getOriginalValue());
                String resultStr = result[0].getOriginalValue();
                Log.d(TAG, "onResult: " + resultStr);
                remoteView.pauseContinuouslyScan();
                Log.d(TAG, "onResult: curMode = " + curMode);
                if(curMode == MODE_SCAN) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopScan();
                        }
                    });
                    handleScanResult(resultStr);
                }
            }
        }
    };

    private void stopScan() {
        Log.d(TAG, "stopScan: ");
        if (remoteView != null) {
            remoteView.pauseContinuouslyScan();
            stopScanAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: " + event.toString());

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        if (null != remoteView) {
            remoteView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_PHOTO) {
                if (data != null) {
                    parsePhoto(data);
                }
            }
        }
    }

    private void initView() {

        ImageView ivBack = findViewById(R.id.iv_back);
        TextView tvAlbum = findViewById(R.id.tv_album);
        imgScan = findViewById(R.id.scan_img);
        ivFlash = findViewById(R.id.iv_flash);
        tvFlash = findViewById(R.id.tv_flash);

        startScanAnimation();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoCode();
            }
        });

        //闪光灯
        ivFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFlashOpen = !isFlashOpen;
//                captureHelper.getCameraManager().setTorch(isFlashOpen);
            }
        });
    }

    private void startScanAnimation() {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                imgScan.setVisibility(View.VISIBLE);
                Animation scanAnim = AnimationUtils.loadAnimation(ScanApiActivity.this, R.anim.scan_up_dowm);
                scanAnim.setRepeatCount(INFINITE);
                imgScan.startAnimation(scanAnim);
            }
        });
    }

    private void stopScanAnimation() {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                imgScan.clearAnimation();
                imgScan.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void handleScanResult(String result) {
        Log.d(TAG, "onResultCallback: " + result);

        if (!TextUtils.isEmpty(result)) {
            Intent intent = new Intent();
            intent.putExtra("result", result);
            setResult(1001, intent);
            finish();
        } else {
            ToastUtils.getInstance().showGlobalShort("扫一扫失败");
        }
    }

    private void startPhotoCode() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
    }

    private void parsePhoto(Intent data) {
        final String path = UriUtils.getImagePath(this, data);
        Log.d(TAG, "path:" + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        //异步解析
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                final String result = CodeUtils.parseCode(path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "result:" + result);
                        ToastUtils.getInstance().showGlobalLong(result);
                    }
                });

            }
        });

    }
}

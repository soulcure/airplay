package com.coocaa.tvpi.module.connection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpilib.R;
import com.huawei.hms.hmsscankit.OnResultCallback;
import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.ml.scan.HmsScan;


public class DefinedActivity extends Activity {

    private static final String TAG = "DefinedActivity2";

    //declare RemoteView instance
    private RemoteView remoteView;
    private Bundle savedInstanceState;
    //declare the key ,used to get the value returned from scankit
    public static final String SCAN_RESULT = "scanResult";

    int mScreenWidth;
    int mScreenHeight;
    //scan_view_finder width & height is  300dp
    final int SCAN_FRAME_SIZE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_defined);

        this.savedInstanceState = savedInstanceState;
        initScan();

        //set back button listener
        ImageView backBtn = findViewById(R.id.back_img);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
    }

    private OnResultCallback onResultCallback = new OnResultCallback() {
        @Override
        public void onResult(HmsScan[] result) {
            if (result != null && result.length > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {
                ToastUtils.getInstance().showGlobalLong(result[0].getOriginalValue());
                remoteView.pauseContinuouslyScan();
            }
        }
    };

    //manage remoteView lifecycle
    @Override
    protected void onStart() {
        super.onStart();
        remoteView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        remoteView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        remoteView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        remoteView.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        remoteView.onStop();
    }

}

package com.coocaa.tvpi.module.mine.userinfo;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.lyft.android.scissors.CropView;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.graphics.Bitmap.CompressFormat.JPEG;

public class CropImageActivity extends BaseActivity implements UnVirtualInputable {
    private CropView cropView;
    private Uri inputUri;
    private Uri outputUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        StatusBarHelper.translucent(this);
        parseIntent();
        initView();
    }

    private void parseIntent() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            inputUri = (Uri) getIntent().getExtras().get("inputUri");
            outputUri = (Uri) getIntent().getExtras().get("outputUri");
        }
    }

    private void initView() {
        TextView tvCancel = findViewById(R.id.cancel_btn);
        TextView tvSave = findViewById(R.id.save_btn);
        cropView = findViewById(R.id.crop_view);
        GlideApp.with(this)
                .load(inputUri)
                .into(cropView);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeIOThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        Future<Void> future = cropView.extensions()
                                .crop()
                                .quality(100)
                                .format(JPEG)
                                .into(new File(outputUri.getPath()));
                        try {
                            future.get(500, TimeUnit.MILLISECONDS);
                            HomeUIThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                        } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            e.printStackTrace();
                            HomeUIThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.getInstance().showGlobalShort("发生异常");
                                    finish();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}

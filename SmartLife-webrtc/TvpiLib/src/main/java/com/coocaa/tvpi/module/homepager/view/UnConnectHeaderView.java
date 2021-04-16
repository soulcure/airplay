package com.coocaa.tvpi.module.homepager.view;

import android.Manifest;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.connection.ScanActivity;
import com.coocaa.tvpi.module.homepager.adapter.UnconnectedBannerAdapter;
import com.coocaa.tvpi.module.homepager.adapter.bean.UnConnectBannerBean;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.transformer.ScaleInTransformer;

import static com.coocaa.tvpi.common.UMengEventId.MAIN_PAGE_SCAN;

/**
 * 未连接的情况下智屏头部UI
 * Created by songxing on 2020/10/21
 */
public class UnConnectHeaderView extends RelativeLayout {
    private Banner banner;
    private RelativeLayout connectLayout;
    private TextView tvConnectProblem;

    public UnConnectHeaderView(Context context) {
        this(context,null);
    }

    public UnConnectHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView(){
        LayoutInflater.from(getContext()).inflate(R.layout.layout_smartscreen_header_unconnected, this, true);
        banner = findViewById(R.id.banner);
        connectLayout = findViewById(R.id.connectInfoLayout);
        tvConnectProblem = findViewById(R.id.tvConnectProblem);
        tvConnectProblem .getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG );
        banner.setAdapter(new UnconnectedBannerAdapter(getContext(),UnConnectBannerBean.getTestData()))
                .setIntercept(false)
                .setIndicator(new CircleIndicator(getContext()));
        banner.setPageTransformer(new ScaleInTransformer());

        connectLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionsUtil.getInstance().requestPermission(getContext(), new PermissionListener() {
                    @Override
                    public void permissionGranted(String[] permission) {
                        ScanActivity.start(getContext());
                        MobclickAgent.onEvent(getContext(), MAIN_PAGE_SCAN);
                    }

                    @Override
                    public void permissionDenied(String[] permission) {
                        ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.request_camera_permission_tips));
                    }
                }, Manifest.permission.CAMERA);
            }
        });
    }
}

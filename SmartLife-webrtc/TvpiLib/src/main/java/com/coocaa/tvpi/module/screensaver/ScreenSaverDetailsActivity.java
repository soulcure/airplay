package com.coocaa.tvpi.module.screensaver;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.coocaa.publib.base.BaseActionBarActivity;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpilib.R;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ScreenSaverDetailsActivity extends BaseActionBarActivity {

    private TextView tvTitle, tvName, tvLikes, tvPreviews;
    private ImageView imgDetails;
    private ViewGroup layoutPush, layoutSetting;

//    ScreenSaverListBean.RecommendInfoBean recommendInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver_details);
        if (getIntent() != null) {
//            recommendInfo = (ScreenSaverListBean.RecommendInfoBean) getIntent()
//                    .getExtras().getSerializable("recommendInfo");
        }

        initView();
        setListener();
        initData();
    }

    private void initView() {
        tvTitle = findViewById(R.id.details_title_tv);
        tvName = findViewById(R.id.details_name_tv);
        tvLikes = findViewById(R.id.details_likes_tv);
        tvPreviews = findViewById(R.id.details_previews_tv);
        imgDetails = findViewById(R.id.details_img);
        layoutPush = findViewById(R.id.details_push_layout);
        layoutSetting = findViewById(R.id.details_setting_layout);
    }

    private void setListener() {
        layoutPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* DeviceControllerManager.getInstance()
                        .pushInternetImg(recommendInfo.getImg720PUrl(), new IPushResourceCallBack.IPlayCallBack() {
                            @Override
                            public void onPlaySuccess() {
                                ToastUtils.getInstance().showGlobalLong("投屏成功");
                            }

                            @Override
                            public void onPlayFailure(Exception e) {
//                                ConnectDialogActivity.openConnectDialog(ConnectDialogActivity.FROM_SELECT_WIFI_DEVICE);
                                ToastUtils.getInstance().showGlobalLong("投屏失败");
                            }
                        });*/
            }
        });
        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        layoutSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* DeviceControllerManager.getInstance()
                        .startScreenSaver(new IPushResourceCallBack.ITransportCallBack() {
                    @Override
                    public void onTransportSuccess() {
                        ToastUtils.getInstance().showGlobalLong("电视正在启动定制屏保");
                    }

                    @Override
                    public void onTransportFailure(Exception e) {
//                        ConnectDialogActivity.openConnectDialog(ConnectDialogActivity.FROM_SELECT_WIFI_DEVICE);
                    }
                });*/
            }
        });
    }

    private void initData() {
        RequestOptions options = new RequestOptions()
                .transform(new RoundedCornersTransformation(DimensUtils.dp2Px(this, 10),
                        0, RoundedCornersTransformation.CornerType.ALL))
                .placeholder(R.drawable.icon_pic_network_failed)
                .error(R.drawable.icon_pic_network_failed);

       /* Glide.with(this)
                .load(recommendInfo.getImgthumUrl())
                .apply(options)
                .into(imgDetails);
        tvTitle.setText(recommendInfo.getImgTitle() == null ? "未知" : recommendInfo.getImgTitle());
        tvName.setText(recommendInfo.getImgAuthor() == null ? "未知" : recommendInfo.getImgAuthor());
        tvLikes.setText(recommendInfo.getUsed() + " 定制");
        tvPreviews.setText(recommendInfo.getPreviews() + " 浏览");*/
    }
}

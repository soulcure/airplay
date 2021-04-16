package com.coocaa.tvpi.module.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.device.BindCodeMsg;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.service.BindCodeRepository;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.service.api.SmartDeviceConnectHelper;
import com.coocaa.tvpi.util.OnDebouncedClick;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.share.api.define.ShareObject;
import swaiotos.share.api.define.ShareType;

public class ShareActivity extends BaseActivity implements UnVirtualInputable {
    private static final String KEY_SHARE_TYPE = "type";//web、image
    private static final String KEY_SHARE_TEXT = "text";
    private static final String KEY_SHARE_IMAGE_BITMAP = "thumbBitmap";
    private static final String KEY_SHARE_WEB_URL = "url";
    private static final String KEY_SHARE_WEB_TITLE = "title";
    private static final String KEY_SHARE_WEB_THUMB = "thumb";
    private static final String KEY_SHARE_WEB_THUMB_RESID = "thumbResId";
    private static final String KEY_SHARE_WEB_DESCRIPTION = "description";
    private static final String KEY_SHARE_FROM = "from";

    private SHARE_MEDIA shareMedia = null;
    private MyShare share;


    public static void startShareUMImage(Context context,
                                         String shareText,
                                         Bitmap shareImageBitmap) {
        Intent starter = new Intent(context, ShareActivity.class);
        starter.putExtra(KEY_SHARE_TYPE, ShareType.IMAGE.toString());
        starter.putExtra(KEY_SHARE_TEXT, shareText);
        starter.putExtra(KEY_SHARE_IMAGE_BITMAP, shareImageBitmap);
        if(!(context instanceof Activity)) {
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(starter);
    }

    public static void startShareUMWeb(Context context,
                                       String shareText,
                                       String shareWebUrl,
                                       String shareWebTitle,
                                       String shareWebThumb,
                                       int shareWebThumbResId,
                                       String shareWebDescription) {
        Intent starter = new Intent(context, ShareActivity.class);
        starter.putExtra(KEY_SHARE_TYPE, ShareType.WEB.toString());
        starter.putExtra(KEY_SHARE_TEXT, shareText);
        starter.putExtra(KEY_SHARE_WEB_URL, shareWebUrl);
        starter.putExtra(KEY_SHARE_WEB_TITLE, shareWebTitle);
        starter.putExtra(KEY_SHARE_WEB_THUMB, shareWebThumb);
        if(shareWebThumbResId != 0) {
            starter.putExtra(KEY_SHARE_WEB_THUMB_RESID, shareWebThumbResId);
        }
        starter.putExtra(KEY_SHARE_WEB_DESCRIPTION, shareWebDescription);
        if(!(context instanceof Activity)) {
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "SmartShare";
        setContentView(R.layout.activity_share);
        overridePendingTransition(R.anim.push_bottom_in, 0);
        StatusBarHelper.translucent(this);
        parserIntent();
        initView();
        getBindCode();
    }

    private void parserIntent() {
        Uri uri = getIntent().getData();
        if(uri != null) {
            Log.d(TAG, "uri=" + uri.toString());
            String shareType = uri.getQueryParameter(KEY_SHARE_TYPE);
            share = createShare(shareType);
            share.setShareListener(listener);
            //print
            Log.d(TAG, "shareType=" + shareType);
            Log.d(TAG, "text=" + uri.getQueryParameter(KEY_SHARE_TEXT));
            Log.d(TAG, "url=" + uri.getQueryParameter(KEY_SHARE_WEB_URL));
            Log.d(TAG, "title=" + uri.getQueryParameter(KEY_SHARE_WEB_TITLE));
            Log.d(TAG, "description=" + uri.getQueryParameter(KEY_SHARE_WEB_DESCRIPTION));
            Log.d(TAG, "thumb=" + uri.getQueryParameter(KEY_SHARE_WEB_THUMB));
            Log.d(TAG, "from=" + uri.getQueryParameter(KEY_SHARE_FROM));

            share.setText(uri.getQueryParameter(KEY_SHARE_TEXT));
            share.setUrl(uri.getQueryParameter(KEY_SHARE_WEB_URL));
            share.setTitle(uri.getQueryParameter(KEY_SHARE_WEB_TITLE));
            share.setThumb(uri.getQueryParameter(KEY_SHARE_WEB_THUMB));
            share.setDescription(uri.getQueryParameter(KEY_SHARE_WEB_DESCRIPTION));
            share.setFrom(uri.getQueryParameter(KEY_SHARE_FROM));
            if(!TextUtils.isEmpty(uri.getQueryParameter(KEY_SHARE_WEB_THUMB_RESID))) {
                try {
                    share.setThumbResId(Integer.parseInt(uri.getQueryParameter(KEY_SHARE_WEB_THUMB_RESID)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (getIntent() != null) {
                String shareType = getIntent().getStringExtra(KEY_SHARE_TYPE);
                share = createShare(shareType);
                share.setShareListener(listener);
                if(!TextUtils.isEmpty(shareType)) {
                    share.setText(getIntent().getStringExtra(KEY_SHARE_TEXT));
                    share.setUrl(getIntent().getStringExtra(KEY_SHARE_WEB_URL));
                    share.setTitle(getIntent().getStringExtra(KEY_SHARE_WEB_TITLE));
                    share.setThumb(getIntent().getStringExtra(KEY_SHARE_WEB_THUMB));
                    share.setDescription(getIntent().getStringExtra(KEY_SHARE_WEB_DESCRIPTION));
                    share.setThumbResId(getIntent().getIntExtra(KEY_SHARE_WEB_THUMB_RESID, R.drawable.logo));
                    share.setFrom(getIntent().getStringExtra(KEY_SHARE_FROM));
                    if(ShareType.IMAGE.toString().equalsIgnoreCase(shareType)) {
                        byte[] buf = getIntent().getByteArrayExtra(KEY_SHARE_IMAGE_BITMAP);
                        if(buf != null) {
                            share.setThumbBitmap(BitmapFactory.decodeByteArray(buf, 0, buf.length));
                        }
                    }
                }
            }
        }
    }

    private UMShareListener listener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
            Log.d(TAG, "onStart : " + share_media.getName());
        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            Log.d(TAG, "onResult : " + share_media.getName());
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            Log.d(TAG, "onError : " + share_media.getName() + ", t=" + throwable);
            if(throwable != null)
                throwable.printStackTrace();
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            Log.d(TAG, "onCancel : " + share_media.getName());
        }
    };

    private MyShare createShare(String shareType) {
        if(TextUtils.isEmpty(shareType)) {
            return new WebShare(new ShareObject());
        }
        try {
            ShareType typeEm = ShareType.valueOf(shareType.toUpperCase());
            switch (typeEm) {
                case IMAGE:
                    return new ImageShare(new ShareObject());
                case WEB:
                    return new WebShare(new ShareObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new WebShare(new ShareObject());
    }

    private void initView() {
        View shareWeiXinLayout = findViewById(R.id.ll_share_weixin);
        View shareWeiXinCircleLayout = findViewById(R.id.ll_share_weixin_circle);
        View shareQQ = findViewById(R.id.ll_share_qq);
        View shareQzone = findViewById(R.id.ll_share_qzone);
        View rootLayout = findViewById(R.id.rl_root);
        View btCancel = findViewById(R.id.bt_cancel);
        shareWeiXinLayout.setOnClickListener(new OnDebouncedClick(shareClickListener));
        shareWeiXinCircleLayout.setOnClickListener(new OnDebouncedClick(shareClickListener));
        shareQQ.setOnClickListener(new OnDebouncedClick(shareClickListener));
        shareQzone.setOnClickListener(new OnDebouncedClick(shareClickListener));
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isFinishing()) {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.push_bottom_out);
    }

    private View.OnClickListener shareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.ll_share_weixin) {
                shareMedia = SHARE_MEDIA.WEIXIN;
            } else if (id == R.id.ll_share_weixin_circle) {
                shareMedia = SHARE_MEDIA.WEIXIN_CIRCLE;
            } else if (id == R.id.ll_share_qq) {
                shareMedia = SHARE_MEDIA.QQ;
            } else if (id == R.id.ll_share_qzone) {
                shareMedia = SHARE_MEDIA.QZONE;
            }
            umengShare();
        }
    };


    private void umengShare() {
        if (shareMedia == SHARE_MEDIA.WEIXIN || shareMedia == SHARE_MEDIA.WEIXIN_CIRCLE) {
            if(!UMShareAPI.get(this).isInstall(this, shareMedia)) {
                ToastUtils.getInstance().showGlobalShort("未安装微信");
                return;
            }
        }

        share.share(this, shareMedia);
    }


    private UMShareListener shareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtils.getInstance().showGlobalShort("分享成功了");
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.getInstance().showGlobalShort("分享失败了");
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            ToastUtils.getInstance().showGlobalShort("分享取消了");
        }
    };

    private void getBindCode() {
        if(!SSConnectManager.getInstance().isConnected())
            return ;
        Device device = SSConnectManager.getInstance().getDevice();
        if(device == null)
            return ;
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                IUserInfo userInfo = SmartApi.getUserInfo();
                Repository.get(BindCodeRepository.class)
                        .getBindCode(userInfo != null ? userInfo.accessToken : "", SmartDeviceConnectHelper.getDeviceActiveId(device), device.getSpaceId())
                        .setCallback(new RepositoryCallback.Default<BindCodeMsg>() {
                            @Override
                            public void onSuccess(BindCodeMsg success) {
                                Log.d(TAG, "getBindCode onSuccess : " + success);
                                if(!isFinishing() && success != null && share != null) {
                                    share.putExtra("bc", success.getBindCode());
                                }
                            }

                            @Override
                            public void onFailed(Throwable e) {
                                Log.d(TAG, "getBindCode onFailed : " + e.toString());
                            }
                        });
            }
        });
    }
}

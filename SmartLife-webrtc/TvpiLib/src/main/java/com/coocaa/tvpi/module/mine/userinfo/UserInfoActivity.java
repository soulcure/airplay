package com.coocaa.tvpi.module.mine.userinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mine.MineActivity;
import com.coocaa.tvpi.util.LogoutHelp;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;

/**
 * @author wuhaiyuan
 */
public class UserInfoActivity extends BaseViewModelActivity<UserInfoViewModel> implements UnVirtualInputable, SelectAvatarDFragment.OnAvatarSelectListener {

    private static final String TAG = UserInfoActivity.class.getSimpleName();
    private final int REQUEST_CODE_CAMERA = 1, REQUEST_CODE_ALBUM = 2, REQUEST_CODE_CROP_PIC = 3;
    public final int REQUEST_CODE_NICKNAME = 4;

    private Context mContext;
    private ImageView imgBack;
    private ImageView imgExit;
    private ImageView imgAvatar;
    private TextView tvName;
    private TextView tvPhoneNum;
    private ImageView imgRightArrow;

    SDialog exitDialog;
    private Uri mCaptureFileUri;
    private Bitmap mBitmap; //头像Bitmap
    private String fileName; //头像存储路径名称
    private Uri uritempFile; //android调用系统图片剪裁时，为兼容小米弄个临时uri
    SelectAvatarDFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_user_info);
        initView();
        initListener();
        initUIData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCpde: " + requestCode + ", resultCode: " + resultCode);

        switch (requestCode) {
            case REQUEST_CODE_NICKNAME:
                if (data != null)
                    tvName.setText(data.getStringExtra("NIKE_NAME"));
            case REQUEST_CODE_CAMERA: //拍照
                if (resultCode == RESULT_OK) {
                    Uri i = mCaptureFileUri;
                    if (data != null) {
                        i = data.getData();
                    }
                    cropPhoto(i);// 裁剪图片
                }
                break;
            case REQUEST_CODE_ALBUM: //从手机相册选择
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        cropPhoto(data.getData());// 裁剪图片
                    }
                }
                break;
            case REQUEST_CODE_CROP_PIC:
//                if (data != null) {
                    //将Uri图片转换为Bitmap
                    try {
                        mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream
                                (uritempFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //将裁剪的bitmap显示在imageview控件上
                    /*Bundle extras = data.getExtras();
                    mBitmap = extras.getParcelable("data");*/
                    if (mBitmap != null) {
                        setPicToView(mBitmap); // 保存在SD卡中
                        updateUserAvatar();
                    }
//                }
                break;
        }
    }

    @Override
    public void onAvatarSelect(int select) {
        if(select == 1){
            editAvatarFromCamera();
        }else if(select ==2){
            editAvatarFromAlbum();
        }
        dialogFragment.dismissDialog();
    }

    private void initView() {
        imgBack = findViewById(R.id.back_img);
        imgExit = findViewById(R.id.exit_img);
        imgAvatar = findViewById(R.id.mine_head_img);
        tvName = findViewById(R.id.user_nickname);
        tvPhoneNum = findViewById(R.id.user_phone_num);
        imgRightArrow = findViewById(R.id.icon_arrow);
    }

    private void initListener() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imgExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginOut();
            }
        });

        imgAvatar.setOnClickListener(avatarClickListener);
        tvName.setOnClickListener(editNameClickListener);
        imgRightArrow.setOnClickListener(editNameClickListener);
    }

    private void initUIData() {
        String phoneNum = viewModel.getUserPhoneNum();
        Log.d(TAG, "initUIData: phoneNum:" + phoneNum);
        if (!TextUtils.isEmpty(phoneNum)) {
            tvPhoneNum.setText(phoneNum);
        }

        String name = viewModel.getUserName();
        Log.d(TAG, "initUIData: name: " + name);
        if (!TextUtils.isEmpty(name)) {
            tvName.setText(name);
        } else {
            tvName.setText(phoneNum);
        }

        String avatar = viewModel.getUserAvatar();
        if (TextUtils.isEmpty(avatar)) {
            Glide.with(this).load(R.drawable.icon_mine_default_unhead).into(imgAvatar);
        } else if (avatar.startsWith("https")) {
            Glide.with(this).load(avatar).error(R.drawable.icon_mine_default_unhead).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(imgAvatar);
        } else if (avatar.startsWith("http")) {
            String avatar2 = avatar.replaceFirst("http", "https");
            Glide.with(this).load(avatar2).error(R.drawable.icon_mine_default_unhead).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(imgAvatar);
        }
    }

    View.OnClickListener avatarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PermissionsUtil.getInstance().requestPermission(mContext, new PermissionListener() {
                @Override
                public void permissionGranted(String[] permission) {
                    dialogFragment = new SelectAvatarDFragment();
                    dialogFragment.setOnAvatarSelectListener(UserInfoActivity.this);
                    dialogFragment.show(getSupportFragmentManager(), SelectAvatarDFragment.DIALOG_FRAGMENT_TAG);
                }

                @Override
                public void permissionDenied(String[] permission) {
                    ToastUtils.getInstance().showGlobalLong("请先设置授权酷开智屏文件读写权限");
                }
            }, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    };

    View.OnClickListener editNameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(UserInfoActivity.this, EditUserNameActivity.class);
            intent.putExtra("USER_NAME", tvName.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_NICKNAME);
        }
    };

    private void loginOut() {
        exitDialog = new SDialog(this, "退出登录", "退出后将无法使用共享屏",
                R.string.mine_login_out, R.string.cancel,
                new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean l, View view) {
                        if (l) {
                            LogoutHelp.logout();
                            Intent intent = new Intent(UserInfoActivity.this, MineActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            exitDialog.dismiss();
                        }
                    }
                });
        exitDialog.show();
    }

    /**
     * 调用相机拍照
     */
    private void editAvatarFromCamera() {
        //检查拍照权限
        PermissionsUtil.getInstance().requestPermission(mContext, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                initAvatarPath();
                File pictureFile = new File(fileName);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            mCaptureFileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID +
//                    ".provider", pictureFile);
                mCaptureFileUri = FileProvider.getUriForFile(mContext,
                        "com.coocaa.smartscreen.provider", pictureFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureFileUri);
                i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                Log.d(TAG, "pictureFile: " + pictureFile);
                Log.d(TAG, "fileUri: " + mCaptureFileUri);
                startActivityForResult(i, REQUEST_CODE_CAMERA);
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalLong("请先设置授权酷开智屏拍照权限");
            }
        },Manifest.permission.CAMERA);
    }

    /**
     * 调用相册选择头像
     */
    private void editAvatarFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }

    /**
     * 裁剪图片
     */
    public void cropPhoto(Uri uri) {
        Log.d(TAG, "cropPhoto: uri: " + uri);
        if (uri == null) {
            Log.e(TAG, "cropPhoto: uri is not exist");
            return;
        }
     /*   Intent intent = new Intent("com.android.camera.action.CROP");
        //需要加上这两句话  ： uri 权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        *//**
         * 此方法返回的图片只能是小图片（sumsang测试为高宽160px的图片）
         * 故只保存图片Uri，调用时将Uri转换为Bitmap，此方法还可解决miui系统不能return data的问题
         *//*
        //intent.putExtra("return-data", true);

        //裁剪后的图片Uri路径，uritempFile为Uri类变量
        uritempFile =
                Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, REQUEST_CODE_CROP_PIC);*/
        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
        Log.d(TAG, "cropPhoto: outputUri" +uritempFile);
        Intent intent = new Intent(this,CropImageActivity.class);
        intent.putExtra("inputUri",uri);
        intent.putExtra("outputUri",uritempFile);
        startActivityForResult(intent, REQUEST_CODE_CROP_PIC);
    }

    private void setPicToView(Bitmap mBitmap) {
        FileOutputStream b = null;
        initAvatarPath();
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initAvatarPath() {
        if (fileName == null) {
            String savePath = this.getExternalFilesDir("avatar").getAbsolutePath();
            fileName = savePath + "/avatar.jpg";
            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    private void updateUserAvatar() {
        byte[] input = null;
        FileInputStream in = null;
        initAvatarPath();
        try {
            File file = new File(fileName);
            in = new FileInputStream(file);
            input = new byte[in.available()];
            in.read(input);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String base64Avatar = Base64.encodeToString(input, Base64.DEFAULT);
        viewModel.updateUserAvatar(UserInfoCenter.getInstance().getAccessToken(), base64Avatar, "jpg")
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean success) {
                        if(success) {
                            imgAvatar.setImageBitmap(mBitmap); // 用ImageView显示出来
                        }
                    }
                });
    }

}
package com.coocaa.tvpi.module.mall.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


/**
 * @ClassName MallQuickStartDialog
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020-8-25
 * @Version TODO (write something)
 */
public class Save2AlbumDialog extends DialogFragment {

    private static final String TAG = Save2AlbumDialog.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_TAG = Save2AlbumDialog.class.getSimpleName();

    private AppCompatActivity mActivity;
    private View mLayout;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.bottom_dialog_anim);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mLayout = inflater.inflate(R.layout.save_2_album_dialog_layout, container);
        initViews();
        return mLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        // ??????dialog???layout
        if (getDialog() == null || getDialog().getWindow() == null || getActivity() == null) {
            return;
        }

        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        int dialogHeight = getContextRect(getActivity());
        //???????????????????????????
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight);
        //????????????
//        layoutParams.dimAmount = 0.0f;
        layoutParams.gravity = Gravity.START | Gravity.BOTTOM;
        getDialog().getWindow().setAttributes(layoutParams);

        StatusBarHelper.translucent(getActivity());
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
    public void onDestroy() {
        super.onDestroy();
    }

    //??????????????????
    private int getContextRect(Activity activity){
        //????????????
        Rect outRect1 = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        return outRect1.height();
    }


    public Save2AlbumDialog with(AppCompatActivity appCompatActivity) {
        mActivity = appCompatActivity;
        return this;
    }

    public void show() {
        this.show(mActivity.getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    public void dismissDialog() {
        Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismissAllowingStateLoss();
        }
    }

    private void initViews() {
        mLayout.findViewById(R.id.save_2_album_dismiss_root_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });
        mLayout.findViewById(R.id.save_2_album_root_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //??????????????????  ????????????dismiss dialog
            }
        });
        mLayout.findViewById(R.id.save_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mall_customer_service_qr);
                saveBmp2Gallery(bitmap, "customer_service_qr");
            }
        });
        mLayout.findViewById(R.id.cancel_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });
    }

    /**
     * @param bmp ?????????bitmap??????
     * @param picName ?????????????????????
     * */
    private void saveBmp2Gallery(Bitmap bmp, String picName) {

        String fileName = null;
        //??????????????????
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;


        // ??????????????????
        File file = null;
        // ???????????????
        FileOutputStream outStream = null;

        try {
            // ????????????????????????????????????????????????????????????????????????filename??????????????????
            file = new File(galleryPath, picName + ".jpg");

            // ????????????????????????
            fileName = file.toString();
            // ?????????????????????????????????????????????????????????
            outStream = new FileOutputStream(fileName);
            if (null != outStream) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            }

        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //???????????????????????????????????????
        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                bmp, fileName, null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        getActivity().sendBroadcast(intent);

        ToastUtils.getInstance().showGlobalShort("??????????????????");
        dismissDialog();
    }

}

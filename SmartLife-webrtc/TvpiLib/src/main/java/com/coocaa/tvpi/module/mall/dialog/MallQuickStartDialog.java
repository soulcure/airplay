package com.coocaa.tvpi.module.mall.dialog;

import android.Manifest;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.connection.ScanActivity2;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.mall.AddressListActivity;
import com.coocaa.tvpi.module.mall.CustomerServiceActivity;
import com.coocaa.tvpi.module.mall.MyOrderActivity;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

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
public class MallQuickStartDialog extends DialogFragment {

    private static final String TAG = MallQuickStartDialog.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_TAG = MallQuickStartDialog.class.getSimpleName();

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
//        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog);
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_black_a50)));
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().getAttributes().windowAnimations = R.style.top_dialog_anim;
        mLayout = inflater.inflate(R.layout.mall_quick_start_dialog_layout, container);
        initViews();
        return mLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置dialog的layout
        if (getDialog() == null || getDialog().getWindow() == null || getActivity() == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        layoutParams.gravity = Gravity.START | Gravity.TOP;
        getDialog().getWindow().setAttributes(layoutParams);

        StatusBarHelper.translucent(getDialog().getWindow());
        StatusBarHelper.setStatusBarLightMode(getDialog().getWindow());
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

    public MallQuickStartDialog with(AppCompatActivity appCompatActivity) {
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
        mLayout.findViewById(R.id.mall_quick_start_dismiss_root_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });
        mLayout.findViewById(R.id.mall_quick_start_root_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //不做任何事情  防止点击dismiss dialog
            }
        });
        mLayout.findViewById(R.id.quick_start_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });
        mLayout.findViewById(R.id.quick_start_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!LoginActivity.checkLogin(v.getContext())){return;}

                PermissionsUtil.getInstance().requestPermission(getContext(), new PermissionListener() {
                    @Override
                    public void permissionGranted(String[] permission) {
                        //扫一扫
                        ScanActivity2.start(getActivity());
                        dismissDialog();
                    }
                    @Override
                    public void permissionDenied(String[] permission) {
                        ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.request_camera_permission_tips));
                    }
                }, Manifest.permission.CAMERA);
            }
        });
        mLayout.findViewById(R.id.quick_start_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!LoginActivity.checkLogin(v.getContext())){return;}
                //订单
                MyOrderActivity.start(getActivity());
                dismissDialog();
            }
        });
        mLayout.findViewById(R.id.quick_start_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!LoginActivity.checkLogin(v.getContext())){return;}
                AddressListActivity.start(getContext());
                //地址
                dismissDialog();
            }
        });
        mLayout.findViewById(R.id.quick_start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //客服
                CustomerServiceActivity.start(getContext());
                dismissDialog();
            }
        });
    }

}

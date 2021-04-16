package swaiotos.channel.iot.tv.init;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmysun.ultrarecyclerview.GravityPagerSnapHelper;
import com.jimmysun.ultrarecyclerview.UltraRecyclerView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.common.utils.StringUtils;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.tv.R;
import swaiotos.channel.iot.tv.adapter.AutoAdapter;
import swaiotos.channel.iot.tv.base.MvpFragment;
import swaiotos.channel.iot.tv.init.devices.DevicesActivity;
import swaiotos.channel.iot.tv.init.devices.DevicesFragment;
import swaiotos.channel.iot.tv.view.ChangeTextSpaceView;
import swaiotos.channel.iot.utils.NetUtils;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class InitFragment extends MvpFragment<InitContract.Presenter> implements InitContract.View {

    private final String TAG = InitFragment.class.getSimpleName();
    private InitContract.Presenter mPresenter;
    private ImageView mBindQRCodeImageView;
    private ChangeTextSpaceView mBindCodeTextView;
    public  TextView mBindDeviceBtn;
    private TextView mIOTChannelTheme;
    private ImageView mVoalWhite,mVoalWhite40;
    private List<Device> devices;
    private int mCurrentPosition = 0 ;
    private int[] ids = {R.drawable.iot_channel_background_one,R.drawable.iot_channel_background_two};
//    private UltraRecyclerView mUltraRecyclerView;
    private ImageView mBindQRCodeProcessBar,mBindCodeTextProcessBar;
    private TextView mBindCodeError,mBindCodeReFlushBtn,mBindTextError;
    private RelativeLayout mBindCodeRelatvie;
    private RotateAnimation animation;
    private Toast mNotNetToast;

    public static InitFragment newInstance(String param1, String param2) {
        InitFragment fragment = new InitFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_init, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBindCodeRelatvie = view.findViewById(R.id.iot_channel_bind_relative);
        mBindQRCodeImageView = view.findViewById(R.id.iot_channel_bind_Qrcode);
        mBindQRCodeProcessBar = view.findViewById(R.id.iot_channel_bind_code_imageView);
        mBindCodeError = view.findViewById(R.id.iot_channel_bind_code_error);
        mBindCodeReFlushBtn = view.findViewById(R.id.iot_channel_bind_code_re_flush);

        mBindCodeTextView = view.findViewById(R.id.iot_channel_bind_code);
        mBindCodeTextProcessBar = view.findViewById(R.id.iot_channel_bind_text_imageView);
        mBindTextError = view.findViewById(R.id.iot_channel_bind_text_error);

        mBindCodeTextView.setSpacing(getResources().getDimension(R.dimen.px_6));
        mBindDeviceBtn = view.findViewById(R.id.iot_channel_btn_devices);

        mIOTChannelTheme = view.findViewById(R.id.iot_channel_theme);
        mVoalWhite = view.findViewById(R.id.iot_channel_oval_white);
        mVoalWhite40 = view.findViewById(R.id.iot_channel_oval_white_40);
//        mUltraRecyclerView = view.findViewById(R.id.ultra_recycler_view);

        setupRecyclerView();

        mBindDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(),DevicesActivity.class);
                getActivity().startActivity(intent);
            }
        });

        mBindCodeReFlushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetUtils.isConnected(Objects.requireNonNull(getActivity()))) {
                    if (mNotNetToast != null) {
                        mNotNetToast.cancel();
                    }
                    mNotNetToast = Toast.makeText(getContext(),"网络连接出现异常，请检查网络!",Toast.LENGTH_SHORT);
                    mNotNetToast.show();
                    return;
                }
                loadingUI();
                initData();
            }
        });

    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"------------onActivityCreated-------------");

    }

    private void initData() {
        InitContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.init(getActivity().getApplicationContext());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!NetUtils.isConnected(Objects.requireNonNull(getActivity()))) {
            Toast.makeText(getContext(),"网络连接出现异常，请检查网络!",Toast.LENGTH_SHORT).show();
            loadingErrorUI();
            return;
        }
        mPresenter = new InitPresenter(this);
        loadingUI();
        initData();
    }

    private void loadingUI() {
        mBindCodeRelatvie.setBackgroundColor(getResources().getColor(R.color.white_20));
        mBindQRCodeImageView.setImageBitmap(null);
        mBindQRCodeProcessBar.setVisibility(View.VISIBLE);
        mBindCodeTextProcessBar.setVisibility(View.VISIBLE);
        startAnimation();

        mBindCodeError.setVisibility(View.INVISIBLE);
        mBindCodeReFlushBtn.setVisibility(View.INVISIBLE);
        mBindTextError.setVisibility(View.INVISIBLE);
        mBindDeviceBtn.setVisibility(View.INVISIBLE);
        mBindCodeTextView.setVisibility(View.INVISIBLE);
        Log.d(TAG,"2-----");
    }

    private void loadingErrorUI() {
        mBindQRCodeImageView.setImageBitmap(null);

        stopAnimation();
        mBindQRCodeProcessBar.clearAnimation();
        mBindCodeTextProcessBar.clearAnimation();

        mBindQRCodeProcessBar.setVisibility(View.INVISIBLE);
        mBindCodeTextProcessBar.setVisibility(View.INVISIBLE);
        mBindCodeError.setVisibility(View.VISIBLE);
        mBindCodeTextView.setVisibility(View.INVISIBLE);
        mBindCodeReFlushBtn.setVisibility(View.VISIBLE);
        mBindTextError.setVisibility(View.VISIBLE);
        mBindDeviceBtn.setVisibility(View.INVISIBLE);

        mBindCodeReFlushBtn.requestFocus();
        Log.d(TAG,"0-----");
    }

    private void loadingSuccessUI(final Bitmap bitmap,final String qrcodeInfo) {
        if (bitmap != null ) {
            stopAnimation();
            mBindQRCodeProcessBar.clearAnimation();
            mBindCodeTextProcessBar.clearAnimation();
            mBindQRCodeProcessBar.setVisibility(View.GONE);
            mBindCodeTextProcessBar.setVisibility(View.GONE);

            mBindQRCodeImageView.setImageBitmap(bitmap);
            mBindCodeRelatvie.setBackgroundColor(getResources().getColor(R.color.white));
            mBindCodeTextView.setText(qrcodeInfo);
        }

        mBindCodeError.setVisibility(View.INVISIBLE);
        mBindCodeReFlushBtn.setVisibility(View.INVISIBLE);
        mBindTextError.setVisibility(View.INVISIBLE);
        mBindCodeTextView.setVisibility(View.VISIBLE);
        if (devices != null ) {
            if (devices.size() > 0) {
                mBindDeviceBtn.setVisibility(View.VISIBLE);
                mBindDeviceBtn.requestFocus();
                mBindDeviceBtn.setText(String.format(getResources().getString(R.string.iot_channel_devices),""+devices.size()));
            } else {
                mBindDeviceBtn.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void startAnimation() {
        if (animation == null) {
            animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            /**设置旋转一圈所需时间**/
            animation.setDuration(1000);
            /**设置旋转次数，近乎无限次**/
            animation.setRepeatCount(Integer.MAX_VALUE);
            /**设置旋转无停顿**/
            animation.setInterpolator(new LinearInterpolator());
            animation.setFillAfter(true);
        }

        mBindQRCodeProcessBar.startAnimation(animation);
        mBindCodeTextProcessBar.startAnimation(animation);

    }

    /**结束旋转**/
    private void stopAnimation(){
        if(animation != null){
            animation.cancel();
            animation = null;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void setPresenter(InitContract.Presenter presenter) {
        this.mPresenter = presenter;
    }


    @Override
    public boolean isActive() {
        return getActivity() != null && !getActivity().isFinishing() &&  isAdded();
    }

    @Override
    protected InitContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void onUnBindCallBack(String sid) {
        Log.d(TAG,"--------OnUnBindCallBack------");

    }

    @Override
    public void reflushOrUpdateQRCode(final String qrcodeInfo, final String url,String qrcodeExpire) {

        if (!TextUtils.isEmpty(qrcodeInfo)) {
            Log.d(TAG,"----reflushOrUpdateQRCode---:"+qrcodeInfo);
            Bitmap bitmap = CodeUtils.createImage(PublicParametersUtils.getURLAndBindCode(qrcodeInfo),
                    (int)getResources().getDimension(R.dimen.px_256), (int)getResources().getDimension(R.dimen.px_256), null);
            loadingSuccessUI(bitmap,qrcodeInfo);
            Log.d(TAG,"----reflushOrUpdateQRCode---end");
        }
    }

    @Override
    public void refreshTips(final int type) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == 1)
                    Toast.makeText(getContext(),getResources().getString(R.string.iot_channel_bind_success),Toast.LENGTH_LONG).show();
                else if (type == 3) {
                    Toast.makeText(getContext(),"绑定服务失败，尝试退出应用在进入",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void triggerQueryDevices(final List<Device> devices,final int type) {
        this.devices = devices;
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @SuppressLint("StringFormatMatches")
            @Override
            public void run() {
                if (devices != null) {
                    loadingSuccessUI(null,null);
                }
                if (type == 1) {
                    startAuto();
                }
            }
        });
    }

    @Override
    public void refreshErrorUI() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingErrorUI();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"------------onPause-------------");
        if (mPresenter != null)
            mPresenter.detachView();
    }

    private void setupRecyclerView() {
//        AutoAdapter adAdapter = new AutoAdapter(getContext().getApplicationContext(), ids);
//        mUltraRecyclerView.setAdapter(adAdapter);
//        // set orientation
//        mUltraRecyclerView.setOrientation(RecyclerView.VERTICAL);
//        // set pager snap, including align gravity and margin
//        mUltraRecyclerView.setPagerSnap(Gravity.START, 0);
//        // set an infinite loop
//        mUltraRecyclerView.setInfiniteLoop(true);
    }


    private void reflushIndexLayout() {
        if (mCurrentPosition%ids.length == 0) {
            mIOTChannelTheme.setText(getResources().getString(R.string.iot_channel_theme_xiaowei));
            mVoalWhite.setBackground(getResources().getDrawable(R.drawable.shape_val_white));
            mVoalWhite40.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
        } else {
            mIOTChannelTheme.setText(getResources().getString(R.string.iot_channel_theme_swaiotos));
            mVoalWhite.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
            mVoalWhite40.setBackground(getResources().getDrawable(R.drawable.shape_val_white));
        }
    }

    private void startAuto() {
        // start auto-scroll
//        mUltraRecyclerView.startAutoScroll(7000);
//        mUltraRecyclerView.setAutoScrollSpeed(1000);
//        mUltraRecyclerView.setOnSnapListener(new GravityPagerSnapHelper.OnSnapListener() {
//            @Override
//            public void onSnap(int position) {
//                mCurrentPosition = position;
//                reflushIndexLayout();
//            }
//        });

    }

}

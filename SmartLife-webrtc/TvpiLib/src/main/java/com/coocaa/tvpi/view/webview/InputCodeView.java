package com.coocaa.tvpi.view.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils;
import com.coocaa.tvpi.module.connection.adapter.BindCodeAdapter;
import com.coocaa.tvpilib.R;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName InputCodeView
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 1/22/21
 * @Version TODO (write something)
 */
public class InputCodeView extends LinearLayout {

    private String TAG = InputCodeView.class.getSimpleName();

    private Context context;
    private RecyclerView recyclerView;
    private BindCodeAdapter bindCodeAdapter;
    private List<Button> btnList = new ArrayList<>();
    private Button btnConnect;
    private ImageView imgBackDelete;
    private InputCallback inputCallback;

    public InputCodeView(Context context) {
        this(context, null);
    }

    public InputCodeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputCodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public InputCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initData();
        initView();
        initListener();
    }

    /**
     * 回调
     */
    public interface InputCallback {
        /**
         * 连接按钮被点击
         *
         * @param code
         */
        void onConnectBtnClick(String code);
    }

    private void initData() {
        bindCodeAdapter = new BindCodeAdapter();
    }


    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.input_code_view, this, true);
        imgBackDelete = view.findViewById(R.id.back_delete_img);
        recyclerView = view.findViewById(R.id.rl_bind_code);
        btnList.add(view.findViewById(R.id.btn_num_zero));
        btnList.add(view.findViewById(R.id.btn_num_one));
        btnList.add(view.findViewById(R.id.btn_num_two));
        btnList.add(view.findViewById(R.id.btn_num_three));
        btnList.add(view.findViewById(R.id.btn_num_four));
        btnList.add(view.findViewById(R.id.btn_num_five));
        btnList.add(view.findViewById(R.id.btn_num_six));
        btnList.add(view.findViewById(R.id.btn_num_seven));
        btnList.add(view.findViewById(R.id.btn_num_eight));
        btnList.add(view.findViewById(R.id.btn_num_nine));
        btnConnect = view.findViewById(R.id.btn_connect);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(bindCodeAdapter);
    }


    private void initListener() {
        for (Button btn : btnList) {
            btn.setOnClickListener(listener);
        }
        btnConnect.setOnClickListener(connectListener);
        imgBackDelete.setOnClickListener(backDeleteListener);
    }

    private View.OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: " + v.getTag().toString());
            bindCodeAdapter.addBindCode(v.getTag().toString());
            if (bindCodeAdapter.getLength() == 8) {
                btnConnect.setBackgroundResource(R.drawable.bg_blue_round_6);
                btnConnect.setTextColor(getResources().getColor(R.color.white));
            }
            imgBackDelete.setVisibility(VISIBLE);
            VirtualInputUtils.playVibrateOneShot();
        }
    };

    private View.OnClickListener backDeleteListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            bindCodeAdapter.deleteBindCode();
            btnConnect.setBackgroundResource(R.drawable.bg_white10_round_6);
            btnConnect.setTextColor(getResources().getColor(R.color.color_white_40));
            if (bindCodeAdapter.getLength() <= 0) {
                imgBackDelete.setVisibility(GONE);
            }
        }
    };

    private View.OnClickListener connectListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: " + bindCodeAdapter.getBindCode());
            if (bindCodeAdapter.getLength() >= 8) {
                if (inputCallback != null) {
                    inputCallback.onConnectBtnClick(bindCodeAdapter.getBindCode());
                }
            } else {
                ToastUtils.getInstance().showGlobalShort("请输入8位数字码");
            }
        }
    };

    public void setInputCallback(InputCallback inputCallback) {
        this.inputCallback = inputCallback;
    }
}

package com.coocaa.tvpi.view.commondialog;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IceStorm on 2018/1/12.
 */

public class CommonDialogFragment extends DialogFragment implements View.OnClickListener{

    private static final String TAG = "CommonDialogFragment";

    private final static String COMMON_DIALOG_SERIALIZE_KEY = "COMMON_DIALOG_SERIALIZE_KEY";
    public final static String DIALOG_FRAGMENT_TAG = "DIALOG_FRAGMENT_TAG";

    private ListView listView;

    // 选项内容
    private List<CommonModel> commonModels;

    private CallBack mCallBack;

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    public interface CallBack {
        void getSelectedIndex(int selectedIndex);
    }

    public static CommonDialogFragment getCommonInstance(List<CommonModel> demoList) {
        CommonDialogFragment dialogFragmentTest = new CommonDialogFragment();
        Bundle b = new Bundle();

        b.putSerializable(COMMON_DIALOG_SERIALIZE_KEY, (Serializable) demoList);
        dialogFragmentTest.setArguments(b);

        return dialogFragmentTest;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置dialog的layout
        DisplayMetrics dm = new DisplayMetrics();

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();

        layoutParams.width = dm.widthPixels;
        layoutParams.height = layoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes(layoutParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog);

        View view = inflater.inflate(R.layout.fragment_common_dialog, container);
        listView =  view.findViewById(R.id.fragment_common_dialog_listview);
        handleArgs();

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        return view;
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

    private void handleArgs() {
        Bundle bundle = getArguments();
        if(bundle == null) {
            return;
        }

        List tempList = (List) bundle.getSerializable(COMMON_DIALOG_SERIALIZE_KEY);

        if(tempList.size() > 0) {
            Object o = tempList.get(0);
            CommonDialogAdapter payDialogAdapter = new CommonDialogAdapter(getActivity());
            listView.setAdapter(payDialogAdapter);

            if(o instanceof CommonModel) {
                commonModels = (List<CommonModel>) bundle.getSerializable(COMMON_DIALOG_SERIALIZE_KEY);
                payDialogAdapter.setData((commonModels));
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mCallBack.getSelectedIndex(position);
                    dismiss();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.cancel_dialog:
//                dismissDialog();
//                break;
//            default:
//        }
    }

    public void dismissDialog() {
        android.app.Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }
}

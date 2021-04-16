package com.coocaa.tvpi.test;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coocaa.publib.data.channel.AppStoreParams;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.R;
import com.coocaa.smartscreen.utils.CmdUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName AppTestAdapter
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/9
 * @Version TODO (write something)
 */
public class AppTestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    String TAG = AppTestAdapter.class.getSimpleName();

    private List<String> dataList;

    public void addAll(List<String> list) {
        dataList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(com.coocaa.tvpilib.R.layout
                .app_test_item, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        ((Holder) holder).onBind(AppStoreParams.CMD.values()[position].toString());
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + dataList.size());
        return dataList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
;
        private TextView textView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "Holder: ");
            textView = (TextView) itemView.findViewById(R.id.cmd_tv);
        }

        public void onBind(String s) {
            Log.d(TAG, "onBind: " + s);
            textView.setText(s);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppStoreParams appStoreParams = new AppStoreParams();
                    appStoreParams.appId = "26436";
                    appStoreParams.mainACtivity = "com.xiaodianshi.tv.yst.ui.splash.SplashActivity";
                    appStoreParams.pkgName = "com.xiaodianshi.tv.yst";
                    CmdUtil.sendAppCmd(s, appStoreParams.toJson());
                }
            });
        }
    }
}

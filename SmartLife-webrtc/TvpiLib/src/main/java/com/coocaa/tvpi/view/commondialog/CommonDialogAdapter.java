package com.coocaa.tvpi.view.commondialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IceStorm on 2018/1/12.
 */

public class CommonDialogAdapter extends BaseAdapter {
    private static final String TAG = "CommonDialogAdapter";

    private List<CommonModel> mDatas;

    private Context mContext;

    public CommonDialogAdapter(Context context) {
        mContext = context;

        mDatas = new ArrayList<CommonModel>();

    }

    public void setData(List<CommonModel> titles) {
        mDatas = titles;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mDatas == null)
            return 0;

        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        CommonDialogAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new CommonDialogAdapter.ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_common_dialog, null);

            holder.iv = convertView.findViewById(R.id.item_common_dialog_iv);
            holder.tv = convertView.findViewById(R.id.item_common_dialog_tv);

            convertView.setTag(holder);
        } else {
            holder = (CommonDialogAdapter.ViewHolder) convertView.getTag();
        }

        try {
            CommonModel item = mDatas.get(position);
            holder.setData(item);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public class ViewHolder {
        public ImageView iv;
        public TextView tv;

        public void setData(CommonModel model) {
            tv.setText(model.title);
            if (model.colorResourceId > 0)
                tv.setTextColor(mContext.getResources().getColor(model.colorResourceId));
            if (model.iconResourceId > 0) {
                iv.setVisibility(View.VISIBLE);
                GlideApp.with(mContext).load(model.iconResourceId).into(iv);
                return;
            } else {
                iv.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(model.iconUrl)) {
                iv.setVisibility(View.VISIBLE);
                GlideApp.with(mContext).load(model.iconUrl).into(iv);
                return;
            } else {
                iv.setVisibility(View.GONE);
            }
        }
    }
}

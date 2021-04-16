package com.coocaa.tvpi.module.homepager.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.module.app.AppDetailActivity;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppHolder> {
    private static final String TAG = AppAdapter.class.getSimpleName();

    private Context context;
    private List<TvAppModel> data = new ArrayList<>();

    public AppAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<TvAppModel> tvAppModels) {
        if (tvAppModels != null) {
            this.data = tvAppModels;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public AppHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_smartscreen_app, parent, false);
        return new AppHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppHolder holder, int position) {
        TvAppModel tvAppModel = data.get(position);
        holder.tvName.setText(tvAppModel.appName);
        if (TextUtils.isEmpty(tvAppModel.coverUrl)) {
            getAppDetail(tvAppModel, holder.getAdapterPosition());
        } else {
            GlideApp.with(context)
                    .load(tvAppModel.coverUrl)
                    .error(R.drawable.place_holder_app)
                    .placeholder(R.drawable.place_holder_app)
                    .into(holder.ivCover);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDetailActivity.start(context, new AppModel(tvAppModel));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class AppHolder extends RecyclerView.ViewHolder {
        ConstraintLayout root;
        ImageView ivCover;
        TextView tvName;

        public AppHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvName = itemView.findViewById(R.id.tvName);
            ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
            layoutParams.width = (int) ((DimensUtils.getDeviceWidth(context) - DimensUtils.dp2Px(context, 50)) / 3f);
        }
    }

    public void getAppDetail(final TvAppModel tvAppModel, final int position) {
        Repository.get(AppRepository.class)
                .getAppDetail(tvAppModel.pkgName)
                .setCallback(new BaseRepositoryCallback<AppModel>() {
                    @Override
                    public void onSuccess(AppModel appModel) {
                        if (!TextUtils.isEmpty(appModel.appNewTvIcon)) {
                            tvAppModel.coverUrl = appModel.appNewTvIcon;
                        } else {
                            tvAppModel.coverUrl = "no url";
                        }
                        notifyItemChanged(position);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        Log.d(TAG, "getAppDetail onFailed:  " + e.toString());
                        tvAppModel.coverUrl = "no url";
                        notifyItemChanged(position);
                    }
                });
    }
}

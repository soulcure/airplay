package swaiotos.channel.iot.tv.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import swaiotos.channel.iot.tv.R;

/**
 *
 *
 */
public class AutoAdapter extends RecyclerView.Adapter<AutoAdapter.AdViewHolder> {
    private Context mContext;
    private long between = 0;
    private int[] ids;
    public AutoAdapter(Context context,int[] ids) {
        mContext = context;
        this.ids = ids;
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_auto, parent, false);
        return new AdViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdViewHolder holder,  int position) {

        if (ids.length != 0) {
            int drawableId = ids[position% ids.length];
            holder.itemView.setBackground(mContext.getResources().getDrawable(drawableId));
        }
    }


    @Override
    public int getItemCount() {

        return Integer.MAX_VALUE;
    }

    class AdViewHolder extends RecyclerView.ViewHolder {

        private AdViewHolder(View itemView) {
            super(itemView);

        }
    }

    @Override
    public void onViewRecycled(AdViewHolder holder) {
        super.onViewRecycled(holder);
    }
}

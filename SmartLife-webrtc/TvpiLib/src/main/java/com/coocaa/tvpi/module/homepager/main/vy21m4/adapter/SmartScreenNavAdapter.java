package com.coocaa.tvpi.module.homepager.main.vy21m4.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.swaiotos.virtualinput.module.adapter.ControlNavigatorAdapter;
import com.coocaa.tvpi.view.CustomPagerTitleView;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

import java.util.List;

public class SmartScreenNavAdapter extends CommonNavigatorAdapter {

    private List<Fragment> fragmentList;
    private List<String> titleList;
    private Context context;

    public SmartScreenNavAdapter(List<Fragment> fragmentList, List<String> titleList, Context context) {
        this.fragmentList = fragmentList;
        this.titleList = titleList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public IPagerTitleView getTitleView(Context context, int index) {
        CustomPagerTitleView itemView = new CustomPagerTitleView(context);
        itemView.setText(titleList.get(index));
//        switch (index) {
//            case 0:
//                itemView.setText("共享空间");
//                break;
//            case 1:
//                itemView.setText("影音娱乐");
//                break;
//            case 2:
//                itemView.setText("互动游戏");
//                break;
//            default:
//                break;
//        }
        itemView.setNormalColor(Color.parseColor("#66000000"));
        itemView.setSelectedColor(Color.parseColor("#FF000000"));
        itemView.setTextSize(16);
        itemView.setSelectedBold(true);
        itemView.setPadding(DimensUtils.dp2Px(context,20),
                DimensUtils.dp2Px(context,0),
                DimensUtils.dp2Px(context,20),
                DimensUtils.dp2Px(context,7));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(index);
            }
        });
        return itemView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        LinePagerIndicator indicator = new LinePagerIndicator(context);
        indicator.setColors(Color.parseColor("#FF3181FE"));
        indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
        indicator.setLineWidth(DimensUtils.dp2Px(context,20));
        indicator.setRoundRadius(DimensUtils.dp2Px(context,4));
        return indicator;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}

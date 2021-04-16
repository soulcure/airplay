package com.coocaa.statemanager.view.widget;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.coocaa.statemanager.R;
import com.coocaa.statemanager.view.UiUtil;

import swaiotos.channel.iot.utils.EmptyUtils;

/**
 * Describe:显示连接码
 * Created by AwenZeng on 2021/01/20
 */
public class ConnectCodeView extends LinearLayout {

    private final int[] mNumberResIds = {
            R.drawable.number_0,
            R.drawable.number_1,
            R.drawable.number_2,
            R.drawable.number_3,
            R.drawable.number_4,
            R.drawable.number_5,
            R.drawable.number_6,
            R.drawable.number_7,
            R.drawable.number_8,
            R.drawable.number_9
    };

    public ConnectCodeView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        for(int i = 0;i<8;i++){
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(UiUtil.div(48), UiUtil.div(80));
            if(i != 0){
                itemParam.leftMargin = UiUtil.div(24);
            }
            addView(imageView,itemParam);
        }
    }

    /**
     * 显示连接码
     * @param qrCode
     */
    public void showQRCode(String qrCode){
        if(EmptyUtils.isNotEmpty(qrCode)){
            char[] qrCodeArray = qrCode.toCharArray();
            for(int i = 0;i<qrCodeArray.length;i++){
                ((ImageView)getChildAt(i)).setImageResource(mNumberResIds[Integer.parseInt(qrCodeArray[i]+"")]);
            }
        }
    }
}

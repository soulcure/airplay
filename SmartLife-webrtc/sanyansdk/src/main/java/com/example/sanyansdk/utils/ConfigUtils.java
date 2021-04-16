package com.example.sanyansdk.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chuanglan.shanyan_sdk.OneKeyLoginManager;
import com.chuanglan.shanyan_sdk.listener.ShanYanCustomInterface;
import com.chuanglan.shanyan_sdk.tool.ShanYanUIConfig;
import com.example.sanyansdk.R;
import com.example.sanyansdk.SanYanManager;


public class ConfigUtils {

    //沉浸式竖屏样式
    public static ShanYanUIConfig getCJSConfig(final Context context, final SanYanManager.LoginAuthResult authResult) {
        //本机号码一键登录的百分比高度，以这个view的位置计算，其他手机号登录按钮和隐私view的位置
        float btnPercent = 0.34f;
        /************************************************自定义控件**************************************************************/
        Drawable logBtnImgPath = context.getResources().getDrawable(R.drawable.shanyan_demo_auth_bt);
        Drawable backgruond = context.getResources().getDrawable(R.drawable.shanyan_demo_auth_no_bg);
        Drawable returnBg = context.getResources().getDrawable(R.drawable.icon_close);
        Drawable checkBoxImgPath = context.getResources().getDrawable(R.drawable.shanyan_checkbox_checked);
        Drawable unCheckBoxImgPath = context.getResources().getDrawable(R.drawable.shanyan_checkbox_normal);

        //其他手机号码登录
        LayoutInflater numberinflater = LayoutInflater.from(context);
        RelativeLayout numberLayout = (RelativeLayout) numberinflater.inflate(R.layout.shanyan_demo_phobackground, null);
        RelativeLayout.LayoutParams numberParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numberParams.setMargins(0, (int) (AbScreenUtils.getScreenHeight(context,false)*btnPercent)+AbScreenUtils.dp2px(context,50)+AbScreenUtils.dp2px(context,10), 0, 0);
        numberParams.width = (int) (AbScreenUtils.getScreenWidth(context, false) *0.89);
        numberParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        numberLayout.setLayoutParams(numberParams);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#00000000"));
        toast.setView(view);

        ShanYanUIConfig uiConfig = new ShanYanUIConfig.Builder()
                .setActivityTranslateAnim("shanyan_demo_bottom_in_anim", "shanyan_demo_bottom_out_anim")
                //授权页导航栏：
                .setNavColor(Color.parseColor("#ffffff"))  //设置导航栏颜色
                .setNavText("")  //设置导航栏标题文字
                .setNavReturnBtnWidth(40)
                .setNavReturnBtnHeight(40)
                .setAuthBGImgPath(backgruond)
                .setLogoHidden(true)   //是否隐藏logo
                .setDialogDimAmount(0.6f)
                .setNavReturnImgPath(returnBg)
                .setFullScreen(false)
                .setStatusBarHidden(false)

                //状态栏设置
                .setStatusBarColor(Color.parseColor("#ffffff"))
                .setLightColor(false)
                .setStatusBarHidden(false)
                .setVirtualKeyTransparent(true)

                //授权页号码栏：
                .setNumberColor(Color.parseColor("#000000"))  //设置手机号码字体颜色
                .setNumFieldOffsetY((int) (AbScreenUtils.getScreenHeight(context,true)*0.15))
                .setNumberSize(30)
                .setNumFieldHeight(50)

                //运营商slogan
                .setSloganTextColor(Color.parseColor("#66000000"))
                .setSloganTextSize(14)
                .setSloganOffsetY((int) (AbScreenUtils.getScreenHeight(context,true)*0.15)+50)

                //授权页登录按钮：
                .setLogBtnText("本机号码一键登录")  //设置登录按钮文字
                .setLogBtnTextColor(0xffffffff)   //设置登录按钮文字颜色
                .setLogBtnImgPath(logBtnImgPath)   //设置登录按钮图片
                .setLogBtnTextSize(14)
                .setLogBtnHeight(50)
                //根据UI图片比例算出
                .setLogBtnOffsetY((int) (AbScreenUtils.getScreenHeight(context,true)*btnPercent))
                .setLogBtnWidth((int) (AbScreenUtils.getScreenWidth(context, true) *0.89))

                //授权页隐私栏：
                .setAppPrivacyOne("用户协议", "http://sky.fs.skysrt.com/statics/server/kkzp_service.html")  //设置开发者隐私条款1名称和URL(名称，url)
                .setAppPrivacyTwo("隐私条款", "http://sky.fs.skysrt.com/statics/server/kkzp_privacy.html")  //设置开发者隐私条款1名称和URL(名称，url)
                .setAppPrivacyColor(Color.parseColor("#66000000"), Color.parseColor("#FF5525"))    //	设置隐私条款名称颜色(基础文字颜色，协议文字颜色)
                .setPrivacyText("我已阅读并同意：", "", "以及", "", "")
                .setPrivacyOffsetGravityLeft(true)
                .setOperatorPrivacyAtLast(true)
                .setPrivacyState(false)
//                .setPrivacyOffsetBottomY(20)//设置隐私条款相对于屏幕下边缘y偏
                .setPrivacyOffsetY((int) (AbScreenUtils.getScreenHeight(context,true)*btnPercent) + 130)
                .setPrivacyOffsetX((int) (AbScreenUtils.getScreenWidth(context,true)*0.11f / 2))
                .setPrivacyTextSize(14)
                .setCheckBoxWH(20,20)
                .setSloganHidden(false)
                .setCheckBoxHidden(false)
                .setPrivacyCustomToast(toast)
                .setcheckBoxOffsetXY(0,0)
                .setCheckedImgPath(checkBoxImgPath)
                .setUncheckedImgPath(unCheckBoxImgPath)
                .setShanYanSloganTextColor(Color.parseColor("#ffffff"))
                .addCustomView(numberLayout, true, false, new ShanYanCustomInterface() {
                    @Override
                    public void onClick(Context context, View view) {
                        authResult.onCustomClick(context,view);
                    }
                })
                .build();
        return uiConfig;

    }

//
//    //沉浸式横屏样式设置
//    public static ShanYanUIConfig getCJSLandscapeUiConfig(final Context context) {
//
//        //其他方式登录
//        LayoutInflater inflater1 = LayoutInflater.from(context);
//        RelativeLayout relativeLayout = (RelativeLayout) inflater1.inflate(R.layout.shanyan_demo_other_login_item, null);
//        RelativeLayout.LayoutParams layoutParamsOther = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        layoutParamsOther.setMargins(0, 0, 0, AbScreenUtils.dp2px(context, 50));
//        layoutParamsOther.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        layoutParamsOther.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        relativeLayout.setLayoutParams(layoutParamsOther);
//
//
//        //号码栏背景
//        LayoutInflater numberinflater = LayoutInflater.from(context);
//        RelativeLayout numberLayout = (RelativeLayout) numberinflater.inflate(R.layout.shanyan_demo_phobackground, null);
//        RelativeLayout.LayoutParams numberParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        numberParams.setMargins(0, 0, 0, AbScreenUtils.dp2px(context, 200));
//        numberParams.width = AbScreenUtils.dp2px(context, 330);
//        numberParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        numberParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        numberLayout.setLayoutParams(numberParams);
//
//        LayoutInflater privacyInflater = LayoutInflater.from(context);
//        final RelativeLayout privacyLayout = (RelativeLayout) privacyInflater.inflate(R.layout.shanyan_demo_dialog_privacy_land, null);
//        RelativeLayout.LayoutParams privacyLayoutLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        privacyLayout.setLayoutParams(privacyLayoutLayoutParams);
//        privacyLayout.findViewById(R.id.shanyan_demo_privace_cancel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                privacyLayout.setVisibility(View.GONE);
//            }
//        });
//        privacyLayout.findViewById(R.id.shanyan_demo_privacy_ensure).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                OneKeyLoginManager.getInstance().setCheckBoxValue(true);
//                privacyLayout.setVisibility(View.GONE);
//            }
//        });
//        //设置授权页固有控件
//        Drawable navReturnImgPath = context.getResources().getDrawable(R.drawable.shanyan_demo_return_left_bg);
//        Drawable logBtnImgPath = context.getResources().getDrawable(R.drawable.shanyan_demo_auth_bt);
//        Drawable uncheckedImgPath = context.getResources().getDrawable(R.drawable.shanyan_demo_uncheck_image);
//        Drawable checkedImgPath = context.getResources().getDrawable(R.drawable.shanyan_demo_check_image);
//        Drawable backgruond = context.getResources().getDrawable(R.drawable.shanyan_demo_auth_no_bg);
//
//        //loading自定义加载框
//        LayoutInflater inflater = LayoutInflater.from(context);
//        RelativeLayout view_dialog = (RelativeLayout) inflater.inflate(R.layout.shanyan_demo_dialog_layout, null);
//        RelativeLayout.LayoutParams mLayoutParams3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        view_dialog.setLayoutParams(mLayoutParams3);
//        view_dialog.setVisibility(View.GONE);
//        ShanYanUIConfig uiConfig = new ShanYanUIConfig.Builder()
//                .setActivityTranslateAnim("shanyan_demo_fade_in_anim", "shanyan_dmeo_fade_out_anim")
//                //授权页导航栏：
//                .setNavColor(Color.parseColor("#ffffff"))  //设置导航栏颜色
//                .setNavText("")  //设置导航栏标题文字
//                .setNavTextColor(0xff080808) //设置标题栏文字颜色
//                .setNavReturnBtnWidth(35)
//                .setNavReturnBtnHeight(35)
//                .setNavReturnImgPath(navReturnImgPath)
//                .setAuthBGImgPath(backgruond)
//                .setDialogDimAmount(0f)
//                .setFullScreen(true)
//
//
//                //授权页logo
//                .setLogoHidden(true)   //是否隐藏logo
//
//                //授权页号码栏：
//                .setNumberColor(0xffffffff)  //设置手机号码字体颜色
//                .setNumFieldOffsetBottomY(200)    //设置号码栏相对于标题栏下边缘y偏移
//                .setNumberSize(18)
//                .setNumFieldHeight(50)
//
//
//                //授权页登录按钮：
//                .setLogBtnText("本机号码一键登录")  //设置登录按钮文字
//                .setLogBtnTextColor(0xffffffff)   //设置登录按钮文字颜色
//                .setLogBtnImgPath(logBtnImgPath)   //设置登录按钮图片
//                .setLogBtnOffsetBottomY(130)   //设置登录按钮相对于标题栏下边缘y偏移
//                .setLogBtnTextSize(15)
//                .setLogBtnWidth(330)
//                .setLogBtnHeight(45)
//
//                //授权页隐私栏：
//                .setAppPrivacyOne("闪验用户协议", "https://api.253.com/api_doc/yin-si-zheng-ce/wei-hu-wang-luo-an-quan-sheng-ming.html")  //设置开发者隐私条款1名称和URL(名称，url)
//                .setAppPrivacyTwo("闪验隐私政策", "https://api.253.com/api_doc/yin-si-zheng-ce/ge-ren-xin-xi-bao-hu-sheng-ming.html")  //设置开发者隐私条款2名称和URL(名称，url)
//                .setAppPrivacyColor(Color.parseColor("#ffffff"), Color.parseColor("#60C4FC"))   //	设置隐私条款名称颜色(基础文字颜色，协议文字颜色)
//                .setPrivacyOffsetBottomY(20)
//                .setPrivacyTextSize(10)
//                .setUncheckedImgPath(uncheckedImgPath)
//                .setCheckedImgPath(checkedImgPath)
//                .setCheckBoxMargin(10, 5, 10, 5)
//                .setPrivacyState(true)
//                .addCustomPrivacyAlertView(privacyLayout)
//
//                //授权页slogan：
//                .setSloganHidden(true)
//                .setShanYanSloganTextColor(Color.parseColor("#ffffff"))
//                .setLoadingView(view_dialog)
//                .addCustomView(relativeLayout, false, false, null)
//                .addCustomView(numberLayout, false, false, null)
//                .build();
//        return uiConfig;
//    }
}

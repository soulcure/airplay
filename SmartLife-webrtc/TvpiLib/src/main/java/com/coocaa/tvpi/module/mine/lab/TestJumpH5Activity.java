package com.coocaa.tvpi.module.mine.lab;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.runtime.np.NPAppletActivity;

/**
 * 支持输入h5页面来跳转h5页面的调试Activity
 * @Author: yuzhan
 */
public class TestJumpH5Activity extends NPAppletActivity {

    Spinner spinner;
    EditText url_input;
    Button start_btn;
    RadioGroup h5_style_select;
    RadioGroup h5_orientation;
    String name = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_h5_activity);

        spinner = findViewById(R.id.url_select);
        initUrlSelect();

        url_input = findViewById(R.id.url_input);
        url_input.setEnabled(true);
        url_input.setHint(getRememberUrl());

        start_btn = findViewById(R.id.start_btn);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "https://beta-webapp.skysrt.com/dongleapi/novice-guide/index.html?position=pageturn/#/text";
                Uri uri = Uri.parse(url);
                Log.d("tt", "raw url=" + url);
                Log.d("tt", "parse uri, path==" + uri.getPath() + ", fragment=" + uri.getFragment());
                Set<String> paramsKeySet = uri.getQueryParameterNames();
                if(paramsKeySet != null && !paramsKeySet.isEmpty()) {
                    for(String key : paramsKeySet) {
                        Log.d("tt", "parse uri params key=" + key + ", value=" + uri.getQueryParameter(key));
                    }
                }

                Uri.Builder builder = new Uri.Builder().scheme("https").encodedAuthority("beta-webapp.skysrt.com").encodedPath("dongleapi/novice-guide/index.html?position=pageturn/#/text");
                Log.d("tt", "build url=" + builder.build().toString());

                start();
            }
        });

        h5_style_select = findViewById(R.id.h5_style_select);
        h5_orientation = findViewById(R.id.h5_orientation);

        h5_style_select.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.h5_style_float_np) {
                    h5_orientation.setVisibility(View.VISIBLE);
                } else {
                    h5_orientation.setVisibility(View.GONE);
                }
            }
        });
    }

    private void start() {
        String text = getUrl().toString();
        Log.d("Test", "start url = " + text);
        if(TextUtils.isEmpty(text)) {
            Toast.makeText(start_btn.getContext(), "请输入url或者选择页面", Toast.LENGTH_SHORT).show();
            return ;
        } else if(!text.startsWith("http://") && !text.startsWith("https://") && !text.startsWith("np://") && !text.startsWith("mp://")){
            Toast.makeText(start_btn.getContext(), "非法url", Toast.LENGTH_SHORT).show();
            return ;
        }
        saveUrl(text);
        FunctionBean functionBean = new FunctionBean();
        functionBean.icon = "https://tv.doubimeizhi.com/images/dongle/app/1001000_game.png";
        Uri uri = Uri.parse(text);
        functionBean.type = uri.getScheme();
        functionBean.id = uri.getAuthority();
        functionBean.target = uri.getPath();
        String fragment = uri.getFragment();
        Log.d("Test", "uri path=" + uri.getPath());
        Log.d("Test", "uri fragment=" + uri.getFragment());
        if(!TextUtils.isEmpty(fragment)) {
            functionBean.fragment = fragment;
        }

        Set<String> paramsKeySet = uri.getQueryParameterNames();
        if(paramsKeySet != null && !paramsKeySet.isEmpty()) {
            functionBean.params = new HashMap<>();
            for(String key : paramsKeySet) {
                functionBean.params.put(key, uri.getQueryParameter(key));
                Log.d("Test", "uri params key=" + key + ", value=" + uri.getQueryParameter(key));
            }
        }

        functionBean.name = TextUtils.isEmpty(name) ? "测试页面" : name;

        functionBean.runtime = new HashMap<>();
        functionBean.runtime.put(H5RunType.RUNTIME_NETWORK_FORCE_KEY, H5RunType.RUNTIME_NETWORK_FORCE_LAN);
        final int checkedId = h5_style_select.getCheckedRadioButtonId();
        if(checkedId == R.id.h5_style_float_np) {
            functionBean.runtime.put(H5RunType.RUNTIME_NAV_KEY, H5RunType.RUNTIME_NAV_FLOAT_NP);
        } else if(checkedId == R.id.h5_style_np) {
            functionBean.runtime.put(H5RunType.RUNTIME_NAV_KEY, H5RunType.RUNTIME_NAV_TOP);
        } else if(checkedId == R.id.h5_style_web) {
            functionBean.runtime.put(H5RunType.RUNTIME_NAV_KEY, H5RunType.RUNTIME_NAV_FLOAT);
        }
        if(h5_orientation.getVisibility() == View.VISIBLE) {
            if(h5_orientation.getCheckedRadioButtonId() == R.id.h5_orientation_landscape) {
                functionBean.runtime.put(H5RunType.RUNTIME_ORIENTATION_KEY, H5RunType.RUNTIME_ORIENTATION_LANDSCAPE);
            }
        }
        TvpiClickUtil.onClick(this, functionBean.uri());
    }

    private CharSequence getUrl() {
        CharSequence text = url_input.getText();
        Log.d("Test", "getText()=" + text);
        if(TextUtils.isEmpty(text)) {
            text = url_input.getHint();
            Log.d("Test", "getHint()=" + text);
        }
        return text;
    }

    private void initUrlSelect() {
        final List<UrlSelectBean> beanList = new ArrayList<>();
        beanList.add(new UrlSelectBean("选择或输入web页面地址", ""));
        beanList.add(new UrlSelectBean("看影视-测试", "https://beta-webapp.skysrt.com/yss/uni-app/index.html"));
        beanList.add(new UrlSelectBean("芝士视频-测试", "https://webapp.skyworthiot.com/sit/cheeseVideo/h5/"));
        beanList.add(new UrlSelectBean("电脑投电视-测试", "https://webapp.skyworthiot.com/lxw/pc-tv/index.html"));
        beanList.add(new UrlSelectBean("JSSDK-测试", "https://beta-webapp.skysrt.com/lqq/zhipingwebtest/index.html"));
        beanList.add(new UrlSelectBean("游戏列表-测试", "https://beta-webapp.skysrt.com/lqq/yss/gameList/index.html"));
        beanList.add(new UrlSelectBean("背景定制-测试", "https://webapp.skyworthiot.com/sit/atmosphere/h5/index.html"));
        beanList.add(new UrlSelectBean("弹幕-测试环境", "https://webapp.skyworthiot.com/sit/barrage/h5/index.html"));
        beanList.add(new UrlSelectBean("摇骰子游戏-测试", "https://webapp.skyworthiot.com/sit/dice/h5/index.html"));
        beanList.add(new UrlSelectBean("抽扑克游戏-测试", "https://webapp.skyworthiot.com/sit/poke/h5/index.html"));
        beanList.add(new UrlSelectBean("大转盘游戏-测试", "https://beta-webapp.skysrt.com/dongleapi/webtest/index.html"));
        beanList.add(new UrlSelectBean("大冒险游戏-测试", "https://beta-webapp.skysrt.com/dongleapi/webtestgame"));
        beanList.add(new UrlSelectBean("LBY-Style测试", "http://172.20.144.68:8080/"));
        beanList.add(new UrlSelectBean("LJ-测试", "http://172.20.148.11:8080/"));
        beanList.add(new UrlSelectBean("LXW-测试", "https://beta-webapp.skysrt.com/lxw/pc-tv/index.html"));
        beanList.add(new UrlSelectBean("YZ-测试", "http://172.20.151.160:8081/yuzhan/web/webview/src/index.html"));
        beanList.add(new UrlSelectBean("怎么控制文档翻页", "https://beta-webapp.skysrt.com/dongleapi/novice-guide/#/text?position=pageturn"));
        beanList.add(new UrlSelectBean("怎么移除文档", "https://beta-webapp.skysrt.com/dongleapi/novice-guide/#/text?position=remove"));

        SpinnerAdapter urlSelectAdapter = new ArrayAdapter<UrlSelectBean>(this, R.layout.test_h5_url_select, beanList);
        spinner.setAdapter(urlSelectAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UrlSelectBean bean = beanList.get(position);
                name = bean.name;
                if(TextUtils.isEmpty(bean.url)) {
                    url_input.setEnabled(true);
                } else {
                    url_input.setEnabled(false);
                }
                url_input.setText("");
                if(TextUtils.isEmpty(bean.url)) {
                    url_input.setHint(getRememberUrl());
                } else {
                    url_input.setHint(bean.url);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(TestJumpH5Activity.this, "未选择", Toast.LENGTH_SHORT).show();
                url_input.setEnabled(true);
                url_input.setHint(getRememberUrl());
            }
        });
    }


    private static class UrlSelectBean {
        String name;
        String url;

        public UrlSelectBean(String name, String url) {
            this.name = name;
            this.url = url;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private String getRememberUrl() {
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        return sp.getString("last_url", "");
    }

    private void saveUrl(String url) {
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        sp.edit().putString("last_url", url).commit();
    }
}
